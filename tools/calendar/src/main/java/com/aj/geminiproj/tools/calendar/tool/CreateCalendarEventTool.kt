package com.aj.geminiproj.tools.calendar.tool

import com.aj.geminiproj.core.model.permission.PermissionManager
import com.aj.geminiproj.tools.calendar.domain.repository.CalendarRepository

class CreateCalendarEventTool(
    private val calendarRepository: CalendarRepository,
    private val permissionManager: PermissionManager
) {
}