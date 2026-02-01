package com.vikashsinghapp.lockin.data.repository

import com.vikashsinghapp.lockin.data.dao.PromiseDao
import com.vikashsinghapp.lockin.data.entity.PromiseTask
import com.vikashsinghapp.lockin.data.entity.PromiseTaskStatus
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class PromiseTaskRepository(
    private val promiseTaskDao: PromiseDao
) {

    fun getAllPromiseTasks(planDate: LocalDate = LocalDate.now()): Flow<List<PromiseTask>> =
        promiseTaskDao.getTasksForDay(planDate)

    suspend fun getRunningByStatus(status: PromiseTaskStatus = PromiseTaskStatus.RUNNING): PromiseTask? =
        promiseTaskDao.getRunningByStatus(status)

    suspend fun getTaskById(id: Long): PromiseTask? =
        promiseTaskDao.getTaskById(id)

    suspend fun addTask(task: PromiseTask) {
        promiseTaskDao.insert(task)
    }
    suspend fun updateTask(task: PromiseTask) {
        promiseTaskDao.update(task)
    }
    suspend fun deleteTask(task: PromiseTask) {
        promiseTaskDao.delete(task)
    }
}