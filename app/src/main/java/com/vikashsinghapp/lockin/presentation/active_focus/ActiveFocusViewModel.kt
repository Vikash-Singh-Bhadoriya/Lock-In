package com.vikashsinghapp.lockin.presentation.active_focus

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vikashsinghapp.lockin.data.entity.JournalMessage
import com.vikashsinghapp.lockin.data.entity.PromiseTask
import com.vikashsinghapp.lockin.data.entity.TaskEndStatus
import com.vikashsinghapp.lockin.data.repository.CategoryRepository
import com.vikashsinghapp.lockin.data.repository.JournalRepository
import com.vikashsinghapp.lockin.data.repository.PromiseTaskRepository
import com.vikashsinghapp.lockin.system.service.toMs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

data class ActiveFocusState(
    val task: PromiseTask? = null,
    val timeRemainingFormatted: String = "00:00",
    val progressPercentage: Float = 0f,
    val themeColorValue: Long? = null,
    val journalMessages: List<JournalMessage> = emptyList(),
    val isFinished: Boolean = false,
)

sealed class ActiveFocusUiEvent {
    data class NavigateToReflection(val taskId: Long) : ActiveFocusUiEvent()
    data class NavigateToSuccess(val taskId: Long) : ActiveFocusUiEvent()
    data class CancelAlarm(val taskId: Long) : ActiveFocusUiEvent()
}

@HiltViewModel
class ActiveFocusViewModel @Inject constructor(
    private val promiseRepository: PromiseTaskRepository,
    private val journalRepository: JournalRepository,
    private val categoryRepository: CategoryRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val taskId: Long = checkNotNull(savedStateHandle["taskId"])

    private val _uiState = MutableStateFlow(ActiveFocusState())
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<ActiveFocusUiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var timerJob: Job? = null

    init {
        // 1. Listen to Task and Journals from DB
        viewModelScope.launch {

            combine(
                promiseRepository.getTaskById(taskId),
                journalRepository.getMessagesForTask(taskId),
                categoryRepository.getAllCategories()
            ) { task, messages, categories ->
                if (task != null) {
                    // Find the matching category to extract the color
                    val matchedCategory = categories.find { it.name == task.category }

                    _uiState.update {
                        it.copy(
                            task = task,
                            journalMessages = messages,
                            themeColorValue = matchedCategory?.colorValue // Pass color to UI
                        )
                    }

                    if (timerJob == null && task.status == TaskEndStatus.PENDING) {
                        startTimer(task)
                    }
                }
            }.collectLatest { }
        }
    }

    // --- The Precision Timer Engine ---
    private fun startTimer(task: PromiseTask) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            // Calculate absolute start and end times in milliseconds
            val startTimeMs = (task.actualStartTime ?: task.startTime).toMs(task.planDate)
            val endTimeMs = task.endTimePlan.toMs(task.planDate)
            val totalDurationMs = endTimeMs - startTimeMs

            while (isActive) {
                val now = System.currentTimeMillis()
                val remainingMs = endTimeMs - now

                // Condition A: Time has run out naturally!
                if (remainingMs <= 0) {
                    _uiState.update {
                        it.copy(
                            timeRemainingFormatted = "00:00",
                            progressPercentage = 1f,
                            isFinished = true
                        )
                    }
                    handleNaturalFinish(task)
                    break // Stop the loop
                }

                // Condition B: Timer is still running
                val elapsedMs = now - startTimeMs
                // Coerce between 0f and 1f so the circle never overflows
                val progress = (elapsedMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f)

                _uiState.update {
                    it.copy(
                        timeRemainingFormatted = formatMillisToTime(remainingMs),
                        progressPercentage = progress
                    )
                }

                // Check exactly every 1 second
                delay(1000)
            }
        }
    }

    // --- Formats MS into HH:MM:SS or MM:SS ---
    private fun formatMillisToTime(millis: Long): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60)) % 60
        val hours = (millis / (1000 * 60 * 60))

        return if (hours > 0) {
            String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        }
    }

    // --- Event Handlers ---

    fun addJournalLog(content: String) {
        viewModelScope.launch {
            journalRepository.addMessage(content, taskId)
        }
    }

    // Triggered when the user HOLDS the 3-second ghost button
    fun onEndEarly() {
        viewModelScope.launch {
            timerJob?.cancel() // Stop the UI clock immediately

            _uiState.value.task?.let { task ->
                // 1. Tell Android OS to kill any alarms for this task
                _eventFlow.emit(ActiveFocusUiEvent.CancelAlarm(taskId))

                // 2. We do NOT mark it Broken here. We send them to the Reflection Screen
                // so they have to write their excuse first. The Reflection Screen handles the DB update.
                _eventFlow.emit(ActiveFocusUiEvent.NavigateToReflection(taskId))
            }
        }
    }

    // Triggered when the timer hits 00:00 naturally
    private fun handleNaturalFinish(task: PromiseTask) {
        viewModelScope.launch {
            // 1. Kill alarms just to be safe
            _eventFlow.emit(ActiveFocusUiEvent.CancelAlarm(taskId))

            // 2. Send them to a "Success / Good Job" screen
            _eventFlow.emit(ActiveFocusUiEvent.NavigateToSuccess(taskId))
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel() // Prevent memory leaks when screen closes
    }
}