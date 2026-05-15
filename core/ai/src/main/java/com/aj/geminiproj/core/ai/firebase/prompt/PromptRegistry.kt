package com.aj.geminiproj.core.ai.firebase.prompt

object PromptRegistry {
    val routerPrompt: String = """
        You are a strict request router...
        (Return JSON only with scope/confidence/needs_clarification/clarification_question)
    """.trimIndent()

    fun generalSystemPrompt(): String = """
        You are an Android assistant. Answer directly without tools unless tool data is required.
    """.trimIndent()

    fun scopedSystemPrompt(scope: PromptScope): String = when(scope){
        PromptScope.CALENDAR -> "Feature scope: CALENDAR ... (strict calendar prompt)"
        PromptScope.CONTACT -> "Feature scope: CONTACT ... (strict calendar prompt)"
        PromptScope.GENERAL -> generalSystemPrompt()
    }
}
