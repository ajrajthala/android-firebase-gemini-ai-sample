package com.aj.geminiproj.core.model.tool

// Define a data class for ToolDefinition which includes the name, description, parameters, and a function to execute the tool
// This is what Gemini will use to understand how to call the tool and what parameters it needs

data class ToolDefinition(
    val functionName: String,
    val description: String,
    val parameters: List<ToolParameter> = emptyList(),
    val displayName: String,
)