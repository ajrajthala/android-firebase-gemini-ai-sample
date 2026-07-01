package com.aj.geminiproj.core.ai.firebase.ingelligence

import com.aj.geminiproj.core.ai.firebase.common.Logger
import com.aj.geminiproj.core.model.tool.ToolResult
import java.util.concurrent.ConcurrentHashMap

/**
 * A simple in-memory cache for tool results, to avoid redundant calls
 * for the same tool with the same parameters within a short time window.
 *
 * @property logger
 * @property ttlMs
 */
class ToolResultCache(
    private val logger: Logger,
    private val ttlMs: Long = 60_000L // 60 seconds
) {
    companion object {
        private const val TAG = "ToolResultCache"
    }

    private data class CacheEntry(val result: Any, val entryTimeMs: Long)

    private val cache = ConcurrentHashMap<String, CacheEntry>()

    fun cacheKey(functionName: String, args: Map<String, Any>): String {
        val storedArgs = args.entries
            .sortedBy { it.key }
            .joinToString(",") { "${it.key}=${it.value}" }
        return "$functionName($storedArgs)"
    }

    /**
     * Return cached ToolResult.Success if exists and not expired, otherwise null.
     *
     * @param functionName
     * @param args
     * @return
     */
    fun getCachedResult(functionName: String, args: Map<String, Any>): ToolResult.Success? {
        val key = cacheKey(functionName, args)
        val entry = cache[key] ?: return null
        val age = System.currentTimeMillis() - entry.entryTimeMs
        return if (age <= ttlMs) {
            logger.i(TAG, "Cache hit for '$key' (age=${age}ms)")
            entry.result as? ToolResult.Success
        } else {
            logger.i(TAG, "Cache expired for '$key' (age=${age}ms)")
            cache.remove(key)
            null
        }
    }

    /**
     * Store cached results
     *
     * @param functionName
     * @param args
     * @param result
     */
    fun putCachedResult(functionName: String, args: Map<String, Any>, result: ToolResult) {
        if (result !is ToolResult.Success) return // Only cache successful results
        val key = cacheKey(functionName, args)
        cache[key] = CacheEntry(result, System.currentTimeMillis())
        logger.i(TAG, "Cached result for '$key'")
    }

    fun invalidate(functionName: String) {
        val keysToRemove = cache.keys.filter { it.startsWith("$functionName[") }
        keysToRemove.forEach { cache.remove(it) }
        logger.i(TAG, "Invalidated cache for function '$functionName'")
    }

    fun clear() {
        cache.clear()
        logger.i(TAG, "Cleared entire tool result cache")
    }

    fun stats(): CacheStats = CacheStats(
        entryCount = cache.size,
        keys = cache.keys.toList()
    )

    data class CacheStats(
        val entryCount: Int,
        val keys: List<String>
    )
}