package com.aj.geminiproj.core.ai.firebase.resolver

import com.aj.geminiproj.core.model.tool.Tool

data class SemanticDomain(
    val id: String,
    val description: String,
    val tools: List<Tool> = emptyList(),
    val systemFragment: String,
    val fewShotExamples: String,
)