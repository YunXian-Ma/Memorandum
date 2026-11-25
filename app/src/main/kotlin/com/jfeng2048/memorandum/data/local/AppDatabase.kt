package com.jfeng2048.memorandum.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jfeng2048.memorandum.data.local.dao.CategoryDao
import com.jfeng2048.memorandum.data.local.dao.EntryDao
import com.jfeng2048.memorandum.data.local.dao.PersonDao
import com.jfeng2048.memorandum.data.local.dao.RelationshipDao
import com.jfeng2048.memorandum.data.local.entity.CategoryEntity
import com.jfeng2048.memorandum.data.local.entity.EntryEntity
import com.jfeng2048.memorandum.data.local.entity.PersonEntity
import com.jfeng2048.memorandum.data.local.entity.RelationshipEntity

@Database(
    entities = [PersonEntity::class, RelationshipEntity::class, CategoryEntity::class, EntryEntity::class],
    version = 3,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun personDao(): PersonDao
    abstract fun relationshipDao(): RelationshipDao
    abstract fun categoryDao(): CategoryDao
    abstract fun entryDao(): EntryDao

    companion object {
        const val NAME = "memorandum.db"
    }
}
