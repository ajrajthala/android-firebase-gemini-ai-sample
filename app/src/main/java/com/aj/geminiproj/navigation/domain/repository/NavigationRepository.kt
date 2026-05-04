package com.aj.geminiproj.navigation.domain.repository

import com.aj.geminiproj.navigation.presentation.ConversationSummary
import com.aj.geminiproj.navigation.presentation.NavigationItem
import kotlinx.coroutines.flow.Flow

interface NavigationRepository {
    fun getNavigationItems(): Flow<List<NavigationItem>>
    fun getConversationSummaries(): Flow<List<ConversationSummary>>
    suspend fun startNewConversation(currentConversationId: String? = null): String
}