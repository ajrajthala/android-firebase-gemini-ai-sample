package com.aj.geminiproj.core.data

import com.aj.geminiproj.core.data.dao.ConversationDao
import com.aj.geminiproj.core.data.dao.MessageDao
import com.aj.geminiproj.core.data.db.mapper.toDomain
import com.aj.geminiproj.core.data.db.mapper.toEntity
import com.aj.geminiproj.core.model.chat.ChatConversation
import com.aj.geminiproj.core.model.chat.ChatMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ConversationStoreImpl(
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
) : ConversationStore {
    override fun observeAll(): Flow<List<ChatConversation>> {
        return conversationDao.observeAll().map { entities ->
            entities.map { entity ->
                val messages = messageDao.getByConversation(entity.id)
                entity.toDomain(messages)
            }
        }
    }

    // For summaries, we can skip loading messages to improve performance
    override fun observeAllSummaries(): Flow<List<ChatConversation>> {
        return conversationDao.observeAll().map { entities ->
            entities.map { entity ->
                entity.toDomain(emptyList())
            }
        }
    }

    override suspend fun getAll(): List<ChatConversation> {
        return conversationDao.getAll().map { entity ->
            val messages = messageDao.getByConversation(entity.id)
            entity.toDomain(messages)
        }
    }

    override suspend fun getById(id: String): ChatConversation? {
        return conversationDao.getById(id)?.let { entity ->
            val messages = messageDao.getByConversation(entity.id)
            entity.toDomain(messages)
        }
    }

    override suspend fun save(chatConversation: ChatConversation) {
        conversationDao.upsert(chatConversation.toEntity())
    }

    override suspend fun saveMessage(chatMessage: ChatMessage, conversationId: String) {
        messageDao.upsert(chatMessage.toEntity(conversationId))
    }

    override suspend fun delete(id: String) {
        messageDao.deleteByConversation(id)
        conversationDao.deleteById(id)
    }

    override suspend fun createNew(id: String): ChatConversation {
        return ChatConversation(
            id = id,
            title = "New Chat",
            messages = emptyList(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        ).also { save(it) }
    }

}