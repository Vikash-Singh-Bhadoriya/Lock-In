package com.vikashsinghapp.lockin.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface TaskDao {

    @Insert
    suspend fun insert(task: Task): Long

    @Update
    suspend fun update(task: Task)

    @Query("SELECT * FROM tasks WHERE status = 'RUNNING' LIMIT 1")
    suspend fun getRunningTask(): Task?

    @Query("SELECT * FROM tasks WHERE startTime >= :dayStart AND startTime < :dayEnd")
    suspend fun getTasksForDay(dayStart: Long, dayEnd: Long): List<Task>
}

