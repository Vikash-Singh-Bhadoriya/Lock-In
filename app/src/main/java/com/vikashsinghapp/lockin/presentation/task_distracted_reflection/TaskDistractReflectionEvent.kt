package com.vikashsinghapp.lockin.presentation.task_distracted_reflection

import com.vikashsinghapp.lockin.data.entity.TaskDistractedOptions

sealed class TaskDistractReflectionEvent {
    data class TaskStatusChanged(val status: TaskDistractedOptions) : TaskDistractReflectionEvent()
    data class ActualEndTimeChanged(val time: java.time.LocalTime) : TaskDistractReflectionEvent()
    data class ActualStartTimeChanged(val time: java.time.LocalTime) : TaskDistractReflectionEvent()
    data class NoteChanged(val note: String) : TaskDistractReflectionEvent()
    object RescheduleThisBlock : TaskDistractReflectionEvent()
    object EndThisBlock : TaskDistractReflectionEvent()
    object ResumeThisBlock : TaskDistractReflectionEvent()
}

sealed class TaskReflectionUiEvent {
    object EndSuccess : TaskReflectionUiEvent()
    object ResumeSuccess : TaskReflectionUiEvent()
    data class ShowSnackbar(val message: String) : TaskReflectionUiEvent()
}

enum class ReflectionMode {
    NUDGE_DELAY,
    ROLL_CALL_DELAY,
    MID_TASK_BREAK
}