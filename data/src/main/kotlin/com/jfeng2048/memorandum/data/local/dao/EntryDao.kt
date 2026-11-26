package com.jfeng2048.memorandum.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.jfeng2048.memorandum.data.local.entity.EntryEntity
import com.jfeng2048.memorandum.data.local.relation.EntryWithCategory

@Dao
interface EntryDao {

    @Transaction
    @Query("SELECT * FROM entry WHERE entry_id = :entryId")
    suspend fun findWithCategoryById(entryId: Long): EntryWithCategory?

    @Query("SELECT * FROM entry WHERE entry_id = :entryId")
    suspend fun findById(entryId: Long): EntryEntity?

    @Query("SELECT COUNT(*) FROM entry WHERE category_id = :categoryId")
    suspend fun countByCategory(categoryId: Long): Int

    @Query("SELECT category_id AS categoryId, COUNT(*) AS count FROM entry GROUP BY category_id")
    fun observeCategoryUsageCounts(): kotlinx.coroutines.flow.Flow<List<CategoryUsageCount>>

    @Insert
    suspend fun insert(entry: EntryEntity): Long

    @Update
    suspend fun update(entry: EntryEntity)

    @Query("DELETE FROM entry WHERE entry_id = :entryId")
    suspend fun deleteById(entryId: Long)
}

data class CategoryUsageCount(
    val categoryId: Long,
    val count: Int,
)
