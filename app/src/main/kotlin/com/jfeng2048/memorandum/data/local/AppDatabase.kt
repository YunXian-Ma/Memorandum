package com.jfeng2048.memorandum.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jfeng2048.memorandum.data.local.dao.EntryDao
import com.jfeng2048.memorandum.data.local.dao.PersonDao
import com.jfeng2048.memorandum.data.local.entity.EntryEntity
import com.jfeng2048.memorandum.data.local.entity.PersonEntity

@Database(
    entities = [PersonEntity::class, EntryEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun personDao(): PersonDao
    abstract fun entryDao(): EntryDao

    companion object {
        const val NAME = "memorandum.db"
    }
}
