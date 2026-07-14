package com.aj.geminiproj.core.data

import com.aj.geminiproj.core.model.chat.ChatConversation
import com.aj.geminiproj.core.model.chat.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ConversationStore {
    fun observeAll(): Flow<List<ChatConversation>>
    fun observeAllSummaries(): Flow<List<ChatConversation>>
    suspend fun getAll(): List<ChatConversation>
    suspend fun getById(id: String): ChatConversation?
    suspend fun save(chatConversation: ChatConversation)
    suspend fun delete(id: String)
    suspend fun createNew(id: String): ChatConversation
    suspend fun saveMessage(chatMessage: ChatMessage, conversationId: String)
}