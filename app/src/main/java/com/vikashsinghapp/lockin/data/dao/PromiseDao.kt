package com.vikashsinghapp.lockin.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.vikashsinghapp.lockin.data.entity.PromiseTask
import com.vikashsinghapp.lockin.data.entity.PromiseTaskStatus
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface PromiseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(promise: PromiseTask): Long

    @Update
    suspend fun update(promise: PromiseTask)

    @Delete
    suspend fun delete(promise: PromiseTask)

    @Query("SELECT * FROM promise_task WHERE status = :status LIMIT 1")
    suspend fun getRunningByStatus(status: PromiseTaskStatus = PromiseTaskStatus.RUNNING): PromiseTask?

//    @Query("SELECT * FROM promise_task WHERE startTime >= :dayStart AND startTime < :dayEnd")
//    A “Plan” is just a collection of tasks with the same planDate.
    @Query("SELECT * FROM promise_task WHERE planDate = :planDate")
    fun getTasksForDay(planDate: LocalDate): Flow<List<PromiseTask>>
}