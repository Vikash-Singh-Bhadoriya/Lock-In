package com.vikashsinghapp.lockin.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.vikashsinghapp.lockin.data.entity.JournalMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalMessageDao {
    @Query("SELECT * FROM journal_messages ORDER BY timestamp")
    fun getAll(): Flow<List<JournalMessage>>

    @Query("SELECT * FROM journal_messages WHERE duringPromiseTaskId = :taskId ORDER BY timestamp")
    fun getForTask(taskId: Long): Flow<List<JournalMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: JournalMessage): Long

    @Update
    suspend fun update(message: JournalMessage)

    @Delete
    suspend fun delete(message: JournalMessage)
}