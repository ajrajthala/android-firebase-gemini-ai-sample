package com.aj.geminiproj.features.chat.domain.usecase

import com.aj.geminiproj.core.model.chat.ChatConversation
import com.aj.geminiproj.features.chat.domain.repository.ChatRepository

class GetConversationUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke(conversationId: String): ChatConversation {
        return repository.getConversation(conversationId)
    }
}