package com.jfeng2048.memorandum.di

import com.jfeng2048.memorandum.data.local.prefs.FloatingSettingsImpl
import com.jfeng2048.memorandum.data.repository.PersonRepositoryImpl
import com.jfeng2048.memorandum.domain.repository.FloatingSettings
import com.jfeng2048.memorandum.domain.repository.PersonRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPersonRepository(impl: PersonRepositoryImpl): PersonRepository

    @Binds
    @Singleton
    abstract fun bindFloatingSettings(impl: FloatingSettingsImpl): FloatingSettings
}
