package com.vikashsinghapp.lockin.presentation.today

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.vikashsinghapp.lockin.data.entity.PromiseTask
import com.vikashsinghapp.lockin.formatTime
import com.vikashsinghapp.lockin.ui.theme.SurfaceDark
import com.vikashsinghapp.lockin.ui.theme.SurfaceDarkElevated
import java.time.LocalTime

@Composable
fun TodayScreen(
    modifier: Modifier = Modifier,
    viewModel: TodayViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentTime by viewModel.currentTime.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceDark)
            .padding(end = 16.dp)
            .padding(start = 8.dp)
    ) {
//        Spacer(Modifier.height(8.dp))
//        Text(
//            uiState.currentTime,
//            color = Color(0xFFB0B0B0),
//            style = MaterialTheme.typography.bodyMedium
//        )
//        Spacer(Modifier.height(16.dp))

        TodayTimeline(
            tasks = uiState.tasks,
            modifier = Modifier.fillMaxSize(),
            now = currentTime
        )
    }
}

@Composable
fun TodayTimeline(tasks: List<PromiseTask>, now: LocalTime, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 16.dp),
        // cannot break the timeline line
//        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(tasks) { idx, task ->
            TodayTaskItem(
                task = task,
                now = now,
                isFirst = idx == 0,
                isLast = idx == tasks.lastIndex
            )
        }
    }
}

@Composable
fun TodayTaskItem(
    task: PromiseTask,
    now: LocalTime,
    isFirst: Boolean,
    isLast: Boolean,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(IntrinsicSize.Min) // Ensures children match tallest child
    ) {
        // Timeline column with drawBehind for line
        Box(
            Modifier
                .width(40.dp)
                .fillMaxHeight()
                .drawBehind {
                    val centerX = size.width / 2
                    val dotRadius = if (task.isCurrent(now)) 7.dp.toPx() else 5.dp.toPx()
                    val centerY = size.height / 2
                    val lineColor = Color(0xFF444444)
                    // Make the line above the circle node
                    if (!isFirst) {
                        drawLine(
                            color = lineColor,
                            start = Offset(centerX, 0f),
                            end = Offset(centerX, centerY - dotRadius),
                            strokeWidth = 6f
                        )
                    }
                    // Make the line below the circle node
                    if (!isLast) {
                        drawLine(
                            color = lineColor,
                            start = Offset(
                                centerX,
                                centerY + dotRadius
                            ),
                            end = Offset(centerX, size.height),
                            strokeWidth = 6f
                        )
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(
                        if (task.isCurrent(now)) Color(0xFF2D5BFF) else Color(0xFF888888)
                    )
            )
        }
        // Task card
        Column(
            Modifier
                .weight(1f)
                .padding(start = 8.dp)
                .padding(vertical = 12.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(
                    when {
                        task.isCurrent(now) -> Color(0xFF101C2C)
                        else -> SurfaceDarkElevated
                    }
                )
                .border(
                    width = if (task.isCurrent(now)) 1.dp else 0.dp,
                    color = if (task.isCurrent(now)) Color(0xFF2D5BFF) else Color.Transparent,
                    shape = MaterialTheme.shapes.medium
                )
                .padding(16.dp)
        ) {
            Text(
                text = task.title,
                color = if (task.isPast(now)) Color(0xFFAAAAAA) else Color.White,
                fontWeight = if (task.isCurrent(now)) FontWeight.Bold else FontWeight.Normal,
                textDecoration = if (task.isPast(now)) TextDecoration.LineThrough else TextDecoration.None
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${task.startTime.formatTime()} – ${task.endTime.formatTime()}",
                color = Color(0xFFB0B0B0),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
