package com.aj.geminiproj.tools.calendar.tool

import android.Manifest
import android.util.Log
import com.aj.geminiproj.core.model.permission.PermissionManager
import com.aj.geminiproj.core.model.permission.PermissionStatus
import com.aj.geminiproj.core.model.tool.ParameterType
import com.aj.geminiproj.core.model.tool.Tool
import com.aj.geminiproj.core.model.tool.ToolDefinition
import com.aj.geminiproj.core.model.tool.ToolParameter
import com.aj.geminiproj.core.model.tool.ToolResult
import com.aj.geminiproj.tools.calendar.domain.repository.CalendarRepository
import com.aj.geminiproj.tools.calendar.util.longParam

class GetCalendarEventsForDayTool(
    private val calendarRepository: CalendarRepository,
    private val permissionManager: PermissionManager
) : Tool {

    companion object {
        private const val TAG = "GetCalendarEventsTool"
    }

    override val definition = ToolDefinition(
        functionName = "get_calendar_events_for_day",
        description = "Returns calendar events between startOfDayMs and endOfDayMs",
        parameters = listOf(
            ToolParameter(
                "startOfDayMs",
                ParameterType.INTEGER,
                "start of day in epoch millis",
                true
            ),
            ToolParameter("endOfDayMs", ParameterType.INTEGER, "end of day in epoch millis", true)
        ),
        displayName = "Reading calendar events..."
    )

    override suspend fun execute(parameters: Map<String, Any>): ToolResult {
        Log.d(TAG, "call args=$parameters")
        val permission = permissionManager.requirePermission(Manifest.permission.READ_CALENDAR)
        if (permission is PermissionStatus.Denied) {
            Log.w(TAG, "result permission_denied")
            return ToolResult.PermissionDenied(
                message = "Calendar read permission is required to fetch events.",
                permission = Manifest.permission.READ_CALENDAR
            )
        }

        val start = parameters.longParam("startOfDayMs")
            ?: run {
                Log.w(TAG, "result error=invalid_startOfDayMs args =$parameters")
                return ToolResult.Error("Missing or invalid startOfDayMs")
            }
        val end = parameters.longParam("endOfDayMs")
            ?: run {
                Log.w(TAG, "result error=invalid_endOfDayMs args =$parameters")
                return ToolResult.Error("Missing or invalid endOfDayMs")
            }
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

        Log.d(TAG, "result success count=${events.size} start=$start end=$end")
        return ToolResult.Success(
            data = mapOf(
                "count" to events.size,
                "events" to eventMaps,
            )
        )
    }
}