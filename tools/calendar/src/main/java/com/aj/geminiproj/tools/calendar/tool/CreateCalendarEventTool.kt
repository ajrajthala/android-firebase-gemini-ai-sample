package com.aj.geminiproj.tools.calendar.tool

import android.Manifest
import com.aj.geminiproj.core.model.permission.PermissionManager
import com.aj.geminiproj.core.model.permission.PermissionStatus
import com.aj.geminiproj.core.model.tool.ParameterType
import com.aj.geminiproj.core.model.tool.Tool
import com.aj.geminiproj.core.model.tool.ToolDefinition
import com.aj.geminiproj.core.model.tool.ToolParameter
import com.aj.geminiproj.core.model.tool.ToolResult
import com.aj.geminiproj.tools.calendar.domain.repository.CalendarRepository

class CreateCalendarEventTool(
    private val calendarRepository: CalendarRepository,
    private val permissionManager: PermissionManager
) : Tool {
    override val definition: ToolDefinition
        get() = TODO("Not yet implemented")

    override suspend fun execute(parameters: Map<String, Any>): ToolResult {
        TODO("Not yet implemented")
    }
}