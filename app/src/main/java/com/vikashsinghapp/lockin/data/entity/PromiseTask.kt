package com.vikashsinghapp.lockin.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// TASK
@Entity(tableName = "promise_task")
data class PromiseTask(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val title: String,
    val startTime: Long,
    val endTime: Long,
    val status: PromiseTaskStatus = PromiseTaskStatus.NOT_STARTED,

    val breakReason: String? = null,
    val actualEndTime: Long? = null
)
enum class PromiseTaskStatus {
    NOT_STARTED,
    RUNNING,
    IMPERFECT,
    COMPLETED,
    BROKEN
}