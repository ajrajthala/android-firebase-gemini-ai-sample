package com.aj.geminiproj.tools.calendar.data

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.provider.CalendarContract
import com.aj.geminiproj.core.model.calendar.CalendarEvent
import com.aj.geminiproj.core.model.calendar.CreateEventRequest
import com.aj.geminiproj.tools.calendar.domain.repository.CalendarRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.TimeZone

class CalendarRepositoryImpl (private val context: Context): CalendarRepository {
    override suspend fun getEventsForDay(
        startOfDayMs: Long,
        endOfDayMs: Long,
    ): List<CalendarEvent> = withContext(Dispatchers.IO) {
        val uri = CalendarContract.Events.CONTENT_URI
        val projection = arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND,
            CalendarContract.Events.EVENT_LOCATION,
        )
        val selection = """
            ${CalendarContract.Events.DTSTART} >= ?
            AND ${CalendarContract.Events.DTEND} <= ?
            AND ${CalendarContract.Events.DELETED} = 0
        """.trimIndent()
        val selectionArgs = arrayOf(
            startOfDayMs.toString(),
            endOfDayMs.toString(),
        )
        val sortOrder = "${CalendarContract.Events.DTSTART} ASC"

        val events = mutableListOf<CalendarEvent>()

        context.contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            sortOrder,
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val eventId = cursor.getLong(
                    cursor.getColumnIndexOrThrow(CalendarContract.Events._ID)
                )
                events.add(
                    CalendarEvent(
                        id = eventId,
                        title = cursor.getString(
                            cursor.getColumnIndexOrThrow(CalendarContract.Events.TITLE)
                        ) ?: "Untitled",
                        description = "", // Description is not included in this query, would require additional query if needed
                        startTime = cursor.getLong(
                            cursor.getColumnIndexOrThrow(CalendarContract.Events.DTSTART)
                        ),
                        endTime = cursor.getLong(
                            cursor.getColumnIndexOrThrow(CalendarContract.Events.DTEND)
                        ),
                        location = cursor.getString(
                            cursor.getColumnIndexOrThrow(CalendarContract.Events.EVENT_LOCATION)
                        ),
                        attendees = resolveAttendees(eventId),
                    )
                )
            }
        }
        events
    }

    override suspend fun isTimeSlotAvailable(
        startTimeMs: Long,
        endTimeMs: Long,
    ): Boolean = withContext(Dispatchers.IO) {
        val uri = CalendarContract.Events.CONTENT_URI
        val projection = arrayOf(CalendarContract.Events._ID)

        // An overlap exists if an event starts before our end
        // AND ends after our start — standard interval overlap check
        val selection = """
            ${CalendarContract.Events.DTSTART} < ?
            AND ${CalendarContract.Events.DTEND} > ?
            AND ${CalendarContract.Events.DELETED} = 0
        """.trimIndent()
        val selectionArgs = arrayOf(
            endTimeMs.toString(),
            startTimeMs.toString(),
        )

        context.contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            null,
        )?.use { cursor ->
            cursor.count == 0   // true = no overlapping events = slot is free
        } ?: true               // null cursor = no calendar = treat as available
    }

    // ─────────────────────────────────────────────────────────────────────
    // Write operations
    // ─────────────────────────────────────────────────────────────────────

    override suspend fun createEvent(request: CreateEventRequest): Long? =
        withContext(Dispatchers.IO) {
            val calendarId = resolvePrimaryCalendarId()
                ?: return@withContext null

            val values = ContentValues().apply {
                put(CalendarContract.Events.CALENDAR_ID, calendarId)
                put(CalendarContract.Events.TITLE, request.title)
                put(CalendarContract.Events.DTSTART, request.startTime)
                put(CalendarContract.Events.DTEND, request.endTime)
                put(
                    CalendarContract.Events.EVENT_TIMEZONE,
                    TimeZone.getDefault().id
                )
                request.location?.let {
                    put(CalendarContract.Events.EVENT_LOCATION, it)
                }
                request.description?.let {
                    put(CalendarContract.Events.DESCRIPTION, it)
                }
            }

            val uri = context.contentResolver.insert(
                CalendarContract.Events.CONTENT_URI,
                values,
            ) ?: return@withContext null

            ContentUris.parseId(uri)
        }

    override suspend fun addAttendee(eventId: Long, email: String): Boolean =
        withContext(Dispatchers.IO) {
            val values = ContentValues().apply {
                put(CalendarContract.Attendees.EVENT_ID, eventId)
                put(CalendarContract.Attendees.ATTENDEE_EMAIL, email)
                put(
                    CalendarContract.Attendees.ATTENDEE_RELATIONSHIP,
                    CalendarContract.Attendees.RELATIONSHIP_ORGANIZER,
                )
                put(
                    CalendarContract.Attendees.ATTENDEE_TYPE,
                    CalendarContract.Attendees.TYPE_REQUIRED,
                )
                put(
                    CalendarContract.Attendees.ATTENDEE_STATUS,
                    CalendarContract.Attendees.ATTENDEE_STATUS_INVITED,
                )
            }

            val uri = context.contentResolver.insert(
                CalendarContract.Attendees.CONTENT_URI,
                values,
            )
            uri != null
        }

    // ─────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Resolves attendee emails for a given event ID.
     * Called during getEventsForDay to populate CalendarEvent.attendees.
     */
    private fun resolveAttendees(eventId: Long): List<String> {
        val uri = CalendarContract.Attendees.CONTENT_URI
        val projection = arrayOf(CalendarContract.Attendees.ATTENDEE_EMAIL)
        val selection = "${CalendarContract.Attendees.EVENT_ID} = ?"
        val selectionArgs = arrayOf(eventId.toString())

        val emails = mutableListOf<String>()
        context.contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            null,
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                cursor.getString(
                    cursor.getColumnIndexOrThrow(
                        CalendarContract.Attendees.ATTENDEE_EMAIL
                    )
                )?.let { emails.add(it) }
            }
        }
        return emails
    }

    /**
     * Finds the primary calendar ID to use for event creation.
     * Prefers IS_PRIMARY = 1, falls back to lowest _ID.
     */
    private fun resolvePrimaryCalendarId(): Long? {
        val uri = CalendarContract.Calendars.CONTENT_URI
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.IS_PRIMARY,
        )
        val selection = "${CalendarContract.Calendars.VISIBLE} = 1"
        val sortOrder = "${CalendarContract.Calendars.IS_PRIMARY} DESC," +
                " ${CalendarContract.Calendars._ID} ASC"

        return context.contentResolver.query(
            uri,
            projection,
            selection,
            null,
            sortOrder,
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                cursor.getLong(
                    cursor.getColumnIndexOrThrow(CalendarContract.Calendars._ID)
                )
            } else null
        }
    }
}