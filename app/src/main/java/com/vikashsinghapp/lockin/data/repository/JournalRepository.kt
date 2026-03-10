package com.vikashsinghapp.lockin.data.repository

import com.vikashsinghapp.lockin.data.dao.JournalMessageDao
import com.vikashsinghapp.lockin.data.entity.JournalMessage
import com.vikashsinghapp.lockin.presentation.journal.JournalMessageWithTask
import kotlinx.coroutines.flow.Flow

class JournalRepository(
    private val journalDao: JournalMessageDao
) {

    fun getAllMessages(): Flow<List<JournalMessage>> =
        journalDao.getAll()

    fun getMessagesForTask(taskId: Long): Flow<List<JournalMessage>> =
        journalDao.getForTask(taskId)

    fun getCombinedMessages(): Flow<List<JournalMessageWithTask>> = journalDao.getAllWithTaskInfo()

    suspend fun addMessage(
        content: String,
        duringPromiseTaskId: Long? = null
//        executionState: ExecutionState
    ) {
//        val duringPromiseTaskId = when (executionState) {
//            is ExecutionState.Running -> executionState.taskId
//            else -> null
//        }
        journalDao.insert(
            JournalMessage(
                content = content,
                timestamp = System.currentTimeMillis(),
//                duringPromiseTaskId = duringPromiseTaskId
                duringPromiseTaskId = duringPromiseTaskId
            )
        )
    }

    suspend fun deleteMessage(message: JournalMessage) {
        journalDao.delete(message)
    }
}
