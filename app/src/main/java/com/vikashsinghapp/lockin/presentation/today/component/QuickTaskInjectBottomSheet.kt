package com.vikashsinghapp.lockin.presentation.today.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vikashsinghapp.lockin.data.entity.CategoryEntity
import com.vikashsinghapp.lockin.presentation.tomorrow_focus.component.TimeField
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickTaskInjectBottomSheet(
    currentTime: LocalTime,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (title: String, start: LocalTime, end: LocalTime, category: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf(currentTime) }
    // Default to a 1-hour block
    var endTime by remember { mutableStateOf(currentTime.plusHours(1)) }
    var selectedCategory by remember { mutableStateOf("Uncategorized") }

    ModalBottomSheet(
        modifier = Modifier.fillMaxHeight(0.8f),
        sheetGesturesEnabled = false,
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF121212), // Match your BackgroundDark
        properties = ModalBottomSheetProperties()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text("Inject Quick Task", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)

            // 1. Title Input
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("What are you locking in on?", color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true
            )

            // 2. Category Chips (Looks clean because it's only one task!)
            Text("Category", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                // Add default Uncategorized option
                item {
                    val isSelected = "Uncategorized" == selectedCategory
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Color.White else Color(0xFF1E1E1E))
                            .clickable { selectedCategory = "Uncategorized" }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Uncategorized",
                            color = if (isSelected) Color.Black else Color.White,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                // Iterate over database categories
                items(categories) { cat ->
                    val isSelected = cat.name == selectedCategory
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Color.White else Color(0xFF1E1E1E))
                            .clickable { selectedCategory = cat.name }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = cat.name,
                            color = if (isSelected) Color.Black else Color.White,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            // 3. Time Pickers
            Text("Time Block", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TimeField(
                    modifier = Modifier.weight(1f),
                    time = startTime,
                    onTimeChange = { startTime = it },
                    enable = true
                )
                Text("to", color = Color.Gray)
                TimeField(
                    modifier = Modifier.weight(1f),
                    time = endTime,
                    onTimeChange = { endTime = it },
                    enable = true
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 4. Save Button
            Button(
                onClick = {
                    if (title.isNotBlank() && startTime.isBefore(endTime)) {
                        onSave(title, startTime, endTime, selectedCategory)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D5BFF)),
                enabled = title.isNotBlank()
            ) {
                Text("Add Task", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}