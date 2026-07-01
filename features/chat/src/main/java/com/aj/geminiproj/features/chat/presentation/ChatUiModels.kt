package com.aj.geminiproj.features.chat.presentation

import android.graphics.Bitmap
import android.net.Uri
import com.aj.geminiproj.core.model.chat.ChatMessage
import com.aj.geminiproj.core.model.chat.ChatStreamEvent


data class ChatUiState(
    val title: String = "New Chat",
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val isStreaming: Boolean = false,
    val streamingText: String = "",
    val error: String? = null,
    val conversationId: String = "",
    val selectedImageUri: Uri? = null,
    val selectedImageBitmap: Bitmap? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val activeToolDisplay: String? = null, // For showing tool execution status in the UI
    val showPermissionRationale: Boolean = false,

    val currentTurnToolSteps: List<String> = emptyList(),
    val lastTurnTokens: ChatStreamEvent.TokenUsageRecorded? = null
) {
    val canSendMessage: Boolean
        get() = !isLoading && !isStreaming
}

sealed interface ChatUiEvent {
    data class OnMessageTextChanged(val text: String) : ChatUiEvent
    data object OnSendMessage : ChatUiEvent
    data object OnRetry : ChatUiEvent
    data object OnDeleteChat : ChatUiEvent
    data object OnDismissError : ChatUiEvent
    data class OnImageSelected(val imageUri: Uri, val bitmap: Bitmap) : ChatUiEvent
    data object OnRemoveImage : ChatUiEvent
}

sealed interface ChatUiEffect {
    data object ScrollToBottom : ChatUiEffect
    data class ShowError(val message: String) : ChatUiEffect
    data object ClearInput : ChatUiEffect
    data object ChatDeleted : ChatUiEffect
    data class ConversationStarted(val conversationId: String) : ChatUiEffect
    data class ToolExecuting(val toolName: String) : ChatUiEffect
    data class ToolCompleted(val summary: String?) : ChatUiEffect
}