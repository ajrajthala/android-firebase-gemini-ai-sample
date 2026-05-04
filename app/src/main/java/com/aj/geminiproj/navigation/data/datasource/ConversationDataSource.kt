package com.aj.geminiproj.navigation.data.datasource

import com.aj.geminiproj.navigation.presentation.ConversationSummary
import kotlinx.coroutines.flow.Flow

interface ConversationDataSource {
    fun getAllConversationSummaries(): Flow<List<ConversationSummary>>
    suspend fun createNewConversation(
        conversationId: String,
        currentConversationId: String? = null,
    ): String
}