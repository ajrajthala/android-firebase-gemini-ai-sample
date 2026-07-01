package com.aj.geminiproj.core.model.tool

data class ToolParameter(
    val name: String,
    val type: ParameterType,
    val description: String,
    val required: Boolean
)

enum class ParameterType{
    STRING,
    NUMBER,
    BOOLEAN,
    INTEGER,
    ARRAY
}