package com.aj.geminiproj.core.ai.firebase.dispatcher

import com.aj.geminiproj.core.ai.firebase.registry.ToolRegistry
import com.aj.geminiproj.core.model.tool.ToolResult

class ToolDispatcher(private val registry: ToolRegistry) {
    suspend fun dispatch(functionName: String, args: Map<String, Any>): ToolResult {
        val tool = registry.getToolByName(functionName)
            ?: return ToolResult.Error("Tool not found: $functionName", isRetryable = false)

        return runCatching {
            tool.execute(args)
        }.getOrElse { e ->
            ToolResult.Error("Error executing tool: ${e.message}", isRetryable = false)
        }
    }
}