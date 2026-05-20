package com.aj.geminiproj.core.ai.firebase.agent

import android.util.Log

/**
 * Guard against prompt injection and adversarial inputs
 *
 * Checks for patterns that attempt to:
 * Override the system prompt
 * Hijack tool execution
 * indirect injection
 */
class InputGuardrail {
    companion object {
        private const val TAG = "InputGuardrail"

        // Guardrail for system instructions
        private val SYSTEM_OVERRIDE_PATTERNS = listOf(
            "ignore previous instructions",
            "ignore all previous",
            "disregard your instructions",
            "forget your instructions",
            "you are now",
            "new instructions:",
            "system prompt:",
            "your new role:",
            "act as if",
            "pretend you are",
            "override your",
            "bypass your"
        )

        // Guardrails for tool calls
        private val TOOL_INJECTION_PATTERNS = listOf(
            "call the tool",
            "execute function",
            "invoke function",
            "run function",
            "call function",
            "<function_calls>",
            "<tool_call>",
            "function_call:",
        )

        // Indirect prompt injection via tool results,..
        private val INDIRECT_INJECTION_MARKERS = listOf(
            "###instruction",
            "## new task",
            "[system]",
            "<<<",
            "---end of context---",
        )

        private const val MAX_INPUT_LENGTH = 4000
    }

    sealed class GuardrailResult {
        object Allowed : GuardrailResult()
        data class Blocked(val reason: String) : GuardrailResult()
    }

    fun check(userInput: String): GuardrailResult {
        if (userInput.isBlank()) {
            return GuardrailResult.Blocked("Empty input")
        }

        if (userInput.length > MAX_INPUT_LENGTH) {
            Log.w(TAG, "Input blocked: exceeds max length(${userInput.length})")
            return GuardrailResult.Blocked("Input too long (max $MAX_INPUT_LENGTH characters")
        }

        val userInputLowercase = userInput.lowercase()
        SYSTEM_OVERRIDE_PATTERNS.forEach { pattern ->
            if (userInputLowercase.contains(pattern)) {
                Log.w(TAG, "Input blocked: system_override pattern='$pattern'")
                return GuardrailResult.Blocked("Potential prompt injection detected")
            }
        }

        TOOL_INJECTION_PATTERNS.forEach { pattern ->
            if (userInputLowercase.contains(pattern)) {
                Log.w(TAG, "Input blocked: tool_injection pattern='$pattern'")
                return GuardrailResult.Blocked("Potential tool injection detected")
            }
        }

        INDIRECT_INJECTION_MARKERS.forEach { marker ->
            if (userInputLowercase.contains(marker)) {
                Log.w(TAG, "Input blocked: indirection_injection_marker pattern='$marker'")
                return GuardrailResult.Blocked("Suspicious input structure detected")
            }
        }

        return GuardrailResult.Allowed
    }

}