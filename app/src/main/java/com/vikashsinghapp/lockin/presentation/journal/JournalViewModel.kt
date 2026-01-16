package com.vikashsinghapp.lockin.presentation.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vikashsinghapp.lockin.data.entity.JournalMessage
import com.vikashsinghapp.lockin.data.repository.JournalRepository
import com.vikashsinghapp.lockin.domain.ExecutionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JournalViewModel @Inject constructor(
    private val repository: JournalRepository,
) : ViewModel() {

    val messages: StateFlow<List<JournalMessage>> =
        repository.getAllMessages()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    fun sendMessage(text: String, executionState: ExecutionState) {
        if (text.isBlank()) return

        viewModelScope.launch {
            repository.addMessage(
                content = text,
                executionState = executionState
            )
        }
    }
}
