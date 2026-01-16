package com.vikashsinghapp.lockin.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ExecutionController {

    private val _state =
        MutableStateFlow<ExecutionState>(ExecutionState.Idle)

    val state: StateFlow<ExecutionState> = _state

    fun start(taskId: Long, title: String) {
        _state.value = ExecutionState.Running(
            taskId = taskId,
            title = title,
            startTime = System.currentTimeMillis()
        )
    }

    fun stop() {
        _state.value = ExecutionState.Idle
    }
}
