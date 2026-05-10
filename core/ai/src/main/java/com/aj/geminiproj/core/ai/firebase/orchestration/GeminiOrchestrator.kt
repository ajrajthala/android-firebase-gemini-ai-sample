package com.aj.geminiproj.core.ai.firebase.orchestration

import com.aj.geminiproj.core.ai.firebase.dispatcher.ToolDispatcher
import com.aj.geminiproj.core.ai.firebase.mapper.FirebaseToolMapper
import com.aj.geminiproj.core.ai.firebase.registry.ToolRegistry
import com.aj.geminiproj.core.model.chat.ChatStreamEvent
import com.aj.geminiproj.core.model.chat.ChatStreamEvent.*
import com.aj.geminiproj.core.model.tool.ToolResult
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.FunctionCallPart
import com.google.firebase.ai.type.FunctionResponsePart
import com.google.firebase.ai.type.TextPart
import com.google.firebase.ai.type.content
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
Orchestrates multi-turn conversations with Gemini including tool execution.
 *
 * FLOW OF A SINGLE TURN WITH TOOLS:
 *
 *   1. Build a tool-aware GenerativeModel with all registered tools
 *   2. Start a streaming request with the user prompt + conversation history
 *   3. Collect the response stream chunk by chunk:
 *      a. TextPart        → emit TextChunk to ViewModel
 *      b. FunctionCallPart → emit ToolExecuting, dispatch to tool,
 *                            emit ToolCompleted/ToolFailed,
 *                            collect all function calls in this round
 *   4. If any tools were called this round:
 *      a. Build FunctionResponseParts from all results
 *      b. Append model turn + all responses to history
 *      c. Start a new streaming request (loop back to step 2)
 *   5. If no tools were called → emit TurnComplete
 * TOOL LOOP SAFETY:
 * maxToolRounds prevents infinite loops if Gemini keeps calling tools.
 * 10 rounds is generous for any realistic use case.
 */
class GeminiOrchestrator(
    private val registry: ToolRegistry,
    private val dispatcher: ToolDispatcher,
    private val mapper: FirebaseToolMapper,
    private val maxToolRounds: Int = 10,
    private val modelName: String = "gemini-3-flash-preview"
) {

    fun chat(
        /**
         * Initiates a multi-turn chat conversation with Gemini, handling tool execution and streaming responses.
         *
         * @param userPrompt The user's input prompt for the conversation.
         * @param history The list of previous conversation contents to maintain context. Defaults to an empty list.
         * @param onHistoryUpdated A callback function invoked when the conversation history is updated with new content.
         * @return A Flow of ChatStreamEvent representing the streaming response from Gemini, including text chunks, tool executions, and completion events.
         * Emits in order:
         * [TextChunk, ToolExecuting, ToolCompleted/ToolFailed(zero or more rounds), TextChunk, ...,TurnComplete/ChatCompleted/StreamError]
         */
        userPrompt: String,
        history: List<Content> = emptyList(),
        onHistoryUpdated: (List<Content>) -> Unit,
    ): Flow<ChatStreamEvent> = flow {

        val firebaseTool = mapper.toFirebaseTool(registry.tools)
        val model =
            Firebase.ai().generativeModel(modelName = modelName, tools = listOf(firebaseTool))

        //
        val turnHistory = history.toMutableList()

        val userContent = content(role = "user") {
            text(userPrompt)
        }
        turnHistory.add(userContent)

        var toolRounds = 0

        try {
            while (toolRounds < maxToolRounds) {
                val functionCallsThisRound = mutableListOf<Pair<String, Map<String, Any>>>()
                val modelTextParts = mutableListOf<TextPart>()
                val modelFunctionCallParts = mutableListOf<FunctionCallPart>()

                model.generateContentStream(turnHistory)
                    .collect { chunk ->
                        chunk.candidates.firstOrNull()?.content?.parts?.forEach { part ->
                            when (part) {
                                is TextPart -> {
                                    modelTextParts.add(part)
                                    emit(ChatStreamEvent.TextChunk(part.text))
                                }

                                is FunctionCallPart -> {
                                    modelFunctionCallParts.add(part)

                                    // Notify UI that a tool is starting
                                    val tool = registry.getToolByName(part.name)
                                    emit(
                                        ChatStreamEvent.ToolExecuting(
                                            functionName = part.name,
                                            displayName = tool?.definition?.displayName ?: part.name
                                        )
                                    )

                                    //Collect for batch execution after stream completes
                                    functionCallsThisRound.add(
                                        part.name to (part.args ?: emptyMap())
                                    )

                                }

                                else -> Unit // ignore unsupported part types for now
                            }
                        }
                    }

                if (functionCallsThisRound.isEmpty()) {
                    // No tools called, turn is complete
                    val modelContent = content(role = "assistant") {
                        modelTextParts.forEach { text(it.text) }
                    }
                    turnHistory.add(modelContent)
                    onHistoryUpdated(turnHistory.toList())
                    emit(ChatStreamEvent.TurnCompleted)
                    break
                }

                val modelToolCallContent = content(role = "assistant") {
                    modelFunctionCallParts.forEach { part(it) }
                }
                turnHistory.add(modelToolCallContent)

                // Execute all tools called this round
                val functionResponseParts = mutableListOf<FunctionResponsePart>()
                functionCallsThisRound.forEach { (functionName, args) ->
                    val result = dispatcher.dispatch(functionName, args)

                    when (result) {
                        is ToolResult.Success -> emit(
                            ToolCompleted(
                                functionName = functionName,
                                summary = buildSummary(functionName, result.data)
                                    ?: "Tool executed successfully"
                            )
                        )

                        is ToolResult.Error -> emit(
                            ChatStreamEvent.ToolFailed(
                                functionName = functionName,
                                errorMessage = result.message,
                                isPermissionError = false

                            )
                        )

                        is ToolResult.PermissionDenied -> emit(
                            ChatStreamEvent.ToolFailed
                                (
                                functionName = functionName,
                                errorMessage = "Permission denied for tool: $functionName",
                                isPermissionError = true
                            )
                        )
                    }
                    // Feed result back to Gemini regardless of outcome so it can decide how to proceed (try again, skip tool, etc)
                    functionResponseParts.add(mapper.toFunctionResponsePart(functionName, result))

                    // Append all function responses as a single user turn
                    val functionResponseContent = content(role = "user") {
                        functionResponseParts.forEach { part(it) }
                    }
                    turnHistory.add(functionResponseContent)
                    toolRounds++
                }

                // Safety check to prevent infinite loops
                if (toolRounds >= maxToolRounds) {
                    emit(
                        ChatStreamEvent.StreamError(
                            throwable = Exception("Max tool execution rounds reached"),
                            errorMessage = "Something went wrong. Please try again."
                        )
                    )
                }

            }
        } catch (e: Exception) {
            emit(
                ChatStreamEvent.StreamError(
                    e,
                    errorMessage = "Something went wrong. Please try again."
                )
            )
        }
    }

    /**
     * Helper function to build a concise summary string for a tool execution result, used in the ToolCompleted event.
     */
    private fun buildSummary(functionName: String, data: Map<String, Any>): String? {
        return when {
            data.containsKey("displayName") -> "Found ${data["displayName"]} in contacts"
            data.containsKey("name") -> "Found ${data["name"]}"
            data.containsKey("eventId") -> "Event ${data["title"]} on ${data["date"]} at ${data["time"]} created"
            data.containsKey("available") -> "The time slot on ${data["date"]} at ${data["time"]} is ${if (data["available"] == true) "available" else "not available"}"
            data.containsKey("events") -> "Found ${(data["events"] as? List<*>)?.size ?: 0} upcoming events"
            else -> null
        }
    }
}