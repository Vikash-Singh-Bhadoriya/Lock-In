package com.vikashsinghapp.lockin.presentation.task_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vikashsinghapp.lockin.checkOverlap
import com.vikashsinghapp.lockin.data.entity.CategoryEntity
import com.vikashsinghapp.lockin.data.entity.JournalMessage
import com.vikashsinghapp.lockin.data.entity.PromiseTask
import com.vikashsinghapp.lockin.data.entity.TaskEndStatus
import com.vikashsinghapp.lockin.data.repository.CategoryRepository
import com.vikashsinghapp.lockin.data.repository.JournalRepository
import com.vikashsinghapp.lockin.data.repository.PromiseTaskRepository
import com.vikashsinghapp.lockin.validateSingleTask
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    private val promiseRepository: PromiseTaskRepository,
    private val journalRepository: JournalRepository,
    private val categoryRepository: CategoryRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val taskId: Long = checkNotNull(savedStateHandle["taskId"])

    private val _uiState = MutableStateFlow(TaskDetailUiState())
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<TaskDetailUiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    val categories: StateFlow<List<CategoryEntity>> = categoryRepository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            // Use combine to listen to both the task and the messages reactively
            combine(
                promiseRepository.getTaskById(taskId),
                journalRepository.getMessagesForTask(taskId)
            ) { task, messages ->

                task?.let {
                    _uiState.update {
                        it.copy(task = task, messages = messages)
                    }
                }
            }.collectLatest {
                // This collector keeps the UI in sync with the DB in real-time
            }
        }
    }

    fun onEvent(event: TaskDetailEvent) {
        viewModelScope.launch {
            when (event) {
                is TaskDetailEvent.AddLog -> {
                    journalRepository.addMessage(
                        event.content,
                        taskId
                    )
                }
                TaskDetailEvent.DeleteTask -> {
                    _uiState.value.task?.let { currentTask ->
                        // 1. Orphan the journals so they aren't lost
                        journalRepository.orphanMessagesForTask(taskId)

                        // 2. Destroy the task in the database
                        promiseRepository.deleteTask(currentTask)

                        // 3. Tell the UI to kill alarms and navigate back safely
                        _eventFlow.emit(TaskDetailUiEvent.CancelAlarmAndNavigateBack(currentTask))
                    }
                }
                is TaskDetailEvent.UpdateDetails -> {
                    _uiState.value.task?.let { currentTask ->
                        val updatedTask = currentTask.copy(
                            title = event.title,
                            category = event.category
                        )
                        promiseRepository.updateTask(updatedTask)
                        // Because of the 'combine' flow in the init block,
                        // the UI will instantly redraw with the new name!
                    }
                }
                is TaskDetailEvent.UpdateTimeWindow -> {
                    _uiState.value.task?.let { currentTask ->
                        val updatedTask = currentTask.copy(
                            startTime = event.newStart,
                            endTimePlan = event.newEnd
                        )

                        // 1. Basic Validation
                        val basicError = validateSingleTask(updatedTask.title, updatedTask.startTime, updatedTask.endTimePlan, require30Min = false)
                        if (basicError != null) {
                            _eventFlow.emit(TaskDetailUiEvent.ValidationError(basicError))
                            return@launch
                        }

                        // 2. Overlap Validation
                        val allTasksForDay = promiseRepository.getAllTasksOnce(updatedTask.planDate)
                        val overlapError = checkOverlap(updatedTask.id, updatedTask.startTime, updatedTask.endTimePlan, allTasksForDay)
                        if (overlapError != null) {
                            _eventFlow.emit(TaskDetailUiEvent.ValidationError(overlapError))
                            return@launch
                        }

                        // 3. Success! Save and reschedule.
                        promiseRepository.updateTask(updatedTask)
                        _eventFlow.emit(TaskDetailUiEvent.RescheduleAlarm(updatedTask))
                    }
                }
                is TaskDetailEvent.UpdateStatus -> {
                    _uiState.value.task?.let { currentTask ->
                        promiseRepository.updateTask(currentTask.copy(status = event.status))
                    }
                }
            }
        }
    }
    // Category Management Functions (Keeps DB perfectly synced)
    fun addCategory(category: CategoryEntity) = viewModelScope.launch {
        categoryRepository.insertCategory(category)
    }

    fun editCategory(oldCategory: CategoryEntity, updatedCategory: CategoryEntity) = viewModelScope.launch {
        categoryRepository.updateCategory(oldCategory.name, updatedCategory)
        // If the task is currently using the old category name, update the task too!
        _uiState.value.task?.let {
            if (it.category == oldCategory.name) {
                promiseRepository.updateTask(it.copy(category = updatedCategory.name))
            }
        }
    }

    fun deleteCategory(category: CategoryEntity) = viewModelScope.launch {
        categoryRepository.deleteCategory(category)
        // If the task was using the deleted category, reset it to Uncategorized
        _uiState.value.task?.let {
            if (it.category == category.name) {
                promiseRepository.updateTask(it.copy(category = ""))
            }
        }
    }
}

data class TaskDetailUiState(
    val task: PromiseTask? = null,
    val messages: List<JournalMessage> = emptyList(),
)

sealed class TaskDetailUiEvent {
    data class RescheduleAlarm(val task: PromiseTask) : TaskDetailUiEvent()
    data class ValidationError(val message: String) : TaskDetailUiEvent()
    data class CancelAlarmAndNavigateBack(val task: PromiseTask) : TaskDetailUiEvent()
}

sealed class TaskDetailEvent {
    data class AddLog(val content: String) : TaskDetailEvent()
    data class UpdateTimeWindow(val newStart: LocalTime, val newEnd: LocalTime) : TaskDetailEvent()
    data class UpdateStatus(val status: TaskEndStatus) : TaskDetailEvent()
    data object DeleteTask : TaskDetailEvent()
    data class UpdateDetails(val title: String, val category: String) : TaskDetailEvent()
}