package com.vikashsinghapp.lockin.presentation.journal

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vikashsinghapp.lockin.ui.theme.Running


@Composable
fun MessageList(
    modifier: Modifier = Modifier,
    messages: List<JournalMessageWithTask>,
    selectedIds: Set<Long>,
    isSelectionMode: Boolean,
    onToggleSelection: (Long) -> Unit,
    listState: LazyListState,
) {

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        items(messages, key = { it.message.id }) { message ->
            val isSelected = selectedIds.contains(message.message.id)
            MessageBubble(
                item = message,
                isSelected = isSelected,
                isSelectionMode = isSelectionMode,
                onToggleSelection = { onToggleSelection(message.message.id) }
            )
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
    val configuration = LocalWindowInfo.current
    val maxBubbleWidth = (configuration.containerSize.width * 0.8f).dp // Max 80% of screen width

    // 1. The entire row becomes the clickable area during selection mode
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isSelected) Running.copy(alpha = 0.2f) else Color.Transparent)
            .combinedClickable(
                onClick = {
                    if (isSelectionMode) onToggleSelection()
                },
                onLongClick = {
                    if (!isSelectionMode) onToggleSelection()
                }
            )
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        // 2. The actual Chat Bubble
        Column(
            modifier = Modifier
                // Takes full width to align the bubble left/right
                .align(Alignment.CenterStart)
                // 1. Wrap content so short texts like "ok" make a tiny bubble
                .wrapContentWidth()
                .widthIn(max = maxBubbleWidth)
                .clip(
                    RoundedCornerShape(
                        topStart = 0.dp,
                        topEnd = 16.dp,
                        bottomEnd = 16.dp,
                        bottomStart = 16.dp
                    )
                )
                .background(Color(0xFF1A1A1A))
                .padding(12.dp)
        ) {
            // Top Row: Category and Task (If present)
            val isTaskLog = item.message.duringPromiseTaskId != null
            if (isTaskLog) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Task Title (Top Left)
                    item.taskTitle?.trim()?.takeIf { it.isNotBlank() }?.let { title ->
                        Text(
                            text = title,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Category (Top Right)
                    val rawCat = item.taskCategory?.trim() ?: ""
                    if (rawCat.isNotBlank() && !rawCat.equals("Uncategorized", ignoreCase = true)) {
                        Text(
                            text = rawCat.uppercase(),
                            color = Running,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
            }

            // Middle: The actual message content
            Text(
                text = item.message.content,
                color = Color.White,
                fontSize = 15.sp,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Bottom Right: Timestamp
            Text(
                text = item.message.timestamp.toTimeString(),
                color = Color.Gray,
                fontSize = 10.sp,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}