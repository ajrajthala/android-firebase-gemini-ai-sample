package com.aj.geminiproj.features.chat.domain.usecase

import com.aj.geminiproj.core.ai.firebase.domain.AiRepository
import com.aj.geminiproj.core.model.AiResult
import com.aj.geminiproj.core.model.ChatMessage

class GenerateConversationTitleUseCase(private val aiRepository: AiRepository) {
    suspend operator fun invoke(messages: List<ChatMessage>): String {
        return when (val result = aiRepository.generateConversationTitle(messages)) {
            is AiResult.Success -> result.data
            is AiResult.Error -> "New Chat"
            is AiResult.Loading -> "New Chat"
        }
    }

}