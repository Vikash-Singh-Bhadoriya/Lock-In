package com.vikashsinghapp.lockin.presentation.tomorrow_focus

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vikashsinghapp.lockin.checkOverlap
import com.vikashsinghapp.lockin.data.entity.CategoryEntity
import com.vikashsinghapp.lockin.data.entity.PromiseTask
import com.vikashsinghapp.lockin.data.entity.TemplateTaskEntity
import com.vikashsinghapp.lockin.data.repository.CategoryRepository
import com.vikashsinghapp.lockin.data.repository.PromiseTaskRepository
import com.vikashsinghapp.lockin.data.repository.TemplateRepository
import com.vikashsinghapp.lockin.validateSingleTask
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class TomorrowFocusViewModel @Inject constructor(
    private val repository: PromiseTaskRepository,
//    private val planPrefs: AppPrefsRepository,
    private val categoryRepository: CategoryRepository,
    templateRepo: TemplateRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Extract and parse the target date
    private val targetDateStr: String = savedStateHandle.get<String>("dateString") ?: LocalDate.now().toString()
    val targetDate: LocalDate = LocalDate.parse(targetDateStr)


    // Fetch the templates to show in the Hub
    val templatesWithTasks = templateRepo.getAllTemplatesWithTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    var isLoading by mutableStateOf(true)
        private set

    // The original repository-backed flow (for initial load only)
//    val promiseTasks: StateFlow<List<PromiseTask>> =
//        repository.getAllPromiseTasks()
//            .stateIn(
//                scope = viewModelScope,
//                started = SharingStarted.WhileSubscribed(5_000),
//                initialValue = emptyList()
//            )

    // Local working list for UI editing
    val localTasks: SnapshotStateList<PromiseTask> = mutableStateListOf()

//    var isReordering by mutableStateOf(false)
//        private set

    // used for one-time UI Events
    private val _eventFlow = MutableSharedFlow<TomorrowFocusScreenViewModelUiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

//    val isLocked = planPrefs.isPlanLocked
//        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)
    private val _isLocked = MutableStateFlow(false)
    val isLocked = _isLocked.asStateFlow()

    // visiblePermissionDialogQueue is a state & it will not survive process death
    // & we don't need to, because we check for permission every time when user click on START
    // when we use savedStateHandle here, then it will show notification after process death at starting
    // & we don't want that
    val visiblePermissionDialogQueue = mutableStateListOf<String>()

    var showOverlayDialog by mutableStateOf(false)
        private set

    private val _categories = MutableStateFlow<List<CategoryEntity>>(emptyList())
    val categories: StateFlow<List<CategoryEntity>> = _categories.asStateFlow()

    init {
        // Load initial tasks into localTasks
        // Do not use Dispatchers.IO = For Compose to know the list has updated and redraw the screen, you MUST modify it on the Main UI thread
        viewModelScope.launch {
            isLoading = true

            // Fetch tasks explicitly for the TARGET DATE, not just the default of "today"
            val tasks = withContext(Dispatchers.IO) {
                repository.getAllTasksOnce(targetDate)
            }

            // If the DB has tasks for this date, the contract is signed (Locked)
            _isLocked.value = tasks.isNotEmpty()

            // Update Compose State on the Main thread!
            localTasks.clear()
            localTasks.addAll(tasks)
            isLoading = false
        }

        viewModelScope.launch {
            categoryRepository.getAllCategories().collect { _categories.value = it }
        }
    }

    fun dismissDialog() {
        if (visiblePermissionDialogQueue.isNotEmpty()) {
            visiblePermissionDialogQueue.removeAt(0)
        }
    }

    fun dismissOverlayDialog() {
        showOverlayDialog = false
    }

    fun allowOverlayDialog() {
        showOverlayDialog = true
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun onPostNotificationsPermissionResult(
        isGranted: Boolean,
    ) {
        if (isGranted) {
            // Stage 1 passed! Move to Stage 2.
            viewModelScope.launch {
                _eventFlow.emit(TomorrowFocusScreenViewModelUiEvent.CheckOverlayPermission)
            }
        } else {
            // Stage 1 failed. Add to standard queue.
            // Explain to the user that the feature is unavailable because the
            // features requires a permission that the user has denied. At the
            // same time, respect the user's decision. Don't link to system
            // settings in an effort to convince the user to change their decision.
            if (!visiblePermissionDialogQueue.contains(Manifest.permission.POST_NOTIFICATIONS)) {
                visiblePermissionDialogQueue.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    fun onOverlayPermissionResult(isGranted: Boolean) {
        if (isGranted) {
            // Both Stage 1 and Stage 2 passed! Save the plan.
            viewModelScope.launch {
                savePlanAfterPermission()
            }
        } else {
            // They came back from settings but didn't grant it.
            viewModelScope.launch {
                _eventFlow.emit(TomorrowFocusScreenViewModelUiEvent.ShowSnackbar("Overlay permission is strictly required to enforce focus."))
            }
        }
    }

//    fun onDisplayOverOtherAppsPermissionPermissionResult(
//        permission: String,
//        isGranted: Boolean,
//    ) {
//        if (isGranted) {
//            // Permission is granted. Continue the action or workflow in your app.
//            Timber.tag(Constants.TAG).d("Permission  Granted")
//            onEvent(TomorrowFocusEvent.SavePlanAfterPermissionGranted)
//        } else if (!visiblePermissionDialogQueue.contains(permission)) {
//            // Explain to the user that the feature is unavailable because the
//            // features requires a permission that the user has denied. At the
//            // same time, respect the user's decision. Don't link to system
//            // settings in an effort to convince the user to change their decision.
//            visiblePermissionDialogQueue.add(permission)
//        }
//    }

    fun addCategory(newCategory: CategoryEntity) = viewModelScope.launch {
        categoryRepository.insertCategory(newCategory)
    }

    fun editCategory(oldCategory: CategoryEntity, updatedCategory: CategoryEntity) =
        viewModelScope.launch {
            categoryRepository.updateCategory(
                oldName = oldCategory.name,
                updatedCategory = updatedCategory
            )

            // 2. Instantly update the UI memory state
            for (i in localTasks.indices) {
                if (localTasks[i].category == oldCategory.name) {
                    localTasks[i] = localTasks[i].copy(category = updatedCategory.name)
                }
            }
        }

    fun deleteCategory(category: CategoryEntity) = viewModelScope.launch {
        // 1. Delete from the database
        categoryRepository.deleteCategory(category)

        // 2. Instantly update the UI memory state
        // Loop through the current tasks and clear the category if it matches the deleted one
        for (i in localTasks.indices) {
            if (localTasks[i].category == category.name) {
                localTasks[i] = localTasks[i].copy(category = "") // Reset to Uncategorized
            }
        }
    }

    fun onEvent(event: TomorrowFocusEvent) {
        when (event) {
            is TomorrowFocusEvent.ApplyTemplate -> {
                applyRoutineToPlan(event.templateTasks, event.isReplace)
            }

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

                    // Generate a unique negative ID so Compose doesn't crash from duplicate keys
                    val tempId = -System.currentTimeMillis()
                    val duplicatedTask = event.task.copy(
                        id = tempId,
                        title = "${event.task.title} (Copy)"
                    )
                    localTasks.add(listIndex + 1, duplicatedTask)
                }
            }

//            TomorrowFocusEvent.AddNewTask -> {
//                // Generate a unique negative ID so Compose doesn't crash from duplicate keys
//                val tempId = -System.currentTimeMillis()
//
//                val promiseTask = PromiseTask(
//                    id = tempId,
//                    title = "New Task",
//                    category = ""
//                )
//                localTasks.add(promiseTask)
//            }

            TomorrowFocusEvent.ValidateAndRequestPermission -> {
                // The list is already validated per-task. Just proceed to permissions.
                viewModelScope.launch {
                    if (localTasks.isEmpty()) {
                        _eventFlow.emit(TomorrowFocusScreenViewModelUiEvent.ShowSnackbar("Plan cannot be empty."))
                        return@launch
                    }
                    _eventFlow.emit(TomorrowFocusScreenViewModelUiEvent.CheckNotificationPermission)
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

//            is TomorrowFocusEvent.ReorderingChanged -> {
//                viewModelScope.launch {
//                    isReordering = event.isReordering
//                    _eventFlow.emit(TomorrowFocusScreenViewModelUiEvent.ScrollToTop)
//                }
//            }
        }
    }

    fun validateAndSaveTask(id: Long?, title: String, startTime: LocalTime, endTimePlan: LocalTime, category: String): String? {
        // 1. Basic checks (Require 30 min for tomorrow's plan)
        val basicError = validateSingleTask(title, startTime, endTimePlan, require30Min = true)
        Timber.d("ADDZ: basicError $basicError")
        if (basicError != null) return basicError

        // 2. Overlap check against Promise Tasks
        val taskId = id ?: -1L
        val overlapError = checkOverlap(taskId, startTime, endTimePlan, localTasks)
        Timber.d("ADDZ: overlapError: $overlapError")
        if (overlapError != null) return overlapError

        // 3. Success! Add or Update in local list.
        if (id == null) {
            val newId = (localTasks.minOfOrNull { it.id } ?: 0L) - 1L
            Timber.d("ADDZ: newId: $newId")

            localTasks.add(PromiseTask(id = newId, title = title, startTime = startTime, endTimePlan = endTimePlan, category = category))
        } else {
            val updatedTask = localTasks.find { it.id == id }?.copy(
                title = title,
                startTime = startTime,
                endTimePlan = endTimePlan,
                category = category
            )
            if (updatedTask != null) onEvent(TomorrowFocusEvent.OnTaskUpdate(updatedTask))
        }
        Timber.d("ADDZ: SUCCESS")

        return null // Success!
    }

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

    suspend fun savePlanAfterPermission() {
        // Add or update tasks from localTasks
        withContext(Dispatchers.IO) {
            localTasks.forEach { task ->
                // Force the planDate to match the targetDate! as can plan for today/tomorrow
                val taskWithDate = task.copy(planDate = targetDate)

                if (task.id > 0) {
                    repository.updateTask(taskWithDate)
                } else {
                    repository.addTask(taskWithDate.copy(id = 0)) // Let DB assign id
                }
            }
        }

        // Fetch the fresh list safely on IO
        val freshTasks = withContext(Dispatchers.IO) {
            repository.getAllTasksOnce(targetDate)
        }

        // Update Compose state safely on Main thread
        localTasks.clear()
        localTasks.addAll(freshTasks)

        // Lock the UI immediately now that DB has data
        _isLocked.value = true

        // 3. Lock the plan and fire alarms
//        planPrefs.lockPlanForToday()

        _eventFlow.emit(TomorrowFocusScreenViewModelUiEvent.ScheduleAllPlanTaskAlarm(freshTasks))

        // Let them see the "PLAN LOCKED" text for just under 1 second
        delay(800)

        _eventFlow.emit(TomorrowFocusScreenViewModelUiEvent.NavigateToTimeline)
    }

//    private suspend fun insertTemplateTasksAsRealTasks(
//        templateTasks: List<TemplateTaskEntity>,
//        targetDate: LocalDate,
//    ) {
//        val newPromiseTasks = templateTasks.map { tTask ->
//            PromiseTask(
//                title = tTask.title,
//                startTime = tTask.startTime,
//                endTimePlan = tTask.endTimePlan,
//                category = tTask.category,
//                planDate = targetDate,
//                status = TaskEndStatus.PENDING
//            )
//        }
//        // Insert these into your PromiseTask table
//         repository.addTasks(newPromiseTasks)
//    }

    private fun applyRoutineToPlan(templateTasks: List<TemplateTaskEntity>, isReplace: Boolean) {
        if (isReplace) {
            // REPLACE MODE: Clear the current screen and add the new ones
            localTasks.clear()
            appendTasksToLocal(templateTasks)
            viewModelScope.launch {
                _eventFlow.emit(TomorrowFocusScreenViewModelUiEvent.ShowSnackbar("Saved Plan applied!"))
            }
        } else {
//            // APPEND MODE: We must check for overlaps with what is already on the screen!
//            val hasOverlap = templateTasks.any { tTask ->
//                localTasks.any { cTask ->
//                    checkTimeOverlap(
//                        cTask.startTime,
//                        cTask.endTimePlan,
//                        tTask.startTime,
//                        tTask.endTimePlan
//                    )
//                }
//            }

//            if (hasOverlap) {
//                viewModelScope.launch {
//                    _eventFlow.emit(TomorrowFocusScreenViewModelUiEvent.ShowSnackbar("Cannot append: Time overlap detected!"))
//                }
//            } else {
            appendTasksToLocal(templateTasks)
            viewModelScope.launch {
                _eventFlow.emit(TomorrowFocusScreenViewModelUiEvent.ShowSnackbar("Saved Plan appended! Adjust timings if needed."))
            }
//            }
        }
    }

    private fun appendTasksToLocal(templateTasks: List<TemplateTaskEntity>) {
        // --- Use a timestamp base so IDs NEVER overlap with cleared tasks ---
        var startingId = -System.currentTimeMillis()

        val newPromiseTasks = templateTasks.map { tTask ->
            PromiseTask(
                id = startingId--, // Decrement for each task to ensure unique negative IDs
                title = tTask.title,
                startTime = tTask.startTime,
                endTimePlan = tTask.endTimePlan,
                category = tTask.category,
                // planDate will be automatically assigned when they actually hit "Save Plan"
            )
        }

        // Push them to the UI list!
        localTasks.addAll(newPromiseTasks)
    }

//    // Standard time overlap formula: max(start1, start2) < min(end1, end2)
//    private fun checkTimeOverlap(
//        start1: LocalTime,
//        end1: LocalTime,
//        start2: LocalTime,
//        end2: LocalTime,
//    ): Boolean {
//        return start1.isBefore(end2) && end1.isAfter(start2)
//    }
}
