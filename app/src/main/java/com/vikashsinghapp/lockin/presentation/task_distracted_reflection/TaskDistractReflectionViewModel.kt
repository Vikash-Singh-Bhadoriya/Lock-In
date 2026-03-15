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
import com.vikashsinghapp.lockin.data.repository.JournalRepository
import com.vikashsinghapp.lockin.data.repository.PromiseTaskRepository
import com.vikashsinghapp.lockin.system.alarm.TaskAlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TaskDistractReflectionViewModel @Inject constructor(
    private val taskRepository: PromiseTaskRepository,
    private val journalRepository: JournalRepository,
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

    var actualEndTime by mutableStateOf(java.time.LocalTime.now())
        private set

    init {
        viewModelScope.launch {
            taskRepository.getTaskById(taskId).collectLatest { task ->
                task?.let {
                    this@TaskDistractReflectionViewModel.task = it
                    title = it.title
                } ?: run {
                    error("Task not found")
                }
            }
        }
    }


    fun onEvent(event: TaskDistractReflectionEvent) {
        viewModelScope.launch {
            when (event) {
                is TaskDistractReflectionEvent.ActualEndTimeChanged -> {
                    actualEndTime = event.time
                }
                is TaskDistractReflectionEvent.TaskStatusChanged -> {
                    selectedStatus = event.status
                }

                is TaskDistractReflectionEvent.NoteChanged -> {
                    note = event.note
                }

                TaskDistractReflectionEvent.EndThisBlock -> {
                    // 1. Format the reflection note
                    val finalNote = "${selectedStatus.label}: $note"

                    // 2. Save it directly to the Journal feed linked to this task
                    journalRepository.addMessage(
                        content = finalNote,
                        duringPromiseTaskId = taskId
                    )

                    // 3. Update the task status (leave task.note alone/null)
                    task = task.copy(
                        status = TaskEndStatus.BROKEN,
                        actualEndTime = actualEndTime
                    )
                    Timber.d("TaskDistractReflectionViewModel TaskDistractReflectionEvent.EndThisBlock note: ${task.note}")
                    taskRepository.updateTask(task)
                }

                TaskDistractReflectionEvent.ResumeThisBlock -> {

                    val finalNote = "${selectedStatus.label}: $note"

                    journalRepository.addMessage(
                        content = finalNote,
                        duringPromiseTaskId = taskId
                    )

                    // Return to default state. If time is current, UI will render as Running.
                    task = task.copy(status = TaskEndStatus.PENDING)
                    Timber.d("TaskDistractReflectionViewModel TaskDistractReflectionEvent.ResumeThisBlock note: ${task.note}")
                    taskRepository.updateTask(task)
                }
            }
        }
    }
}