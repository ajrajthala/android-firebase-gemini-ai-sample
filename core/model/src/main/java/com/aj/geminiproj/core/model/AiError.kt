package com.aj.geminiproj.core.model

sealed class AiError : Exception() {
    data class NetworkError(override val message: String = "Network error occurred") : AiError()
    data class ApiError(val code: Int, override val message: String = "API error occurred") :
        AiError()

    data class RateLimitError(override val message: String = "Rate limit exceeded") : AiError()
    data class InvalidResponseError(override val message: String = "Invalid response from AI service") :
        AiError()

    data class ModelNotFoundError(override val message: String = "Requested AI model not found") :
        AiError()

    data class ContentFilterError(override val message: String = "Content violates policy") :
        AiError()

    data class UnknownError(override val message: String = "An unknown error occurred") : AiError()

    data class TimeoutError(override val message: String = "Request timed out") : AiError()

    companion object {
        fun fromThrowable(throwable: Throwable): AiError {
            return when {
                throwable.message?.contains("network", ignoreCase = true) == true -> TimeoutError()
                throwable.message?.contains(
                    "rate limit",
                    ignoreCase = true
                ) == true -> RateLimitError()

                throwable.message?.contains(
                    "invalid response",
                    ignoreCase = true
                ) == true -> InvalidResponseError()

                throwable.message?.contains(
                    "model not found",
                    ignoreCase = true
                ) == true -> ModelNotFoundError()

                throwable.message?.contains(
                    "content filter",
                    ignoreCase = true
                ) == true -> ContentFilterError()

                throwable.message?.contains("timeout", ignoreCase = true) == true -> TimeoutError()
                else -> UnknownError(throwable.message ?: "An unknown error occurred")
            }
        }
    }
}