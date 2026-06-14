package com.vikashsinghapp.lockin.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.vikashsinghapp.lockin.data.entity.DailyCompletionStat
import com.vikashsinghapp.lockin.data.entity.PromiseTask
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface PromiseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(promise: PromiseTask): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(promises: List<PromiseTask>)

    @Update
    suspend fun update(promise: PromiseTask)

    @Query("UPDATE promise_task SET category = :newName WHERE category = :oldName")
    suspend fun updateCategoryNameInTasks(oldName: String, newName: String)

    @Delete
    suspend fun delete(promise: PromiseTask)

//    @Query("SELECT * FROM promise_task WHERE status = :status LIMIT 1")
//    suspend fun getRunningByStatus(status: PromiseTaskStatus = PromiseTaskStatus.RUNNING): PromiseTask?

    @Query("SELECT * FROM promise_task WHERE id = :id")
    fun getTaskById(id: Long): Flow<PromiseTask?>

    @Query("SELECT * FROM promise_task WHERE id = :id")
    suspend fun getTaskByIdOnce(id: Long): PromiseTask?

//    @Query("SELECT * FROM promise_task WHERE startTime >= :dayStart AND startTime < :dayEnd")
//    A “Plan” is just a collection of tasks with the same planDate.
    @Query("SELECT * FROM promise_task WHERE planDate = :planDate")
    fun getTasksForDay(planDate: LocalDate): Flow<List<PromiseTask>>

    @Query("SELECT * FROM promise_task WHERE planDate = :planDate")
    suspend fun getAllTasksOnce(planDate: LocalDate): List<PromiseTask>

    // No date filter — returns every day that has tasks, from the user's very first entry.
    // UNFINISHED counts as partial credit (50) so days with honest work still light up the heatmap.
    @Query(
        """
        SELECT planDate as date,
               COUNT(*) as totalTasks,
               SUM(CASE WHEN status != 'PENDING' THEN 1 ELSE 0 END) as activeTasks,
               SUM(CASE
                   WHEN status = 'COMPLETED' THEN 100
                   WHEN status = 'UNFINISHED' THEN 50
                   ELSE 0
               END) as weightedScore
        FROM promise_task
        GROUP BY planDate
        ORDER BY planDate ASC
        """
    )
    fun getAllDailyCompletionStats(): Flow<List<DailyCompletionStat>>
}