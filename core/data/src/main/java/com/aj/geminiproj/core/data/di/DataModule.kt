package com.aj.geminiproj.core.data.di

import android.content.Context
import androidx.room.Room
import com.aj.geminiproj.core.data.ConversationStore
import com.aj.geminiproj.core.data.ConversationStoreImpl
import com.aj.geminiproj.core.data.calendar.CalendarRepository
import com.aj.geminiproj.core.data.calendar.CalendarRepositoryImpl
import com.aj.geminiproj.core.data.contacts.ContactRepository
import com.aj.geminiproj.core.data.contacts.ContactRepositoryImpl
import com.aj.geminiproj.core.data.db.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "geminiproj-db"
        ).fallbackToDestructiveMigration(false)
            .build()
    }

    single { get<AppDatabase>().conversationDao() }
    single { get<AppDatabase>().messageDao() }
    single<ConversationStore> { ConversationStoreImpl(get(), get()) }

    single<ContactRepository> { ContactRepositoryImpl(get<Context>()) }
    single<CalendarRepository> { CalendarRepositoryImpl(get<Context>()) }

}