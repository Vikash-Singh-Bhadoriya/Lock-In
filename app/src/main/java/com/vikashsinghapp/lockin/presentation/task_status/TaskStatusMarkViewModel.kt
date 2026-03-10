package com.vikashsinghapp.lockin.presentation.task_status

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vikashsinghapp.lockin.data.entity.PromiseTask
import com.vikashsinghapp.lockin.data.entity.TaskEndStatus
import com.vikashsinghapp.lockin.data.repository.PromiseTaskRepository
import com.vikashsinghapp.lockin.formatTime
import com.vikashsinghapp.lockin.system.alarm.TaskAlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class TaskStatusMarkViewModel @Inject constructor(
    private val repository: PromiseTaskRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val taskId: Long =
        savedStateHandle[TaskAlarmScheduler.EXTRA_TASK_ID] ?: run {
            error("TaskId missing")
        }


    private lateinit var task: PromiseTask

    var title by mutableStateOf("")
        private set

    var selectedStatus by mutableStateOf(TaskEndStatus.NONE)
        private set

    var note by mutableStateOf("")
        private set

    init {
        viewModelScope.launch {
            repository.getTaskById(taskId).collectLatest { task ->
                title = task?.title
                    ?: error("Task not found")
                selectedStatus = task.status
            }
        }
    }


    fun onEvent(event: TaskStatusEvent) {
        viewModelScope.launch {
            when (event) {
                is TaskStatusEvent.TaskStatusChanged -> {
                    selectedStatus = event.status
                }

                is TaskStatusEvent.NoteChanged -> {
                    note = event.note
                }

                TaskStatusEvent.TaskSubmit -> {
                    val noteMsg = task.note?.let {
                        "${task.note}\n${LocalTime.now().formatTime()} ${selectedStatus.label}: $note"
                    } ?: "${LocalTime.now().formatTime()} ${selectedStatus.label}: $note"

                    task = task.copy(
                        status = selectedStatus,
                        note = noteMsg
                    )
                    Timber.d("TaskStatusMarkViewModel TaskStatusEvent.TaskSubmit note: ${task.note}")
                    repository.updateTask(task)
                }
            }
        }
    }
}
