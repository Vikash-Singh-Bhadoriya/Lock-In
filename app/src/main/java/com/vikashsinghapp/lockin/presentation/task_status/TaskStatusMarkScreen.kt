package com.vikashsinghapp.lockin.presentation.task_status

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.vikashsinghapp.lockin.ui.theme.BackgroundDark
import com.vikashsinghapp.lockin.ui.theme.StatusOrange
import com.vikashsinghapp.lockin.ui.theme.SurfaceDarkElevated

@Composable
fun TaskStatusMarkScreen(
    @Suppress("unused") taskId: Long,
    onNavigateBack: () -> Unit,
    viewModel: TaskStatusMarkViewModel = hiltViewModel()
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
                        text = "How did this block actually go?",
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
                            fontSize = 14.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- Status Buttons ---
            // Filter out NONE so we don't show a button for it
            val statusOptions = TaskEndStatus.entries.filter { it != TaskEndStatus.NONE }

            statusOptions.forEach { status ->
                StatusSelectionButton(
                    status = status,
                    isSelected = viewModel.selectedStatus == status,
                    selectedColor = status.color,
                    onSelect = {
                        viewModel.onEvent(TaskStatusEvent.TaskStatusChanged(it))
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- Note Input ---
            NoteInputArea(
                text = viewModel.note,
                onValueChange = {
                    viewModel.onEvent(TaskStatusEvent.NoteChanged(it))
                }
            )

            // Fill space to push button to bottom (optional, depends on preference)
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(24.dp))

            // --- Submit Button ---
            Button(
                onClick = {
                    viewModel.onEvent(TaskStatusEvent.TaskSubmit)
                    onNavigateBack()
                },
                enabled = viewModel.selectedStatus != TaskEndStatus.NONE,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black,
                    disabledContainerColor = SurfaceDarkElevated,
                    disabledContentColor = Color.Gray
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Submit",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}

@Composable
fun StatusSelectionButton(
    status: TaskEndStatus,
    isSelected: Boolean,
    selectedColor: Color = StatusOrange,
    onSelect: (TaskEndStatus) -> Unit
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

@Composable
fun NoteInputArea(
    modifier: Modifier = Modifier,
    text: String,
    hint: String = "What went well, what didn't, what could be improved?",
       textStyle: TextStyle = TextStyle(),

    onValueChange: (String) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp) // Minimum height for the text area
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceDarkElevated) // Matches your theme
            .padding(16.dp)
    ) {
        // Using BasicTextField for complete control over the "block" look
        BasicTextField(
            value = text,
            onValueChange = onValueChange,
            textStyle = TextStyle(
                color = Color.White,
                fontSize = 15.sp,
                lineHeight = 22.sp
            ),
            cursorBrush = SolidColor(Color.White),
        )

        if (text.isEmpty()) {
            Text(text = hint, style = textStyle, color = Color.Gray)
        }
    }
}