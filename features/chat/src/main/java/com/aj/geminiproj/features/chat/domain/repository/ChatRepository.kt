package com.aj.geminiproj.features.chat.domain.repository

import android.graphics.Bitmap
import com.aj.geminiproj.core.model.AiResult
import com.aj.geminiproj.core.model.ChatMessage
import com.aj.geminiproj.core.model.ChatConversation
import com.aj.geminiproj.core.model.StreamState
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    suspend fun sendMessage(message: String, conversationId: String): AiResult<ChatMessage>

    suspend fun sendMessageStream(
        message: String,
        conversationId: String,
        conversationHistory: List<ChatMessage>,
    ): Flow<StreamState<String>>

    suspend fun sendMessageWithImage(
        message: String,
        conversationId: String,
        bitmap: Bitmap,
        conversationHistory: List<ChatMessage>,
    ): AiResult<String>

    suspend fun sendMessageWithImageStream(
        message: String,
        conversationId: String,
        bitmap: Bitmap,
        conversationHistory: List<ChatMessage>,
    ): Flow<StreamState<String>>

    suspend fun getConversation(conversationId: String): ChatConversation

    suspend fun getAllConversations(): List<ChatConversation>

    suspend fun deleteConversation(conversationId: String)

    suspend fun saveConversation(conversation: ChatConversation)

    suspend fun saveMessage(chatMessage: ChatMessage, conversationId: String)

    fun observeAllConversations(): Flow<List<ChatConversation>>

    fun observeAllConversationSummaries(): Flow<List<ChatConversation>>
}