package com.vikashsinghapp.lockin.di

import com.vikashsinghapp.lockin.data.dao.JournalMessageDao
import com.vikashsinghapp.lockin.data.repository.JournalRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideJournalRepository(
        dao: JournalMessageDao
    ): JournalRepository = JournalRepository(dao)

}
