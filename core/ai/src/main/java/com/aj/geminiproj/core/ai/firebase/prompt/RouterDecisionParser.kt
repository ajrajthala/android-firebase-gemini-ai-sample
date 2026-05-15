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
            ?: return fallbackGeneral()
        if (parsed.confidence !in 0.0..1.0) {
            return fallbackGeneral()
        }
        if (parsed.needsClarification && parsed.clarificationQuestion.isBlank()) {
            return parsed.copy(
                needsClarification = false,
                clarificationQuestion = ""
            )
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

    private fun fallbackGeneral(): RouterDecision = RouterDecision(
        scope = PromptScope.GENERAL,
        confidence = 0.0,
        needsClarification = false,
        clarificationQuestion = ""
    )

}