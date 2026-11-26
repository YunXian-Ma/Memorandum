package com.jfeng2048.memorandum.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.jfeng2048.memorandum.data.local.entity.PersonEntity
import com.jfeng2048.memorandum.data.local.entity.PersonRelationshipCrossRef
import com.jfeng2048.memorandum.data.local.relation.PersonWithEntries
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonDao {

    @Transaction
    @Query("SELECT * FROM person ORDER BY updated_at DESC")
    fun observeAllWithEntries(): Flow<List<PersonWithEntries>>

    @Transaction
    @Query("SELECT * FROM person WHERE person_id = :personId")
    fun observeWithEntries(personId: Long): Flow<PersonWithEntries?>

    @Query("SELECT * FROM person WHERE person_id = :personId")
    suspend fun findById(personId: Long): PersonEntity?

    @Query("SELECT COUNT(*) FROM person_relationship WHERE relationship_id = :relationshipId")
    suspend fun countByRelationship(relationshipId: Long): Int

    @Insert
    suspend fun insert(person: PersonEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPersonRelationships(crossRefs: List<PersonRelationshipCrossRef>)

    @Query("DELETE FROM person_relationship WHERE person_id = :personId")
    suspend fun deletePersonRelationships(personId: Long)

    @Update
    suspend fun update(person: PersonEntity)

    /** 词条变更时同步刷新人物的「最后更新时间」 */
    @Query("UPDATE person SET updated_at = :timestamp WHERE person_id = :personId")
    suspend fun touch(personId: Long, timestamp: Long)

    @Query("DELETE FROM person WHERE person_id = :personId")
    suspend fun deleteById(personId: Long)
}
