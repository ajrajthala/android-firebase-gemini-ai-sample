package com.aj.geminiproj.features.chat.domain.usecase

import com.aj.geminiproj.core.model.AiResult
import com.aj.geminiproj.core.model.chat.ChatMessage
import com.aj.geminiproj.core.model.chat.MessageRole
import com.aj.geminiproj.core.model.chat.MessageStatus
import com.aj.geminiproj.features.chat.domain.repository.ChatRepository
import java.util.UUID

class SendMessageUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke(message: String, conversationId: String)
            : Result<Pair<ChatMessage, ChatMessage>> {
        if (message.isBlank()) {
            return Result.failure(IllegalArgumentException("Message cannot be empty"))
        }

        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            content = message,
            role = MessageRole.USER,
            status = MessageStatus.SENT,
            timeStamp = System.currentTimeMillis()
        )

        val conversation = repository.getConversation(conversationId)
        repository.saveConversation(conversation.addMessage(userMessage))

        //send to AI
        return when (val aiResult = repository.sendMessage(message, conversationId)) {
            is AiResult.Success -> {
                repository.saveConversation(
                    repository.getConversation(conversationId).addMessage(aiResult.data)
                )
                Result.success(userMessage to aiResult.data)
            }

            is AiResult.Error -> {
                Result.failure(aiResult.error)
            }

            is AiResult.Loading -> {
                Result.failure(IllegalStateException("Unexpected loading state"))
            }
        }

    }
}