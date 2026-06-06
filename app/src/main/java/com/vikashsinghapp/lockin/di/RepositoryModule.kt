package com.vikashsinghapp.lockin.di

import android.content.Context
import com.vikashsinghapp.lockin.data.dao.CategoryDao
import com.vikashsinghapp.lockin.data.dao.JournalMessageDao
import com.vikashsinghapp.lockin.data.dao.PromiseDao
import com.vikashsinghapp.lockin.data.dao.TemplateDao
import com.vikashsinghapp.lockin.data.repository.AppPrefsRepository
import com.vikashsinghapp.lockin.data.repository.CategoryRepository
import com.vikashsinghapp.lockin.data.repository.JournalRepository
import com.vikashsinghapp.lockin.data.repository.PromiseTaskRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideJournalRepository(
        dao: JournalMessageDao,
    ): JournalRepository = JournalRepository(dao)

    @Provides
    @Singleton
    fun provideTaskRepository(
        dao: PromiseDao,
    ): PromiseTaskRepository = PromiseTaskRepository(dao)

    @Provides
    @Singleton
    fun provideCategoryRepository(
        categoryDao: CategoryDao,
        promiseDao: PromiseDao,
        templateDao: TemplateDao,
    ): CategoryRepository = CategoryRepository(categoryDao, promiseDao, templateDao)

    @Provides
    @Singleton
    fun provideAppPrefsRepository(
        @ApplicationContext context: Context,
    ): AppPrefsRepository = AppPrefsRepository(context)

//}
//    @Binds // When someone asks for Interface, give them this implementation
//    @Singleton
//    abstract fun bindPromiseTaskRepository(
//        impl: PromiseTaskRepository
//    ): PromiseTaskRepository

}
