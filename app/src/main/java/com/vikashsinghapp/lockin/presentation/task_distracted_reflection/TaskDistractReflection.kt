package com.vikashsinghapp.lockin.presentation.task_distracted_reflection

import com.vikashsinghapp.lockin.data.entity.TaskDistractedOptions

sealed class TaskDistractReflectionEvent {
    data class TaskStatusChanged(val status: TaskDistractedOptions): TaskDistractReflectionEvent()
    data class NoteChanged(val note: String): TaskDistractReflectionEvent()
    object EndThisBlock: TaskDistractReflectionEvent()
    object ResumeThisBlock: TaskDistractReflectionEvent()
}