package com.aj.geminiproj.features.chat.domain.usecase

import com.aj.geminiproj.core.ai.firebase.agent.ConversationAgent

class ResetConversationUseCase(private val agent: ConversationAgent) {
    operator fun invoke() = agent.reset()
}