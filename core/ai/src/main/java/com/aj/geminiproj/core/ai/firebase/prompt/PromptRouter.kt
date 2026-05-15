package com.aj.geminiproj.core.ai.firebase.prompt

interface RouterLlmClient {
    suspend fun classify(systemPrompt: String, userMessage: String): String
}

class PromptRouter(
    private val llmClient: RouterLlmClient,
    private val parser: RouterDecisionParser
) {
    suspend fun route(userMessage: String): ToolCallDecision {
        val raw = llmClient.classify(
            systemPrompt = PromptRegistry.routerPrompt,
            userMessage = userMessage
        )

        val decision: RouterDecision = parser.parseOrFallback(raw)

        if (decision.needsClarification) {
            return ToolCallDecision.AskClarification(
                question = decision.clarificationQuestion,
                scopeHint = decision.scope
            )
        }

        return when (decision.scope) {
            PromptScope.GENERAL -> ToolCallDecision.AnswerDirectly(
                systemPrompt = PromptRegistry.generalSystemPrompt()
            )

            PromptScope.CONTACT, PromptScope.CALENDAR -> ToolCallDecision.UseScopedTools(
                scope = decision.scope,
                systemPrompt = PromptRegistry.scopedSystemPrompt(decision.scope),
                includeExamples = true

            )
        }
    }
}