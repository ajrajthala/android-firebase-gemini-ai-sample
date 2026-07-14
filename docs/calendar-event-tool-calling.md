# Feature: Calendar Event Tool Calling

> **Branch:** `feature/calendar-event-tool-calling`
> **Base:** `master`

## Overview

This branch demonstrates **Gemini function calling** via the Firebase AI Logic SDK on Android, focused on smart calendar event scheduling.

Instead of Gemini only responding with text, it can now call real device functions — reading contacts, checking your calendar, and creating events — and synthesize the results into a natural language response.

**Target interaction:**

```
User:   "I need to meet Sarah tomorrow at the downtown cafe around 4 pm"

Gemini: find_contact("Sarah")         → resolves Sarah's email
        check_availability(tomorrow)  → confirms you are free at 4 pm
        create_event(...)             → creates the calendar event

Gemini: "Done! Created 'Meet Sarah' at Downtown Cafe tomorrow at 4 PM.
         Invite sent to sarah.johnson@email.com.
         Based on traffic, your commute is ~30 min — I've blocked
         time from 3:30 PM."
```

No routing logic in the app. Gemini decides which tools to call, in what order, and when none are needed.

---

## Architecture

This feature introduces a new `:tools` layer alongside the existing `core/` and `features/` structure.

```
app/
├── core/
│   ├── model/        ← Tool contracts (pure Kotlin — no Android imports)
│   ├── common/       ← PermissionManager (interface + Android impl)
│   ├── data/         ← ContactsRepository, CalendarRepository
│   └── ai/           ← GeminiOrchestrator, ToolRegistry, ToolDispatcher,
│                        FirebaseToolMapper
├── tools/
│   ├── contacts/     ← FindContactTool
│   ├── calendar/     ← CheckAvailabilityTool, GetCalendarEventsTool,
│   │                    CreateEventTool, SendInviteTool
│   └── maps/         ← CommuteTimeTool (stub)
└── features/
    └── chat/         ← Extended with streaming tool status UI
```

### How Tool Registration Works

Every tool module binds itself to the `Tool` interface in Koin:

```kotlin
// tools/contacts/di/ContactsToolModule.kt
val contactsToolModule = module {
    single { FindContactTool(get(), get()) } bind Tool::class
}
```

`ToolRegistry` calls `getAll<Tool>()` at session start — automatically collecting every registered tool across all modules. **Adding a new tool = add one Koin module. Nothing else changes.**

### How Gemini Decides Which Tools to Call

All tools are registered with the model at session start as `FunctionDeclaration` objects. Gemini reads the `description` field of each tool and decides at runtime:

- Whether any tool is relevant for the prompt
- Which tools to call and in what order
- When to stop calling tools and stream the final response

No intent classification or routing logic exists in the app.

---

## Tools

| Tool | Function Name | Description | Permission |
|---|---|---|---|
| FindContactTool | `find_contact` | Resolves a name to contact details | `READ_CONTACTS` |
| CheckAvailabilityTool | `check_availability` | Checks if a time slot is free | `READ_CALENDAR` |
| GetCalendarEventsTool | `get_calendar_events` | Lists events for a given day | `READ_CALENDAR` |
| CreateEventTool | `create_event` | Creates a calendar event | `WRITE_CALENDAR` |
| SendInviteTool | `send_invite` | Adds an attendee to an event | `WRITE_CALENDAR` |
| CommuteTimeTool | `get_commute_time` | Estimates commute time (stub) | None |

---

## Edge Cases Handled

### No Contact Found
If `find_contact` finds no match, it returns a `Success` with `found=false` rather than an error. The tool chain continues — the event is still created without an attendee email. Gemini informs the user naturally:

```
"Done! Created the event. I couldn't find Sarah in your contacts
so no invite was sent. You can share the details with her directly."
```

### Multiple Contacts Match
If `find_contact` returns more than one match, it returns `NeedsConfirmation` with the list of options. Gemini pauses the chain and asks the user to clarify:

```
Gemini: "I found multiple contacts named Sarah:
         1. Sarah Johnson (sarah.j@email.com)
         2. Sarah Smith (sarah.s@email.com)
         Which Sarah did you mean?"

User:   "Sarah Johnson"

Gemini: resumes the full scheduling chain with the correct contact.
```

---

## Permissions

Declared in `:app/AndroidManifest.xml` only — never in library module manifests.

```xml
<uses-permission android:name="android.permission.READ_CONTACTS" />
<uses-permission android:name="android.permission.READ_CALENDAR" />
<uses-permission android:name="android.permission.WRITE_CALENDAR" />
```

Runtime permission requests are handled by `AndroidPermissionManager` in `:core:common`. Tools call `permissionManager.requirePermission(...)` which suspends the coroutine, shows the system dialog, and resumes with `PermissionStatus.Granted` or `PermissionStatus.Denied`. The `ActivityResultLauncher` is wired in `ChatScreen`.

---

## Key Design Decisions

**Single conversation path** — every prompt goes through `GeminiOrchestrator`. No routing between a "simple chat" path and a "tools" path. When Gemini determines no tools are needed, it streams a text response directly with zero tool overhead.

**Stateless orchestrator** — `GeminiOrchestrator` holds no conversation state. History is owned by `ChatViewModel` and passed in on each call. This makes the orchestrator reusable across future features.

**`JSONObject` over `Map<String, Any>`** — `ToolResult.Success.data` uses `org.json.JSONObject` directly. `Map<String, Any>` causes a `kotlinx.serialization.SerializationException` at runtime because the Firebase AI Logic SDK cannot serialize open types. `JSONObject` is passed through to `FunctionResponsePart` natively.

**`ToolResult.NeedsConfirmation`** — a dedicated sealed subclass signals to Gemini that user input is required before the tool chain can continue. Gemini handles the clarification turn naturally — no special UI state needed.

**Maps stub** — `CommuteTimeTool` is backed by `StubCommuteTimeDataSource` which returns a hardcoded 30-minute estimate. Swap to `GoogleMapsCommuteDataSource` by changing one line in `MapsToolModule.kt`.

---

## Implementation Progress

### ✅ Step 1 — Contact Resolution (initial commit)
- `FindContactTool` — resolves name → contact details via `ContentResolver`
- `AndroidContactsRepository` — returns all partial matches
- `AndroidPermissionManager` — suspending permission bridge
- Edge cases: no contact found, multiple contacts found
- Runtime permission flow wired in `ChatScreen`

### 🚧 Step 2 — Calendar Read
- `CheckAvailabilityTool`
- `GetCalendarEventsTool`

### 🚧 Step 3 — Calendar Write
- `CreateEventTool`
- `SendInviteTool`

### 🚧 Step 4 — Commute Time
- `CommuteTimeTool` (stub → real Maps API)

### 🚧 Step 5 — Full Chain
- End-to-end: "Meet Sarah at downtown Cafe tomorrow at 4 pm"

---

## Running the Project

1. Clone and open in Android Studio
2. Add your `google-services.json` to `app/`
3. Enable Firebase AI Logic in the Firebase Console
4. Run on a device or emulator with Android API 21+
5. Grant Contacts permission when prompted
6. Type a message mentioning a person's name — e.g. `"Message from Sarah"`

The model will call `find_contact`, display the result in the chat, and handle the no-match and multi-match cases automatically.

---

## Module Dependencies

```
:app
 └── wires all Koin modules

:features:chat
 ├── :core:model       (ChatStreamEvent, Tool interface)
 ├── :core:ai          (GeminiOrchestrator)
 └── :core:ui

:core:ai
 ├── :core:model
 └── :core:common

:tools:contacts
 ├── :core:model
 ├── :core:common      (PermissionManager)
 └── :core:data        (ContactsRepository)

:tools:calendar
 ├── :core:model
 ├── :core:common
 └── :core:data        (CalendarRepository)

:tools:maps
 ├── :core:model
 └── :core:common

:core:data
 ├── :core:model
 └── :core:common

:core:model            (no dependencies — pure Kotlin)
:core:common           ← :core:model only
```

**Hard rule:** `:features:chat` has zero imports from `:tools:*`. It only knows `ChatStreamEvent` and `Tool` from `:core:model`. The orchestrator is the only layer that touches tools directly.
