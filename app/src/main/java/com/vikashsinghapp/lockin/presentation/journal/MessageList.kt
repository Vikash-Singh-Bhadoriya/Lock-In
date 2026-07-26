package com.vikashsinghapp.lockin.presentation.journal

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.vikashsinghapp.lockin.ui.theme.BackgroundDark

@Composable
fun MessageList(
    modifier: Modifier = Modifier,
    messages: List<JournalMessageWithTask>,
    selectedIds: Set<Long>,
    isSelectionMode: Boolean,
    onToggleSelection: (Long) -> Unit,
    listState: LazyListState,
) {
    val chatRows = remember(messages) { buildChatRows(messages) }

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark),
    ) {
        items(
            items = chatRows,
            key = { row ->
                when (row) {
                    is ChatRow.DateHeader -> "date_${row.label}"
                    is ChatRow.Message -> row.item.message.id
                }
            },
        ) { row ->
            when (row) {
                is ChatRow.DateHeader -> ChatDateHeader(label = row.label)
                is ChatRow.Message -> {
                    val message = row.item
                    val isSelected = selectedIds.contains(message.message.id)
                    MessageBubble(
                        item = message,
                        isSelected = isSelected,
                        isSelectionMode = isSelectionMode,
                        onToggleSelection = { onToggleSelection(message.message.id) },
                    )
                }
            }
        }
    }
}

@Composable
fun MessageBubble(
    item: JournalMessageWithTask,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onToggleSelection: () -> Unit,
) {
    val taskTitle = item.taskTitle?.trim()?.takeIf { it.isNotBlank() }

    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isSelected) Color(0xFF1A2A2A) else Color.Transparent)
            .combinedClickable(
                onClick = { if (isSelectionMode) onToggleSelection() },
                onLongClick = { if (!isSelectionMode) onToggleSelection() },
            ),
    ) {
        JournalLogCard(
            text = item.message.content,
            timestamp = item.message.timestamp.toTimeString(),
            taskTitle = taskTitle,
            taskStatus = item.taskStatus,
        )
    }
}
