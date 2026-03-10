package com.vikashsinghapp.lockin.presentation.today.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vikashsinghapp.lockin.data.entity.PromiseTask
import com.vikashsinghapp.lockin.data.entity.TaskEndStatus
import com.vikashsinghapp.lockin.formatTime
import com.vikashsinghapp.lockin.presentation.today.isCurrent
import com.vikashsinghapp.lockin.ui.theme.Broken
import com.vikashsinghapp.lockin.ui.theme.Completed
import com.vikashsinghapp.lockin.ui.theme.NotStarted
import com.vikashsinghapp.lockin.ui.theme.Running
import com.vikashsinghapp.lockin.ui.theme.Unfinished
import java.time.LocalDate
import java.time.LocalTime

private const val HOUR_HEIGHT = 90 // dp per hour. Increase for more space.

@Composable
fun HourBasedTimeline(
    tasks: List<PromiseTask>,
    now: LocalTime,
    selectedDate: LocalDate,
    onNavigateToTaskDetail: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val totalHeight = (24 * HOUR_HEIGHT).dp

    // Auto-scroll to current time on first load
    LaunchedEffect(Unit) {
        val scrollTarget = (now.hour * HOUR_HEIGHT).dp
        scrollState.scrollTo(scrollTarget.value.toInt())
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Box(
            modifier = modifier
                .height(totalHeight)
                .fillMaxWidth()
        ) {
            // 1. Draw Hour Labels and Grid Lines
            (0..23).forEach { hour ->
                HourLine(hour = hour)
            }

            // 2. Position Task Blocks
            tasks.forEach { task ->
                CalendarTaskItem(task = task, now = now, onNavigateToTaskDetail = {
                    onNavigateToTaskDetail(task.id)
                })
            }

            if (selectedDate == LocalDate.now()) {
                // Draw a red line to indicate current time if it's after now
                CurrentTimeIndicator(now = now)
            }
        }
    }
}

@Composable
fun CalendarTaskItem(task: PromiseTask, now: LocalTime, onNavigateToTaskDetail: () -> Unit) {
    val startMinutes = task.startTime.hour * 60 + task.startTime.minute
    val durationMinutes =
        java.time.Duration.between(task.startTime, task.endTime).toMinutes().toInt()

    val topOffset = (startMinutes * HOUR_HEIGHT / 60).dp
    val blockHeight = (durationMinutes * HOUR_HEIGHT / 60).dp

    val taskColor = when (task.status) {
        TaskEndStatus.COMPLETED -> Completed
        TaskEndStatus.UNFINISHED -> Unfinished
        TaskEndStatus.BROKEN -> Broken
        TaskEndStatus.NONE -> if (task.isCurrent(now)) Running else NotStarted
    }

    Box(
        modifier = Modifier
            .padding(start = 50.dp, end = 8.dp) // Leave space for hour labels
            .offset(y = topOffset)
            .height(blockHeight)
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(taskColor)
            .clickable {
                onNavigateToTaskDetail()
            }
            .padding(8.dp)
    ) {
        Column {
            Text(
                text = task.title,
                color = White,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (blockHeight > 50.dp) { // Only show time if block is large enough
                Text(
                    text = "${task.startTime.formatTime()} - ${task.endTime.formatTime()}",
                    color = White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun HourLine(hour: Int) {
    val topOffset = (hour * HOUR_HEIGHT).dp
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(HOUR_HEIGHT.dp)
            .offset(y = topOffset)
            .drawBehind {
                drawLine(
                    color = Color.Gray.copy(alpha = 0.5f),
                    start = Offset(50.dp.toPx(), 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1f
                )
            }
    ) {
        Text(
            text = if (hour == 0) "12 AM" else if (hour < 12) "$hour AM" else if (hour == 12) "12 PM" else "${hour - 12} PM",
            modifier = Modifier.padding(start = 8.dp, top = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
        )
    }
}

@Composable
fun CurrentTimeIndicator(now: LocalTime) {
    val minutesPassed = now.hour * 60 + now.minute
    val topOffset = (minutesPassed * HOUR_HEIGHT / 60).dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = topOffset)
            .height(2.dp)
            .background(Color.Red)
    )
}