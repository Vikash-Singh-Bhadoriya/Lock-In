package com.vikashsinghapp.lockin.presentation.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vikashsinghapp.lockin.data.entity.PromiseTask
import com.vikashsinghapp.lockin.data.repository.PromiseTaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

data class TodayUiState(
//    val currentTime: String = "",
    val tasks: List<PromiseTask> = emptyList()
)

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val promiseRepository: PromiseTaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodayUiState())
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    private val _currentTime = MutableStateFlow(LocalTime.now())
    val currentTime: StateFlow<LocalTime> = _currentTime.asStateFlow()

    init {
        viewModelScope.launch {
            promiseRepository.getAllPromiseTasks(LocalDate.now())
                .collect { todayTasks ->
                    _uiState.value = _uiState.value.copy(
//                        currentTime = System.currentTimeMillis().toTimeString(),
                        tasks = todayTasks
                    )
                }

            while (true) {
                _currentTime.value = LocalTime.now()
                delay(60_000) // update every minute
            }
        }
    }
}