package com.aj.geminiproj.core.model.calendar

data class CreateEventRequest(
    val title: String,
    val description: String?,
    val startTime: Long,
    val endTime: Long,
    val location: String?,
    val attendees: List<String>? = emptyList(),
)