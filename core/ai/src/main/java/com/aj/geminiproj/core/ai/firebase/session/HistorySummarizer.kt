package com.aj.geminiproj.core.ai.firebase.session

import com.aj.geminiproj.core.ai.firebase.GenerativeModelFactory
import com.aj.geminiproj.core.ai.firebase.common.Logger
import com.aj.geminiproj.core.ai.firebase.di.GEMINI_MODEL
import com.aj.geminiproj.core.model.chat.ChatMessage
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend

class HistorySummarizer(
    private val modelFactory: GenerativeModelFactory,
    private val logger: Logger,
) {
    companion object {
        private const val TAG = "HistorySummarizer"
    }

    suspend fun summarize(messages: List<ChatMessage>): String? {
        if (messages.isEmpty()) return null

        val transcript = messages.joinToString("\n") { msg ->
            "${msg.role.name}: ${msg.content}"
        }

        val prompt = """
            Summarize the following conversation in 3-5 sentences.
            Focus on: key user goals, decisions made, tool results that matter, and any unresolved items.
            Be concise. This summary will be prepended to the next AI turn for context.
            
            Conversation:
            $transcript
        """.trimIndent()

        return try {
            val model = modelFactory.create(GEMINI_MODEL)
            val response = model.generateContent(prompt)
            val summary = response.text?.trim()
            logger.i(TAG, "Summary generated (${summary?.length} chars")
            summary
        } catch (e: Exception) {
            logger.e(TAG, "Summarization failed: ${e.message}")
            null
        }
    }
}