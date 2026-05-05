package com.aj.geminiproj.core.data.di

import androidx.room.Room
import com.aj.geminiproj.core.data.ConversationStore
import com.aj.geminiproj.core.data.ConversationStoreImpl
import com.aj.geminiproj.core.data.db.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "geminiproj-db"
        ).addMigrations(AppDatabase.MIGRATION_1_2)
            .fallbackToDestructiveMigration(false)
            .build()
    }

    single { get<AppDatabase>().conversationDao() }
    single { get<AppDatabase>().messageDao() }
    single<ConversationStore> { ConversationStoreImpl(get(), get()) }

}