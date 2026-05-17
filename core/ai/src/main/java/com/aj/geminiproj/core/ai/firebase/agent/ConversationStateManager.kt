package com.aj.geminiproj.core.ai.firebase.agent

import android.util.Log
import com.aj.geminiproj.core.model.chat.ChatMessage
import com.aj.geminiproj.core.model.chat.MessageRole

/**
 * Owns all session-level state for a single conversation.
 *
 * Tracks:
 *  activeDomains
 *  introduceDomains
 *  historyWindow
 *  historySummary
 *
 * Doesn't track pending takss, pendig scope, clarification questions
 *  -> these are owned natively by the LLM via conversation history
 */
class ConversationStateManager(
    private val contextBuilder: AgentContextBuilder,
    private val historyWindowSize: Int = 10,
    private val summarizationThreshold: Int = 20,
) {
    companion object {
        private const val TAG = "ConversationStateMgr"
    }

    private var activeDomains: List<SemanticDomain> = emptyList()
    private val introducedDomains = mutableSetOf<String>()
    private var systemPromptCache: String? = null
    private var historySummary: String? = null
    private var isFirstTurn: Boolean = true

    fun setActiveDomains(domains: List<SemanticDomain>) {
        val changed = domains.map { it.id }.toSet() != activeDomains.map { it.id }.toSet()
        if (changed) {
            activeDomains = domains
            systemPromptCache = null
            Log.i(TAG, "Active domains updated: ${domains.map { it.id }}")
        }
    }

    fun getActiveDomains() = activeDomains

    fun isFirstTurn(): Boolean = isFirstTurn

    fun markTurnProcessed() {
        isFirstTurn = false
    }

    /**
     * Detects topic shifts; return true if the current message appears
     * unrelated to all active domains, triggering re-resolution.
     * Uses lightweight description word overlap = no LLM call.
     */
    fun detectTopicShift(userMessage: String): Boolean {
        if (activeDomains.isEmpty()) return false
        val messageWords = userMessage.lowercase()
            .split(" ", ", ", ".", "?", "!")
            .filter { it.length > 3 }
            .toSet()

        val domainWords = activeDomains
            .flatMap { domain ->
                domain.description.lowercase().split(" ", "\n")
                    .filter { it.length > 3 }
                    .toSet()
            }

        val overlap = messageWords.intersect(domainWords)
        val isShift = overlap.isEmpty() && messageWords.size > 2
        if (isShift) Log.i(TAG, "Topic shift detected for: $userMessage")
        return isShift
    }

    fun buildSystemPrompt(): String {
        return systemPromptCache ?: contextBuilder.buildSystemPrompt(activeDomains).also {
            systemPromptCache = it
        }
    }

    fun consumeFewShotPrimers(): String? {
        val newDomains = activeDomains.filter { !introducedDomains.contains(it.id) }
        if (newDomains.isEmpty()) return null
        introducedDomains.addAll(newDomains.map { it.id })
        Log.i(TAG, "Injecting few-shot primers for: ${newDomains.map { it.id }}")
        return contextBuilder.buildFewShotPrimer(newDomains)
    }

    // --------------- History Window Management -------------------------------
    fun boundedHistory(fullHistory: List<ChatMessage>): List<ChatMessage> {
        if (fullHistory.size <= historyWindowSize) return fullHistory
        val window = fullHistory.takeLast(historyWindowSize)
        return if (historySummary != null) {
            val summaryMessage = ChatMessage(
                id = "summary",
                content = "[Earlier conversation summary]: $historySummary",
                role = MessageRole.ASSISTANT,
                timeStamp = 0L
            )
            listOf(summaryMessage) + window
        }else{
            window
        }
    }

    fun shouldSummarize(fullHistory: List<ChatMessage>): Boolean =
        fullHistory.size> summarizationThreshold

    fun applyHistorySummary(summary: String){
        historySummary = summary
        Log.i(TAG, "History summary applied.")
    }

    fun reset(){
        activeDomains = emptyList()
        introducedDomains.clear()
        systemPromptCache = null
        historySummary = null
        isFirstTurn = true
        Log.i(TAG, "Session reset.")
    }

}