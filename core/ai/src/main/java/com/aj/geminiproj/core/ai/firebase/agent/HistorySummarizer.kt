package com.aj.geminiproj.core.ai.firebase.agent

import android.util.Log
import com.aj.geminiproj.core.model.chat.ChatMessage
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend

class HistorySummarizer(
    private val modelName: String = "gemini-3-flash-preview"
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
            val model = Firebase
                .ai(backend = GenerativeBackend.googleAI())
                .generativeModel(modelName)
            val response = model.generateContent(prompt)
            val summary = response.text?.trim()
            Log.i(TAG, "Summary generated (${summary?.length} chars")
            summary
        } catch (e: Exception) {
            Log.e(TAG, "Summarization failed: ${e.message}")
            null
        }
    }
}