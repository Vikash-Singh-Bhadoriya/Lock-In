package com.vikashsinghapp.lockin.presentation.tomorrow_focus

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vikashsinghapp.lockin.Constants
import com.vikashsinghapp.lockin.data.entity.PromiseTask
import com.vikashsinghapp.lockin.data.repository.PlanPrefsRepository
import com.vikashsinghapp.lockin.data.repository.PromiseTaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TomorrowFocusViewModel @Inject constructor(
    private val repository: PromiseTaskRepository,
    private val planPrefs: PlanPrefsRepository,
) : ViewModel() {

    // The original repository-backed flow (for initial load only)
    val promiseTasks: StateFlow<List<PromiseTask>> =
        repository.getAllPromiseTasks()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    // Local working list for UI editing
    val localTasks: SnapshotStateList<PromiseTask> = mutableStateListOf()

    var isReordering by mutableStateOf(false)
        private set

    // used for one-time UI Events
    private val _eventFlow = MutableSharedFlow<TomorrowFocusScreenViewModelUiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    val isLocked = planPrefs.isPlanLocked
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    // visiblePermissionDialogQueue is a state & it will not survive process death
    // & we don't need to, because we check for permission every time when user click on START
    // when we use savedStateHandle here, then it will show notification after process death at starting
    // & we don't want that
    val visiblePermissionDialogQueue = mutableStateListOf<String>()

    init {
        // Load initial tasks into localTasks
        viewModelScope.launch(Dispatchers.IO) {
            localTasks.clear()
            localTasks.addAll(repository.getAllTasksOnce())
        }
    }

    fun dismissDialog() {
        visiblePermissionDialogQueue.removeAt(0)
    }

    fun onPostNotificationsPermissionPermissionResult(
        permission: String,
        isGranted: Boolean,
    ) {
        if (isGranted) {
            // Permission is granted. Continue the action or workflow in your app.
            Timber.tag(Constants.TAG).d("Permission  Granted")
            onEvent(TomorrowFocusEvent.SavePlanAfterPermissionGranted)
        } else if (!visiblePermissionDialogQueue.contains(permission)) {
            // Explain to the user that the feature is unavailable because the
            // features requires a permission that the user has denied. At the
            // same time, respect the user's decision. Don't link to system
            // settings in an effort to convince the user to change their decision.
            visiblePermissionDialogQueue.add(permission)
        }
    }

    fun onEvent(event: TomorrowFocusEvent) {
        when (event) {
            is TomorrowFocusEvent.OnTaskUpdate -> {
                val listIndex = localTasks.indexOfFirst { it.id == event.newTask.id }
                if (listIndex != -1) {
                    localTasks[listIndex] = event.newTask
                }
            }

            is TomorrowFocusEvent.DeleteTask -> {
                localTasks.removeAll { it.id == event.task.id }
            }

            is TomorrowFocusEvent.DuplicateTask -> {
                val listIndex = localTasks.indexOfFirst { it.id == event.task.id }
                if (listIndex != -1) {
//                    // I need to update taskId from listIndex + 1 till end of the list.
//                    // bcs then it would have duplicate Ids
//                    ((listIndex+1)..localTasks.lastIndex).forEach { ind ->
//                        // do -1 bcs Ids are in negative
//                        localTasks[ind] = localTasks[ind].copy(id = localTasks[ind].id - 1)
//                    }
//                    // Add new task at next index, we need to update it Id also by doing -1
//                    localTasks.add(listIndex + 1, event.task.copy(id = event.task.id - 1))

                    val newId = (localTasks.minOfOrNull { it.id } ?: 0) - 1
                    localTasks.add(listIndex + 1, event.task.copy(id = newId))
                }
            }

            TomorrowFocusEvent.AddNewTask -> {
                // Use a negative id for new tasks (not yet in DB)
                val newId = (localTasks.minOfOrNull { it.id } ?: 0) - 1
                val promiseTask = PromiseTask(
                    id = newId,
                    title = "New Task",
                )
                localTasks.add(promiseTask)
            }

            TomorrowFocusEvent.ValidateAndRequestPermission -> {
                viewModelScope.launch {
                    validateAndRequestPermission()
                }
            }

            TomorrowFocusEvent.SavePlanAfterPermissionGranted -> {
                viewModelScope.launch {
                    savePlanAfterPermission()
                }
            }

            is TomorrowFocusEvent.SwapTask -> {
                // when swap do not change id's
                if (event.toIndex < localTasks.size) {

                    val fromTask = localTasks.removeAt(event.fromIndex)
                    localTasks.add(event.toIndex, fromTask);
                }
            }

            is TomorrowFocusEvent.ReorderingChanged -> {
                viewModelScope.launch {
                    isReordering = event.isReordering
                    _eventFlow.emit(TomorrowFocusScreenViewModelUiEvent.ScrollToTop)
                }
            }
        }
    }

    // TO DO : ADD ANIMATION LATER, that click on add task to add new task
//    private suspend fun insertDefaultTasks() {
//        repository.addTask(
//            PromiseTask(
//                title = "Deep Work",
//                startTime = LocalTime.of(9, 0),
//                endTime = LocalTime.of(11, 30)
//            )
//        )
//        repository.addTask(
//            PromiseTask(
//                title = "Workout",
//                startTime = LocalTime.of(18, 0),
//                endTime = LocalTime.of(19, 0)
//            )
//        )
//    }
    suspend fun validateAndRequestPermission() {
        val validationError = validateTasks(localTasks)
        if (validationError != null) {
            _eventFlow.emit(TomorrowFocusScreenViewModelUiEvent.ValidationError(validationError))
            return
        }

        _eventFlow.emit(TomorrowFocusScreenViewModelUiEvent.RequestNotificationPermission)
    }

    suspend fun savePlanAfterPermission() {
        // Add or update tasks from localTasks
        localTasks.forEach { task ->
            Timber.d(" localTasks.forEach task : $task\n")
        }
        localTasks.forEach { task ->
            if (task.id > 0) {
                repository.updateTask(task)
            } else {
                repository.addTask(task.copy(id = 0)) // Let DB assign id
            }
        }
        promiseTasks.first { tasks ->
            localTasks.clear()
            localTasks.addAll(tasks)
        }
        planPrefs.lockPlanForToday()
        _eventFlow.emit(TomorrowFocusScreenViewModelUiEvent.ScheduleAllPlanTaskAlarm)
    }

    private fun validateTasks(tasks: List<PromiseTask>): String? {
        // 1. No empty titles
        if (tasks.any { it.title.isBlank() }) {
            return "Task titles cannot be empty."
        }
        // 2. Start < End
        if (tasks.any { it.startTime >= it.endTime }) {
            return "Each task's start time must be before its end time."
        }
        // 3. No overlapping time ranges
        val sorted = tasks.sortedBy { it.startTime }
        for (i in 0 until sorted.size - 1) {
            val current = sorted[i]
            val next = sorted[i + 1]
            if (current.endTime > next.startTime) {
                return "Tasks cannot overlap in time."
            }
        }
        return null
    }

}
