package com.aj.geminiproj.features.chat.domain.repository

import com.aj.geminiproj.core.model.AiResult
import com.aj.geminiproj.core.model.chat.ChatMessage
import com.aj.geminiproj.core.model.chat.ChatConversation
import com.aj.geminiproj.core.model.chat.ChatStreamEvent
import com.aj.geminiproj.core.model.StreamState
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    // ============= Conversation Management =============

    suspend fun getConversation(conversationId: String): ChatConversation

    suspend fun getAllConversations(): List<ChatConversation>

    suspend fun deleteConversation(conversationId: String)

    suspend fun saveConversation(conversation: ChatConversation)

    suspend fun saveMessage(chatMessage: ChatMessage, conversationId: String)

    fun observeAllConversations(): Flow<List<ChatConversation>>

    fun observeAllConversationSummaries(): Flow<List<ChatConversation>>

    // ============= AI Operations =============

    suspend fun sendMessage(
        message: String,
        conversationId: String
    ): AiResult<ChatMessage>

    /**
     * Send a message and stream the response as text chunks.
     * Use for text-heavy responses where you want streaming UX.
     */
    suspend fun sendMessageStream(
        message: String,
        conversationId: String,
        conversationHistory: List<ChatMessage>
    ): Flow<StreamState<String>>

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
        conversationId: String,
        conversationHistory: List<ChatMessage>
    ): Flow<ChatStreamEvent>

    /**
     * Generate a title for the conversation based on messages.
     * Useful for initial thread naming or summarization.
     */
    suspend fun generateConversationTitle(
        messages: List<ChatMessage>
    ): AiResult<String>
}