package com.vikashsinghapp.lockin.presentation.task_detail

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.vikashsinghapp.lockin.data.entity.JournalMessage
import com.vikashsinghapp.lockin.data.entity.TaskEndStatus
import com.vikashsinghapp.lockin.formatTime
import com.vikashsinghapp.lockin.presentation.journal.toTimeString
import com.vikashsinghapp.lockin.ui.theme.BackgroundDark
import com.vikashsinghapp.lockin.ui.theme.Running
import com.vikashsinghapp.lockin.ui.theme.SurfaceDarkElevated

@Composable
fun TaskDetailScreen(
    onBack: () -> Unit,
    viewModel: TaskDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var logInput by remember { mutableStateOf("") }

    BackHandler {
        onBack()
    }

    uiState.task?.let { task ->
        Scaffold(
            containerColor = BackgroundDark,
            bottomBar = {
                JournalInputBar(
                    value = logInput,
                    onValueChange = { logInput = it },
                    onSend = {
                        viewModel.onEvent(TaskDetailEvent.AddLog(it))
                        logInput = ""
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(Modifier.height(16.dp))

                // --- Task Identity ---
                Text(text = task.title, color = White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "${task.startTime.formatTime()} – ${task.endTime.formatTime()}",
                    color = Color.Gray, fontSize = 16.sp
                )

                Spacer(Modifier.height(24.dp))

                // --- Status Picker (For corrections/updates) ---
                Text("Update Task Status", color = White, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TaskEndStatus.entries.filter { it != TaskEndStatus.NONE }.forEach { status ->
                        StatusSelectionButton(
                            status = status,
                            isSelected = task.status == status,
                            selectedColor = status.color,
                            onSelect = { viewModel.onEvent(TaskDetailEvent.UpdateStatus(it)) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                // --- Journal Timeline (Sync Feed) ---
                Text("Task Logs & Journals", color = White, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))

                if (uiState.messages.isEmpty()) {
                    Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        Text("No logs for this block.", color = Color.Gray)
                    }
                } else {
                    uiState.messages.forEach { msg ->
                        LogMessageItem(msg)
                        Spacer(Modifier.height(8.dp))
                    }
                }

                // --- Original Task Note (From Review/Break screen) ---
                task.note?.let {
                    Spacer(Modifier.height(16.dp))
                    Text("Original Reflection", color = Color.Gray, fontSize = 12.sp)
                    Box(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                            .background(SurfaceDarkElevated).padding(12.dp)
                    ) {
                        Text(it, color = White, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun LogMessageItem(message: JournalMessage) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
            .background(SurfaceDarkElevated).padding(12.dp)
    ) {
        Text(text = message.content, color = White, fontSize = 15.sp)
        Text(
            text = message.timestamp.toTimeString(),
            color = Color.Gray, fontSize = 11.sp, modifier = Modifier.align(Alignment.End)
        )
    }
}

@Composable
fun StatusSelectionButton(
    status: TaskEndStatus,
    isSelected: Boolean,
    selectedColor: Color,
    onSelect: (TaskEndStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) selectedColor else SurfaceDarkElevated)
            .clickable { onSelect(status) },
        contentAlignment = Alignment.Center
    ) {
        Text(text = status.label, color = if (isSelected) White else Color.Gray, fontSize = 13.sp)
    }
}

@Composable
fun JournalInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundDark)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .imePadding(), // Ensures the bar stays above the keyboard
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- The Styled Text Input ---
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(SurfaceDarkElevated)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(color = White, fontSize = 15.sp),
                cursorBrush = SolidColor(White),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(
                            text = "Log progress or a bug...",
                            color = Color.Gray,
                            fontSize = 15.sp
                        )
                    }
                    innerTextField()
                }
            )
        }

        Spacer(Modifier.width(12.dp))

        // --- The Send Button ---
        IconButton(
            onClick = {
                if (value.isNotBlank()) onSend(value)
            },
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (value.isNotBlank()) Running else SurfaceDarkElevated)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send Log",
                tint = if (value.isNotBlank()) White else Color.Gray
            )
        }
    }
}