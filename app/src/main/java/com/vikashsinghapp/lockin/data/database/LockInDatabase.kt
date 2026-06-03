package com.vikashsinghapp.lockin.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.vikashsinghapp.lockin.data.dao.CategoryDao
import com.vikashsinghapp.lockin.data.dao.DayReflectionDao
import com.vikashsinghapp.lockin.data.dao.JournalMessageDao
import com.vikashsinghapp.lockin.data.dao.PromiseDao
import com.vikashsinghapp.lockin.data.dao.TemplateDao
import com.vikashsinghapp.lockin.data.entity.CategoryEntity
import com.vikashsinghapp.lockin.data.entity.DayReflection
import com.vikashsinghapp.lockin.data.entity.JournalMessage
import com.vikashsinghapp.lockin.data.entity.PromiseTask
import com.vikashsinghapp.lockin.data.entity.TemplateEntity
import com.vikashsinghapp.lockin.data.entity.TemplateTaskEntity
import com.vikashsinghapp.lockin.data.type_converter.LocalDateConverter
import com.vikashsinghapp.lockin.data.type_converter.LocalTimeConverter

@Database(
    entities = [PromiseTask::class, JournalMessage::class, DayReflection::class, CategoryEntity::class,
        TemplateEntity::class, TemplateTaskEntity::class],
    exportSchema = true,
    version = 2, // You just change 1 to 2
    // Auto Migration only work when app version are different => for development => use database migrations
    // AutoMigration is fantastic for production (when V1 is frozen on the Play Store).
    // But during development, when you are constantly tweaking data classes
    // without bumping version numbers, your local device's database gets out of sync with your JSON files.
    // Manual migrations bypass the JSONs and force the SQL onto the device.
//    autoMigrations = [
//        AutoMigration(from = 1, to = 2) // Room does ALL the SQL work for you automatically
//    ]
)
@TypeConverters(LocalTimeConverter::class, LocalDateConverter::class)
abstract class LockInDatabase : RoomDatabase() {
    abstract fun promiseDao(): PromiseDao
    abstract fun journalMessageDao(): JournalMessageDao
    abstract fun categoryDao(): CategoryDao
    abstract fun dayReflectionDao(): DayReflectionDao
    abstract fun templateDao(): TemplateDao
}