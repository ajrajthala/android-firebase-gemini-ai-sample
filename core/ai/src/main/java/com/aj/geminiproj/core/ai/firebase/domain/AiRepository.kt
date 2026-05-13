package com.aj.geminiproj.core.ai.firebase.domain

import com.aj.geminiproj.core.model.AiResult
import com.aj.geminiproj.core.model.chat.ChatMessage
import com.aj.geminiproj.core.model.StreamState
import com.aj.geminiproj.core.model.chat.ChatStreamEvent
import kotlinx.coroutines.flow.Flow

interface AiRepository {
    suspend fun sendMessage(
        message: String,
        conversationHistory: List<ChatMessage> = emptyList(),
    ): AiResult<ChatMessage>

    fun sendMessageStream(
        message: String,
        conversationHistory: List<ChatMessage> = emptyList(),
    ): Flow<StreamState<String>>

    suspend fun generateConversationTitle(
        message: List<ChatMessage>,
    ): AiResult<String>

    /**
     * Send a message with tool/function calling support.
     * Use for messages that might trigger tool execution (e.g., scheduling meetings).
     *
     * Returns a flow of discrete events:
     * - TextChunk: AI is generating text
     * - ToolExecuting: A tool is about to run
     * - ToolCompleted/ToolFailed: Tool execution result
     * - TurnCompleted: Conversation turn is done
     * - StreamError: Something went wrong
     * - ChatCompleted: Entire operation finished
     *
     * This provides complete visibility into what's happening during the operation.
     */
    suspend fun sendMessageWithTools(
        message: String,
        conversationHistory: List<ChatMessage> = emptyList()
    ): Flow<ChatStreamEvent>

}