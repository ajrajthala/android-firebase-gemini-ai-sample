package com.aj.geminiproj.core.model.chat

import kotlinx.serialization.Serializable

@Serializable
data class ChatConversation(
    val id: String,
    val title: String = "New Chat",
    val messages: List<ChatMessage> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false,
) {
    fun lastMessage(): ChatMessage? {
        return messages.lastOrNull()
    }

    fun isEmpty(): Boolean = messages.isEmpty()

    fun addMessage(message: ChatMessage): ChatConversation {
        val updatedMessages = messages + message
        return copy(messages = updatedMessages, updatedAt = System.currentTimeMillis())
    }

    fun updateMessage(messageId: String, update: (ChatMessage) -> ChatMessage): ChatConversation {
        return copy(
            messages = messages.map { if (it.id == messageId) update(it) else it },
            updatedAt = System.currentTimeMillis()
        )
    }
}
