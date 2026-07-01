package com.aj.geminiproj.core.ai.firebase.domain

import android.graphics.Bitmap
import com.aj.geminiproj.core.model.AiResult
import com.aj.geminiproj.core.model.chat.ChatMessage
import com.aj.geminiproj.core.model.chat.ChatStreamEvent
import com.aj.geminiproj.core.model.StreamState
import com.aj.geminiproj.core.model.tool.Tool
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

    suspend fun sendMessageWithImage(
        message: String,
        bitmap: Bitmap,
        history: List<ChatMessage>
    ): AiResult<String>

    fun sendMessageWithImageStream(
        message: String,
        bitmap: Bitmap,
        history: List<ChatMessage>
    ): Flow<StreamState<String>>

    suspend fun sendMessageWithTools(
        message: String,
        systemPrompt: String,
        activeTools: List<Tool>,
        conversationHistory: List<ChatMessage> = emptyList(),
        fewShotPrimer: String? = null,
        turnId: String,
    ): Flow<ChatStreamEvent>
}