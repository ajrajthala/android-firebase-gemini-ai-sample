package com.aj.geminiproj.core.ai.firebase.registry

import com.aj.geminiproj.core.model.tool.Tool
import org.koin.core.component.KoinComponent

class ToolRegistry : KoinComponent {
    // All registered tools are lazily loaded from the Koin container when first accessed
    // Lazy loading ensures Koin is fully initialized before we attempt to retrieve any tools, preventing potential issues with dependency resolution
    val tools: List<Tool> by lazy { getKoin().getAll<Tool>() }

    fun getToolByName(name: String): Tool? {
        return tools.firstOrNull() { it.definition.functionName == name }
    }
}