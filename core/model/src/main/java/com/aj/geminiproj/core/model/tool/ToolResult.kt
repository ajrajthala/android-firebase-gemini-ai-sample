package com.aj.geminiproj.core.model.tool

sealed class ToolResult {
    data class Success(val data: Map<String, Any>) : ToolResult()
    data class Error(val message: String, val isRetryable: Boolean = false) : ToolResult()
    data class PermissionDenied(val message: String, val permission: String) : ToolResult()
    data class NeedsConfirmation(
        val message: String,
        val options: List<String>,
        val context: Map<String, Any> = emptyMap()
    ) : ToolResult()
}