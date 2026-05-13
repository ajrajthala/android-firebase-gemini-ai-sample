package com.aj.geminiproj.tools.calendar.domain.repository

import com.aj.geminiproj.core.model.calendar.CalendarEvent
import com.aj.geminiproj.core.model.calendar.CreateEventRequest

interface CalendarRepository {
    suspend fun getEventsForDay(startOfDayMs: Long, endOfDayMs: Long): List<CalendarEvent>

    suspend fun isTimeSlotAvailable(startTimeMs: Long, endTimeMs: Long): Boolean

    suspend fun createEvent(request: CreateEventRequest) : Long?

    suspend fun addAttendee(eventId: Long, email: String): Boolean
}