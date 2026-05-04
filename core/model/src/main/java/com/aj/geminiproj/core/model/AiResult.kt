package com.aj.geminiproj.core.model

sealed class AiResult<out T> {
    data class Success<T>(val data: T) : AiResult<T>()
    data class Error(val error: AiError) : AiResult<Nothing>()
    data object Loading : AiResult<Nothing>()
    companion object {
        fun <T> success(data: T): AiResult<T> = Success(data)
        fun error(error: AiError): AiResult<Nothing> = Error(error)
    }

    inline fun <R> map(transform: (T) -> R): AiResult<R> {
        return when (this) {
            is Success -> Success(transform(data))
            is Error -> this
            Loading -> Loading
        }
    }

    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }

    fun getOrDefault(defaultValue: @UnsafeVariance T): T = when (this) {
        is Success -> data
        else -> defaultValue
    }
}