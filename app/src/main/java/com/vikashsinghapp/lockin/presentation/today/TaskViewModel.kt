package com.vikashsinghapp.lockin.presentation.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vikashsinghapp.lockin.data.entity.PromiseTask
import com.vikashsinghapp.lockin.data.repository.PromiseTaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

data class TodayUiState(
    val isDateCalendarVisible: Boolean = false,
//    val currentTime: String = "",
    val tasks: List<PromiseTask> = emptyList(),
)

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val promiseRepository: PromiseTaskRepository,
//    prefs: PlanPrefsRepository
) : ViewModel() {

//    val hasPendingBlock = prefs.pendingTaskId
//        .map { it != null }
//        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    private val _uiState = MutableStateFlow(TodayUiState())
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    private val _currentTime = MutableStateFlow(LocalTime.now())
    val currentTime: StateFlow<LocalTime> = _currentTime.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate = _selectedDate.asStateFlow()

    private var job: Job? = null

    init {
        // Both run independently and concurrently
        viewModelScope.launch {
            // Emit immediately
            _currentTime.value = LocalTime.now()

            // use isActive not true => bcs I want
            while (isActive) {
                Timber.d("TodayViewModel _currentTime.value: ${_currentTime.value}")
                _currentTime.value = LocalTime.now()
                delay(60_000) // update every minute
            }
        }

        job = viewModelScope.launch {
            Timber.d("TodayViewModel launch")

            promiseRepository.getAllPromiseTasks(LocalDate.now())
                .collectLatest { todayTasks ->
                    Timber.d("TodayViewModel task: $todayTasks")
                    _uiState.value = _uiState.value.copy(
//                        currentTime = System.currentTimeMillis().toTimeString(),
                        tasks = todayTasks.sortedBy { it.startTime }
                    )
                }
        }
    }


    fun onEvent(event: TaskScreenEvent) {
        when (event) {
            is TaskScreenEvent.OnDateSelected -> {
                job?.cancel()
                _selectedDate.value = event.date
                job = viewModelScope.launch {
                    promiseRepository.getAllPromiseTasks(_selectedDate.value)
                        .collectLatest { dateTasks ->
                            _uiState.value = _uiState.value.copy(
                                tasks = dateTasks.sortedBy { it.startTime }
                            )
                        }
                }
            }

            TaskScreenEvent.HideDateCalendar -> {
                _uiState.value = _uiState.value.copy(
                    isDateCalendarVisible = false
                )
            }
            TaskScreenEvent.ShowDateCalendar -> {

                _uiState.value = _uiState.value.copy(
                    isDateCalendarVisible = true
                )
            }
        }
    }
}

sealed class TaskScreenEvent {
    data class OnDateSelected(val date: LocalDate) : TaskScreenEvent()
    data object ShowDateCalendar: TaskScreenEvent()
    data object HideDateCalendar: TaskScreenEvent()
}