package com.aj.geminiproj.features.chat.domain.usecase

import com.aj.geminiproj.core.model.chat.ChatMessage
import com.aj.geminiproj.core.model.chat.ChatStreamEvent
import com.aj.geminiproj.core.model.tool.Tool
import com.aj.geminiproj.features.chat.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow

class SendMessageWithToolsUseCase(private val chatRepository: ChatRepository) {

    suspend operator fun invoke(
        message: String,
        systemPrompt: String,
        conversationId: String,
        activeTools: List<Tool>,
        conversationHistory: List<ChatMessage>,
        fewShotPrimer: String? = null
    ): Flow<ChatStreamEvent> {
        return chatRepository.sendMessageWithTools(
            message,
            systemPrompt,
            conversationId,
            activeTools,
            conversationHistory,
            fewShotPrimer
        )
    }
}