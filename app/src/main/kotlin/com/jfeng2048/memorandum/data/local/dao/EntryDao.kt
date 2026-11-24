package com.jfeng2048.memorandum.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.jfeng2048.memorandum.data.local.entity.EntryEntity

@Dao
interface EntryDao {

    @Query("SELECT * FROM entries WHERE id = :entryId")
    suspend fun findById(entryId: Long): EntryEntity?

    @Insert
    suspend fun insert(entry: EntryEntity): Long

    @Update
    suspend fun update(entry: EntryEntity)

    @Query("DELETE FROM entries WHERE id = :entryId")
    suspend fun deleteById(entryId: Long)
}
