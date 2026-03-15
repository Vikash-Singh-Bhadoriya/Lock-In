package com.vikashsinghapp.lockin.presentation.journal

import android.content.ClipData
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vikashsinghapp.lockin.ui.theme.Running
import com.vikashsinghapp.lockin.ui.theme.SurfaceDarkElevated
import kotlinx.coroutines.launch


@Composable
fun MessageList(
    modifier: Modifier = Modifier,
    messages: List<JournalMessageWithTask>,
    onDeleteMessage: (JournalMessageWithTask) -> Unit,
    listState: LazyListState,
//    bringIntoViewRequester: BringIntoViewRequester
) {

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
//            .bringIntoViewRequester(bringIntoViewRequester),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        items(messages, key = { it.message.id }) { message ->
            MessageBubble(message) {
                onDeleteMessage(message)
            }
        }
    }
}

@Composable
fun MessageBubble(item: JournalMessageWithTask, onDeleteMessage: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    val clipboard = LocalClipboard.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        // 1. Check if it's tied to a real task by checking the ID
        val isTaskLog = item.message.duringPromiseTaskId != null

        if (isTaskLog) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Safely handle nullable category
                val rawCat = item.taskCategory?.trim() ?: "" // <--- Safe call ?.
                val displayCategory = if (rawCat.isBlank() || rawCat.equals("Uncategorized", ignoreCase = true)) "UNCATEGORIZED" else rawCat

                Text(
                    text = displayCategory.uppercase(),
                    color = Running,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp)
                )
                Spacer(Modifier.width(8.dp))

                // Safely handle nullable title
                val displayTitle = item.taskTitle?.trim()?.takeIf { it.isNotBlank() } ?: "Task" // <--- Safe call ?.
                Text(text = "• $displayTitle", color = Color.Gray, fontSize = 10.sp)
            }
            Spacer(Modifier.height(4.dp))
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1A1A1A))
                .combinedClickable(
                    onClick = { /* Do nothing on normal tap */ },
                    onLongClick = { showMenu = true }
                )
                .padding(14.dp)
        ) {
            Text(
                text = item.message.content,
                color = Color.White,
                fontSize = 15.sp
            )

            // The Long-Press Menu
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                containerColor = SurfaceDarkElevated
            ) {
                DropdownMenuItem(
                    text = { Text("Copy", color = Color.White) },
                    onClick = {
                        // 2. Launch a coroutine to handle the new suspend function safely
                        coroutineScope.launch {
                            val clipData =
                                ClipData.newPlainText("Journal Log", item.message.content)
                            clipboard.setClipEntry(ClipEntry(clipData))
                            Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                            showMenu = false
                        }
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete", color = Color.Red) },
                    onClick = {
                        onDeleteMessage()
                        showMenu = false
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = item.message.timestamp.toTimeString(),
            color = Color.Gray,
            fontSize = 11.sp,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}