package com.aj.geminiproj.core.ai.firebase.resolver

import com.aj.geminiproj.core.ai.firebase.common.Logger

class DomainResolverFallback(
    private val registry: SemanticDomainRegistry,
    private val logger: Logger
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
            logger.i(TAG, "Keyword fallback matched: ${matched.map { it.id }}")
            matched
        } else {
            logger.w(TAG, "No keyword match found - returning all domains as safe fallback")
            domains
        }
    }
}