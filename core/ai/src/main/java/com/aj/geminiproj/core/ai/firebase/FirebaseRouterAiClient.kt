package com.aj.geminiproj.core.ai.firebase

import com.aj.geminiproj.core.ai.firebase.prompt.RouterLlmClient
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content

class FirebaseRouterAiClient(
    private val modelName: String = "gemini-2.5-flash"
) : RouterLlmClient {

    override suspend fun classify(
        systemPrompt: String,
        userMessage: String
    ): String {
        val model = Firebase
            .ai(backend = GenerativeBackend.googleAI())
            .generativeModel(
                modelName = modelName,
                systemInstruction = content { text(systemPrompt) }
            )
        val response = model.generateContent(userMessage)
        return response.text.orEmpty().trim()
    }
}