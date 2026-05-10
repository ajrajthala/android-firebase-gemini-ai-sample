package com.aj.geminiproj.core.model.chat

sealed class ChatStreamEvent {
    data class TextChunk(val text: String) : ChatStreamEvent()
    data class ToolExecuting(val functionName: String, val displayName: String) : ChatStreamEvent()
    data class ToolCompleted(val functionName: String, val summary: String? = null) :
        ChatStreamEvent()

    data class ToolFailed(
        val functionName: String,
        val errorMessage: String,
        val isPermissionError: Boolean = false
    ) : ChatStreamEvent()

    data object TurnCompleted : ChatStreamEvent()

    data class StreamError(
        val throwable: Throwable,
        val errorMessage: String = "Something went wrong. Please try again."
    ) : ChatStreamEvent()
}