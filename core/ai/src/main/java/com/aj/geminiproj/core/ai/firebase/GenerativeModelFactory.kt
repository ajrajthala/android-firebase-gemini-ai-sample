package com.aj.geminiproj.core.ai.firebase

import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.HarmBlockThreshold
import com.google.firebase.ai.type.HarmCategory
import com.google.firebase.ai.type.SafetySetting
import com.google.firebase.ai.type.Tool
import com.google.firebase.ai.type.content

interface GenerativeModelFactory {
    fun create(modelName: String): GenerativeModel

    fun createWithSystemPrompt(modelName: String, systemPrompt: String): GenerativeModel

    fun createWithTools(
        modelName: String,
        systemPrompt: String,
        firebaseTool: Tool,
    ): GenerativeModel
}

class FirebaseGenerativeModelFactory :
    GenerativeModelFactory {
    private val defaultSafetySettings = listOf(
        SafetySetting(HarmCategory.DANGEROUS_CONTENT, HarmBlockThreshold.ONLY_HIGH),
        SafetySetting(HarmCategory.HARASSMENT, HarmBlockThreshold.ONLY_HIGH),
        SafetySetting(HarmCategory.HATE_SPEECH, HarmBlockThreshold.ONLY_HIGH),
        SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, HarmBlockThreshold.ONLY_HIGH)
    )

    override fun create(modelName: String): GenerativeModel =
        Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel(modelName)


    override fun createWithSystemPrompt(modelName: String, systemPrompt: String): GenerativeModel =
        Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel(modelName, systemInstruction = content { text(systemPrompt) })

    override fun createWithTools(
        modelName: String,
        systemPrompt: String,
        firebaseTool: Tool
    ): GenerativeModel {
        return Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel(
                modelName = modelName,
                tools = listOf(firebaseTool),
                systemInstruction = content { text(systemPrompt) },
                safetySettings = defaultSafetySettings
            )

    }
}