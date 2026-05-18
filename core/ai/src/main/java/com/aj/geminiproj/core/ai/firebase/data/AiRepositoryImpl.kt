package com.aj.geminiproj.core.ai.firebase.data

import com.aj.geminiproj.core.ai.firebase.FirebaseAiClient
import com.aj.geminiproj.core.ai.firebase.domain.AiRepository
import com.aj.geminiproj.core.ai.firebase.orchestration.GeminiOrchestrator
import com.aj.geminiproj.core.model.AiError
import com.aj.geminiproj.core.model.AiResult
import com.aj.geminiproj.core.model.chat.ChatMessage
import com.aj.geminiproj.core.model.chat.MessageRole
import com.aj.geminiproj.core.model.chat.MessageStatus
import com.aj.geminiproj.core.model.StreamState
import com.aj.geminiproj.core.model.chat.ChatStreamEvent
import com.aj.geminiproj.core.model.tool.Tool
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import java.util.UUID

class AiRepositoryImpl(
    private val aiClient: FirebaseAiClient,
    private val geminiOrchestrator: GeminiOrchestrator
) : AiRepository {
    override suspend fun sendMessage(
        message: String,
        conversationHistory: List<ChatMessage>,
    ): AiResult<ChatMessage> {
        return try {
            val response = aiClient.generateContent(message, conversationHistory)

            AiResult.Success(
                ChatMessage(
                    id = UUID.randomUUID().toString(),
                    content = response,
                    role = MessageRole.ASSISTANT,
                    status = MessageStatus.SENT,
                    timeStamp = System.currentTimeMillis()
                )
            )
        } catch (e: Exception) {
            AiResult.Error(AiError.fromThrowable(e))
        }
    }

    override fun sendMessageStream(
        message: String,
        conversationHistory: List<ChatMessage>,
    ): Flow<StreamState<String>> = flow {
        emit(StreamState.Loading)
        var accumulatedText = ""

        aiClient.generateContentStream(message, conversationHistory)
            .collect { chunk ->
                accumulatedText += chunk
                emit(StreamState.Streaming(accumulatedText, isComplete = false))
            }
        emit(StreamState.Success(accumulatedText))
    }.catch { e -> emit(StreamState.Error(AiError.fromThrowable(e).message)) }

    override suspend fun generateConversationTitle(message: List<ChatMessage>): AiResult<String> {
        if (message.isEmpty()) {
            return AiResult.Success("New Chat")
        }

        return try {
            val firstUserMessage = message.firstOrNull() { it.role == MessageRole.USER }
                ?: return AiResult.Success("New Chat")

            val prompt = """
                Generate a concise title (3-5 words) for a conversation based on the following user message:
                    "${firstUserMessage.content}"
                    Return only the title, nothing else.
                """.trimIndent()

            val title = aiClient.generateContent(prompt, emptyList())
                .take(50) // Limit title length
                .trim()
            AiResult.Success(title)
        } catch (e: Exception) {
            AiResult.Success("New Chat") // Fallback title on error
        }
    }

    override suspend fun sendMessageWithTools(
        message: String,
        systemPrompt: String,
        activeTools: List<Tool>,
        conversationHistory: List<ChatMessage>,
        fewShotPrimer: String?
    ): Flow<ChatStreamEvent> {
        return geminiOrchestrator.sendChatMessageWithTools(
            message,
            systemPrompt,
            conversationHistory,
            activeTools,
            fewShotPrimer
        )
    }
}