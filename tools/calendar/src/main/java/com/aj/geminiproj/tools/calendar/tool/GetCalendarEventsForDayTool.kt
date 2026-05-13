package com.aj.geminiproj.tools.calendar.tool

import com.aj.geminiproj.core.model.permission.PermissionManager
import com.aj.geminiproj.tools.calendar.domain.repository.CalendarRepository

class GetCalendarEventsForDayTool(
    private val calendarRepository: CalendarRepository,
    private val permissionManager: PermissionManager
) {
}