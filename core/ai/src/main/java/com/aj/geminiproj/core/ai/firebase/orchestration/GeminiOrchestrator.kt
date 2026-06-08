package com.aj.geminiproj.core.ai.firebase.orchestration

import com.aj.geminiproj.core.ai.firebase.GenerativeModelFactory
import com.aj.geminiproj.core.ai.firebase.di.GEMINI_MODEL
import com.aj.geminiproj.core.ai.firebase.dispatcher.ToolDispatcher
import com.aj.geminiproj.core.ai.firebase.ingelligence.ParallelToolExecutor
import com.aj.geminiproj.core.ai.firebase.ingelligence.ToolCall
import com.aj.geminiproj.core.ai.firebase.mapper.FirebaseToolMapper
import com.aj.geminiproj.core.ai.firebase.observability.AgentTracer
import com.aj.geminiproj.core.ai.firebase.observability.TurnTokenCounter
import com.aj.geminiproj.core.ai.firebase.resolver.SemanticDomainRegistry
import com.aj.geminiproj.core.model.chat.ChatMessage
import com.aj.geminiproj.core.model.chat.ChatStreamEvent
import com.aj.geminiproj.core.model.chat.ChatStreamEvent.ToolCompleted
import com.aj.geminiproj.core.model.chat.MessageRole
import com.aj.geminiproj.core.model.tool.Tool
import com.aj.geminiproj.core.model.tool.ToolResult
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.FunctionCallPart
import com.google.firebase.ai.type.FunctionResponsePart
import com.google.firebase.ai.type.QuotaExceededException
import com.google.firebase.ai.type.TextPart
import com.google.firebase.ai.type.UsageMetadata
import com.google.firebase.ai.type.content
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
    private val domainRegistry: SemanticDomainRegistry,
    private val dispatcher: ToolDispatcher,
    private val mapper: FirebaseToolMapper,
    private val tracer: AgentTracer,
    private val modelFactory: GenerativeModelFactory,
    private val parallelToolExecutor: ParallelToolExecutor,
    private val maxToolRounds: Int = 10,
) {

    companion object {
        private const val TAG = "GeminiOrchestrator"

        val defaultSystemPrompt: String = """
            You are a helpful Android assistant with access to specific tools.
            Use only the tools explicitly provided to you.
            When calling a tool, ensure all required parameters are present.
            If information needed to call a tool is missing, ask user instead of guessing.
            Never fabricate tool results.
        """.trimIndent()
    }

    fun chatWithTools(
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
        systemPrompt: String = defaultSystemPrompt,
        history: List<Content> = emptyList(),
        tools: List<Tool>,
        onHistoryUpdated: (List<Content>) -> Unit,
        turnId: String
    ): Flow<ChatStreamEvent> = flow {
        val tokenCounter = TurnTokenCounter()
        val model =
            if (tools.isNotEmpty()) {
                val firebaseTool = mapper.toFirebaseTool(tools)
                modelFactory.createWithTools(GEMINI_MODEL, systemPrompt, firebaseTool)
            } else {
                modelFactory.createWithSystemPrompt(GEMINI_MODEL, systemPrompt)
            }
        //
        val turnHistory = history.toMutableList()

        val userContent = content(role = "user") {
            text(userPrompt)
        }
        turnHistory.add(userContent)

        var toolRounds = 0

        try {
            while (toolRounds < maxToolRounds) {
                var lastUsageMetadata: UsageMetadata? = null
                val functionCallsThisRound = mutableListOf<Pair<String, Map<String, Any>>>()
                val modelTextParts = mutableListOf<TextPart>()
                val modelFunctionCallParts = mutableListOf<FunctionCallPart>()

                model.generateContentStream(turnHistory)
                    .collect { chunk ->
                        chunk.usageMetadata?.let { lastUsageMetadata = it }
                        val candidate = chunk.candidates.firstOrNull()
                        if (candidate?.finishReason != null && candidate.finishReason != com.google.firebase.ai.type.FinishReason.STOP) {
                            tracer.logTurnError(Throwable("Streaming stopped with reason: ${candidate.finishReason}"))
                        }

                        candidate?.content?.parts?.forEach { part ->
                            when (part) {
                                is TextPart -> {
                                    modelTextParts.add(part)
                                    emit(ChatStreamEvent.TextChunk(part.text))
                                }

                                is FunctionCallPart -> {
                                    modelFunctionCallParts.add(part)

                                    // Notify UI that a tool is starting
                                    val tool = domainRegistry.getToolByName(part.name)
                                    tracer.logToolCall(part.name, part.args)
                                    emit(
                                        ChatStreamEvent.ToolExecuting(
                                            functionName = part.name,
                                            displayName = tool?.definition?.displayName ?: part.name
                                        )
                                    )

                                    //Collect for batch execution after stream completes
                                    functionCallsThisRound.add(part.name to (part.args))
                                }

                                else -> Unit // ignore unsupported part types for now
                            }
                        }
                    }

                lastUsageMetadata?.let {
                    tokenCounter.recordRound(
                        promptTokens = it.promptTokenCount,
                        candidateTokens = it.candidatesTokenCount ?: 0
                    )
                }
                val fullText = modelTextParts.joinToString(",") { it.text }

                if (functionCallsThisRound.isEmpty()) {
                    tracer.logTurnCompletion(fullText, tokenCounter.summary())
                    val summary = tokenCounter.summary()
                    emit(
                        ChatStreamEvent.TokenUsageRecorded(
                            promptTokens = summary.promptTokens,
                            candidateTokens = summary.promptTokens,
                            totalTokens = summary.totalTokens,
                            toolRounds = summary.toolRounds
                        )
                    )
                    // No tools called, turn is complete
                    val modelContent = content(role = "model") {
                        modelTextParts.forEach { text(it.text) }
                    }
                    turnHistory.add(modelContent)
                    onHistoryUpdated(turnHistory.toList())
                    emit(ChatStreamEvent.TurnCompleted)
                    break
                }

                // If tools were called, include any text parts that preceded the tool call
                val modelTurnContent = content(role = "model") {
                    modelTextParts.forEach { text(it.text) }
                    modelFunctionCallParts.forEach { part(it) }
                }
                turnHistory.add(modelTurnContent)

                // Execute all tools called this round
                val toolCalls = functionCallsThisRound.map { (name, args) ->
                    ToolCall(name, args)
                }
                val executionResults = parallelToolExecutor.executeAll(toolCalls)
                val functionResponseParts = mutableListOf<FunctionResponsePart>()

                executionResults.forEach { executionResult ->
                    val functionName = executionResult.toolCall.toolName
                    val result = executionResult.result
                    tracer.logToolResult(functionName, result, executionResult.latencyMs)
                    when (result) {
                        is ToolResult.Success -> {
                            emit(
                                ToolCompleted(
                                    functionName = functionName,
                                    summary = buildSummary(functionName, result.data)
                                        ?: "Tool executed successfully"
                                )
                            )
                        }

                        is ToolResult.Error -> {
                            emit(
                                ChatStreamEvent.ToolFailed(
                                    functionName = functionName,
                                    errorMessage = result.message,
                                    isPermissionError = false

                                )
                            )
                        }

                        is ToolResult.PermissionDenied -> {
                            emit(
                                ChatStreamEvent.ToolFailed
                                    (
                                    functionName = functionName,
                                    errorMessage = "Permission denied for tool: $functionName",
                                    isPermissionError = true
                                )
                            )
                        }

                        is ToolResult.NeedsConfirmation -> {
                            // Treat as completed, Gemini should handle the clarification naturally via the FunctionResponsePart we feed back below.
                            emit(
                                ToolCompleted(
                                    functionName = functionName,
                                    summary = result.message
                                )
                            )
                        }
                    }
                    // Feed result back to Gemini regardless of outcome so it can decide how to proceed (try again, skip tool, etc)
                    functionResponseParts.add(mapper.toFunctionResponsePart(functionName, result))

                }

                // Append all function responses as a single user turn
                val functionResponseContent = content(role = "user") {
                    functionResponseParts.forEach { part(it) }
                }
                turnHistory.add(functionResponseContent)
                toolRounds++

                // Safety check to prevent infinite loops
                if (toolRounds >= maxToolRounds) {
                    tracer.logTurnError(Throwable("Max tool rounds ($maxToolRounds) reached - aborting turn."))
                    emit(
                        ChatStreamEvent.StreamError(
                            throwable = Exception("Max tool execution rounds reached"),
                            errorMessage = "Something went wrong. Please try again."
                        )
                    )
                }
            }
        } catch (e: Exception) {
            tracer.logTurnError(e)
            e.printStackTrace()
            val message = when {
                e is QuotaExceededException -> "AI quota exceeded. You've reached the free tier limit. Please try again later or upgrade your plan."
                e.javaClass.simpleName == "ResponseStoppedException" -> "Content generation was stopped (e.g., due to safety filters or an internal issue). Please try a different prompt."
                else -> "Something went wrong. Try again..."
            }
            emit(
                ChatStreamEvent.StreamError(
                    e,
                    errorMessage = message
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
            data.containsKey("available") -> {
                val available = data["available"] as? Boolean ?: false
                val startTimeMs = data["startTimeMs"] as? Long ?: 0L
                val endTimeMs = data["endTimeMs"] as? Long ?: 0L
                val status = if (available) "available" else "not available"

                //formate t=date time from millis to human readable
                val formatter =
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault())
                val startTime = formatter.format(Instant.ofEpochMilli(startTimeMs))
                val endTime = formatter.format(Instant.ofEpochMilli(endTimeMs))
                "Time slot from $startTime to $endTime is $status"
            }

            data.containsKey("events") -> "Found ${(data["events"] as? List<*>)?.size ?: 0} upcoming events"
            else -> null
        }
    }

    private fun List<ChatMessage>.toFirebaseChatHistory(): List<Content> {
        return this.mapNotNull { message ->
            when (message.role) {
                MessageRole.USER -> content(role = "user") {
                    text(message.content)
                }

                MessageRole.ASSISTANT -> content(role = "model") {
                    text(message.content)
                }

                MessageRole.SYSTEM -> null // Firebase doesn't support system role
            }
        }
    }

    fun sendChatMessageWithTools(
        message: String,
        systemPrompt: String,
        conversationHistory: List<ChatMessage>,
        activeTools: List<Tool>, // only active domain tools
        fewShotPrimer: String? = null, // injected only once
        turnId: String,
    ): Flow<ChatStreamEvent> {
        val firebaseChatHistory = conversationHistory.toFirebaseChatHistory().toMutableList()

        // Inject few-shot primer as a priming model turn at the start of history
        if (!fewShotPrimer.isNullOrBlank()) {
            firebaseChatHistory.add(0, content(role = "model") { text(fewShotPrimer) })
        }

        return chatWithTools(
            userPrompt = message,
            systemPrompt = systemPrompt,
            history = firebaseChatHistory,
            tools = activeTools,
            onHistoryUpdated = { },
            turnId = turnId
        )
    }
}