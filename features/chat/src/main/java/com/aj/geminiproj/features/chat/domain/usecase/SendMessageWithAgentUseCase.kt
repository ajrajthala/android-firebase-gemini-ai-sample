package com.aj.geminiproj.features.chat.domain.usecase

import com.aj.geminiproj.core.ai.firebase.agent.ConversationAgent
import com.aj.geminiproj.core.model.chat.ChatMessage
import com.aj.geminiproj.core.model.chat.ChatStreamEvent
import com.aj.geminiproj.core.model.tool.Tool
import com.aj.geminiproj.features.chat.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow

class SendMessageWithAgentUseCase(private val agent: ConversationAgent) {

    operator fun invoke(
        userMessage: String,
        fullHistory: List<ChatMessage>,
    ): Flow<ChatStreamEvent> {
        return agent.processMessage(userMessage, fullHistory)
    }
}