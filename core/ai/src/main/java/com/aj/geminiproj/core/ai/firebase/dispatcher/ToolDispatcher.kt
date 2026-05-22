package com.aj.geminiproj.core.ai.firebase.dispatcher

import com.aj.geminiproj.core.ai.firebase.resolver.SemanticDomainRegistry
import com.aj.geminiproj.core.model.tool.ToolResult

class ToolDispatcher(
    private val registry: SemanticDomainRegistry,
    private val validator: ToolValidator
) {
    suspend fun dispatch(functionName: String, args: Map<String, Any>): ToolResult {
        val tool = registry.getToolByName(functionName)
            ?: return ToolResult.Error("Tool not found: $functionName", isRetryable = false)
        //validate schema before executing
        when (val validation = validator.validate(tool, args)) {
            is ToolValidator.ValidationResult.Invalid -> {
                val reason = validation.violations.joinToString("; ")
                return ToolResult.Error(
                    message = "Invalid tool arguments: $reason",
                    isRetryable = false
                )
            }

            ToolValidator.ValidationResult.Valid -> Unit
        }

        return runCatching {
            tool.execute(args)
        }.getOrElse { e ->
            ToolResult.Error("Error executing tool: ${e.message}", isRetryable = false)
        }
    }
}