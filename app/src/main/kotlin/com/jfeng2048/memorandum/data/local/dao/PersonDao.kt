package com.jfeng2048.memorandum.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.jfeng2048.memorandum.data.local.entity.PersonEntity
import com.jfeng2048.memorandum.data.local.relation.PersonWithEntries
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonDao {

    @Transaction
    @Query("SELECT * FROM persons ORDER BY updatedAt DESC")
    fun observeAllWithEntries(): Flow<List<PersonWithEntries>>

    @Transaction
    @Query("SELECT * FROM persons WHERE id = :personId")
    fun observeWithEntries(personId: Long): Flow<PersonWithEntries?>

    @Query("SELECT * FROM persons WHERE id = :personId")
    suspend fun findById(personId: Long): PersonEntity?

    @Query("SELECT COUNT(*) FROM persons")
    suspend fun count(): Int

    @Insert
    suspend fun insert(person: PersonEntity): Long

    @Update
    suspend fun update(person: PersonEntity)

    /** 词条变更时同步刷新人物的「最后更新时间」 */
    @Query("UPDATE persons SET updatedAt = :timestamp WHERE id = :personId")
    suspend fun touch(personId: Long, timestamp: Long)

    @Query("DELETE FROM persons WHERE id = :personId")
    suspend fun deleteById(personId: Long)
}
