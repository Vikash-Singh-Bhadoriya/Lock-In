package com.vikashsinghapp.lockin.presentation.task_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vikashsinghapp.lockin.data.entity.JournalMessage
import com.vikashsinghapp.lockin.data.entity.PromiseTask
import com.vikashsinghapp.lockin.data.repository.JournalRepository
import com.vikashsinghapp.lockin.data.repository.PromiseTaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    private val promiseRepository: PromiseTaskRepository,
    private val journalRepository: JournalRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val taskId: Long = checkNotNull(savedStateHandle["taskId"])

    private val _uiState = MutableStateFlow(TaskDetailUiState())
    val uiState = _uiState.asStateFlow()

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
                is TaskDetailEvent.UpdateStartTime -> {
                    _uiState.value.task?.let { currentTask ->
                        promiseRepository.updateTask(currentTask.copy(startTime = event.time))
                    }
                }
                is TaskDetailEvent.UpdateEndTime -> {
                    _uiState.value.task?.let { currentTask ->
                        promiseRepository.updateTask(currentTask.copy(endTimePlan = event.time))
                    }
                }
            }
        }
    }
}

data class TaskDetailUiState(
    val task: PromiseTask? = null,
    val messages: List<JournalMessage> = emptyList(),
)

sealed class TaskDetailEvent {
    data class AddLog(val content: String) : TaskDetailEvent()
    data class UpdateStartTime(val time: LocalTime) : TaskDetailEvent()
    data class UpdateEndTime(val time: LocalTime) : TaskDetailEvent()
}