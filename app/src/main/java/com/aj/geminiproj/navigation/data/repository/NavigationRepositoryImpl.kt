package com.aj.geminiproj.navigation.data.repository

import com.aj.geminiproj.navigation.data.datasource.ConversationDataSource
import com.aj.geminiproj.navigation.presentation.ConversationSummary
import com.aj.geminiproj.navigation.presentation.NavigationItem
import com.aj.geminiproj.navigation.domain.repository.NavigationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class NavigationRepositoryImpl(private val conversationDataSource: ConversationDataSource) :
    NavigationRepository {
    override fun getNavigationItems(): Flow<List<NavigationItem>> {
        return getConversationSummaries().map { conversations ->
            buildList {
                addAll(conversations.map { conversationSummary ->
                    NavigationItem.ConversationItem(
                        id = "conversation_${conversationSummary.id}",
                        title = conversationSummary.title,
                        lastMessage = conversationSummary.lastMessage ?: "",
                        timeStamp = conversationSummary.updatedAt
                    )
                })
            }
        }
    }

    override fun getConversationSummaries(): Flow<List<ConversationSummary>> {
        return conversationDataSource.getAllConversationSummaries()
    }

    override suspend fun startNewConversation(currentConversationId: String?): String {
        val newConversationId = UUID.randomUUID().toString()
        return conversationDataSource.createNewConversation(newConversationId, currentConversationId)
    }
}