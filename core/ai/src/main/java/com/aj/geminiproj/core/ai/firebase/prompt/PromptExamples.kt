package com.aj.geminiproj.core.ai.firebase.prompt

object PromptExamples {
    val calendar: String = """
    ### CALENDAR - One match
    User: What do I have tomorrow at 3 PM?
    Assistant: [calls get_calendar_events with date=tomorrow, time="3:00PM"]
    Tool result: [{"title":"Dentist Appointment", "time":"3:00 PM", "date":"tomorrow"}]
    Assistant: You have 1 event tomorrow at 3:00 PM
    - 3:00 PM - Dentist Appointment
    
    ### CALENDAR - Multiple matches
    User: What do I have on Friday?
    Assistant: [calls get_calendar_events with date=Friday]
    Tool result: [
      {"title":"Team Standup","time":"10:00 AM","date":"Friday"},
      {"title":"Lunch with Sara","time":"12:30 PM","date":"Friday"},
      {"title":"Project Review","time":"3:00 PM","date":"Friday"}
    ]
    Assistant: You have 3 events on Friday:
    - 10:00 AM — Team Standup
    - 12:30 PM — Lunch with Sara
    - 3:00 PM — Project Review
    
    ### CALENDAR - Create Calendar Event (all info provided)
    User: Schedule a dentist appointment tomorrow at 3 PM.
    Assistant: [calls create_calendar_event with title="Dentist Appointment", date="tomorrow", time="3:00 PM", duration=60]
    Tool result: {"status":"created", "event_id": "abc123"}
    Assistant: Done! I've scheduled your Dentist Appointment for tomorrow at 3:00 PM (1 hour).
    Note: No duration was provided, so the default of 1 hour was used.
    
    ### CALENDAR - Create calendar event with contact lookup
    User: Schedule a meeting with Sarah Chen tomorrow at 2 PM.
    Assistant: [calls get_contact with name="Sarah Chen"]
    Tool result: {"contact_id": "c789", "name": "Sarah Chen", "email": "sarah.chen@example.com"}
    Assistant: [calls check_availability with date=tomorrow, time="2:00 PM", duration=60]
    Tool result: {"available": true}
    Assistant: [calls create_calendar_event with title="Meeting with Sarah Chen", date=tomorrow, time="2:00 PM", duration=60, attendees=["sarah.chen@example.com"]]
    Tool result: {"status": "created", "event_id": "evt456", "invite_sent": true}
    Assistant: Done! I've scheduled a 1-hour meeting with Sarah Chen tomorrow at 2:00 PM and sent her a calendar invite.

    ### CALENDAR - Create calendar event (slot unavailable → suggest next)
    User: Schedule a meeting with Sarah Chen tomorrow at 2 PM.
    Assistant: [calls get_contact with name="Sarah Chen"]
    Tool result: {"contact_id": "c789", "name": "Sarah Chen", "email": "sarah.chen@example.com"}
    Assistant: [calls check_availability with date=tomorrow, time="2:00 PM", duration=60]
    Tool result: {"available": false, "next_available": "3:30 PM"}
    Assistant: You're not available at 2:00 PM tomorrow. Your next open slot is 3:30 PM — would you like me to schedule the meeting with Sarah Chen then?

     ### CALENDAR - Create calendar event with contact lookup (multiple contacts found)
    User: Schedule a meeting with Alex Johnson tomorrow at 10 AM.
    Assistant: [calls get_contact with name="Alex Johnson"]
    Tool result:
    json[
      {"contact_id": "c101", "name": "Alex Johnson", "email": "alex.johnson@acme.com", "company": "Acme Corp"},
      {"contact_id": "c102", "name": "Alex Johnson", "email": "alexj@personal.com", "company": "Personal"}
    ]
    Assistant: I found two contacts named Alex Johnson — which one did you mean?
    Alex Johnson — alex.johnson@acme.com (Acme Corp)
    Alex Johnson — alexj@personal.com (Personal)
    
    ### CALENDAR — Create Calendar Event (Missing fields -> Ask first)
    User: Add a meeting to my calendar.
    Assistant: I can help with that. Please share:
    1. Title
    2. Date and time

    ### CALENDAR — No matches
    User: Do I have anything Friday at 8 PM?
    Assistant: [calls get_calendar_events with date=Friday, time="8:00 PM"]
    Tool result: []
    Assistant: I couldn't find any events at that time. Would you like me to check a different time or day?
    """.trimIndent()

    val contacts: String = """
    ### CONTACTS — One match
    User: What's John Smith's phone number?
    Assistant: [calls get_contact with name="John Smith"]
    Tool result: [
    { "name":"John Smith", "phone":"+1-555-0192", "company":"Acme Corp" }]
    Assistant: John Smith's number is +1-555-0192.

    ### CONTACTS — Multiple matches
    User: Find Sarah's email.
    Assistant: [calls get_contact with name="Sarah"]
    Tool result: [
    { "name":"Sarah Lee", "email":"sarah.lee@company.com", "title":"Marketing Lead" },
    { "name":"Sarah Kim", "email":"sarah.kim@company.com", "title":"Product Designer" },
    { "name":"Sarah Patel", "email":"sarah.patel@company.com", "title":"Sales Manager" }
    ]
    Assistant: I found a few contacts named Sarah:
    1. Sarah Lee — Marketing Lead — sarah.lee@company.com
    2. Sarah Kim — Product Designer — sarah.kim@company.com
    3. Sarah Patel — Sales Manager — sarah.patel@company.com
    Which one do you mean?

    ### CONTACTS — No matches
    User: What's Olivia Chen's phone number?
    Assistant: [calls get_contact with name="Olivia Chen"]
    Tool result: []
    Assistant: I couldn't find a contact named Olivia Chen. Could you check the spelling or try another detail?

    ### CONTACTS — Multiple values
    User: What's Alex Brown's email?
    Assistant: [calls get_contact with name="Alex Brown"]
    Tool result: [
    {
        "name":"Alex Brown",
        "emails":["alex.brown@work.com", "alexbrown@gmail.com"],
        "primary_email":null
    }
    ]
    Assistant: I found more than one email for Alex Brown:
    1. alex.brown@work.com
    2. alexbrown@gmail.com
    Which one should I use?
  
    """.trimIndent()
}