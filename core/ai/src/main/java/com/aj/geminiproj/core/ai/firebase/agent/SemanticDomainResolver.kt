package com.aj.geminiproj.core.ai.firebase.agent

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.QuotaExceededException
import com.google.firebase.ai.type.content
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonPrimitive

class SemanticDomainResolver(
    private val registry: SemanticDomainRegistry,
    private val confidenceThreshold: Double = .60,
    private val modelName: String = "gemini-3-flash-preview"
) {
    companion object {
        private const val TAG = "SemanticDomainResolver"
    }

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun resolve(userMessage: String): List<SemanticDomain> {
        val domains = registry.domains
        if (domains.isEmpty()) return emptyList()
        return try {
            val raw = classify(userMessage, domains)
            val scores = parseScores(raw)

            val active = domains.filter { domain ->
                val score = scores[domain.id] ?: 0.0
                score >= confidenceThreshold
            }

            Log.i(TAG, "Domain scores: $scores")
            Log.i(TAG, "Active domains: ${active.map { it.id }}")
            active
        } catch (e: QuotaExceededException) {
            e.printStackTrace()
            Log.w(TAG, "Quota exceeded during resolution - loading all domains as fallback")
            domains
        } catch (e: Exception) {
            e.printStackTrace()
            domains
        }
    }

    private suspend fun classify(userMessage: String, domains: List<SemanticDomain>): String {
        val domainDescriptions = domains.joinToString("\n") { domain ->
            "${domain.id}: ${domain.description.replace("\n", " ").trim()}"
        }

        val systemPrompt = """
            You are a domain classifier for an Android assistant.
            Vien a user message and domain descriptions, return a confidence score(0.0-1.0)
            for each domain indicating how relevant it is to the user's request.
            
            Domains:
            $domainDescriptions
            GENERAL: Any question that does not require device data or tools.
            
            Rules:
            - Score 0.9+ if the message clearly belongs to that domain.
            - Score 0.6-0.89 if the message likely needs that domain.
            - Score below 0.6 if the domain is not needed.
            - A message can score high on multiple domains.
            - Follow-up replies (dates, time, names) should score high on the domain
              that was active in the prior turn.
              
            Return JSON only. No markdown. No commentry. 
            Format: {"DOMAIN_ID": score, ...}
        """.trimIndent()

        val model = Firebase
            .ai(backend = GenerativeBackend.googleAI())
            .generativeModel(
                modelName = modelName,
                systemInstruction = content { text(systemPrompt) }
            )

        val response = model.generateContent(userMessage)
        return response.text.orEmpty().trim()
    }

    private fun parseScores(raw: String): Map<String, Double> {
        val cleaned = raw.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        return try {
            val obj = json.parseToJsonElement(cleaned) as JsonObject
            obj.entries.associate { (key, value) ->
                key to value.jsonPrimitive.double
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "Failed to parse domain scores from: $cleaned")
            emptyMap()
        }
    }
}