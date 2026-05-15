package com.aj.geminiproj.core.ai.firebase.prompt

import kotlinx.serialization.json.Json

class RouterDecisionParser {
    private val json = Json {
        ignoreUnknownKeys = false
        isLenient = false
        coerceInputValues = false
        explicitNulls = false
    }

    fun parseOrFallback(raw: String): RouterDecision {
        val cleaned = sanitize(raw)
        val parsed = runCatching { json.decodeFromString<RouterDecision>(cleaned) }.getOrNull()
            ?: return fallback("I can help with calendar or contacts. Which one do you need?")
        if (parsed.confidence !in 0.0..1.0) {
            return fallback("Could you clarify if this is about calendar or contacts?")
        }
        if (parsed.needsClarification && parsed.clarificationQuestion.isBlank()) {
            return fallback("Could you clarify your request?")
        }
        return parsed
    }

    private fun sanitize(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.startsWith("```")) {
            return trimmed.removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()
        }
        return trimmed
    }

    private fun fallback(question: String): RouterDecision = RouterDecision(
        scope = PromptScope.GENERAL,
        confidence = 0.0,
        needsClarification = true,
        clarificationQuestion = question
    )

}