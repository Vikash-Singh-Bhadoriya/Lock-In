package com.vikashsinghapp.lockin.presentation.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vikashsinghapp.lockin.data.entity.CategoryEntity
import com.vikashsinghapp.lockin.data.entity.PromiseTask
import com.vikashsinghapp.lockin.data.entity.TaskEndStatus
import com.vikashsinghapp.lockin.data.repository.CategoryRepository
import com.vikashsinghapp.lockin.data.repository.PromiseTaskRepository
import com.vikashsinghapp.lockin.validateTaskUpdate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
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
    val isLoading: Boolean = true
)

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val promiseRepository: PromiseTaskRepository,
    private val categoryRepository: CategoryRepository,
//    planPrefs: AppPrefsRepository
) : ViewModel() {

//    val isLocked = planPrefs.isPlanLocked
//        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

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
                        tasks = todayTasks.sortedBy { it.startTime },
                        isLoading = false
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

                // Show loading state while fetching new date
                _uiState.value = _uiState.value.copy(isLoading = true)

                job = viewModelScope.launch {
                    promiseRepository.getAllPromiseTasks(_selectedDate.value)
                        .collectLatest { dateTasks ->
                            _uiState.value = _uiState.value.copy(
                                tasks = dateTasks.sortedBy { it.startTime },
                                isLoading = false
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
                    )


                    val tasks = _uiState.value.tasks.toMutableList()
                    tasks.add(newTask)

                    // Use new Id
                    val newId = promiseRepository.addTask(newTask)
                    _eventFlow.emit(TaskScreenViewModelUiEvent.SchedulePlanTaskAlarm(newTask.copy(id = newId)))
                }
            }
        }
    }
    fun validateAndInjectTask(title: String, startTime: LocalTime, endTime: LocalTime, category: String): String? {
        val newTask = PromiseTask(
            id = 0, planDate = LocalDate.now(), title = title,
            category = category, startTime = startTime, endTimePlan = endTime,
            status = TaskEndStatus.PENDING,
        )

        // Pass the whole list to check for overlaps. fromFab = false means 5 min allowed.
        val error = validateTaskUpdate(newTask, _uiState.value.tasks, require30Min = false)
        if (error != null) return error

        // If valid, fire the standard inject event
        onEvent(TaskScreenEvent.InjectTask(title, startTime, endTime, category))
        return null
    }

    // --- Category Management Functions ---
    fun addCategory(category: CategoryEntity) {
        viewModelScope.launch {
            // Assuming your categoryRepository has this method from your TomorrowFocusViewModel
            categoryRepository.insertCategory(category)
        }
    }

    fun editCategory(oldCategory: CategoryEntity, updatedCategory: CategoryEntity) {
        viewModelScope.launch {

            categoryRepository.updateCategory(oldName = oldCategory.name, updatedCategory = updatedCategory)

            // 2. Instantly update the UI memory state
            val tasks = _uiState.value.tasks.toMutableList()
            for (i in tasks.indices) {
                if (tasks[i].category == oldCategory.name) {
                    tasks[i] = tasks[i].copy(category = updatedCategory.name)
                }
            }
            _uiState.update { it.copy(tasks = tasks) }
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            categoryRepository.deleteCategory(category)
        }
    }
}

sealed class TaskScreenViewModelUiEvent {
    data class SchedulePlanTaskAlarm(val task: PromiseTask) : TaskScreenViewModelUiEvent()
    data class ValidationError(val message: String) : TaskScreenViewModelUiEvent()
    data class ShowSnackbar(val message: String) : TaskScreenViewModelUiEvent()
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