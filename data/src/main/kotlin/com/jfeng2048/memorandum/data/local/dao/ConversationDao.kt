package com.jfeng2048.memorandum.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.jfeng2048.memorandum.data.local.entity.ConversationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {

    @Insert
    suspend fun insert(conversation: ConversationEntity): Long

    @Query("DELETE FROM conversation WHERE conversation_id = :conversationId")
    suspend fun deleteById(conversationId: Long)

    @Query("SELECT * FROM conversation ORDER BY created_at DESC LIMIT 100")
    fun observeRecent(): Flow<List<ConversationEntity>>
}
