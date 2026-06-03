package com.aj.geminiproj.core.ai.firebase.ingelligence

import com.aj.geminiproj.core.ai.firebase.common.Logger
import com.aj.geminiproj.core.ai.firebase.dispatcher.ToolDispatcher
import com.aj.geminiproj.core.model.tool.ToolResult
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlin.collections.map

class ParallelToolExecutor(
    private val dispatcher: ToolDispatcher,
    private val cache: ToolResultCache,
    private val logger: Logger
) {
    companion object {
        private const val TAG = "ParallelToolExecutor"
    }

    // Tools that cause side effects and should not be cached.
    private val WRITE_TOOLS = setOf("createCalendarEvent")

    private val INVALIDATION_MAP = mapOf(
        "createCalendarEvent" to "getCalendarEvents"
    )

    /**
     * Execute multiple tool calls in parallel and cache results.
     *
     * @param toolCalls list of tool calls to execute
     * @return map of tool call id to result
     */
    suspend fun executeAll(toolCalls: List<ToolCall>): List<ExecutionResult> = coroutineScope {
        val jobs = toolCalls.map { toolCall ->
            async {
                executeSingle(toolCall)
            }
        }
        jobs.awaitAll()
    }

    private suspend fun executeSingle(toolCall: ToolCall): ExecutionResult {
        val isWrite = toolCall.toolName in WRITE_TOOLS
        val startMs = System.currentTimeMillis()

        // --- Cache check (reads only) ---
        if (!isWrite) {
            cache.getCachedResult(toolCall.toolName, toolCall.args)?.let {
                val latency = System.currentTimeMillis() - startMs
                logger.d(TAG, "Cache hit for '${toolCall.toolName}' latency=${latency}ms")
                return ExecutionResult(
                    toolCall,
                    it,
                    latency,
                    fromCache = true
                )
            }
        }

        // --- Execute via dispatcher ---
        val result = runCatching {
            dispatcher.dispatch(toolCall.toolName, toolCall.args)
        }.getOrElse { e ->
            logger.e(TAG, "Tool execution failed for '${toolCall.toolName}': ${e.message}")
            ToolResult.Error("Execution failed: ${e.message}")
        }

        val latency = System.currentTimeMillis() - startMs
        logger.i(
            TAG,
            "Parallel Executed '${toolCall.toolName}' latency=${latency}ms fromCache=false"
        )

        // --- Cache result if applicable ---
        if (!isWrite && result is ToolResult.Success) {
            cache.putCachedResult(toolCall.toolName, toolCall.args, result)
        } else {
            // Invalidate reads caused by this write
            INVALIDATION_MAP[toolCall.toolName]?.let { prefix ->
                cache.invalidate(prefix)
            }
        }

        return ExecutionResult(
            toolCall,
            result,
            latency,
            fromCache = false
        )

    }
}

data class ToolCall(
    val toolName: String,
    val args: Map<String, Any>
)

data class ExecutionResult(
    val toolCall: ToolCall,
    val result: ToolResult,
    val latencyMs: Long,
    val fromCache: Boolean
)