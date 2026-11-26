package com.jfeng2048.memorandum.di

import android.content.Context
import androidx.room.Room
import com.jfeng2048.memorandum.data.local.AppDatabase
import com.jfeng2048.memorandum.data.local.dao.CategoryDao
import com.jfeng2048.memorandum.data.local.dao.ConversationDao
import com.jfeng2048.memorandum.data.local.dao.EntryDao
import com.jfeng2048.memorandum.data.local.dao.PersonDao
import com.jfeng2048.memorandum.data.local.dao.RelationshipDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.NAME)
            // Demo 阶段允许版本升级时直接重建，避免维护迁移脚本
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun providePersonDao(database: AppDatabase): PersonDao = database.personDao()

    @Provides
    fun provideRelationshipDao(database: AppDatabase): RelationshipDao = database.relationshipDao()

    @Provides
    fun provideCategoryDao(database: AppDatabase): CategoryDao = database.categoryDao()

    @Provides
    fun provideEntryDao(database: AppDatabase): EntryDao = database.entryDao()

    @Provides
    fun provideConversationDao(database: AppDatabase): ConversationDao = database.conversationDao()
}
