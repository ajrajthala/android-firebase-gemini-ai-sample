package com.aj.geminiproj.core.ai.firebase

import com.aj.geminiproj.core.model.chat.ChatMessage
import com.aj.geminiproj.core.model.chat.MessageRole
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FirebaseAiClient(val modelName: String = "gemini-3-flash-preview") {
    private val generativeModel by lazy {
        Firebase.ai(backend = GenerativeBackend.Companion.googleAI())
            .generativeModel(modelName)
    }

    suspend fun generateContent(prompt: String, history: List<ChatMessage>): String {
        val chat = generativeModel.startChat(
            history = history.toFirebaseChatHistory(),
        )

        val response = chat.sendMessage(prompt)
        return response.text ?: throw Exception("No response from AI")
    }

    fun generateContentStream(prompt: String, history: List<ChatMessage>): Flow<String> {
        val chat = generativeModel.startChat(
            history = history.toFirebaseChatHistory(),
        )
        return chat.sendMessageStream(prompt).map { it.text ?: "" }
    }

    private fun List<ChatMessage>.toFirebaseChatHistory(): List<Content> {
        return this.mapNotNull { message ->
            when (message.role) {
                MessageRole.USER -> content(role = "user") {
                    text(message.content)
                }

                MessageRole.ASSISTANT -> content(role = "assistant") {
                    text(message.content)
                }

                MessageRole.SYSTEM -> null // Firebase doesn't support system role
            }
        }
    }
}