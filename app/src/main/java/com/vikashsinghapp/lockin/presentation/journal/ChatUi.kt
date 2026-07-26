package com.vikashsinghapp.lockin.presentation.journal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.vikashsinghapp.lockin.ui.theme.Broken
import com.vikashsinghapp.lockin.ui.theme.Completed
import com.vikashsinghapp.lockin.ui.theme.Running
import com.vikashsinghapp.lockin.ui.theme.SurfaceDarkElevated
import com.vikashsinghapp.lockin.ui.theme.Unfinished
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// --- Status border colors ---
private val BorderGeneral = Color(0xFF444444) // Muted gray for logs with no linked task
private val BorderPending = Running           // Running Blue for pending/running tasks
private val BorderCompleted = Completed
private val BorderUnfinished = Unfinished
private val BorderBroken = Broken

// --- Muted label color for task titles ---
private val TaskLabelColor = Color(0xFF8696A0)

sealed interface ChatRow {
    data class DateHeader(val label: String) : ChatRow
    data class Message(val item: JournalMessageWithTask) : ChatRow
}

fun buildChatRows(messages: List<JournalMessageWithTask>): List<ChatRow> {
    if (messages.isEmpty()) return emptyList()
    val today = LocalDate.now()
    val yesterday = today.minusDays(1)
    val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.getDefault())
    val rows = ArrayList<ChatRow>(messages.size + 4)
    var lastDate: LocalDate? = null

    messages.forEach { item ->
        val messageDate = Instant.ofEpochMilli(item.message.timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        if (messageDate != lastDate) {
            val label = when (messageDate) {
                today -> "Today"
                yesterday -> "Yesterday"
                else -> messageDate.format(formatter)
            }
            rows.add(ChatRow.DateHeader(label))
            lastDate = messageDate
        }
        rows.add(ChatRow.Message(item))
    }
    return rows
}

/**
 * Resolves the 2px left-border color for a journal log card.
 * [taskStatus] is the raw String from Room (e.g. "BROKEN", "COMPLETED") or null for general logs.
 */
fun statusBorderColor(taskStatus: String?): Color = when (taskStatus) {
    "BROKEN" -> BorderBroken
    "COMPLETED" -> BorderCompleted
    "UNFINISHED" -> BorderUnfinished
    "PENDING" -> BorderPending
    else -> BorderGeneral
}

@Composable
fun ChatDateHeader(label: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = Color(0xFF8696A0),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(SurfaceDarkElevated)
                .padding(horizontal = 12.dp, vertical = 4.dp),
        )
    }
}

/**
 * Full-width log card with a 2px left border indicating task status.
 * Replaces the old WhatsApp-style chat bubble.
 */
@Composable
fun JournalLogCard(
    text: String,
    timestamp: String,
    taskTitle: String? = null,
    taskStatus: String? = null,
    modifier: Modifier = Modifier,
) {
    val borderColor = statusBorderColor(taskStatus)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 3.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // 2px colored left border strip — spans full card height
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight()
                    .background(borderColor)
            )

            // Card content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDarkElevated)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                if (!taskTitle.isNullOrBlank()) {
                    Text(
                        text = taskTitle,
                        color = TaskLabelColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(2.dp))
                }
                Text(
                    text = text,
                    color = Color(0xFFE9EDEF),
                    fontSize = 15.sp,
                    lineHeight = 20.sp,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = timestamp,
                    color = Color(0xFF666666),
                    fontSize = 11.sp,
                    modifier = Modifier.align(Alignment.End),
                )
            }
        }
    }
}

/**
 * Simpler log card for screens that only have JournalMessage (no task info),
 * such as TaskDetail and ActiveFocus which show task-scoped journals.
 * Uses [borderColor] directly since the task status is known from context.
 */
@Composable
fun JournalLogCardSimple(
    text: String,
    timestamp: String,
    borderColor: Color = BorderPending,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 3.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight()
                    .background(borderColor)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDarkElevated)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Text(
                    text = text,
                    color = Color(0xFFE9EDEF),
                    fontSize = 15.sp,
                    lineHeight = 20.sp,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = timestamp,
                    color = Color(0xFF666666),
                    fontSize = 11.sp,
                    modifier = Modifier.align(Alignment.End),
                )
            }
        }
    }
}
