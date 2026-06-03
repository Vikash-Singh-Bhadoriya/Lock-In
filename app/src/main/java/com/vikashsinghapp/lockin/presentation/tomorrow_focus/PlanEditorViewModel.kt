package com.vikashsinghapp.lockin.presentation.tomorrow_focus

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vikashsinghapp.lockin.checkTemplateOverlap
import com.vikashsinghapp.lockin.data.entity.CategoryEntity
import com.vikashsinghapp.lockin.data.entity.TemplateEntity
import com.vikashsinghapp.lockin.data.entity.TemplateTaskEntity
import com.vikashsinghapp.lockin.data.repository.CategoryRepository
import com.vikashsinghapp.lockin.data.repository.TemplateRepository
import com.vikashsinghapp.lockin.validateSingleTask
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalTime
import javax.inject.Inject

data class PlanEditorState(
    val id: Long = -1L,
    val name: String = "",
    val tasks: List<TemplateTaskEntity> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class PlanEditorViewModel @Inject constructor(
    private val repository: TemplateRepository,
    private val categoryRepository: CategoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Retrieve the ID passed from navigation (e.g., -1 for a new template)
    private val id: Long = savedStateHandle.get<Long>("planId") ?: -1L

    private val _uiState = MutableStateFlow(PlanEditorState(id = id))
    val uiState = _uiState.asStateFlow()

    // Expose categories to the UI
    private val _categories = MutableStateFlow<List<CategoryEntity>>(emptyList())
    val categories = _categories.asStateFlow()

    init {
        loadTemplate()

        // Fetch categories for the selector
        viewModelScope.launch {
            categoryRepository.getAllCategories().collect { _categories.value = it }
        }
    }

    private fun loadTemplate() {
        viewModelScope.launch {
            if (id != -1L) {
                // Editing an existing template
                val templateData = repository.getTemplateWithTasksById(id).firstOrNull()
                if (templateData != null) {
                    _uiState.update { it.copy(
                        name = templateData.template.name,
                        tasks = templateData.tasks,
                        isLoading = false
                    )}
                }
            } else {
                // Creating a new template
                _uiState.update { it.copy(
                    name = "New Plan",
                    isLoading = false
                )}
            }
        }
    }

    fun updateName(newName: String) {
        _uiState.update { it.copy(name = newName) }
    }

    // Call this when the user adds a task via your Add Task Bottom Sheet
    fun addTaskToTemplate(title: String, startTime: LocalTime, endTime: LocalTime, category: String) {
        val newTask = TemplateTaskEntity(
            planId = id, // Will be updated on save if it's a new template
            title = title,
            startTime = startTime,
            endTimePlan = endTime,
            category = category
        )
        _uiState.update { it.copy(tasks = it.tasks + newTask) }
    }

    fun updateTaskInTemplate(oldTask: TemplateTaskEntity, title: String, startTime: LocalTime, endTime: LocalTime, category: String) {
        val updatedTask = oldTask.copy(
            title = title,
            startTime = startTime,
            endTimePlan = endTime,
            category = category
        )
        _uiState.update { state ->
            // Replace the old task with the new one in the list
            state.copy(tasks = state.tasks.map { if (it == oldTask) updatedTask else it })
        }
    }


    fun validateAndSaveTask(id: Long?, title: String, startTime: LocalTime, endTimePlan: LocalTime, category: String): String? {
        // 1. Basic checks (Require 30 min for templates)
        val basicError = validateSingleTask(title, startTime, endTimePlan, require30Min = true)
        if (basicError != null) return basicError

        // 2. Overlap check against Template Tasks
        val taskId = id ?: -1L
        val overlapError = checkTemplateOverlap(taskId, startTime, endTimePlan, _uiState.value.tasks)
        if (overlapError != null) return overlapError

        // 3. Success!
        if (id == null) {
            addTaskToTemplate(title, startTime, endTimePlan, category)
        } else {
            val oldTask = _uiState.value.tasks.find { it.id == id }
            if (oldTask != null) updateTaskInTemplate(oldTask, title, startTime, endTimePlan, category)
        }
        return null
    }


    // Call this to remove a task
    fun removeTask(task: TemplateTaskEntity) {
        _uiState.update { state ->
            state.copy(tasks = state.tasks.filter { it != task })
        }
    }

    // Save everything to the database
    fun saveTemplate(onSaved: () -> Unit) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState.name.isBlank()) return@launch

            var finalTemplateId = currentState.id

            if (finalTemplateId == -1L) {
                // 1. Create the new template first to generate an ID
                finalTemplateId = repository.createTemplate(currentState.name)
            } else {
                // 1. Update existing template name
                repository.updateTemplate(TemplateEntity(id = finalTemplateId, name = currentState.name))
            }

            // 2. Map the tasks to ensure they all have the correct Template ID AND Order Index
            val tasksToSave = currentState.tasks.mapIndexed { index, task ->
                task.copy(
                    planId = finalTemplateId,
                    // If the ID is negative (a temp duplicated task), map it to 0 so Room creates a new ID
                    id = if (task.id < 0) 0 else task.id,
                    orderIndex = index // THIS SAVES THE REORDERED STATE
                )
            }

            // 3. Overwrite the old tasks with the new list
            repository.saveTasksToTemplate(finalTemplateId, tasksToSave)

            onSaved()
        }
    }

    fun duplicateTask(task: TemplateTaskEntity) {
        val listIndex = uiState.value.tasks.indexOfFirst { it.id == task.id }
        if (listIndex != -1) {
            // Generate a unique negative ID so Compose doesn't crash from duplicate keys
            val tempId = -System.currentTimeMillis()
            val duplicatedTask = task.copy(
                id = tempId,
                title = "${task.title} (Copy)"
            )

            val newTasks = uiState.value.tasks.toMutableList()
            newTasks.add(listIndex + 1, duplicatedTask) // Adds right below the current item!
            _uiState.update { it.copy(tasks = newTasks) }
        }
    }

    // --- Reorder by Key to avoid LazyColumn index offsets ---
    fun reorderTasksByKey(fromKey: Long, toKey: Long) {
        Timber.d("Reorder Task planeditor screen $fromKey to $toKey")

        val currentTasks = _uiState.value.tasks.toMutableList()

        // Find the actual data list index using the unique Task ID
        val fromIndex = currentTasks.indexOfFirst { it.id == fromKey }
        val toIndex = currentTasks.indexOfFirst { it.id == toKey }

        if (fromIndex != -1 && toIndex != -1) {
            val item = currentTasks.removeAt(fromIndex)
            currentTasks.add(toIndex, item)
            Timber.d("Reorder Task planeditor screen currentTasks $currentTasks")

            _uiState.update { it.copy(tasks = currentTasks) }
        }
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

            // Note: We do NOT loop through localTasks to delete the string here,
            // so historical tasks keep their category name!
        }
    }
}