package com.aj.geminiproj.core.ai.firebase.domain

import com.aj.geminiproj.core.model.AiResult
import com.aj.geminiproj.core.model.ChatMessage
import com.aj.geminiproj.core.model.StreamState
import kotlinx.coroutines.flow.Flow

interface AiRepository {
    suspend fun sendMessage(
        message: String,
        conversationHistory: List<ChatMessage> = emptyList(),
    ): AiResult<ChatMessage>

    fun sendMessageStream(
        message: String,
        conversationHistory: List<ChatMessage> = emptyList(),
    ): Flow<StreamState<String>>

    suspend fun generateConversationTitle(
        message: List<ChatMessage>,
    ): AiResult<String>

}