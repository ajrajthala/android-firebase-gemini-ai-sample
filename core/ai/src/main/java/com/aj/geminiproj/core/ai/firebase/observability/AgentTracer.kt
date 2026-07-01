package com.aj.geminiproj.core.ai.firebase.observability

import com.aj.geminiproj.core.ai.firebase.common.Logger
import com.aj.geminiproj.core.ai.firebase.resolver.SemanticDomain
import com.aj.geminiproj.core.model.tool.Tool
import com.aj.geminiproj.core.model.tool.ToolResult
import kotlin.concurrent.atomics.AtomicLong
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.update

@OptIn(ExperimentalAtomicApi::class)
class AgentTracer(private val logger: Logger) {
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
        logger.i(TAG, SEPARATOR)
        logger.i(TAG, "TURN START id=$turnId")
        logger.i(TAG, "USER: $userMessage")
    }

    fun logDomainResolution(
        domains: List<SemanticDomain>,
        resolutionMs: Long,
        wasSkipped: Boolean
    ) {
        if (wasSkipped) {
            logger.i(TAG, "  DOMAINS: [cached] ${domains.map { it.id }} (skipped resolver)")
        } else {
            logger.i(TAG, "  DOMAINS: resolved=${domains.map { it.id }} latency=${resolutionMs}ms")
        }

        if (domains.isEmpty()) {
            logger.i(TAG, "  No domains resolved - will answer without tools")
        }
    }

    fun logSystemPrompt(systemPrompt: String) {
        val preview = systemPrompt.take(200).replace("\n", " ")
        logger.d(TAG, "  SYSTEM_PROMPT: $preview...")
    }

    fun logToolsLoaded(tools: List<Tool>) {
        if (tools.isEmpty()) {
            logger.w(TAG, "  TOOLS LOADED: none - check domain resolution and function name mapping")
        } else {
            logger.i(TAG, "  TOOLS LOADED: ${tools.map { it.definition.functionName }}")
        }
    }

    fun logFewShotInjection(injected: Boolean, domains: List<String>) {
        if (injected) {
            logger.i(TAG, "  FEW_SHOT: injected for $domains")
        } else {
            logger.d(TAG, "  FEW_SHOT: skipped (already in history)")
        }
    }

    fun logHistoryWindow(fullSize: Int, windowSize: Int) {
        logger.d(TAG, "  HISTORY: full=$fullSize window=$windowSize")
    }

    //----------- Tool execution ---------------------
    fun logToolCall(functionName: String, args: Map<String, Any>) {
        logger.i(TAG, "  TOOL_CALL: $functionName")
        args.forEach { (k, v) -> logger.i(TAG, "     args $k = $v") }
    }

    fun logToolResult(
        functionName: String,
        result: ToolResult,
        latencyMs: Long
    ) {
        when (result) {
            is ToolResult.Error -> {
                logger.w(TAG, "  TOOL_RESULT: $functionName ERROR latency=${latencyMs}ms")
                logger.w(TAG, "  message: ${result.message} retryable=${result.isRetryable}")
            }

            is ToolResult.NeedsConfirmation -> {
                logger.w(TAG, "  TOOL_RESULT: $functionName NEED_CONFIRMATION latency=${latencyMs}ms")
                logger.w(TAG, "  options: ${result.options}")
            }

            is ToolResult.PermissionDenied -> {
                logger.w(TAG, "  TOOL_RESULT: $functionName PERMISSION_DENIED latency=${latencyMs}ms")
                logger.w(TAG, "  message: ${result.permission}")

            }

            is ToolResult.Success -> {
                logger.i(TAG, "  TOOL_RESULT: $functionName SUCCESS latency=${latencyMs}ms")
                result.data.forEach { (k, v) -> logger.d(TAG, "    $k-$v") }
            }
        }
    }

    //-------------- Turn completion -----------------
    fun logTurnCompletion(aiResponse: String, tokenSummary: TokenSummary? = null) {
        val totalMs = System.currentTimeMillis() - turnStartMs.load()
        logger.i(TAG, "  AI_RESPONSE: ${aiResponse.take(150).replace("\n", " ")}")
        tokenSummary?.let {
            logTokenSummary(it)
        }
        logger.i(TAG, "  TURN END id=$currentTurnId total=${totalMs}ms")
        logger.i(TAG, SEPARATOR)
    }

    fun logTurnError(error: Throwable) {
        val totalMs = System.currentTimeMillis() - turnStartMs.load()
        logger.e(TAG, "  TURN_ERROR: id=$currentTurnId ${error.message ?: "Unknown error"} total=${totalMs}ms")
        logger.e(TAG, SEPARATOR)
    }

    fun logTopicShift(detected: Boolean, message: String) {
        if (detected) logger.i(TAG, "  TOPIC_SHIFT detected for: $message")
    }

    //---------------- Token Counter
    fun logTokenSummary(summary: TokenSummary) {
        logger.i(
            TAG, "   TOKENS: prompt=${summary.promptTokens} " +
                    "candidates=${summary.candidateTokens} " +
                    "total=${summary.totalTokens} " +
                    "rounds=${summary.toolRounds}"
        )
    }

    fun logGuardrailBlocked(reason: String) {
        val totalMs = System.currentTimeMillis() - turnStartMs.load()
        logger.w(TAG, "   GUARDRAIL_BLOCKED: $reason total=${totalMs}ms")
        logger.w(TAG, SEPARATOR)
    }
}