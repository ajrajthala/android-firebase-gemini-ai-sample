package com.aj.geminiproj.core.model.tool

interface Tool {
    val definition: ToolDefinition
    suspend fun execute(parameters: Map<String, Any>): ToolResult
}