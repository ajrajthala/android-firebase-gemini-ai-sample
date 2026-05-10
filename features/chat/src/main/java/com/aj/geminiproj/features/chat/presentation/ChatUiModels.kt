package com.aj.geminiproj.features.chat.presentation

import com.aj.geminiproj.core.model.chat.ChatMessage


data class ChatUiState(
    val title: String = "New Chat",
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val isStreaming: Boolean = false,
    val streamingText: String = "",
    val error: String? = null,
    val conversationId: String = "",
    val createdAt: Long = System.currentTimeMillis()
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
}

sealed interface ChatUiEffect {
    data object ScrollToBottom : ChatUiEffect
    data class ShowError(val message: String) : ChatUiEffect
    data object ClearInput : ChatUiEffect
    data object ChatDeleted : ChatUiEffect
    data class ConversationStarted(val conversationId: String) : ChatUiEffect
}