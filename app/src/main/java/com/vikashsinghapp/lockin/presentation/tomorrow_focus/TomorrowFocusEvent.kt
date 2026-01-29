package com.vikashsinghapp.lockin.presentation.tomorrow_focus

import com.vikashsinghapp.lockin.data.entity.PromiseTask

sealed class TomorrowFocusEvent {
    data class OnTaskUpdate(val newTask: PromiseTask) : TomorrowFocusEvent()
    data class DeleteTask(val task: PromiseTask) : TomorrowFocusEvent()
    object AddNewTask : TomorrowFocusEvent()
}