package com.vikashsinghapp.lockin.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.vikashsinghapp.lockin.Constants
import com.vikashsinghapp.lockin.data.dao.CategoryDao
import com.vikashsinghapp.lockin.data.dao.DayReflectionDao
import com.vikashsinghapp.lockin.data.dao.JournalMessageDao
import com.vikashsinghapp.lockin.data.dao.PromiseDao
import com.vikashsinghapp.lockin.data.dao.TemplateDao
import com.vikashsinghapp.lockin.data.database.LockInDatabase
import com.vikashsinghapp.lockin.data.database.MIGRATION_1_2
import com.vikashsinghapp.lockin.data.repository.AppPrefsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private lateinit var database: LockInDatabase


    @Provides
    @Singleton
    fun provideCoroutineScope(): CoroutineScope {
        return CoroutineScope(Dispatchers.IO + SupervisorJob())
    }

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        coroutineScope: CoroutineScope,
        preferencesRepository: AppPrefsRepository
    ): LockInDatabase {
        database = Room.databaseBuilder(
            context,
            LockInDatabase::class.java,
            "lockin_db"
        )            // prepopulate the database after onCreate was called
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    Timber.tag(Constants.TAG).d("Room.databaseBuilder onCreate called !!")
                    prePopulateSavedPlans(
                        coroutineScope,
                        preferencesRepository
                    )
                }

            })
            .addMigrations(MIGRATION_1_2)
            .build()
        return database
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

    @Provides
    fun provideTemplateDao(database: LockInDatabase): TemplateDao {
        return database.templateDao()
    }

    fun prePopulateSavedPlans(
        coroutineScope: CoroutineScope,
        preferencesRepository: AppPrefsRepository
    ) {
        Timber.tag(Constants.TAG)
            .d("in prepopulateAppTourWorkout !!")
        coroutineScope.launch {
            // 1. Check if we need to seed the database
            val hasSeeded = preferencesRepository.hasSeededDefaults.first()

            if (!hasSeeded) {

                Timber.tag(Constants.TAG)
                    .d("prepopulate db with new appTourWorkout!!")
                val templateDao = database.templateDao()
                val categoryDao = database.categoryDao()

                coroutineScope.launch {
                    // 1. Insert Categories
                    PRE_POPULATE_CATEGORIES.forEach { categoryDao.insertCategory(it) }

                    // 2. Insert Templates
                    PRE_POPULATE_TEMPLATES.forEach { templateDao.insertTemplate(it) }

                    // 3. Insert Tasks
                    templateDao.insertAllTemplateTasks(PRE_POPULATE_TEMPLATE_TASKS)

                    // Lock it in so this NEVER happens again
                    preferencesRepository.setHasSeededDefaults(true)
                }
            }
        }
    }
}