package com.aj.geminiproj.navigation.data.datasource

import com.aj.geminiproj.features.chat.domain.repository.ChatRepository
import com.aj.geminiproj.navigation.presentation.ConversationSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ConversationDataSourceImpl(private val chatRepository: ChatRepository) :
    ConversationDataSource {

    override fun getAllConversationSummaries(): Flow<List<ConversationSummary>> =
        chatRepository.observeAllConversationSummaries()
            .map { conversations ->
                conversations
                    .map { conversation ->
                        ConversationSummary(
                            id = conversation.id,
                            title = conversation.title,
                            lastMessage = conversation.lastMessage()?.content?.take(50),
                            updatedAt = conversation.updatedAt
                        )
                    }
            }

    override suspend fun createNewConversation(
        conversationId: String,
        currentConversationId: String?,
    ): String {
        if (currentConversationId != null) {
            val currentConversation = chatRepository.getConversation(currentConversationId)
            if (currentConversation.messages.isEmpty()) {
                return currentConversationId
            }
        }
        return conversationId
    }
}