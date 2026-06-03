package com.vikashsinghapp.lockin.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// 1. Define the migration from Version 1 to Version 2
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // This is standard SQLite.
        // We tell it to alter the table and add your new column as a nullable TEXT.
        db.execSQL("ALTER TABLE promise_task ADD COLUMN actualStartTime TEXT DEFAULT NULL")
    }
}