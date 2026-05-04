package com.aj.geminiproj.features.chat.domain.usecase

import com.aj.geminiproj.features.chat.domain.repository.ChatRepository

class DeleteConversationUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke(conversationId: String) {
        repository.deleteConversation(conversationId)
    }
}