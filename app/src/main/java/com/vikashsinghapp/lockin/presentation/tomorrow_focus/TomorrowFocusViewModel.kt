package com.vikashsinghapp.lockin.presentation.tomorrow_focus

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vikashsinghapp.lockin.Constants
import com.vikashsinghapp.lockin.data.entity.PromiseTask
import com.vikashsinghapp.lockin.data.repository.PlanPrefsRepository
import com.vikashsinghapp.lockin.data.repository.PromiseTaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class TomorrowFocusViewModel @Inject constructor(
    private val repository: PromiseTaskRepository,
    private val planPrefs: PlanPrefsRepository,
) : ViewModel() {

    val promiseTasks: StateFlow<List<PromiseTask>> =
        repository.getAllPromiseTasks()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    // used for one-time UI Events
    private val _eventFlow = MutableSharedFlow<TomorrowFocusScreenViewModelUiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    val isLocked = planPrefs.isPlanLocked
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    init {
        viewModelScope.launch {
            val todayTasks = repository.getAllPromiseTasks(LocalDate.now()).first()
            if (todayTasks.isEmpty()) {
                insertDefaultTasks()
            }
        }
    }
    // visiblePermissionDialogQueue is a state & it will not survive process death
    // & we don't need to, because we check for permission every time when user click on START
    // when we use savedStateHandle here, then it will show notification after process death at starting
    // & we don't want that
    val visiblePermissionDialogQueue = mutableStateListOf<String>()

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
            onEvent(TomorrowFocusEvent.LockPlan)
        } else if (!visiblePermissionDialogQueue.contains(permission)) {
            // Explain to the user that the feature is unavailable because the
            // features requires a permission that the user has denied. At the
            // same time, respect the user's decision. Don't link to system
            // settings in an effort to convince the user to change their decision.
            visiblePermissionDialogQueue.add(permission)
        }
    }


    fun onEvent(event: TomorrowFocusEvent) {
        viewModelScope.launch {
            when (event) {
                is TomorrowFocusEvent.OnTaskUpdate -> {
                    repository.updateTask(event.newTask)
                }

                is TomorrowFocusEvent.DeleteTask -> {
                    repository.deleteTask(event.task)
                }

                TomorrowFocusEvent.LockPlan -> {
                    planPrefs.lockPlanForToday()
                    _eventFlow.emit(TomorrowFocusScreenViewModelUiEvent.ScheduleAllPlanTaskAlarm)
                }

                TomorrowFocusEvent.AddNewTask -> {
                    val promiseTask = PromiseTask(
                        title = "New Task",
                    )
                    repository.addTask(promiseTask)
                }
            }
        }
    }

    private suspend fun insertDefaultTasks() {
        repository.addTask(
            PromiseTask(
                title = "Deep Work",
                startTime = LocalTime.of(9, 0),
                endTime = LocalTime.of(11, 30)
            )
        )
        repository.addTask(
            PromiseTask(
                title = "Workout",
                startTime = LocalTime.of(18, 0),
                endTime = LocalTime.of(19, 0)
            )
        )
    }

}
