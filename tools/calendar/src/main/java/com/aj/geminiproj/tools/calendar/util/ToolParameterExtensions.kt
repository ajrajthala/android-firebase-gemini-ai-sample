package com.aj.geminiproj.tools.calendar.util

internal fun Map<String, Any>.longParam(key: String): Long? {
    val raw =
        this[key] ?: this.entries.firstOrNull() { it.key.equals(key, ignoreCase = true) }?.value
    return when (raw) {
        is Number -> raw.toLong()
        is String -> raw.toLongOrNull()
        else -> raw?.toString()?.trim()?.toLongOrNull()
    }
}

internal fun Map<String, Any>.stringParam(key: String): String? {
    val raw =
        this[key] ?: this.entries.firstOrNull() { it.key.equals(key, ignoreCase = true) }?.value
    val value = raw?.toString()?.trim()
    return value?.takeIf { it.isNotEmpty() }
}

internal fun Map<String, Any>.stringListParam(key: String): List<String>? {
    val raw =
        this[key] ?: this.entries.firstOrNull() { it.key.equals(key, ignoreCase = true) }?.value
    return when (raw) {
        is List<*> -> raw.mapNotNull { it?.toString()?.trim()?.takeIf(String::isNotEmpty) }
        is String -> raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        else -> null
    }
}