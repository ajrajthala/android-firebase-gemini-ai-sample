package com.aj.geminiproj.core.ai.firebase.agent

import com.aj.geminiproj.core.model.tool.Tool
import org.koin.core.component.KoinComponent

class SemanticDomainRegistry : KoinComponent {

    private val allTools: List<Tool> by lazy { getKoin().getAll() }

    val domains: List<SemanticDomain> by lazy { buildDomains() }

    fun getToolsForDomains(activeDomains: List<SemanticDomain>): List<Tool> {
        val domainIds = activeDomains.map { it.id }.toSet()
        return domains.filter { it.id in domainIds }
            .flatMap { it.tools }
            .distinctBy { it.definition.functionName }
    }

    private fun buildDomains(): List<SemanticDomain> {
        val calendarTools = allTools.filter { tool ->
            tool.definition.functionName in listOf(
                "create_calendar_event",
                "get_calendar_events",
                "check_availability",
                "update_calendar_event",
                "delete_calendar_event"
            )
        }

        val contactTools = allTools.filter { tool ->
            tool.definition.functionName in listOf(
                "get_contact",
                "search_contacts"
            )
        }

        return listOf(
            SemanticDomain(
                id = "CALENDAR",
                description = """
                    Requests involving time, scheduling, events, meetings, appointments,
                    reminders, availability, coordination, planning, catch-ups, syncs,
                    grabbing coffee, blocking time, or anything happening on a specific
                    date or time. Includes follow-up replies providing dates or times.
                """.trimIndent(),
                tools = calendarTools,
                systemFragment = """
                    Calendar rules:
                    - Use calendar tools to create, fetch, and update events.
                    - If multiple events match, show up to 3 and ask the user to choose.
                    - If a time slot is unavailable, suggest the next available slot.
                    - If the same validation error repeats after one retry, stop and ask.
                    - Never fabricate event data.
                """.trimIndent(),
                fewShotExamples = """
                    ### CALENDAR — Fetch events
                    User: What do I have on Friday?
                    Assistant: [calls get_calendar_events date=Friday]
                    Tool: [{"title":"Team Standup","time":"10:00 AM"},{"title":"Lunch with Sara","time":"12:30 PM"}]
                    Assistant: You have 2 events on Friday:
                    - 10:00 AM — Team Standup
                    - 12:30 PM — Lunch with Sara

                    ### CALENDAR — Create event (all info)
                    User: Schedule a dentist appointment tomorrow at 3 PM for 1 hour.
                    Assistant: [calls create_calendar_event title="Dentist Appointment" date=tomorrow time="3:00 PM" duration=60]
                    Tool: {"status":"created"}
                    Assistant: Done! Dentist Appointment scheduled for tomorrow at 3:00 PM.

                    ### CALENDAR — Create event with contact
                    User: I need to catchup with Sarah tomorrow at 2 PM.
                    Assistant: [calls get_contact name="Sarah"]
                    Tool: {"name":"Sarah Chen","email":"sarah@example.com"}
                    Assistant: [calls check_availability date=tomorrow time="2:00 PM" duration=60]
                    Tool: {"available": true}
                    Assistant: [calls create_calendar_event title="Catchup with Sarah Chen" date=tomorrow time="2:00 PM" duration=60 attendees=["sarah@example.com"]]
                    Tool: {"status":"created","invite_sent":true}
                    Assistant: Done! Catchup with Sarah Chen scheduled for tomorrow at 2:00 PM.

                    ### CALENDAR — Missing info
                    User: Add a meeting to my calendar.
                    Assistant: I can help with that. What is the title, date, and time?

                    ### CALENDAR — No results
                    User: Do I have anything Friday at 8 PM?
                    Assistant: [calls get_calendar_events date=Friday time="8:00 PM"]
                    Tool: []
                    Assistant: No events found at that time. Want me to check another slot?
                """.trimIndent()
            ),

            SemanticDomain(
                id = "CONTACTS",
                description = """
                    Requests involving looking up people, finding phone numbers or emails,
                    resolving names, identifying attendees, or any request about a specific
                    person's contact details. Includes partial names and pronouns referring
                    to a previously mentioned person.
                """.trimIndent(),
                tools = contactTools,
                systemFragment = """
                    Contact rules:
                    - Use contact tools to look up people, phone numbers, and emails.
                    - If multiple contacts match, show up to 3 and ask the user to choose.
                    - If a contact has multiple phones or emails with no clear primary, ask.
                    - If no contact is found, say so clearly.
                    - Never guess or fabricate contact details.
                """.trimIndent(),
                fewShotExamples = """
                    ### CONTACTS — One match
                    User: What's John Smith's number?
                    Assistant: [calls get_contact name="John Smith"]
                    Tool: [{"name":"John Smith","phone":"+1-555-0192"}]
                    Assistant: John Smith's number is +1-555-0192.

                    ### CONTACTS — Multiple matches
                    User: Find Sarah's email.
                    Assistant: [calls get_contact name="Sarah"]
                    Tool: [{"name":"Sarah Lee","email":"sarah.lee@co.com"},{"name":"Sarah Kim","email":"sarah.kim@co.com"}]
                    Assistant: I found 2 contacts named Sarah — which one?
                    1. Sarah Lee — sarah.lee@co.com
                    2. Sarah Kim — sarah.kim@co.com

                    ### CONTACTS — No match
                    User: What's Olivia Chen's number?
                    Assistant: [calls get_contact name="Olivia Chen"]
                    Tool: []
                    Assistant: I couldn't find Olivia Chen. Check the spelling or try another name.

                    ### CONTACTS — Multiple values
                    User: What's Alex's email?
                    Assistant: [calls get_contact name="Alex"]
                    Tool: [{"name":"Alex Brown","emails":["alex@work.com","alex@gmail.com"],"primary_email":null}]
                    Assistant: Alex has 2 emails — which should I use?
                    1. alex@work.com
                    2. alex@gmail.com
                """.trimIndent()
            )
        )
    }

    fun getToolByName(name: String) = allTools.firstOrNull(){it.definition.functionName == name}
}