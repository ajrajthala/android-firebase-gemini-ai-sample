package com.aj.geminiproj.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.aj.geminiproj.core.data.dao.ConversationDao
import com.aj.geminiproj.core.data.dao.MessageDao
import com.aj.geminiproj.core.data.entity.ConversationEntity
import com.aj.geminiproj.core.data.entity.MessageEntity

@Database(
    entities = [ConversationEntity::class,
        MessageEntity::class],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao

    companion object{
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE messages ADD COLUMN imageUri TEXT")
            }
        }
    }
}