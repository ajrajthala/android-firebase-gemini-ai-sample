package com.aj.geminiproj.features.chat.domain.usecase

import com.aj.geminiproj.core.model.AiResult
import com.aj.geminiproj.core.model.chat.ChatMessage
import com.aj.geminiproj.features.chat.domain.repository.ChatRepository

class SendMessageWithImageUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke(
        message: String,
        conversationId: String,
        bitmap: android.graphics.Bitmap,
        conversationHistory: List<ChatMessage>
    ): AiResult<String> {
        return repository.sendMessageWithImage(message, conversationId, bitmap, conversationHistory)
    }
}