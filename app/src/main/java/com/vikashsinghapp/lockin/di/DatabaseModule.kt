package com.vikashsinghapp.lockin.di

import android.content.Context
import androidx.room.Room
import com.vikashsinghapp.lockin.data.LockInDatabase
import com.vikashsinghapp.lockin.data.dao.CategoryDao
import com.vikashsinghapp.lockin.data.dao.DayReflectionDao
import com.vikashsinghapp.lockin.data.dao.JournalMessageDao
import com.vikashsinghapp.lockin.data.dao.PromiseDao
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
    fun provideDatabase(
        @ApplicationContext context: Context
    ): LockInDatabase {
        return Room.databaseBuilder(
            context,
            LockInDatabase::class.java,
            "lockin_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideJournalDao(db: LockInDatabase): JournalMessageDao =
        db.journalMessageDao()

    @Provides
    @Singleton
    fun providePromiseDao(db: LockInDatabase): PromiseDao =
        db.promiseDao()

    @Provides
    @Singleton
    fun provideCategoryDao(db: LockInDatabase): CategoryDao =
        db.categoryDao()

    @Provides
    @Singleton
    fun provideDayReflectionDao(db: LockInDatabase): DayReflectionDao =
        db.dayReflectionDao()
}
