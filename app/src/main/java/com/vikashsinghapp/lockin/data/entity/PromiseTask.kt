package com.vikashsinghapp.lockin.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

// TASK
@Entity(tableName = "promise_task")
data class PromiseTask(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val planDate: LocalDate = LocalDate.now(),
    val title: String,
    val category: String, // Grouping: "DSA", "Android Dev", "Workout", across multiple days
    val startTime: LocalTime = LocalTime.now(),
    val endTimePlan: LocalTime = LocalTime.now().plusMinutes(60),
    val status: TaskEndStatus = TaskEndStatus.PENDING,
    val actualStartTime: LocalTime? = null,
    val actualEndTime: LocalTime? = null,
//    val status: PromiseTaskStatus = PromiseTaskStatus.NOT_STARTED,

    val note: String? = null, // "Phone", "P*rn", "Fatigue"
)