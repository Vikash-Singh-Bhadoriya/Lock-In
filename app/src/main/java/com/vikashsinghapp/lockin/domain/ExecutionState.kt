package com.vikashsinghapp.lockin.domain

sealed class ExecutionState {
    object Idle : ExecutionState()

    data class Running(
        val taskId: Long,
        val title: String,
        val startTime: Long
    ) : ExecutionState()
}
