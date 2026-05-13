package com.aj.geminiproj.core.model

sealed class StreamState<out T> {
    data object Idle : StreamState<Nothing>()
    data object Loading : StreamState<Nothing>()
    data class Success<T>(val data: T) : StreamState<T>()
    data class Error(val message: String? = null) : StreamState<Nothing>()
    data class Streaming<T>(val partialData: T, val isComplete: Boolean) : StreamState<T>()
}

