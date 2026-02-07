package com.vikashsinghapp.lockin.presentation.task_status

sealed class TaskStatusEvent {
    data class TaskStatusChanged(val status: TaskEndStatus): TaskStatusEvent()
    data class NoteChanged(val note: String): TaskStatusEvent()
    object TaskSubmit: TaskStatusEvent()
}