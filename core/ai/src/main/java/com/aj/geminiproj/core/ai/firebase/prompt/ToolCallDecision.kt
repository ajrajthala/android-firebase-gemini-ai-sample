package com.aj.geminiproj.core.ai.firebase.prompt

sealed interface ToolCallDecision {
    data class AskClarification(
        val question: String,
        val scopeHint: PromptScope? = null
    ) : ToolCallDecision

    data class UseScopedTools(
        val scope: PromptScope,
        val systemPrompt: String,
        val includeExamples: Boolean
    ) : ToolCallDecision

    data class AnswerDirectly(
        val systemPrompt: String
    ) : ToolCallDecision
}
