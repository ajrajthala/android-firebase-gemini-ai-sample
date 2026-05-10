package com.aj.geminiproj.core.model.calendar

data class CalendarEvent(
    val id: Long,
    val title: String,
    val description: String?,
    val startTime: Long,
    val endTime: Long,
    val location: String?,
    val attendees: List<String>?,
)
