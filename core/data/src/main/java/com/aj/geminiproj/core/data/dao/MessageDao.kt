package com.aj.geminiproj.core.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.aj.geminiproj.core.data.entity.MessageEntity

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId")
    suspend fun getByConversation(conversationId: String): List<MessageEntity>

    @Upsert
    suspend fun upsert(message: MessageEntity)

    @Query("DELETE FROM messages WHERE conversationId = :conversationId")
    suspend fun deleteByConversation(conversationId: String)
}