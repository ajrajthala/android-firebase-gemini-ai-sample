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

class GetCalendarEventsForDayTool(
    private val calendarRepository: CalendarRepository,
    private val permissionManager: PermissionManager
) : Tool{
    override val definition = ToolDefinition(
        functionName = "get_calendar_events_for_day",
        description = "Returns calendar events between startOfDayMs and endOfDayMs",
        parameters = listOf(
            ToolParameter(
                "startOfDayMs",
                ParameterType.NUMBER,
                "start of day in epoch millis",
                true
            ),
            ToolParameter("endOfDayMs", ParameterType.NUMBER, "end of day in epoch millis", true)
        ),
        displayName = "Reading calendar events..."
    )

    override suspend fun execute(parameters: Map<String, Any>): ToolResult {
        val permission = permissionManager.requirePermission(Manifest.permission.READ_CALENDAR)
        if (permission is PermissionStatus.Denied) {
            return ToolResult.PermissionDenied(
                message = "Calendar read permission is required to fetch events.",
                permission = Manifest.permission.READ_CALENDAR
            )
        }

        val start = parameters.longParam("startOfDayMs")
            ?: return ToolResult.Error("Missing or invalid startOfDayMs")
        val end = parameters.longParam("endOfDayMs")
            ?: return ToolResult.Error("Missing or invalid endOfDayMs")
        if (end > start) return ToolResult.Error("'endOfDayMs' must be greater than 'startOfDayMs'.")
        val events = calendarRepository.getEventsForDay(startOfDayMs = start, endOfDayMs = end)

        val eventMaps = events.map { event ->
            buildMap<String, Any> {
                put("id", event.id)
                put("title", event.title)
                put("startTime", event.startTime)
                put("endTime", event.endTime)
                event.description?.let { put("description", it) }
                event.location?.let { put("location", it) }
                event.attendees?.let { put("attendees", it) }
            }
        }

        return ToolResult.Success(
            data = mapOf(
                "count" to events.size,
                "events" to eventMaps,
            )
        )
    }
}

private fun Map<String, Any>.longParam(key: String): Long? {
    val raw =
        this[key] ?: this.entries.firstOrNull() { it.key.equals(key, ignoreCase = true) }?.value
    return when (raw) {
        is Number -> raw.toLong()
        is String -> raw.toLongOrNull()
        else -> null
    }
}