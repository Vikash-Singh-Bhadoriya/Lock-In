package com.vikashsinghapp.lockin.presentation.task_distracted_reflection

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vikashsinghapp.lockin.data.entity.PromiseTask
import com.vikashsinghapp.lockin.data.entity.TaskDistractedOptions
import com.vikashsinghapp.lockin.data.entity.TaskEndStatus
import com.vikashsinghapp.lockin.data.repository.PromiseTaskRepository
import com.vikashsinghapp.lockin.formatTime
import com.vikashsinghapp.lockin.system.alarm.TaskAlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class TaskDistractReflectionViewModel @Inject constructor(
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

    var selectedStatus by mutableStateOf(TaskDistractedOptions.NONE)
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



    fun onEvent(event: TaskDistractReflectionEvent) {
        viewModelScope.launch {
            when (event) {
                is TaskDistractReflectionEvent.TaskStatusChanged -> {
                    selectedStatus = event.status
                }
                is TaskDistractReflectionEvent.NoteChanged -> {
                    note = event.note
                }
                TaskDistractReflectionEvent.EndThisBlock -> {
                    val noteMsg = task.note?.let {
                        "${task.note}\n${LocalTime.now().formatTime()} ${selectedStatus.label}: $note"
                    } ?: "${LocalTime.now().formatTime()} ${selectedStatus.label}: $note"



                    task = task.copy(
                        status = TaskEndStatus.BROKEN,
                        note = noteMsg
                    )
                    Timber.d("TaskDistractReflectionViewModel TaskDistractReflectionEvent.EndThisBlock note: ${task.note}")
                    repository.updateTask(task)
                }
                TaskDistractReflectionEvent.ResumeThisBlock -> {

                    val noteMsg = task.note?.let {
                        "${task.note}\n${LocalTime.now().formatTime()} ${selectedStatus.label}: $note"
                    } ?: "${LocalTime.now().formatTime()} ${selectedStatus.label}: $note"

                    task = task.copy(
                        status = TaskEndStatus.NONE,
                        note = noteMsg
                    )
                    Timber.d("TaskDistractReflectionViewModel TaskDistractReflectionEvent.ResumeThisBlock note: ${task.note}")
                    repository.updateTask(task)
                }
            }
        }
    }
}
