package com.vikashsinghapp.lockin.presentation.task_distracted_reflection

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.vikashsinghapp.lockin.data.entity.TaskDistractedOptions
import com.vikashsinghapp.lockin.formatTime
import com.vikashsinghapp.lockin.presentation.task_status.NoteInputArea
import com.vikashsinghapp.lockin.presentation.tomorrow_focus.component.TimePickerInputDialog
import com.vikashsinghapp.lockin.ui.theme.BackgroundDark
import com.vikashsinghapp.lockin.ui.theme.Broken
import com.vikashsinghapp.lockin.ui.theme.Running
import com.vikashsinghapp.lockin.ui.theme.SurfaceDarkElevated
import com.vikashsinghapp.lockin.ui.theme.spacing
import kotlinx.coroutines.launch

// When display over other apps (disable from app settings) => still Break screen show on when break button click while using other app
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDistractedReflectionScreen(
    @Suppress("unused") taskId: Long,
    mode: ReflectionMode,
    onBlockEnd: () -> Unit,
    onBlockResume: () -> Unit,
    viewModel: TaskDistractReflectionViewModel = hiltViewModel(),
) {
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val isPreStartDelay = mode != ReflectionMode.MID_TASK_BREAK

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is TaskReflectionUiEvent.EndSuccess -> onBlockEnd()
                is TaskReflectionUiEvent.ResumeSuccess -> onBlockResume()
                is TaskReflectionUiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        containerColor = BackgroundDark,
        modifier = Modifier.fillMaxSize(),
        snackbarHost = {
            // reuse default SnackbarHost to have default animation and timing handling
            SnackbarHost(snackbarHostState) { data ->
                // custom snackbar with the custom colors
                Snackbar(
                    actionColor = MaterialTheme.colorScheme.onBackground,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    snackbarData = data,
                    dismissActionContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = MaterialTheme.spacing.extraLarge, vertical = MaterialTheme.spacing.large)
                .verticalScroll(scrollState)
                .imePadding() // Essential for keeping input visible above keyboard
        ) {
            // --- Header Section ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {

                val headerText = rememberSaveable {
                    when (mode) {
                        ReflectionMode.NUDGE_DELAY -> listOf(
                            "Why are you delaying your commitment? 🔒",
                            "Cold feet already? 🥶",
                            "Procrastination kicking in? 🛋️",
                            "Why are we pushing this back? ⏳",
                            "Delaying the inevitable? 🕰️"
                        ).random()

                        ReflectionMode.ROLL_CALL_DELAY -> listOf(
                            "Dodging the roll call? 🫣",
                            "Failure to launch? 🚀📉",
                            "Running away from the start line? 🏃‍♂️💨",
                            "The clock is ticking. Why the delay? ⏱️"
                        ).random()

                        ReflectionMode.MID_TASK_BREAK -> listOf(
                            "Giving up already? 🤡",
                            "Letting the dopamine win? 📱",
                            "Is that all you got? 🤨",
                            "Quitting so soon? 🐢",
                            "Lost focus? 🫠",
                            "Focus slipping? 🪫",
                            "Breaking the lock? 🔓"
                        ).random()
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = headerText, // Random header for a bit of fun
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraSmall))
                    Text(
                        text = "Task: " + viewModel.title, // Task Title from VM
                                       color = Color.Gray,
                        style = MaterialTheme.typography.titleMedium

                    )
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.massive))

            // --- Status Buttons ---
            // Filter out NONE so we don't show a button for it
            val statusOptions =
                TaskDistractedOptions.entries.filter { it != TaskDistractedOptions.NONE }

            statusOptions.forEach { status ->
                StatusSelectionButton(
                    status = status,
                    isSelected = viewModel.selectedStatus == status,
                    selectedColor = status.color,
                    onSelect = {
                        viewModel.onEvent(TaskDistractReflectionEvent.TaskStatusChanged(it))
                    }
                )
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraLarge))

            // --- Note Input ---
            NoteInputArea(
                text = viewModel.note,
                onValueChange = {
                    viewModel.onEvent(TaskDistractReflectionEvent.NoteChanged(it))
                },
                hint = when (viewModel.selectedStatus) {
                    TaskDistractedOptions.TEMPORARY_DISTRACTION ->
                        "Describe the temporary distraction. What pulled your attention away? (Mandatory)"

                    TaskDistractedOptions.MENTAL_FATIGUE ->
                        "How did mental fatigue affect your focus? What signs did you notice? (Mandatory)"

                    TaskDistractedOptions.EXTERNAL_INTERRUPTION ->
                        "What was the external interruption? How did it impact your task? (Mandatory)"

                    TaskDistractedOptions.EMOTIONAL_RESISTANCE ->
                        "What emotional resistance did you feel? How did it influence your actions? (Mandatory)"

                    else -> "Reflect briefly (Mandatory)"
                }
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.huge))

            var isDialogVisible by rememberSaveable { mutableStateOf(false) }
            // Dialog should not be used inside Scaffold, Column => apply paddding etc => size change
            if (isDialogVisible) {
                TimePickerInputDialog(
                    time = if (isPreStartDelay) viewModel.actualStartTime else viewModel.actualEndTime,
                    onTimeChange = {
                        viewModel.onEvent(
                            if (isPreStartDelay) TaskDistractReflectionEvent.ActualStartTimeChanged(
                                it
                            )
                            else TaskDistractReflectionEvent.ActualEndTimeChanged(it)
                        )
                    },
                    hideTimeDialog = {
                        isDialogVisible = false
                    },
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceDarkElevated) // Use your premium dark grey background here
                    .clickable {
                        // Handle click to open time picker dialog
                        isDialogVisible = true
                    }
                    .padding(horizontal = MaterialTheme.spacing.extraLarge, vertical = MaterialTheme.spacing.extraLarge),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isPreStartDelay) "When will you start?" else "When did you stop?",
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall
                )

                // The interactive time part
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                ) {
                    Text(
                        text = if (isPreStartDelay) viewModel.actualStartTime.formatTime()
                        else viewModel.actualEndTime.formatTime(),
                        color = Running,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Icon(
                        imageVector = Icons.Default.Schedule, // Or whatever clock icon you use
                        contentDescription = "Edit Time",
                        tint = Running,
                        modifier = Modifier.size(MaterialTheme.spacing.iconSizeMedium)
                    )
                }
            }
//            Text(
//                text = "When did you actually stop?",
//                style = TextStyle(color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
//            )
//            Spacer(modifier = Modifier.height(8.dp))
//
//            // Align it to the start so it doesn't stretch across the whole screen
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.Center,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                TimeField(
//                    modifier = Modifier.weight(0.5f), // Takes up half the row so it looks proportional
//                    bgColor = SurfaceDarkElevated,
//                    borderColor = SurfaceDarkElevated,
//                    time = viewModel.actualEndTime,
//                    onTimeChange = { viewModel.onEvent(TaskDistractReflectionEvent.ActualEndTimeChanged(it)) },
//                    enable = true
//                )
//                Spacer(modifier = Modifier.weight(0.5f))
//            }

            // Fill space to push button to bottom (optional, depends on preference)
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.huge))

            // --- 1. Calculate if the form is completely filled ---
            val isReady =
                viewModel.selectedStatus != TaskDistractedOptions.NONE && viewModel.note.trim()
                    .isNotEmpty()

            // --- Submit Button ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        if (!isReady) {
                            // If they click while it's not ready, tell them exactly why!
                            coroutineScope.launch {
                                val message =
                                    if (viewModel.selectedStatus == TaskDistractedOptions.NONE) {
                                        "Please select a reason first"
                                    } else if (isPreStartDelay)
                                        "Journaling your reason is mandatory to reschedule task"
                                    else
                                        "Journaling your reason is mandatory to break task"
                                snackbarHostState.showSnackbar(message)
                            }
                        } else {
                            // Fire the correct event based on context!
                            if (isPreStartDelay) {
                                viewModel.onEvent(TaskDistractReflectionEvent.RescheduleThisBlock)
                            } else {
                                viewModel.onEvent(TaskDistractReflectionEvent.EndThisBlock)
                            }
                        }
                    },
                    modifier = if (isPreStartDelay) Modifier.fillMaxWidth() else Modifier
                        .weight(1f)
                        .height(MaterialTheme.spacing.buttonHeight),
                    colors = ButtonDefaults.buttonColors(
                        // Faint red background when ready, standard dark grey when disabled
                        containerColor = if (isReady) Broken.copy(alpha = 0.15f) else SurfaceDarkElevated,
                        contentColor = if (isReady) Broken else Color.Gray,
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isPreStartDelay) "Reschedule Task" else "End Task",
                        style = MaterialTheme.typography.titleSmall
                    )
                }

                // Only show Resume if it's an active mid-task break
                if (!isPreStartDelay) {
                    Button(
                        onClick = {
                            viewModel.onEvent(TaskDistractReflectionEvent.ResumeThisBlock)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(MaterialTheme.spacing.buttonHeight),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Running,
                            contentColor = White,
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Resume Task",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusSelectionButton(
    status: TaskDistractedOptions,
    isSelected: Boolean,
    selectedColor: Color,
    onSelect: (TaskDistractedOptions) -> Unit,
) {
    // Logic: If selected, use Orange. If not, use SurfaceDarkElevated.
    val backgroundColor = if (isSelected) selectedColor else SurfaceDarkElevated
    val textColor = if (isSelected) Color.White else Color.Gray
    val fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(MaterialTheme.spacing.buttonHeight)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable { onSelect(status) }
            .padding(horizontal = MaterialTheme.spacing.large),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = status.label,
            color = textColor,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
        )
    }
}