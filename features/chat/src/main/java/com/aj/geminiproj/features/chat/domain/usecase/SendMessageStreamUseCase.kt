package com.aj.geminiproj.features.chat.domain.usecase

import com.aj.geminiproj.core.model.chat.ChatMessage
import com.aj.geminiproj.core.model.StreamState
import com.aj.geminiproj.features.chat.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow

class SendMessageStreamUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke(
        message: String,
        conversationId: String,
        conversationHistory: List<ChatMessage>,
    ): Flow<StreamState<String>> {
        return repository.sendMessageStream(message, conversationId, conversationHistory)
    }
}