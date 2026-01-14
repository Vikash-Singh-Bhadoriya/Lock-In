package com.vikashsinghapp.lockin.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.vikashsinghapp.lockin.data.dao.DayReflectionDao
import com.vikashsinghapp.lockin.data.dao.JournalMessageDao
import com.vikashsinghapp.lockin.data.dao.PromiseDao
import com.vikashsinghapp.lockin.data.entity.DayReflection
import com.vikashsinghapp.lockin.data.entity.JournalMessage
import com.vikashsinghapp.lockin.data.entity.PromiseTaskStatus

@Database(
    entities = [PromiseTaskStatus::class, JournalMessage::class, DayReflection::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class LockInDatabase : RoomDatabase() {
    abstract fun promiseDao(): PromiseDao
    abstract fun journalMessageDao(): JournalMessageDao
    abstract fun dayReflectionDao(): DayReflectionDao
}