package com.vikashsinghapp.lockin.data.repository

import com.vikashsinghapp.lockin.data.dao.JournalMessageDao
import com.vikashsinghapp.lockin.data.entity.JournalMessage
import kotlinx.coroutines.flow.Flow

class JournalRepository(
    private val journalDao: JournalMessageDao
) {

    fun getAllMessages(): Flow<List<JournalMessage>> =
        journalDao.getAll()

    fun getMessagesForTask(taskId: Long): Flow<List<JournalMessage>> =
        journalDao.getForTask(taskId)

    suspend fun addMessage(
        content: String,
        duringPromiseTaskId: Long?
    ) {
        journalDao.insert(
            JournalMessage(
                content = content,
                timestamp = System.currentTimeMillis(),
                duringPromiseTaskId = duringPromiseTaskId
            )
        )
    }

    suspend fun deleteMessage(message: JournalMessage) {
        journalDao.delete(message)
    }
}
