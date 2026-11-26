package com.jfeng2048.memorandum.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.jfeng2048.memorandum.data.local.entity.RelationshipEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RelationshipDao {

    @Query("SELECT * FROM relationships ORDER BY sortOrder ASC, id ASC")
    fun observeAll(): Flow<List<RelationshipEntity>>

    @Query("SELECT * FROM relationships WHERE id = :relationshipId")
    suspend fun findById(relationshipId: Long): RelationshipEntity?

    @Query("SELECT COUNT(*) FROM relationships")
    suspend fun count(): Int

    @Insert
    suspend fun insert(relationship: RelationshipEntity): Long

    @Update
    suspend fun update(relationship: RelationshipEntity)

    @Query("DELETE FROM relationships WHERE id = :relationshipId")
    suspend fun deleteById(relationshipId: Long)
}
