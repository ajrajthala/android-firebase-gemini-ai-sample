package com.aj.geminiproj.features.chat.domain.usecase

import com.aj.geminiproj.core.model.chat.ChatMessage
import com.aj.geminiproj.features.chat.domain.repository.ChatRepository

class SaveMessageUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke(chatMessage: ChatMessage, conversationId: String) {
        repository.saveMessage(chatMessage, conversationId)
    }
}