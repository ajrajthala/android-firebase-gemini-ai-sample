package com.aj.geminiproj.features.chat.domain.usecase

import com.aj.geminiproj.core.model.ChatConversation
import com.aj.geminiproj.features.chat.domain.repository.ChatRepository

class SaveConversationUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke(conversation: ChatConversation) {
        repository.saveConversation(conversation)
    }
}