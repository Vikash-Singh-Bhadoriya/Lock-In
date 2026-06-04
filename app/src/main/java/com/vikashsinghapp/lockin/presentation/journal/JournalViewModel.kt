package com.vikashsinghapp.lockin.presentation.journal

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Embedded
import com.vikashsinghapp.lockin.data.entity.JournalMessage
import com.vikashsinghapp.lockin.data.entity.TaskEndStatus
import com.vikashsinghapp.lockin.data.repository.JournalRepository
import com.vikashsinghapp.lockin.data.repository.PromiseTaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class JournalViewModel @Inject constructor(
    private val repository: JournalRepository,
    private val promiseRepository: PromiseTaskRepository,
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory = _selectedCategory.asStateFlow()

    // 1. Get all raw messages
    private val allMessages = repository.getCombinedMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 2. Dynamically extract unique categories from the messages
    val categories: StateFlow<List<String>> = allMessages.map { messages ->
        val uniqueCats = messages
            .mapNotNull { it.taskCategory?.takeIf { c -> c.isNotBlank() } }
            .toSet()
            .sorted()

        // --- Check if we have any messages without a category ---
        val hasUncategorized = messages.any { it.taskCategory.isNullOrBlank() }

        val result = mutableListOf("All")
        result.addAll(uniqueCats)

        // Only show "Uncategorized" filter if there are actually categorized messages to filter against
        if (hasUncategorized && uniqueCats.isNotEmpty()) {
            result.add("Uncategorized")
        }

        result
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("All"))

    // 3. Filter messages based on the selected category
    val filteredMessages: StateFlow<List<JournalMessageWithTask>> = combine(
        allMessages,
        _selectedCategory
    ) { messages, category ->
        when (category) {
            "All" -> messages
            // Use isNullOrBlank() to catch both nulls and empty strings
            "Uncategorized" -> messages.filter { it.taskCategory.isNullOrBlank() }
            else -> messages.filter { it.taskCategory == category }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedMessages = MutableStateFlow<Set<Long>>(emptySet())
    val selectedMessages = _selectedMessages.asStateFlow()

    fun toggleMessageSelection(messageId: Long) {
        val currentSet = _selectedMessages.value.toMutableSet()
        if (currentSet.contains(messageId)) {
            currentSet.remove(messageId)
        } else {
            currentSet.add(messageId)
        }
        _selectedMessages.value = currentSet
    }

    fun clearSelection() {
        _selectedMessages.value = emptySet()
    }

    fun deleteSelectedMessages() {
        viewModelScope.launch {
            val idsToDelete = _selectedMessages.value
            if (idsToDelete.isEmpty()) return@launch

            // Delete them all. (You might need to add a deleteMessages(ids) function to your repo/DAO for efficiency)
            allMessages.value.filter { idsToDelete.contains(it.message.id) }.forEach {
                repository.deleteMessage(it.message)
            }
            clearSelection()
        }
    }

    fun setCategory(category: String) { _selectedCategory.value = category }

    fun sendMessage(
        text: String
    ) {
        if (text.isBlank()) return

        viewModelScope.launch {
            // 1. Get today's tasks
            val todayTasks = promiseRepository.getAllTasksOnce(LocalDate.now())
            val now = LocalTime.now()

            // 2. Find if any task is currently running right now
            val runningTask = todayTasks.find { task ->
                !now.isBefore(task.startTime) && now.isBefore(task.actualEndTime ?: task.endTimePlan) && task.status == TaskEndStatus.PENDING
            }

            // 3. Save the message. If runningTask is null, it saves as a general log.
            // If it finds a task, Room will automatically link the Category and Title via your DAO!
            repository.addMessage(
                content = text,
                duringPromiseTaskId = runningTask?.id
            )
        }
    }

    // --- EXPORT LOGIC ---
    fun exportJournals(context: Context, exportAll: Boolean) {
        viewModelScope.launch {
            val messagesToExport = if (exportAll) allMessages.value else filteredMessages.value
            if (messagesToExport.isEmpty()) return@launch

            val sb = StringBuilder()
            val header = if (exportAll) "All Journals" else "${_selectedCategory.value} Journals"
            sb.append("--- $header ---\n\n")

            val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

            messagesToExport.forEach { msgWithTask ->
                val timeString = formatter.format(Date(msgWithTask.message.timestamp))

                // Use isNullOrBlank() so it resolves to a strict Boolean, not Boolean?
                if (msgWithTask.taskTitle.isNullOrBlank()) {
                    sb.append("[$timeString]\n")
                } else {
                    val taskName = msgWithTask.taskTitle
                    // Use safe call `?.` before takeIf
                    val cat = msgWithTask.taskCategory?.takeIf { it.isNotBlank() } ?: "Uncategorized"
                    sb.append("[$timeString] $cat • $taskName\n")
                }

                sb.append("${msgWithTask.message.content}\n")
                sb.append("--------------------------------------------------\n")
            }

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "LockIn Journal Export")
                putExtra(Intent.EXTRA_TEXT, sb.toString())
            }
            context.startActivity(Intent.createChooser(shareIntent, "Export Journals"))
        }
    }

    fun deleteMessage(messageWithTask: JournalMessageWithTask) {
        viewModelScope.launch {
            repository.deleteMessage(messageWithTask.message)
        }
    }
}

// Data Class for the UI (The "Joined" Result)
data class JournalMessageWithTask(
    // ap the standard JournalMessage columns (id, content, timestamp etc) directly into that property.
    @Embedded val message: JournalMessage,
    val taskTitle: String?, //bcs a general msg has not task title or task category
    val taskCategory: String?
)