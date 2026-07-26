package com.vikashsinghapp.lockin.presentation.task_detail

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.vikashsinghapp.lockin.data.entity.JournalMessage
import com.vikashsinghapp.lockin.data.entity.TaskEndStatus
import com.vikashsinghapp.lockin.formatTime
import com.vikashsinghapp.lockin.presentation.journal.JournalLogCardSimple
import com.vikashsinghapp.lockin.presentation.journal.statusBorderColor
import com.vikashsinghapp.lockin.presentation.journal.toTimeString
import com.vikashsinghapp.lockin.presentation.today.isCurrent
import com.vikashsinghapp.lockin.presentation.today.isPast
import com.vikashsinghapp.lockin.system.alarm.TaskAlarmScheduler
import com.vikashsinghapp.lockin.ui.theme.BackgroundDark
import com.vikashsinghapp.lockin.ui.theme.Broken
import com.vikashsinghapp.lockin.ui.theme.Completed
import com.vikashsinghapp.lockin.ui.theme.Error
import com.vikashsinghapp.lockin.ui.theme.NotStarted
import com.vikashsinghapp.lockin.ui.theme.Running
import com.vikashsinghapp.lockin.ui.theme.SurfaceDarkElevated
import com.vikashsinghapp.lockin.ui.theme.Unfinished
import kotlinx.coroutines.launch
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    viewModel: TaskDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    // Collect categories for the dialog
    val categories by viewModel.categories.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    var logInput by remember { mutableStateOf("") }
    var showTimeEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    //  State for the Details Editor
    var showEditDetailsDialog by remember { mutableStateOf(false) }

    BackHandler {
        onBack()
    }

    val context = LocalContext.current

    uiState.task?.let { task ->

        if (showTimeEditDialog) {
            EditTimeWindowDialog(
                initialStart = task.startTime,
                initialEnd = task.actualEndTime ?: task.endTimePlan,
                onDismiss = { showTimeEditDialog = false },
                onSave = { newStart, newEnd ->
                    viewModel.onEvent(TaskDetailEvent.UpdateTimeWindow(newStart, newEnd))
                    showTimeEditDialog = false
                }
            )
        }
        if (showEditDetailsDialog) {
            EditDetailsDialog(
                initialTitle = task.title,
                initialCategory = task.category,
                categories = categories,
                onDismiss = { showEditDetailsDialog = false },
                onSave = { newTitle, newCategory ->
                    viewModel.onEvent(TaskDetailEvent.UpdateDetails(newTitle, newCategory))
                    showEditDetailsDialog = false
                },
                onAddCategory = viewModel::addCategory,
                onEditCategory = viewModel::editCategory,
                onDeleteCategory = viewModel::deleteCategory
            )
        }

        val isPastTask = task.isPast(LocalTime.now())
        val hintText = if (isPastTask) "Add a retrospective note..." else "Log progress or a bug..."

        Scaffold(
            containerColor = BackgroundDark,
            bottomBar = {
                JournalInputBar(
                    value = logInput,
                    onValueChange = { logInput = it },
                    onSend = {
                        viewModel.onEvent(TaskDetailEvent.AddLog(it))
                        logInput = ""
                    },
                    hintText = hintText
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

                // --- Task header card ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceDarkElevated)
                        .padding(16.dp),
                ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = task.title,
                        color = White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    // The Guardrail logic (Only show if PENDING and NOT time-locked)
                    val isTimeLockedNow = task.status == TaskEndStatus.PENDING &&
                            LocalTime.now().isAfter(task.startTime.minusMinutes(10)) &&
                            LocalTime.now().isBefore(task.endTimePlan)

                    // Only show Edit/Delete if it's PENDING (Locked during the 10-minute window)
                    if (task.status == TaskEndStatus.PENDING && !isTimeLockedNow) {
                        Row(verticalAlignment = Alignment.CenterVertically) {

                            // Edit Title/Category Button (Always available if pending/future)
                            IconButton(onClick = { showEditDetailsDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Details",
                                    tint = Color.Gray // Subtle grey pencil
                                )
                            }

                            // Delete Button
                                IconButton(onClick = { showDeleteDialog = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Task",
                                        tint = Broken
                                    )
                                }
                        }
                    }
                }

                // The Safety Dialog
                if (showDeleteDialog) {
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { showDeleteDialog = false },
                        containerColor = SurfaceDarkElevated,
                        title = {
                            Text(
                                "Delete Task?",
                                color = White,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        text = {
                            Text(
                                "Are you sure you want to delete this task? Any journals you wrote will be kept and moved to your daily feed.",
                                color = Color.LightGray
                            )
                        },
                        confirmButton = {
                            androidx.compose.material3.Button(
                                onClick = {
                                    showDeleteDialog = false
                                    viewModel.onEvent(TaskDetailEvent.DeleteTask)
                                },
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                    containerColor = Broken
                                )
                            ) {
                                Text("Delete", color = White)
                            }
                        },
                        dismissButton = {
                            androidx.compose.material3.TextButton(onClick = {
                                showDeleteDialog = false
                            }) {
                                Text("Cancel", color = Color.Gray)
                            }
                        }
                    )
                }

                Spacer(Modifier.height(16.dp))

                val now = LocalTime.now()

                // A task is "future" if it hasn't started yet AND hasn't been marked broken/completed
                val isFutureTask =
                    now.isBefore(task.startTime) && task.status == TaskEndStatus.PENDING

                val isTimeLocked = task.status == TaskEndStatus.PENDING &&
                        now.isAfter(task.startTime.minusMinutes(10)) &&
                        now.isBefore(task.endTimePlan)

                // With your interactive TimeFields:
                if (isFutureTask) {
                    if (isTimeLocked) {
                        Text(
                            text = "${(task.actualStartTime ?: task.startTime).formatTime()} – ${(task.actualEndTime ?: task.endTimePlan).formatTime()}",
                            color = Color.Gray, fontSize = 16.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        // 🟢 THE PUNISHMENT UI: Explain why it's locked
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceDarkElevated)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Broken,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Time Locked",
                                    color = Error,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Task starts in less than 10 minutes. To change the time, you must wait for the Roll Call or use the 'Delay' button on your notification and provide a reason.",
                                color = Color.LightGray, fontSize = 12.sp
                            )
                        }
                    } else {
                        // 🟢 REPLACE the inline TimeFields with a clickable row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceDarkElevated)
                                .clickable { showTimeEditDialog = true },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Scheduled Time", color = Color.LightGray, fontSize = 14.sp)
                            Spacer(Modifier.width(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${task.startTime.formatTime()} - ${(task.actualEndTime ?: task.endTimePlan).formatTime()}",
                                    color = Running,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Spacer(Modifier.width(8.dp))
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = Running,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = "${(task.actualStartTime ?: task.startTime).formatTime()} – ${(task.actualEndTime ?: task.endTimePlan).formatTime()}",
                        color = Color.Gray, fontSize = 16.sp
                    )
                }
                } // end header card

                Spacer(Modifier.height(24.dp))

                val now = LocalTime.now()

                // --- STATUS DISPLAY & PICKER ---
                Text("Task Status", color = White, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(12.dp))

                // 1. Calculate the Derived State
                val displayColor = when (task.status) {
                    TaskEndStatus.PENDING -> if (task.isCurrent(now)) Running else NotStarted
                    else -> task.status.color
                }

                val displayLabel = when (task.status) {
                    TaskEndStatus.PENDING -> if (task.isCurrent(now)) "Running" else "Not Started"
                    else -> task.status.label
                }

                // 2. The Prominent Visual Indicator (This stays forever)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(displayColor)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = displayLabel,
                        color = White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 3. The Strict "One-Way Door" Interactive Picker
                // --- Change this to check the END time instead of the start time ---
                val isPastTask = !now.isBefore(task.actualEndTime ?: task.endTimePlan)

                // --- Only show the picker if the task time is OVER and STILL PENDING! ---
                if (isPastTask && task.status == TaskEndStatus.PENDING) {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Mark Final Outcome (Cannot be undone):",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                    Spacer(Modifier.height(8.dp))

                    // Check if this task bypassed the live timer
                    val isUntracked = task.actualStartTime == null

                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatusChip(
                            label = "Completed",
                            icon = Icons.Default.CheckCircle,
                            color = Completed,
                            isSelected = false,
                            onClick = {
                                //  The Friction Tax
                                if (isUntracked && uiState.messages.isEmpty()) {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(
                                            "Proof required: Write a journal entry detailing what you accomplished before marking an untracked task as complete."
                                        )
                                    }
                                } else {
                                    viewModel.onEvent(TaskDetailEvent.UpdateStatus(TaskEndStatus.COMPLETED))
                                }
                            }
                        )
                        StatusChip(
                            label = "Unfinished",
                            icon = Icons.Default.Warning,
                            color = Unfinished,
                            isSelected = false,
                            onClick = { viewModel.onEvent(TaskDetailEvent.UpdateStatus(TaskEndStatus.UNFINISHED)) }
                        )
                        StatusChip(
                            label = "Broken",
                            icon = Icons.Default.Cancel,
                            color = Broken,
                            isSelected = false,
                            onClick = { viewModel.onEvent(TaskDetailEvent.UpdateStatus(TaskEndStatus.BROKEN)) }
                        )
                    }
                } else if (task.status != TaskEndStatus.PENDING) {
                    // Optional: A subtle visual reinforcement that their choice is locked in.
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color.DarkGray,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Status is locked and permanently recorded.",
                            color = Color.DarkGray,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                // --- Journal chat feed ---
                Text("Task Journals", color = White, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                    .background(BackgroundDark)
                        .padding(vertical = 8.dp),
                ) {
                if (uiState.messages.isEmpty()) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No journal entries for this task.", color = Color(0xFF8696A0), fontSize = 14.sp)
                    }
                } else {
                    val logBorderColor = statusBorderColor(task.status.name)
                    uiState.messages.forEach { msg ->
                        LogMessageItem(msg, borderColor = logBorderColor)
                    }
                }
                }
            }
        }
    }


    // --- Listen for Reschedule Events ---
    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is TaskDetailUiEvent.RescheduleAlarm -> {
                    // 1. Explicitly kill the old alarm to be 100% safe
                    TaskAlarmScheduler.cancelTaskAlarm(context, event.task.id)

                    // 2. Schedule the new one (the function will abort if the new time is in the past)
                    TaskAlarmScheduler.scheduleTaskStart(context, event.task)
                }

                is TaskDetailUiEvent.ValidationError -> {
                    // Launch a new coroutine so it doesn't block the Flow!
                    launch {
                        snackbarHostState.showSnackbar(event.message)
                    }
                }

                is TaskDetailUiEvent.CancelAlarmAndNavigateBack -> {
                    TaskAlarmScheduler.cancelTaskAlarm(context, event.task.id)
                    onBack() // Pops the backstack before the UI crashes from a null task!
                }
            }
        }
    }
}

@Composable
fun LogMessageItem(message: JournalMessage, borderColor: Color) {
    JournalLogCardSimple(
        text = message.content,
        timestamp = message.timestamp.toTimeString(),
        borderColor = borderColor,
    )
}

@Composable
fun JournalInputBar(
    value: String,
    hintText: String = "Log progress",
    onValueChange: (String) -> Unit,
    onSend: (String) -> Unit,
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
                            text = hintText,
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

@Composable
fun StatusChip(
    label: String,
    icon: ImageVector,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) color.copy(alpha = 0.15f) else SurfaceDarkElevated)
            .border(
                width = 1.dp,
                color = if (isSelected) color else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) color else Color.Gray,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            color = if (isSelected) color else Color.LightGray,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}