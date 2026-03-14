package com.vikashsinghapp.lockin.presentation.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vikashsinghapp.lockin.data.entity.CategoryEntity
import com.vikashsinghapp.lockin.data.entity.PromiseTask
import com.vikashsinghapp.lockin.data.entity.TaskEndStatus
import com.vikashsinghapp.lockin.data.repository.CategoryRepository
import com.vikashsinghapp.lockin.data.repository.PlanPrefsRepository
import com.vikashsinghapp.lockin.data.repository.PromiseTaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
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
    private val categoryRepository: CategoryRepository,
    planPrefs: PlanPrefsRepository
) : ViewModel() {

    val isLocked = planPrefs.isPlanLocked
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

//    val hasPendingBlock = prefs.pendingTaskId
//        .map { it != null }
//        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    private val _uiState = MutableStateFlow(TodayUiState())
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    private val _currentTime = MutableStateFlow(LocalTime.now())
    val currentTime: StateFlow<LocalTime> = _currentTime.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate = _selectedDate.asStateFlow()

    private val _categories = MutableStateFlow<List<CategoryEntity>>(emptyList())
    val categories: StateFlow<List<CategoryEntity>> = _categories.asStateFlow()

    private var job: Job? = null

    // used for one-time UI Events
    private val _eventFlow = MutableSharedFlow<TaskScreenViewModelUiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

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
        viewModelScope.launch {
            categoryRepository.getAllCategories().collectLatest {
                _categories.value = it
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

            is TaskScreenEvent.InjectTask -> {
                viewModelScope.launch {
                    val newTask = PromiseTask(
                        id = 0,
                        planDate = LocalDate.now(), // Injected tasks always happen today
                        title = event.title,
                        category = event.category,
                        startTime = event.startTime,
                        endTimePlan = event.endTimePlan,
                        status = TaskEndStatus.PENDING,
                    )
                    promiseRepository.addTask(newTask)
                    _eventFlow.emit(TaskScreenViewModelUiEvent.SchedulePlanTaskAlarm(newTask))
                }
            }
        }
    }
}

sealed class TaskScreenViewModelUiEvent {
    data class SchedulePlanTaskAlarm(val task: PromiseTask) : TaskScreenViewModelUiEvent()
}

sealed class TaskScreenEvent {
    data class OnDateSelected(val date: LocalDate) : TaskScreenEvent()
    data class InjectTask(
        val title: String,
        val startTime: LocalTime,
        val endTimePlan: LocalTime,
        val category: String,
    ) : TaskScreenEvent()
    data object ShowDateCalendar : TaskScreenEvent()
    data object HideDateCalendar : TaskScreenEvent()
}