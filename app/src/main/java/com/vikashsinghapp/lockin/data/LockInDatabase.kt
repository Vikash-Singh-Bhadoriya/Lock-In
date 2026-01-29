package com.vikashsinghapp.lockin.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.vikashsinghapp.lockin.data.dao.DayReflectionDao
import com.vikashsinghapp.lockin.data.dao.JournalMessageDao
import com.vikashsinghapp.lockin.data.dao.PromiseDao
import com.vikashsinghapp.lockin.data.entity.DayReflection
import com.vikashsinghapp.lockin.data.entity.JournalMessage
import com.vikashsinghapp.lockin.data.entity.PromiseTask
import com.vikashsinghapp.lockin.data.type_converter.LocalDateConverter
import com.vikashsinghapp.lockin.data.type_converter.LocalTimeConverter
import com.vikashsinghapp.lockin.data.type_converter.PromiseStatusConverter

@Database(
    entities = [PromiseTask::class, JournalMessage::class, DayReflection::class],
    version = 1
)
@TypeConverters(PromiseStatusConverter::class, LocalTimeConverter::class, LocalDateConverter::class)
abstract class LockInDatabase : RoomDatabase() {
    abstract fun promiseDao(): PromiseDao
    abstract fun journalMessageDao(): JournalMessageDao
    abstract fun dayReflectionDao(): DayReflectionDao
}