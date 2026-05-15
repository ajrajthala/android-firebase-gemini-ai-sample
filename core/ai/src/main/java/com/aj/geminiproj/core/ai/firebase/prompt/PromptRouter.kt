package com.aj.geminiproj.core.ai.firebase.prompt

interface RouterLlmClient {
    suspend fun classify(systemPrompt: String, userMessage: String): String
}

class PromptRouter(
    private val llmClient: RouterLlmClient,
    private val parser: RouterDecisionParser
) {

    private fun looksLikeGeneralQuestion(input: String): Boolean {
        val q = input.lowercase()
        val toolKeywords = listOf(
            "calendar", "event", "schedule", "meeting", "appointment",
            "reminder", "tomorrow", "today", "next week", "free slot",
            "contact", "phone", "email", "call", "invite", "attendee"
        )
        return toolKeywords.none { q.contains(it) }
    }

    suspend fun route(userMessage: String): ToolCallDecision {
        // Fast-path: obvious general question -> answer directly without a router llm call
        if (looksLikeGeneralQuestion(userMessage)) {
            return ToolCallDecision.AnswerDirectly(
                systemPrompt = PromptAssembler.buildSystemPrompt(PromptScope.GENERAL)
            )
        }

        val raw = llmClient.classify(
            systemPrompt = PromptPolicy.routerPrompt,
            userMessage = userMessage
        )

        val decision: RouterDecision = parser.parseOrFallback(raw)

        if (decision.scope == PromptScope.GENERAL) {
            return ToolCallDecision.AnswerDirectly(
                systemPrompt = PromptAssembler.buildSystemPrompt(PromptScope.GENERAL)
            )
        }

        if (decision.needsClarification) {
            return ToolCallDecision.AskClarification(
                question = decision.clarificationQuestion,
                scopeHint = decision.scope
            )
        }

        return ToolCallDecision.UseScopedTools(
            scope = decision.scope,
            systemPrompt = PromptAssembler.buildSystemPrompt(decision.scope),
            includeExamples = true
        )
    }
}