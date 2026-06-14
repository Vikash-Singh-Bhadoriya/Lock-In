package com.vikashsinghapp.lockin.data.repository

import com.vikashsinghapp.lockin.data.dao.PromiseDao
import com.vikashsinghapp.lockin.data.entity.DailyCompletionStat
import com.vikashsinghapp.lockin.data.entity.PromiseTask
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class PromiseTaskRepository(
    private val promiseTaskDao: PromiseDao
) {

    fun getAllPromiseTasks(planDate: LocalDate = LocalDate.now()): Flow<List<PromiseTask>> =
        promiseTaskDao.getTasksForDay(planDate)

    suspend fun getAllTasksOnce(planDate: LocalDate = LocalDate.now()): List<PromiseTask> =
        promiseTaskDao.getAllTasksOnce(planDate)

//    suspend fun getRunningByStatus(status: PromiseTaskStatus = PromiseTaskStatus.RUNNING): PromiseTask? =
//        promiseTaskDao.getRunningByStatus(status)

    fun getTaskById(id: Long): Flow<PromiseTask?> =
        promiseTaskDao.getTaskById(id)

    suspend fun getTaskByIdOnce(id: Long): PromiseTask? =
        promiseTaskDao.getTaskByIdOnce(id)

    suspend fun addTask(task: PromiseTask): Long {
        return promiseTaskDao.insert(task)
    }
    suspend fun addTasks(tasks: List<PromiseTask>) {
        promiseTaskDao.insert(tasks)
    }
    suspend fun updateTask(task: PromiseTask) {
        promiseTaskDao.update(task)
    }
    suspend fun deleteTask(task: PromiseTask) {
        promiseTaskDao.delete(task)
    }

    fun getAllDailyCompletionStats(): Flow<List<DailyCompletionStat>> =
        promiseTaskDao.getAllDailyCompletionStats()
}