package com.vikashsinghapp.lockin.presentation.tomorrow_focus

import com.vikashsinghapp.lockin.data.entity.PromiseTask

sealed class TomorrowFocusEvent {
    data class OnTaskUpdate(val newTask: PromiseTask) : TomorrowFocusEvent()
    data class DeleteTask(val task: PromiseTask) : TomorrowFocusEvent()
    data class DuplicateTask(val task: PromiseTask) : TomorrowFocusEvent()
    data class SwapTask(val fromIndex: Int, val toIndex: Int) : TomorrowFocusEvent()
    data class ReorderingChanged(val isReordering: Boolean) : TomorrowFocusEvent()
    object AddNewTask : TomorrowFocusEvent()
    object ValidateAndRequestPermission : TomorrowFocusEvent()
    object SavePlanAfterPermissionGranted : TomorrowFocusEvent()
}