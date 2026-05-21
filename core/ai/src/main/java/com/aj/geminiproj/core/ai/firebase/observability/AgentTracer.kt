package com.aj.geminiproj.core.ai.firebase.observability

import android.util.Log
import com.aj.geminiproj.core.ai.firebase.resolver.SemanticDomain
import com.aj.geminiproj.core.model.tool.Tool
import com.aj.geminiproj.core.model.tool.ToolResult
import kotlin.concurrent.atomics.AtomicLong
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.update

@OptIn(ExperimentalAtomicApi::class)
class AgentTracer {
    companion object {
        private const val TAG = "AgentTracer"
        private const val SEPARATOR = "========================================="
    }


    private val turnStartMs= AtomicLong(0L)
    private val currentTurnId = AtomicReference("")

    // ------------- Turn lifecycle -----------------

    fun beginTurn(turnId: String, userMessage: String) {
        turnStartMs.update { System.currentTimeMillis()}
        currentTurnId.update { turnId}
        Log.i(TAG, SEPARATOR)
        Log.i(TAG, "TURN START id=$turnId")
        Log.i(TAG, "USER: $userMessage")
    }

    fun logDomainResolution(
        domains: List<SemanticDomain>,
        resolutionMs: Long,
        wasSkipped: Boolean
    ) {
        if (wasSkipped) {
            Log.i(TAG, "  DOMAINS: [cached] ${domains.map { it.id }} (skipped resolver)")
        } else {
            Log.i(TAG, "  DOMAINS: resolved=${domains.map { it.id }} latency=${resolutionMs}ms")
        }

        if (domains.isEmpty()) {
            Log.i(TAG, "  No domains resolved - will answer without tools")
        }
    }

    fun logSystemPrompt(systemPrompt: String) {
        val preview = systemPrompt.take(200).replace("\n", " ")
        Log.d(TAG, "  SYSTEM_PROMPT: $preview...")
    }

    fun logToolsLoaded(tools: List<Tool>) {
        if (tools.isEmpty()) {
            Log.w(TAG, "  TOOLS LOADED: none - check domain resolution and function name mapping")
        } else {
            Log.i(TAG, "  TOOLS LOADED: ${tools.map { it.definition.functionName }}")
        }
    }

    fun logFewShotInjection(injected: Boolean, domains: List<String>) {
        if (injected) {
            Log.i(TAG, "  FEW_SHOT: injected for $domains")
        } else {
            Log.d(TAG, "  FEW_SHOT: skipped (already in history)")
        }
    }

    fun logHistoryWindow(fullSize: Int, windowSize: Int) {
        Log.d(TAG, "  HISTORY: full=$fullSize window=$windowSize")
    }

    //----------- Tool execution ---------------------
    fun logToolCall(functionName: String, args: Map<String, Any>) {
        Log.i(TAG, "  TOOL_CALL: $functionName")
        args.forEach { (k, v) -> Log.i(TAG, "     args $k = $v") }
    }

    fun logToolResult(
        functionName: String,
        result: ToolResult,
        latencyMs: Long
    ) {
        when (result) {
            is ToolResult.Error -> {
                Log.w(TAG, "  TOOL_RESULT: $functionName ERROR latency=${latencyMs}ms")
                Log.w(TAG, "  message: ${result.message} retryable=${result.isRetryable}")
            }

            is ToolResult.NeedsConfirmation -> {
                Log.w(TAG, "  TOOL_RESULT: $functionName NEED_CONFIRMATION latency=${latencyMs}ms")
                Log.w(TAG, "  options: ${result.options}")
            }

            is ToolResult.PermissionDenied -> {
                Log.w(TAG, "  TOOL_RESULT: $functionName PERMISSION_DENIED latency=${latencyMs}ms")
                Log.w(TAG, "  message: ${result.permission}")

            }

            is ToolResult.Success -> {
                Log.i(TAG, "  TOOL_RESULT: $functionName SUCCESS latency=${latencyMs}ms")
                result.data.forEach { (k, v) -> Log.d(TAG, "    $k-$v") }
            }
        }
    }

    //-------------- Turn completion -----------------
    fun logTurnCompletion(aiResponse: String, tokenSummary: TokenSummary? = null) {
        val totalMs = System.currentTimeMillis() - turnStartMs.load()
        Log.i(TAG, "  AI_RESPONSE: ${aiResponse.take(150).replace("\n", " ")}")
        tokenSummary?.let {
            logTokenSummary(it)
        }
        Log.i(TAG, "  TURN END id=$currentTurnId total=${totalMs}ms")
        Log.i(TAG, SEPARATOR)
    }

    fun logTurnError(error: Throwable) {
        val totalMs = System.currentTimeMillis() - turnStartMs.load()
        Log.e(TAG, "  TURN_ERROR: id=$currentTurnId ${error.message ?: "Unknown error"} total=${totalMs}ms")
        Log.e(TAG, SEPARATOR)
    }

    fun logTopicShift(detected: Boolean, message: String) {
        if (detected) Log.i(TAG, "  TOPIC_SHIFT detected for: $message")
    }

    //---------------- Token Counter
    fun logTokenSummary(summary: TokenSummary) {
        Log.i(
            TAG, "   TOKENS: prompt=${summary.promptTokens} " +
                    "candidates=${summary.candidateTokens} " +
                    "total=${summary.totalTokens} " +
                    "rounds=${summary.toolRounds}"
        )
    }

    fun logGuardrailBlocked(reason: String) {
        val totalMs = System.currentTimeMillis() - turnStartMs.load()
        Log.w(TAG, "   GUARDRAIL_BLOCKED: $reason total=${totalMs}ms")
        Log.w(TAG, SEPARATOR)
    }
}