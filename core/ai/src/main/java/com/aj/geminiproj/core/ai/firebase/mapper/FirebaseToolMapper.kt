package com.aj.geminiproj.core.ai.firebase.mapper

import com.aj.geminiproj.core.model.tool.ParameterType
import com.aj.geminiproj.core.model.tool.Tool
import com.aj.geminiproj.core.model.tool.ToolDefinition
import com.aj.geminiproj.core.model.tool.ToolParameter
import com.aj.geminiproj.core.model.tool.ToolResult
import com.google.firebase.ai.type.FunctionDeclaration
import com.google.firebase.ai.type.FunctionResponsePart
import com.google.firebase.ai.type.Schema
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import com.google.firebase.ai.type.Tool as FirebaseTool

class FirebaseToolMapper {
    fun toFirebaseTool(tools: List<Tool>): FirebaseTool {
        val declarations = tools.map { tool ->
            toFunctionDeclaration(tool.definition)
        }
        return FirebaseTool.functionDeclarations(declarations)
    }

    private fun toFunctionDeclaration(definition: ToolDefinition): FunctionDeclaration {
        val properties = definition.parameters.associate { param ->
            param.name to toSchema(param)
        }
        val optionalParameters = definition.parameters.filter { !it.required }.map { it.name }
        return FunctionDeclaration(
            name = definition.functionName,
            description = definition.description,
            parameters = properties,
            optionalParameters = optionalParameters,
        )
    }

    private fun toSchema(param: ToolParameter): Schema {
        return when (param.type) {
            ParameterType.STRING -> Schema.string(description = param.description)
            ParameterType.NUMBER -> Schema.double(description = param.description)
            ParameterType.BOOLEAN -> Schema.boolean(description = param.description)
            ParameterType.INTEGER -> Schema.integer(description = param.description)
            ParameterType.ARRAY -> Schema.array(
                items = Schema.string(),
                description = param.description
            ) // Assuming array of strings for simplicity
        }
    }

    fun toFunctionResponsePart(functionName: String?, result: ToolResult): FunctionResponsePart {
        val responseData = when (result) {
            is ToolResult.Success -> result.data
            is ToolResult.Error -> mapOf(
                "error" to result.message,
                "isRetryable" to result.isRetryable
            )

            is ToolResult.PermissionDenied -> mapOf(
                "error" to result.message,
                "permission" to result.permission
            )

            is ToolResult.NeedsConfirmation -> mapOf(
                "needs_confirmation" to true,
                "message" to result.message,
                "options" to result.options,
                "context" to result.context
            )
        }

        return FunctionResponsePart(
            name = functionName ?: "",
            response = buildJsonObject(responseData)
        )
    }

    private fun buildJsonObject(data: Map<String, Any>): JsonObject {
        val jsonMap = mutableMapOf<String, JsonElement>()
        for ((key, value) in data) {
            jsonMap[key] = toJsonElement(value)
        }
        return JsonObject(jsonMap)
    }

    private fun toJsonElement(value: Any?): JsonElement {
        return when (value) {
            is String -> Json.encodeToJsonElement(value)
            is Int -> Json.encodeToJsonElement(value)
            is Long -> Json.encodeToJsonElement(value)
            is Double -> Json.encodeToJsonElement(value)
            is Float -> Json.encodeToJsonElement(value)
            is Number -> Json.encodeToJsonElement(value.toDouble())
            is Boolean -> Json.encodeToJsonElement(value)
            is List<*> -> JsonArray(value.map { toJsonElement(it) }) // assuming list contains serializable items
            is Map<*, *> -> buildJsonObject(value as Map<String, Any>)
            else -> {
                Json.encodeToJsonElement(value.toString())
            }
        }
    }
}