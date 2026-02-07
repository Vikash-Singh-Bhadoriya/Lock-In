package com.vikashsinghapp.lockin.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.vikashsinghapp.lockin.presentation.task_status.TaskEndStatus
import java.time.LocalDate
import java.time.LocalTime

// TASK
@Entity(tableName = "promise_task")
data class PromiseTask(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val planDate: LocalDate = LocalDate.now(),
    val title: String,
    val startTime: LocalTime = LocalTime.now(),
    val endTime: LocalTime = LocalTime.now().plusMinutes(15),
    val status: TaskEndStatus = TaskEndStatus.NONE,
//    val status: PromiseTaskStatus = PromiseTaskStatus.NOT_STARTED,

    val note: String? = null, // "Phone", "P*rn", "Fatigue"
    val actualEndTime: Long? = null
)