package com.aj.geminiproj.core.model.chat

import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    val id: String,
    val content: String,
    val role: MessageRole,
    val timeStamp: Long = System.currentTimeMillis(),
    val status: MessageStatus = MessageStatus.SENT,
    val metadata: MessageMetaData? = null,
    val toolSteps: List<String> = emptyList()
)

@Serializable
enum class MessageRole {
    USER,
    ASSISTANT,
    SYSTEM
}

@Serializable
enum class MessageStatus {
    SENT,
    RECEIVED,
    STREAMING,
    FAILED,
    UNKNOWN
}

@Serializable
data class MessageMetaData  (
    val model: String? = null,
    val tokensUsed: Int? = null,
    val responseTimeMs: Long? = null,
    val additionalInfo: Map<String, String>? = null,
    val finishReason: String? = null,
    val errorMessage: String? = null

)