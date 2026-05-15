package com.aj.geminiproj.core.ai.firebase.prompt

data class PromptPack(
    val systemPrompt: String,
    val examples: String? = null
)

object PromptAssembler {
    fun buildSystemPrompt(scope: PromptScope): String {
        val feature = when (scope) {
            PromptScope.CALENDAR -> PromptPolicy.calendarFeature
            PromptScope.CONTACT -> PromptPolicy.contactsFeature
            PromptScope.GENERAL -> PromptPolicy.generalFeature
        }

        return buildString {
            appendLine(PromptPolicy.globalBase)
            appendLine()
            appendLine(feature)
        }
    }

    fun buildPack(scope: PromptScope, includeExamples: Boolean = true): PromptPack {
        val examples = if (!includeExamples) {
            null
        } else {
            when (scope) {
                PromptScope.CALENDAR -> PromptExamples.calendar
                PromptScope.CONTACT -> PromptExamples.contacts
                PromptScope.GENERAL -> null
            }
        }

        return PromptPack(
            systemPrompt = buildSystemPrompt(scope),
            examples = examples
        )
    }

    fun buildFinalPrompt(scope: PromptScope, includeExamples: Boolean = true): String {
        val pack = buildPack(scope, includeExamples)
        return buildString {
            appendLine(pack.systemPrompt)
            if (!pack.examples.isNullOrBlank()) {
                appendLine()
                appendLine(pack.examples)
            }
        }
    }

    fun fallbackPrompt(): String = buildString {
        appendLine(PromptPolicy.globalBase)
        appendLine()
        append(PromptPolicy.generalFeature)
    }
}