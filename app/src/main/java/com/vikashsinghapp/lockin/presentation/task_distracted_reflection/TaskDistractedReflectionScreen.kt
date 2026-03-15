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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.vikashsinghapp.lockin.data.entity.TaskDistractedOptions
import com.vikashsinghapp.lockin.presentation.task_status.NoteInputArea
import com.vikashsinghapp.lockin.presentation.tomorrow_focus.component.TimeField
import com.vikashsinghapp.lockin.ui.theme.BackgroundDark
import com.vikashsinghapp.lockin.ui.theme.Broken
import com.vikashsinghapp.lockin.ui.theme.Running
import com.vikashsinghapp.lockin.ui.theme.SurfaceDarkElevated

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDistractedReflectionScreen(
    @Suppress("unused") taskId: Long,
    onBlockEnd: () -> Unit,
    onBlockResume: () -> Unit,
    viewModel: TaskDistractReflectionViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = BackgroundDark,
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(scrollState)
                .imePadding() // Essential for keeping input visible above keyboard
        ) {
            // --- Header Section ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "What Happened?",
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = viewModel.title, // Task Title from VM
                        style = TextStyle(
                            color = Color.Gray,
                            fontSize = 18.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- Status Buttons ---
            // Filter out NONE so we don't show a button for it
            val statusOptions = TaskDistractedOptions.entries.filter { it != TaskDistractedOptions.NONE }

            statusOptions.forEach { status ->
                StatusSelectionButton(
                    status = status,
                    isSelected = viewModel.selectedStatus == status,
                    selectedColor = status.color,
                    onSelect = {
                        viewModel.onEvent(TaskDistractReflectionEvent.TaskStatusChanged(it))
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

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

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "When did you actually stop?",
                style = TextStyle(color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Align it to the start so it doesn't stretch across the whole screen
            Row {
                TimeField(
                    modifier = Modifier.weight(0.5f), // Takes up half the row so it looks proportional
                    time = viewModel.actualEndTime,
                    onTimeChange = { viewModel.onEvent(TaskDistractReflectionEvent.ActualEndTimeChanged(it)) },
                    enable = true
                )
                Spacer(modifier = Modifier.weight(0.5f))
            }

            // Fill space to push button to bottom (optional, depends on preference)
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(24.dp))

            // --- Submit Button ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        viewModel.onEvent(TaskDistractReflectionEvent.EndThisBlock)
                        onBlockEnd()
                    },
                    enabled = viewModel.selectedStatus != TaskDistractedOptions.NONE && viewModel.note.isNotEmpty(),
                    modifier = Modifier.weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Broken,
                        contentColor = White,
                        disabledContainerColor = SurfaceDarkElevated,
                        disabledContentColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "End Block",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
                Button(
                    onClick = {
                        viewModel.onEvent(TaskDistractReflectionEvent.ResumeThisBlock)
                        onBlockResume()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Running,
                        contentColor = White,
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Resume Block",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
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
    onSelect: (TaskDistractedOptions) -> Unit
) {
    // Logic: If selected, use Orange. If not, use SurfaceDarkElevated.
    val backgroundColor = if (isSelected) selectedColor else SurfaceDarkElevated
    val textColor = if (isSelected) Color.White else Color.Gray
    val fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable { onSelect(status) }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = status.label,
            style = TextStyle(
                color = textColor,
                fontSize = 15.sp,
                fontWeight = fontWeight
            )
        )
    }
}