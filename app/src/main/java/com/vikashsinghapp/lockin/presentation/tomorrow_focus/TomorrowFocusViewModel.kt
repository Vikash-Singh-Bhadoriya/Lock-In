package com.vikashsinghapp.lockin.presentation.tomorrow_focus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vikashsinghapp.lockin.data.entity.PromiseTask
import com.vikashsinghapp.lockin.data.repository.PromiseTaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TomorrowFocusViewModel @Inject constructor(
    private val repository: PromiseTaskRepository,
) : ViewModel() {

    val promiseTasks: StateFlow<List<PromiseTask>> =
        repository.getAllPromiseTasks()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    fun onEvent(event: TomorrowFocusEvent) {
        viewModelScope.launch {
            when (event) {
                is TomorrowFocusEvent.OnTaskUpdate -> {
                    repository.updateTask(event.newTask)
                }

                is TomorrowFocusEvent.DeleteTask -> {
                    repository.deleteTask(event.task)
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
}
