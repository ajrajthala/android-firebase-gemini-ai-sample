package com.aj.geminiproj.tools.calendar.tool

import android.Manifest
import com.aj.geminiproj.core.model.calendar.CreateEventRequest
import com.aj.geminiproj.core.model.permission.PermissionManager
import com.aj.geminiproj.core.model.permission.PermissionStatus
import com.aj.geminiproj.core.model.tool.ParameterType
import com.aj.geminiproj.core.model.tool.Tool
import com.aj.geminiproj.core.model.tool.ToolDefinition
import com.aj.geminiproj.core.model.tool.ToolParameter
import com.aj.geminiproj.core.model.tool.ToolResult
import com.aj.geminiproj.tools.calendar.domain.repository.CalendarRepository
import com.aj.geminiproj.tools.calendar.util.longParam
import com.aj.geminiproj.tools.calendar.util.stringListParam
import com.aj.geminiproj.tools.calendar.util.stringParam

class CreateCalendarEventTool(
    private val calendarRepository: CalendarRepository,
    private val permissionManager: PermissionManager
) : Tool {
    override val definition = ToolDefinition(
        functionName = "create_calendar_event",
        description = "Create a calendar event. Time inputs are epoch millis.",
        parameters = listOf(
            ToolParameter("title", ParameterType.STRING, "Event title", true),
            ToolParameter("startTimeMs", ParameterType.NUMBER, "Start time in epoch millis", true),
            ToolParameter("endTimeMs", ParameterType.NUMBER, "End time in epoch millis", true),
            ToolParameter("description", ParameterType.STRING, "Optional event description", false),
            ToolParameter("location", ParameterType.STRING, "Optional event location", false),
            ToolParameter("attendees", ParameterType.STRING, "Optional event emails", false),
        ),
        displayName = "Creating calendar event..."
    )

    override suspend fun execute(parameters: Map<String, Any>): ToolResult {
        val permission = permissionManager.requirePermission(Manifest.permission.WRITE_CALENDAR)
        if (permission is PermissionStatus.Denied) {
            return ToolResult.PermissionDenied(
                message = "Calendar write permission is required to create events.",
                permission = Manifest.permission.WRITE_CALENDAR,
            )
        }

        val title = parameters.stringParam("title")
            ?: return ToolResult.Error("Missing or invalid 'title.'")
        val start = parameters.longParam("startTimeMs")
            ?: return ToolResult.Error("Missing or invalid 'startTimeMs.'")
        val end = parameters.longParam("endTimeMs")
            ?: return ToolResult.Error("Missing or invalid 'endTimeMs.'")
        if (end < start) return ToolResult.Error("'endTimeMs' must be greater than 'startTimeMs'")

        val request = CreateEventRequest(
            title = title,
            description = parameters.stringParam("description"),
            startTime = start,
            endTime = end,
            location = parameters.stringParam("location"),
            attendees = parameters.stringListParam("attendees")
        )

        val eventId = calendarRepository.createEvent(request)
            ?: return ToolResult.Error("Failed to create calendar event.", isRetryable = false)
        val attendees = request.attendees.orEmpty()
        val failedAttendees =
            attendees.filterNot { email -> calendarRepository.addAttendee(eventId, email) }

        val MAX_FAILED_LOG = 5
        return ToolResult.Success(
            data = buildMap {
                put("created", true)
                put("eventId", eventId)
                put("title", title)
                put("startTimeMs", start)
                put("endTimeMs", end)
                if (attendees.isNotEmpty()) {
                    put("attendeesRequested", attendees.size)
                    put("attendeesAdded", attendees.size - failedAttendees.size)
                    if (failedAttendees.isNotEmpty()) {
                        put("failedAttendeesCount", failedAttendees.size)
                        put("failedAttendeesSample", failedAttendees.take(MAX_FAILED_LOG))
                    }
                }
            }
        )
    }
}