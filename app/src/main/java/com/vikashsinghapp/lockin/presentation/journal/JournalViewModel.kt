package com.vikashsinghapp.lockin.presentation.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Embedded
import com.vikashsinghapp.lockin.data.entity.JournalMessage
import com.vikashsinghapp.lockin.data.repository.JournalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JournalViewModel @Inject constructor(
    private val repository: JournalRepository,
) : ViewModel() {

//    val messages: StateFlow<List<JournalMessage>> =
//        repository.getAllMessages()
//            .stateIn(
//                scope = viewModelScope,
//                started = SharingStarted.WhileSubscribed(5_000),
//                initialValue = emptyList()
//            )

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory = _selectedCategory.asStateFlow()

    val messages: StateFlow<List<JournalMessageWithTask>> = repository.getCombinedMessages()
        .combine(_selectedCategory) { list, category ->
            if (category == "All") list else list.filter { it.taskCategory == category }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<String>> = repository.getCombinedMessages()
        .map { list -> listOf("All") + list.mapNotNull { it.taskCategory }.distinct() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("All"))

    fun setCategory(category: String) { _selectedCategory.value = category }

    fun sendMessage(text: String
//                    , executionState: ExecutionState
    ) {
        if (text.isBlank()) return

        viewModelScope.launch {
            repository.addMessage(
                content = text,
//                executionState = executionState
            )
        }
    }
}

// Data Class for the UI (The "Joined" Result)
data class JournalMessageWithTask(
    // ap the standard JournalMessage columns (id, content, timestamp etc) directly into that property.
    @Embedded val message: JournalMessage,
    val taskTitle: String?,
    val taskCategory: String?
)