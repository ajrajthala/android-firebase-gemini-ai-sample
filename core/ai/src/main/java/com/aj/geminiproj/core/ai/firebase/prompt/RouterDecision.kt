package com.aj.geminiproj.core.ai.firebase.prompt

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RouterDecision(
    val scope: PromptScope,
    val confidence: Double,
    @SerialName("needs_clarification")
    val needsClarification: Boolean,
    @SerialName("clarification_question")
    val clarificationQuestion: String
)