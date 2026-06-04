package com.vikashsinghapp.lockin.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.vikashsinghapp.lockin.data.entity.JournalMessage
import com.vikashsinghapp.lockin.presentation.journal.JournalMessageWithTask
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalMessageDao {
    @Query("SELECT * FROM journal_messages ORDER BY timestamp")
    fun getAll(): Flow<List<JournalMessage>>

    @Query("SELECT * FROM journal_messages WHERE duringPromiseTaskId = :taskId ORDER BY timestamp")
    fun getForTask(taskId: Long): Flow<List<JournalMessage>>

    @Query("""
        SELECT j.*, 
               IFNULL(t.title, '') as taskTitle, 
               IFNULL(t.category, '') as taskCategory 
        FROM journal_messages j 
        LEFT JOIN promise_task t ON j.duringPromiseTaskId = t.id 
        ORDER BY j.timestamp ASC
    """)
    fun getAllWithTaskInfo(): Flow<List<JournalMessageWithTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: JournalMessage): Long

    @Update
    suspend fun update(message: JournalMessage)

    @Delete
    suspend fun delete(message: JournalMessage)

     @Query("UPDATE journal_messages SET duringPromiseTaskId = NULL WHERE duringPromiseTaskId = :taskId")
     suspend fun orphanMessagesForTask(taskId: Long)
}