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

class IsTimeSlotAvailableTool(
    private val calendarRepository: CalendarRepository,
    private val permissionManager: PermissionManager
) : Tool {
    override val definition = ToolDefinition(
        functionName = "is_time_slot_available",
        description = "Checks whether a [startTimeMs, endTimeMs] slot is free in calendar.",
        parameters = listOf(
            ToolParameter("startTimeMs", ParameterType.NUMBER, "Start time in epoch millis.", true),
            ToolParameter("endTimeMs", ParameterType.NUMBER, "End time in epoch millis.", true)
        ),
        displayName = "Checking availability..."
    )

    override suspend fun execute(parameters: Map<String, Any>): ToolResult {
        val permission = permissionManager.requirePermission(Manifest.permission.READ_CALENDAR)
        if (permission is PermissionStatus.Denied) {
            return ToolResult.PermissionDenied(
                message = "Calendar read permission is required to check the availability.",
                permission = Manifest.permission.READ_CALENDAR,
            )
        }

        val start = parameters.longParam("startTimeMs")
            ?: return ToolResult.Error("Missing or invalid 'startTimeMs'.")
        val end = parameters.longParam("endTimeMs")
            ?: return ToolResult.Error("Missing or invalid 'endTimeMs'.")
        if (end < start) return ToolResult.Error("'endTimeMs' must be greater than 'startTimeMs'.")


        val available = calendarRepository.isTimeSlotAvailable(start, end)
        return ToolResult.Success(
            data = mapOf(
                "available" to available,
                "startTimeMs" to start,
                "endTimeMs" to end,
            )
        )

    }

}