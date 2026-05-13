package com.aj.geminiproj.tools.calendar.di

import android.content.Context
import com.aj.geminiproj.tools.calendar.data.CalendarRepositoryImpl
import com.aj.geminiproj.tools.calendar.domain.repository.CalendarRepository
import com.aj.geminiproj.tools.calendar.tool.CreateCalendarEventTool
import com.aj.geminiproj.tools.calendar.tool.GetCalendarEventsForDayTool
import com.aj.geminiproj.tools.calendar.tool.IsTimeSlotAvailableTool
import org.koin.dsl.module

val calendarToolModule = module {
    single<CalendarRepository> { CalendarRepositoryImpl(get<Context>()) }
    single { GetCalendarEventsForDayTool(calendarRepository = get(), permissionManager = get()) }
    single { IsTimeSlotAvailableTool(calendarRepository = get(), permissionManager = get()) }
    single { CreateCalendarEventTool(calendarRepository = get(), permissionManager = get()) }
}