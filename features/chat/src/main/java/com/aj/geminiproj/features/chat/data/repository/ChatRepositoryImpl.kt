package com.aj.geminiproj.features.chat.data.repository

import com.aj.geminiproj.core.ai.firebase.domain.AiRepository
import com.aj.geminiproj.core.data.ConversationStore
import com.aj.geminiproj.core.model.AiResult
import com.aj.geminiproj.core.model.ChatConversation
import com.aj.geminiproj.core.model.ChatMessage
import com.aj.geminiproj.core.model.StreamState
import com.aj.geminiproj.features.chat.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class ChatRepositoryImpl(
    private val aiRepository: AiRepository,
    private val conversationStore: ConversationStore,
) : ChatRepository {


    override suspend fun sendMessage(
        message: String,
        conversationId: String,
    ): AiResult<ChatMessage> {
        val conversation = conversationStore.getById(conversationId)
        return aiRepository.sendMessage(message, conversation?.messages ?: emptyList())
    }

    override suspend fun sendMessageStream(
        message: String,
        conversationId: String,
        conversationHistory: List<ChatMessage>,
    ): Flow<StreamState<String>> {
        return aiRepository.sendMessageStream(message, conversationHistory)
    }

    override suspend fun getConversation(conversationId: String): ChatConversation {
        return conversationStore.getById(conversationId) ?: ChatConversation(
            id = conversationId,
            title = "New Chat",
            messages = emptyList(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }

    override fun observeAllConversations(): Flow<List<ChatConversation>> =
        conversationStore.observeAll()

    override fun observeAllConversationSummaries(): Flow<List<ChatConversation>> {
        return conversationStore.observeAllSummaries()
    }

    override suspend fun getAllConversations(): List<ChatConversation> =
        conversationStore.getAll()

    override suspend fun deleteConversation(conversationId: String) {
        conversationStore.delete(conversationId)
    }

    override suspend fun saveConversation(conversation: ChatConversation) {
        conversationStore.save(conversation)
    }

    override suspend fun saveMessage(
        chatMessage: ChatMessage,
        conversationId: String,
    ) {
        conversationStore.saveMessage(chatMessage, conversationId)
    }
}