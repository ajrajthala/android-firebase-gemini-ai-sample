package com.aj.geminiproj.core.ai.firebase.agent

import android.util.Log

class DomainResolverFallback(
    private val registry: SemanticDomainRegistry
) {
    companion object {
        private const val TAG = "DomainResolverFallback"
    }

    /**
     * use keyword matching as fallback for domain resolving
     *
     * @param userMessage
     * @return
     */
    fun resolve(userMessage: String): List<SemanticDomain> {
        val domains = registry.domains
        if (domains.isEmpty()) return emptyList()

        val inputWords = userMessage.lowercase()
            .split(" ", ",", ".", "?", "!", "\n")
            .filter { it.length > 2 }
            .toSet()

        val matched = domains.filter { domain ->
            val domainKeywords = domain.description
                .lowercase()
                .split(" ", "\n", ",")
                .filter { it.length > 2 }
                .toSet()
            inputWords.intersect(domainKeywords).isNotEmpty()
        }

        return if (matched.isNotEmpty()) {
            Log.i(TAG, "Keyword fallback matched: ${matched.map { it.id }}")
            matched
        } else {
            Log.w(TAG, "No keyword match found - returning all domains as safe fallback")
            domains
        }
    }
}