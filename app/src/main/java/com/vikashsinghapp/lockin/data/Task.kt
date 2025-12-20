package com.vikashsinghapp.lockin.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val title: String,
    val startTime: Long,
    val endTime: Long,
    val status: TaskStatus,

    val breakReason: String?,
    val actualStartTime: Long?,
    val actualEndTime: Long?
)

enum class TaskStatus {
    PENDING,
    IN_PROGRESS,
    PARTIAL_COMPLETED,
    COMPLETED,
    BROKEN
}