package com.aj.geminiproj.core.ai.firebase.agent

import com.aj.geminiproj.core.ai.firebase.orchestration.GeminiOrchestrator
import com.aj.geminiproj.core.model.chat.ChatMessage
import com.aj.geminiproj.core.model.chat.ChatStreamEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID

/**
 * Owns the full agent turn pipeline from user message to llm response stream
 */
class ConversationAgent(
    private val stateManager: ConversationStateManager,
    private val domainResolver: SemanticDomainResolver,
    private val domainRegistry: SemanticDomainRegistry,
    private val tracer: AgentTracer,
    private val orchestrator: GeminiOrchestrator,
    private val guardrail: InputGuardrail,
) {

    fun processMessage(
        userMessage: String,
        fullHistory: List<ChatMessage>
    ): Flow<ChatStreamEvent> = flow {
        val turnId = UUID.randomUUID().toString()
        tracer.beginTurn(turnId, userMessage)

        // Input guardrail
        when (val guardrail = guardrail.check(userMessage)) {
            is InputGuardrail.GuardrailResult.Blocked -> {
                emit(
                    ChatStreamEvent.StreamError(
                        throwable = SecurityException(guardrail.reason),
                        errorMessage = "Unable to process the request. ${guardrail.reason}."
                    )
                )
                return@flow
            }

            InputGuardrail.GuardrailResult.Allowed -> Unit
        }

        //------- Step 1: Resolve domains (Once per conversation or on topic shift)
        val needsResolution =
            stateManager.isFirstTurn() || stateManager.detectTopicShift(userMessage)
        tracer.logTopicShift(
            detected = needsResolution && !stateManager.isFirstTurn(),
            message = userMessage
        )
        if (needsResolution) {
            val resolutionStartTime = System.currentTimeMillis()
            val resolvedDomains = domainResolver.resolve(userMessage)
            stateManager.setActiveDomains(resolvedDomains)
            tracer.logDomainResolution(
                domains = resolvedDomains,
                resolutionMs = System.currentTimeMillis() - resolutionStartTime,
                wasSkipped = false
            )
        } else {
            tracer.logDomainResolution(
                domains = stateManager.getActiveDomains(),
                resolutionMs = 0L,
                wasSkipped = true
            )
        }
        stateManager.markTurnProcessed()

        //----Step 2: Build system prompt -------------
        val systemPrompt = stateManager.buildSystemPrompt()

        //----Step 3: Few-shot primers
        val fewShotPrimer = stateManager.consumeFewShotPrimers()
        tracer.logFewShotInjection(
            injected = fewShotPrimer != null,
            domains = stateManager.getActiveDomains().map { it.id }
        )

        //-----Step 4: active tools for this session only
        val activeTools = domainRegistry.getToolsForDomains(stateManager.getActiveDomains())

        //-----Step 5: Bounded history (Sliding window)
        val boundedHistory = stateManager.boundedHistory(fullHistory)
        tracer.logHistoryWindow(
            fullSize = fullHistory.size,
            windowSize = boundedHistory.size
        )
        //----------Step 6: LLM call
        orchestrator.sendChatMessageWithTools(
            message = userMessage,
            systemPrompt = systemPrompt,
            activeTools = activeTools,
            conversationHistory = boundedHistory,
            fewShotPrimer = fewShotPrimer,
            turnId = turnId
        ).collect { event ->
            emit(event)
        }
    }

    fun reset() {
        stateManager.reset()
    }
}