package com.vikashsinghapp.lockin.presentation.task_detail

import CategorySelectionAndManageSheet
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vikashsinghapp.lockin.data.entity.CategoryEntity
import com.vikashsinghapp.lockin.presentation.tomorrow_focus.component.TimeField
import com.vikashsinghapp.lockin.ui.theme.BackgroundDark
import com.vikashsinghapp.lockin.ui.theme.Running
import com.vikashsinghapp.lockin.ui.theme.SurfaceDarkElevated
import java.time.LocalTime

@Composable
fun EditTimeWindowDialog(
    initialStart: LocalTime,
    initialEnd: LocalTime,
    onDismiss: () -> Unit,
    onSave: (LocalTime, LocalTime) -> Unit
) {
    var tempStart by remember { mutableStateOf(initialStart) }
    var tempEnd by remember { mutableStateOf(initialEnd) }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDarkElevated,
        title = { Text("Edit Time Window", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Start", color = Color.LightGray)
                    TimeField(
                        modifier = Modifier.width(100.dp),
                        time = tempStart,
                        onTimeChange = {
                            // Bonus UX: Auto-shift the end time to maintain duration!
                            val duration = java.time.Duration.between(tempStart, tempEnd)
                            tempStart = it
                            tempEnd = it.plus(duration)
                        },
                        enable = true,
                        bgColor = BackgroundDark,
                        borderColor = Color.DarkGray
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("End", color = Color.LightGray)
                    TimeField(
                        modifier = Modifier.width(100.dp),
                        time = tempEnd,
                        onTimeChange = { tempEnd = it },
                        enable = true,
                        bgColor = BackgroundDark,
                        borderColor = Color.DarkGray
                    )
                }
            }
        },
        confirmButton = {
            androidx.compose.material3.Button(
                onClick = { onSave(tempStart, tempEnd) },
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Running)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDetailsDialog(
    initialTitle: String,
    initialCategory: String,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit,
    onAddCategory: (CategoryEntity) -> Unit,
    onEditCategory: (CategoryEntity, CategoryEntity) -> Unit,
    onDeleteCategory: (CategoryEntity) -> Unit
) {
    var tempTitle by remember { mutableStateOf(initialTitle) }
    var tempCategory by remember { mutableStateOf(initialCategory) }
    var showCategorySheet by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDarkElevated,
        title = { Text("Edit Task Details", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Title Input
                OutlinedTextField(
                    value = tempTitle,
                    onValueChange = { tempTitle = it },
                    label = { Text("Task Name", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Running,
                        unfocusedBorderColor = Color.DarkGray
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    })
                )

                // 2. Dynamic Category Row
                val matchedCategory = categories.find { it.name.equals(tempCategory, ignoreCase = true) }
                val displayColor = matchedCategory?.let { Color(it.colorValue) } ?: Color.Gray

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (tempCategory.isBlank()) Running.copy(alpha = 0.15f) else displayColor.copy(alpha = 0.15f)
                        )
                        .clickable {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            showCategorySheet = true
                        }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Category", color = Color.LightGray, fontSize = 15.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (tempCategory.isNotBlank()) tempCategory.uppercase() else "SELECT",
                            color = if (tempCategory.isNotBlank()) displayColor else Running,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = if (tempCategory.isNotBlank()) displayColor else Running,
                        )
                    }
                }
            }
        },
        confirmButton = {
            androidx.compose.material3.Button(
                onClick = { onSave(tempTitle, tempCategory) },
                enabled = tempTitle.isNotBlank(), // Prevent saving empty titles
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = Running,
                    disabledContainerColor = Color.DarkGray
                )
            ) {
                Text("Save", color = if (tempTitle.isNotBlank()) Color.White else Color.Gray)
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )

    // Mount the Unified Category Sheet OVER the AlertDialog
    if (showCategorySheet) {
        CategorySelectionAndManageSheet(
            categories = categories,
            currentCategory = tempCategory,
            onDismiss = { showCategorySheet = false },
            onSelect = {
                tempCategory = it
                showCategorySheet = false
            },
            onAdd = onAddCategory,
            onEdit = onEditCategory,
            onDelete = onDeleteCategory
        )
    }
}