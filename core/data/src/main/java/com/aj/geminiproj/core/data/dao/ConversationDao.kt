package com.aj.geminiproj.core.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.aj.geminiproj.core.data.entity.ConversationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {

    @Query("SELECT * FROM conversations WHERE isArchived =0 ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE isArchived =0 ORDER BY updatedAt DESC")
    suspend fun getAll(): List<ConversationEntity>

    @Query("SELECT * FROM conversations WHERE id = :id")
    suspend fun getById(id: String) : ConversationEntity?

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteById(id: String)

    @Upsert
    suspend fun upsert(conversation: ConversationEntity)
}