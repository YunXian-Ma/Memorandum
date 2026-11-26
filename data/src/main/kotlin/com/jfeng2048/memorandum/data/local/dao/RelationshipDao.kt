package com.jfeng2048.memorandum.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.jfeng2048.memorandum.data.local.entity.RelationshipEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RelationshipDao {

    @Query("SELECT * FROM relationship ORDER BY sort_order ASC, relationship_id ASC")
    fun observeAll(): Flow<List<RelationshipEntity>>

    @Query("SELECT * FROM relationship WHERE relationship_id = :relationshipId")
    suspend fun findById(relationshipId: Long): RelationshipEntity?

    @Query("SELECT COUNT(*) FROM relationship")
    suspend fun count(): Int

    @Insert
    suspend fun insert(relationship: RelationshipEntity): Long

    @Update
    suspend fun update(relationship: RelationshipEntity)

    @Query("DELETE FROM relationship WHERE relationship_id = :relationshipId")
    suspend fun deleteById(relationshipId: Long)
}
