package com.aj.geminiproj.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aj.geminiproj.core.data.dao.ConversationDao
import com.aj.geminiproj.core.data.dao.MessageDao
import com.aj.geminiproj.core.data.entity.ConversationEntity
import com.aj.geminiproj.core.data.entity.MessageEntity

@Database(
    entities = [ConversationEntity::class,
        MessageEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
}