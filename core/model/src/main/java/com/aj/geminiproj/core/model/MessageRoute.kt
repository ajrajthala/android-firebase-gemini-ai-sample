package com.aj.geminiproj.core.model

import kotlinx.serialization.Serializable

@Serializable
sealed class MessageRoute {
    @Serializable
    data object ChatList : MessageRoute()

    @Serializable
    data class ChatDetail(val conversationId: String) : MessageRoute()

    @Serializable
    data object NewChat : MessageRoute()

    @Serializable
    data object Settings : MessageRoute()
}