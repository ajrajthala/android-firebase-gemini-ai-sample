package com.aj.geminiproj.core.data.db.mapper

import com.aj.geminiproj.core.data.entity.ConversationEntity
import com.aj.geminiproj.core.data.entity.MessageEntity
import com.aj.geminiproj.core.model.chat.ChatMessage
import com.aj.geminiproj.core.model.chat.ChatConversation
import com.aj.geminiproj.core.model.chat.MessageRole
import com.aj.geminiproj.core.model.chat.MessageStatus

fun ChatConversation.toEntity(): ConversationEntity =
    ConversationEntity(
        id = id,
        title = title,
        createdAt = createdAt,
        updatedAt = updatedAt,
        lastMessage = messages.lastOrNull()?.content,
    )

fun ConversationEntity.toDomain(messages: List<MessageEntity>): ChatConversation =
    ChatConversation(
        id = id,
        title = title,
        createdAt = createdAt,
        updatedAt = updatedAt,
        messages = messages.map { it.toDomain() }
    )

fun ChatMessage.toEntity(conversationId: String): MessageEntity =
    MessageEntity(
        id = id,
        conversationId = conversationId,
        content = content,
        role = role.name,
        status = status.name,
        timeStamp = timeStamp
    )

fun MessageEntity.toDomain(): ChatMessage =
    ChatMessage(
        id = id,
        content = content,
        role = MessageRole.valueOf(role),
        status = MessageStatus.valueOf(status),
        timeStamp = timeStamp
    )