package com.vikashsinghapp.lockin.presentation.today.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.vikashsinghapp.lockin.data.entity.CategoryEntity
import com.vikashsinghapp.lockin.data.entity.PromiseTask
import com.vikashsinghapp.lockin.data.entity.TaskEndStatus
import com.vikashsinghapp.lockin.formatTime
import com.vikashsinghapp.lockin.system.service.toMs
import com.vikashsinghapp.lockin.ui.theme.Broken
import com.vikashsinghapp.lockin.ui.theme.Completed
import com.vikashsinghapp.lockin.ui.theme.Error
import com.vikashsinghapp.lockin.ui.theme.Running
import com.vikashsinghapp.lockin.ui.theme.Unfinished
import java.time.LocalDate
import java.time.LocalTime

private const val HOUR_HEIGHT = 90 // dp per hour. Increase for more space.

private data class VisualTask(val task: PromiseTask, val startMins: Float, val endMins: Float)
private data class TaskLayoutInfo(val colIndex: Int, val totalCols: Int)

@Composable
fun HourBasedTimeline(
    tasks: List<PromiseTask>,
    categories: List<CategoryEntity>,
    now: LocalTime,
    selectedDate: LocalDate,
    onTaskClick: (PromiseTask) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val totalHeight = (24 * HOUR_HEIGHT).dp
    val density = LocalDensity.current

    // Auto-scroll to current time on first load, perfectly centered.
    LaunchedEffect(Unit) {
        if (selectedDate == LocalDate.now()) {
            // 1. Calculate the exact minutes passed to match the red line
            val minutesPassed = now.hour * 60 + now.minute
            val exactTimeDp = (minutesPassed * HOUR_HEIGHT / 60f).dp

            // 2. Subtract a buffer so the red line isn't touching the absolute top of the screen
            val targetDp = exactTimeDp - 100.dp

            // 3. Convert DP directly to Pixels for the scroll state
            val scrollTargetPx = with(density) { targetDp.toPx().coerceAtLeast(0f).toInt() }

            // 4. Smooth scroll to the line
            scrollState.scrollTo(scrollTargetPx)
        }
    }

    val layoutMap = remember(tasks) {
        val map = mutableMapOf<Long, TaskLayoutInfo>()

        // 1. Map to visual bounds (Forcing minimum 10 visual minutes to prevent 0-height collapses)
        val visualTasks = tasks.map { task ->
            val drawStartTime = task.actualStartTime ?: task.startTime
            val drawEndTime = task.actualEndTime ?: task.endTimePlan
            val startMins = drawStartTime.hour * 60f + drawStartTime.minute
            val durationMins =
                java.time.Duration.between(drawStartTime, drawEndTime).toMinutes().toFloat()

            // The crucial step: Forcing the minimum visual size
            val visualDuration = maxOf(10f, durationMins) // 10 mins visually equals 15dp
            VisualTask(task, startMins, startMins + visualDuration)
        }.sortedBy { it.startMins }

        // 2. Group VISUALLY overlapping task into each Cluster
        val clusters = mutableListOf<MutableList<VisualTask>>()
        for (vt in visualTasks) {
            val lastCluster = clusters.lastOrNull()

            // Does this task start BEFORE the last task in the cluster visually ends?
            if (lastCluster != null && vt.startMins < lastCluster.maxOf { it.endMins }) {
                // Yes! It's a traffic jam. Add it to the cluster.
                lastCluster.add(vt)
            } else {
                // No! It's clear. Start a new cluster.
                clusters.add(mutableListOf(vt))
            }
        }

        // 3. Assign columns within each cluster
        for (cluster in clusters) {
            val columns = mutableListOf<MutableList<VisualTask>>()
            for (vt in cluster) {
                var placed = false
                for (col in columns) {
                    // Can this task fit at the bottom of an existing column?
                    if (col.last().endMins <= vt.startMins) {
                        col.add(vt)
                        placed = true
                        break
                    }
                }
                // No? We have to create a new column to the right.
                if (!placed) {
                    columns.add(mutableListOf(vt))
                }
            }

            // Map the final layout to the Task ID
            columns.forEachIndexed { colIdx, colTasks ->
                for (vt in colTasks) {
                    map[vt.task.id] = TaskLayoutInfo(colIdx, columns.size)
                }
            }
        }
        map
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

            // 2. Draw Tasks in their specific columns
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 50.dp, end = 8.dp)
            ) {
                val availableWidth = maxWidth

                tasks.forEach { task ->
                    val layoutInfo = layoutMap[task.id] ?: TaskLayoutInfo(0, 1)
                    val itemWidth = availableWidth / layoutInfo.totalCols
                    val offsetX = itemWidth * layoutInfo.colIndex

                    CalendarTaskItem(
                        task = task,
//                        now = now,
                        categories = categories,
                        // 🟢 Pass the dynamic width and X offset into the item
                        modifier = Modifier
                            .width(itemWidth)
                            .offset(x = offsetX),
                        onTaskClick = { onTaskClick(task) }
                    )
                }
            }

            if (selectedDate == LocalDate.now()) {
                // Draw a red line to indicate current time if it's after now
                CurrentTimeIndicator(now = now)
            }
        }
    }
}

@Composable
fun CalendarTaskItem(
    modifier: Modifier = Modifier,
    task: PromiseTask,
    categories: List<CategoryEntity>,
//    now: LocalTime,
    onTaskClick: () -> Unit,
) {
    val drawStartTime = task.actualStartTime ?: task.startTime
    val startMinutes = drawStartTime.hour * 60 + drawStartTime.minute
    val durationMinutes =
        java.time.Duration.between(drawStartTime, task.actualEndTime ?: task.endTimePlan)
            .toMinutes().toInt()

    val topOffset = (startMinutes * HOUR_HEIGHT / 60).dp
    // Force a minimum height of 15.dp so short tasks are always visible and clickable
    val exactHeight = (durationMinutes * HOUR_HEIGHT / 60f).dp
    val blockHeight = maxOf(15.dp, exactHeight)

    // --- Dynamic Category Colors ---
    val matchedCategory = categories.find { it.name == task.category }
    val categoryColor = matchedCategory?.let { Color(it.colorValue) } ?: Color(0xFF444444)

    // Dim past un-started tasks, otherwise use 15% opacity category color
    val isPast = task.endTimePlan.toMs(task.planDate) < System.currentTimeMillis()
    val backgroundColor = if (task.status == TaskEndStatus.PENDING && isPast) {
        Color.DarkGray.copy(alpha = 0.5f)
    } else {
        categoryColor.copy(alpha = 0.3f)
    }

//    val taskColor = when (task.status) {
//        TaskEndStatus.COMPLETED -> Completed
//        TaskEndStatus.UNFINISHED -> Unfinished
//        TaskEndStatus.BROKEN -> Broken
//        else -> if (task.isCurrent(now)) Running else NotStarted
//    }

    Box(
        modifier = modifier
//            .padding(start = 50.dp, end = 8.dp) // Leave space for hour labels
            .offset(y = topOffset)
            .height(blockHeight)
            .padding(end = 4.dp) // Slight padding so side-by-side tasks have a tiny gap
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(backgroundColor) // Soft 15% Background
            .drawBehind {
                drawRect(
                    color = if (task.status == TaskEndStatus.PENDING && isPast) Color.DarkGray else categoryColor,
                    size = Size(4.dp.toPx(), size.height) // Left Accent Line
                )
            }
            .clickable {
                onTaskClick() // Update to use the new parameter
            }
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                if (blockHeight > 25.dp) {
                    Text(
                        text = task.title,
                        color = White,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (blockHeight > 50.dp) { // Only show time if block is large enough
                    val timeText = buildAnnotatedString {

                        // 1. Always append the start time
                        append("${drawStartTime.formatTime()} – ")

                        // 2. Check if they finished at a different time than planned
                        if (task.actualEndTime != null && task.actualEndTime != task.endTimePlan) {
                            // Strike through the old planned time (e.g., ~~10:00 PM~~)
                            withStyle(
                                style = SpanStyle(
                                    textDecoration = TextDecoration.LineThrough,
                                    color = Color.Gray
                                )
                            ) {
                                append(task.endTimePlan.formatTime())
                            }

                            // Boldly display the ACTUAL time they finished
                            withStyle(
                                style = SpanStyle(
                                    color = Running,
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append(" ${task.actualEndTime.formatTime()}")
                            }
                        } else {
                            // 3. Normal scenario: just show the planned end time
                            append(task.endTimePlan.formatTime())
                        }
                    }
                    Text(
                        text = timeText,
                        color = White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            // --- The Status Icons ---

            // ---  The Status Icons with Custom White Backings ---
            when (task.status) {
                TaskEndStatus.COMPLETED -> {
                    Box(contentAlignment = Alignment.Center) {
                        // A slightly smaller white circle that hides perfectly behind the green edges
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(Color.White, CircleShape)
                        )
                        Icon(
                            Icons.Default.CheckCircle,
                            "Done",
                            tint = Completed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                TaskEndStatus.BROKEN -> {
                    Box(contentAlignment = Alignment.Center) {
                        // A slightly smaller white circle that hides perfectly behind the red edges
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(Color.White, CircleShape)
                        )
                        Icon(
                            Icons.Default.Cancel,
                            "Broken",
                            tint = Broken,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                TaskEndStatus.UNFINISHED -> {
                    Box(contentAlignment = Alignment.Center) {
                        // Draw a custom white triangle that fits exactly behind the yellow warning icon
                        Canvas(
                            modifier = Modifier
                                .size(11.dp)
                                .offset(y = 1.dp)
                        ) {
                            val path = Path().apply {
                                moveTo(size.width / 2f, 0f) // Top center
                                lineTo(size.width, size.height) // Bottom right
                                lineTo(0f, size.height) // Bottom left
                                close()
                            }
                            drawPath(path, Color.White)
                        }
                        Icon(
                            Icons.Default.Warning,
                            "Unfinished",
                            tint = Unfinished,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                TaskEndStatus.PENDING -> { /* No icon if pending */
                }
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
            .background(Error)
    )
}