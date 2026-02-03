package com.vikashsinghapp.lockin.presentation.today

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.vikashsinghapp.lockin.data.entity.PromiseTask
import com.vikashsinghapp.lockin.ui.theme.SurfaceDark
import com.vikashsinghapp.lockin.ui.theme.SurfaceDarkElevated
import com.vikashsinghapp.lockin.ui.theme.mediaQuery
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
            .padding(16.dp)
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
    Box(modifier) {
        // Timeline line
        Box(
            Modifier
                .fillMaxHeight()
                .width(2.dp)
                .background(Color(0xFF444444))
                .align(Alignment.CenterStart)
                .padding(start = 50.dp)
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 6.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(tasks) { idx, task ->
                TodayTaskItem(
                    task = task,
                    now = now,
                )
            }
        }
    }
}

@Composable
fun TodayTaskItem(task: PromiseTask, now: LocalTime) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        // Timeline dot
        Column(
            Modifier
                .width(40.dp)
                .height(IntrinsicSize.Min),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
//            if (!isFirst) Box(
//                Modifier
//                    .width(2.dp)
//                    .weight(1f)
//                    .background(Color(0xFF444444))
//            )

            Box(
                Modifier
                    .size(if (task.isCurrent(now)) 14.dp else 10.dp)
                    .background(
                        if (task.isCurrent(now)) Color(0xFF2D5BFF) else Color(0xFF888888),
                        shape = MaterialTheme.shapes.small
                    )
                    .mediaQuery(
                        task.isCurrent(now),
                        Modifier.border(3.dp, Color(
                            ColorUtils.blendARGB(
                                Color(0xFF2D5BFF).toArgb(),
                                Color.White.toArgb(),
                                0.35f
                            )
                        ), MaterialTheme.shapes.small)
                    )
            )
        }
        // Task card
        Column(
            Modifier
                .weight(1f)
                .padding(start = 8.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(
                    when {
                        task.isCurrent(now) -> Color(0xFF101C2C)
                        else -> SurfaceDarkElevated
                    }
                )
                .mediaQuery(
                    task.isCurrent(now),
                    Modifier.border(1.dp, Color.Blue, MaterialTheme.shapes.medium)
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
                text = "${task.startTime} – ${task.endTime}",
                color = Color(0xFFB0B0B0),
                style = MaterialTheme.typography.bodySmall
            )
            // Category badge
//            Box(
//                Modifier
//                    .align(Alignment.TopEnd)
//                    .clip(MaterialTheme.shapes.small)
//                    .background(task.category.color)
//                    .padding(horizontal = 8.dp, vertical = 2.dp)
//            ) {
//                Text(
//                    text = task.category.label,
//                    color = Color.White,
//                    style = MaterialTheme.typography.labelSmall
//                )
//            }
        }
    }
}
