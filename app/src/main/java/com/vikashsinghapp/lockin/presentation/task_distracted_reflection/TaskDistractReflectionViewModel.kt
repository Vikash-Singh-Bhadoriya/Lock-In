package com.vikashsinghapp.lockin.presentation.task_distracted_reflection

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalTime
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

    private val _uiEvent = kotlinx.coroutines.flow.MutableSharedFlow<TaskReflectionUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private lateinit var task: PromiseTask

    var title by mutableStateOf("")
        private set

    var selectedStatus by mutableStateOf(TaskDistractedOptions.NONE)
        private set

    var note by mutableStateOf("")
        private set

    var actualEndTime: LocalTime by mutableStateOf(LocalTime.now())
        private set

    var actualStartTime: LocalTime by mutableStateOf(LocalTime.now())
        private set

    var secondsElapsed: Int by mutableIntStateOf(0)
        private set

    init {
        viewModelScope.launch {
            Timber.d("TaskDistractReflectionViewModel init")
            Timber.d("TaskDistractReflectionViewModel init taskId: $taskId")
            taskRepository.getTaskById(taskId).collectLatest { task ->
                Timber.d("TaskDistractReflectionViewModel init task: $task")
                task?.let {
                    this@TaskDistractReflectionViewModel.task = it
                    title = it.title

                    // on Nudge notification => click delay => When will you start: time. The time should should the plan start time.
                    // not LocalTime.now() time
                    actualStartTime = it.startTime
                    secondsElapsed = LocalTime.now().toSecondOfDay() - (it.actualStartTime?.toSecondOfDay() ?: it.startTime.toSecondOfDay())
                } ?: run {
                    // GRACEFUL EXIT: Instead of crashing, just tell the UI to close.
                    Timber.e("Task not found in Database. It may have been deleted.")
                    _uiEvent.emit(TaskReflectionUiEvent.EndSuccess)
                }
            }
        }
    }


    fun onEvent(event: TaskDistractReflectionEvent) {
        viewModelScope.launch {
            when (event) {
                TaskDistractReflectionEvent.RescheduleThisBlock -> {

                    // Calculate the original duration so we can push the end time forward
                    val originalDuration = java.time.Duration.between(task.startTime, task.endTimePlan)
                    val newEndTime = actualStartTime.plus(originalDuration) // actualStartTime holds the UI TimePicker value

                    // TODO: validate overlapping of task, show toast if needed
                    // 1. OVERLAP VALIDATOR
                    // Fetch all tasks for today
                    val allTasks = taskRepository.getAllTasksOnce(task.planDate).filter {
                        it.id != taskId
                    }

                    val hasOverlap = allTasks.any { existing ->
                        actualStartTime.isBefore(existing.endTimePlan) && newEndTime.isAfter(existing.startTime)
                    }

                    if (hasOverlap) {
                        _uiEvent.emit(TaskReflectionUiEvent.ShowSnackbar("Cannot delay: This overlaps with another task!"))
                        return@launch // Stop execution! Do NOT save, do NOT cancel the Dead Man's Switch!
                    }

                    // 2. Form is valid and time is clear. Save it!
                    val finalNote = "Delayed (${selectedStatus.label}): $note"
                    journalRepository.addMessage(content = finalNote, duringPromiseTaskId = taskId)

                    task = task.copy(
                        startTime = actualStartTime,
                        endTimePlan = newEndTime,
                        status = TaskEndStatus.PENDING // Keep it pending!
                    )

                    taskRepository.updateTask(task)

                    // 3. Tell the UI it was successful
                    _uiEvent.emit(TaskReflectionUiEvent.EndSuccess)
                }
                is TaskDistractReflectionEvent.ActualEndTimeChanged -> {
                    actualEndTime = event.time
                }
                is TaskDistractReflectionEvent.ActualStartTimeChanged -> {
                    actualStartTime = event.time
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
                        actualEndTime = actualEndTime,
                        actualStartTime = actualStartTime
                    )
                    Timber.d("TaskDistractReflectionViewModel TaskDistractReflectionEvent.EndThisBlock note: ${task.note}")
                    taskRepository.updateTask(task)

                    // Tell the UI to close
                    _uiEvent.emit(TaskReflectionUiEvent.EndSuccess)
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

                    // Tell the UI to close
                    _uiEvent.emit(TaskReflectionUiEvent.ResumeSuccess)
                }
            }
        }
    }
}