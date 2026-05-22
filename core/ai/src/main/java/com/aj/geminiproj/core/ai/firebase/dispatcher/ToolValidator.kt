package com.aj.geminiproj.core.ai.firebase.dispatcher

import com.aj.geminiproj.core.ai.firebase.common.Logger
import com.aj.geminiproj.core.model.tool.ParameterType
import com.aj.geminiproj.core.model.tool.Tool
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.longOrNull
import kotlin.math.floor

/**
 * Validates tool call arguments against the tool's declared schema before dispatch
 * to prevent malformed calls
 *
 */
class ToolValidator(private val logger: Logger) {
    companion object {
        private const val TAG = "ToolValidator"
    }

    sealed class ValidationResult {
        object Valid : ValidationResult()
        data class Invalid(val violations: List<String>) : ValidationResult()
    }

    fun validate(tool: Tool, args: Map<String, Any>): ValidationResult {
        val violations = mutableListOf<String>()
        val definition = tool.definition

        // Check for required fields
        definition.parameters
            .filter { it.required }
            .forEach { parameter ->
                if (!args.containsKey(parameter.name)) {
                    violations += "Missing required parameter: '${parameter.name}'"
                }
            }

        // Check for parameter types
        args.forEach { (argName, argValue) ->
            val paramDef = definition.parameters.find { it.name == argName }
            if (paramDef == null) {
                //Unknown parameter type - warn but do not block as LLM may send extra fields
                logger.w(TAG, "Unknown parameter '${argName}' for tool '${definition.functionName}'")
            } else {
                val typeError = checkType(argName, argValue, paramDef.type)
                if (typeError != null) violations += typeError
            }
        }

        return if (violations.isEmpty()) {
            ValidationResult.Valid
        } else {
            logger.w(TAG, "Validation failed for '${definition.functionName}':$violations")
            ValidationResult.Invalid(violations)
        }
    }

    private fun checkType(
        argName: String,
        argValue: Any,
        expectedType: ParameterType
    ): String? {
        val isValid = when (expectedType) {
            ParameterType.STRING -> argValue is String || (argValue is JsonPrimitive && argValue.isString)
            ParameterType.INTEGER -> argValue is Int || argValue is Long ||
                    (argValue is Double && argValue == floor(argValue)) ||
                    (argValue is String && argValue.toLongOrNull() != null) ||
                    (argValue is JsonPrimitive && argValue.longOrNull != null)
            ParameterType.NUMBER -> argValue is Number ||
                    (argValue is String && argValue.toDoubleOrNull() != null) ||
                    (argValue is JsonPrimitive && argValue.doubleOrNull != null)
            ParameterType.BOOLEAN -> argValue is Boolean ||
                    (argValue is JsonPrimitive && argValue.booleanOrNull != null)
            ParameterType.ARRAY -> argValue is List<*>
        }
        return if (!isValid) "Parameter '$argName' expected '$expectedType' but got ${argValue::class.simpleName}"
        else null
    }
}