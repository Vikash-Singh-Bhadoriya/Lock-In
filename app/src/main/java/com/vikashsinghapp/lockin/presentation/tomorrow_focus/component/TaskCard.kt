package com.vikashsinghapp.lockin.presentation.tomorrow_focus.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vikashsinghapp.lockin.formatTime
import com.vikashsinghapp.lockin.ui.theme.Error
import com.vikashsinghapp.lockin.ui.theme.SurfaceDarkElevated

@Composable
fun TaskCard(
    modifier: Modifier = Modifier,
    title: String,
    category: String,
    categoryColor: Color,
    startTime: java.time.LocalTime,
    endTime: java.time.LocalTime,
    isLocked: Boolean = false, // Specific for TomorrowFocus screen
    onCardClick: () -> Unit,
    onDuplicateClick: () -> Unit,
    onDeleteClick: () -> Unit,
    dragModifier: Modifier = Modifier, // Pass the reorder modifier here
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceDarkElevated)
            .drawBehind {
                drawRect(
                    color = categoryColor,
                    size = Size(4.dp.toPx(), size.height)
                )
            }
            .padding(start = 12.dp, end = 8.dp, top = 16.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isLocked) {
            // LOCKED UI
            Column(
                modifier = Modifier
                    .padding(start = 6.dp)
                    .weight(1f)
            ) {
                Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text(
                    "${startTime.formatTime()} - ${endTime.formatTime()}",
                    color = Color.Gray,
                    fontSize = 13.sp
                )
            }
        } else {
            // --- EDITABLE UI ---
            Icon(
                Icons.Default.DragHandle,
                contentDescription = "Reorder",
                tint = Color.Gray,
                modifier = dragModifier // This catches the drag gestures!
                    .padding(end = 12.dp)
                    .size(32.dp)
                    .padding(4.dp)

//                        modifier = Modifier
//                        .padding(end = 12.dp)
//                    .size(32.dp) // Make the physical touch target bigger
//                    .draggableHandle(
//                        // Immediate drag on touch (no long-press needed for dedicated handles)
//                        dragGestureDetector = DragGestureDetector.Press
//                    ) // --- Attach drag listener here ---
//                    .padding(4.dp) // Visual padding so the icon doesn't stretch huge

            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onCardClick() }
            ) {
                Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${startTime.formatTime()} - ${endTime.formatTime()}",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                    if (category.isNotBlank()) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "• $category",
                            color = categoryColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Box {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = Color.Gray)
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    containerColor = SurfaceDarkElevated
                ) {
                    DropdownMenuItem(
                        text = { Text("Duplicate", color = Color.White) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = null,
                                tint = Color.White
                            )
                        },
                        onClick = { onDuplicateClick(); showMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = Error) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = Error
                            )
                        },
                        onClick = {
                            onDeleteClick()
                            showMenu = false
                        }
                    )
                }
            }
        }
    }
}