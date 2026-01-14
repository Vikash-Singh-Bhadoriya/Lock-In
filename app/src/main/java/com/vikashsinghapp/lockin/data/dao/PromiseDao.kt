package com.vikashsinghapp.lockin.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.vikashsinghapp.lockin.data.entity.PromiseTask
import com.vikashsinghapp.lockin.data.entity.PromiseTaskStatus

@Dao
interface PromiseDao {

    @Insert
    suspend fun insert(promise: PromiseTask): Long

    @Update
    suspend fun update(promise: PromiseTask)

    @Query("SELECT * FROM promise_task WHERE status = :status LIMIT 1")
    suspend fun getRunningByStatus(status: PromiseTaskStatus = PromiseTaskStatus.RUNNING): PromiseTask?

    @Query("SELECT * FROM promise_task WHERE startTime >= :dayStart AND startTime < :dayEnd")
    suspend fun getTasksForDay(dayStart: Long, dayEnd: Long): List<PromiseTask>
}