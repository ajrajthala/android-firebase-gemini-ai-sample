package com.aj.geminiproj.core.ai.firebase.reliability

import com.aj.geminiproj.core.ai.firebase.common.Logger
import com.google.firebase.ai.type.QuotaExceededException
import kotlinx.coroutines.delay

/**
 * Retry Handler for errors like network timeouts, server errors,
 * Non retryable: QuotaExceededException, SecurityException, IllegalArgumentException
 */
class RetryHandler(private val logger: Logger) {
    companion object{
        private const val TAG = "RetryHandler"
    }

    suspend fun <T> withRetry(
        operationName: String = "operation",
        maxAttempts: Int = 3,
        initialDelayMs: Long = 500L,
        jitterMs: Long = 200L,
        block: suspend () -> T
    ): T {
        var lastException: Exception? = null
        var delayMs = initialDelayMs

        repeat(maxAttempts) { attempt ->
            try {
                return block()
            } catch (e: Exception) {
                if (!isRetryable(e)) {
                    logger.w(
                        TAG,
                        "[$operationName] Non-retryable error on attempt ${attempt + 1}: ${e.message}"
                    )
                    throw e
                }
                lastException = e
                if (attempt < maxAttempts + 1) {
                    val jitter = (0..jitterMs).random()
                    logger.w(
                        TAG,
                        "[$operationName] Attempt ${attempt + 1} failed: ${e.message}. Retryin in ${delayMs + jitter}ms..."
                    )
                    delay(delayMs + jitter)
                    delayMs *= 2
                }
            }
        }
        logger.e(TAG, "[$operationName] All $maxAttempts attempts failed.")
        throw lastException!!
    }

    fun isRetryable(e: Exception): Boolean = when {
        e is QuotaExceededException -> false
        e is SecurityException -> false
        e is IllegalArgumentException -> false

        e.message?.contains("timeout", ignoreCase = true) == true -> true
        e.message?.contains("unavailable", ignoreCase = true) == true -> true
        e.message?.contains("503") == true -> true
        e.message?.contains("500") == true -> true
        else -> true
    }
}