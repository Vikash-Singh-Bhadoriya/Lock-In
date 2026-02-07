package com.vikashsinghapp.lockin.presentation.task_status

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vikashsinghapp.lockin.data.entity.PromiseTask
import com.vikashsinghapp.lockin.data.repository.PromiseTaskRepository
import com.vikashsinghapp.lockin.system.alarm.TaskAlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
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
            task = repository.getTaskById(taskId)
                ?: error("Task not found")

            title = task.title
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
                    task = task.copy(
                        status = selectedStatus,
                        note = note
                    )
                    repository.updateTask(task)
                }
            }
        }
    }
}
