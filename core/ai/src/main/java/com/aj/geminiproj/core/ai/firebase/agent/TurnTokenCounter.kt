package com.aj.geminiproj.core.ai.firebase.agent

class TurnTokenCounter {

    private var promptTokensTotal: Int = 0
    private var candidateTokensTotal: Int = 0
    private var roundCount: Int = 0

    /**
     * Record token usage for one streaming round.
     *
     * @param promptTokens tokens in the input to the model
     * @param candidateTokens tokens in the output from the model
     */
    fun recordRound(promptTokens: Int, candidateTokens: Int) {
        promptTokensTotal += promptTokens
        candidateTokensTotal += candidateTokens
        roundCount++
    }

    fun summary(): TokenSummary = TokenSummary(
        promptTokens = promptTokensTotal,
        candidateTokens = candidateTokensTotal,
        totalTokens = promptTokensTotal + candidateTokensTotal,
        toolRounds = roundCount
    )

    fun reset() {
        promptTokensTotal = 0
        candidateTokensTotal = 0
        roundCount = 0
    }
}

data class TokenSummary(
    val promptTokens: Int,
    val candidateTokens: Int,
    val totalTokens: Int,
    val toolRounds: Int
)