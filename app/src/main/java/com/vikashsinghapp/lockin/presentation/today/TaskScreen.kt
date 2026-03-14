package com.vikashsinghapp.lockin.presentation.today

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.vikashsinghapp.lockin.data.entity.TaskEndStatus
import com.vikashsinghapp.lockin.presentation.today.component.DateCalendar
import com.vikashsinghapp.lockin.presentation.today.component.HourBasedTimeline
import com.vikashsinghapp.lockin.presentation.today.component.QuickTaskInjectBottomSheet
import com.vikashsinghapp.lockin.system.alarm.TaskAlarmScheduler
import com.vikashsinghapp.lockin.ui.theme.Running
import com.vikashsinghapp.lockin.ui.theme.SurfaceDark
import timber.log.Timber
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(
    modifier: Modifier = Modifier,
    viewModel: TaskViewModel = hiltViewModel(),
    onNavigateToTaskDetail: (Long) -> Unit,
    onOpenDrawer: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentTime by viewModel.currentTime.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    var showQuickAddSheet by remember { mutableStateOf(false) }
    val categories by viewModel.categories.collectAsState()

    // Check if ANY task is currently actively running right now
    val isAnyTaskRunning = uiState.tasks.any { task ->
        task.isCurrent(currentTime) && task.status == TaskEndStatus.PENDING
    }

    val context = LocalContext.current
    val isLocked by viewModel.isLocked.collectAsState()

    // Only show FAB if looking at TODAY, no task is running, AND the plan is locked
    val showFab = !isAnyTaskRunning && selectedDate == LocalDate.now() && isLocked

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Task", color = Color.White, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
            )
        },
        floatingActionButton = {
            if (showFab) {
                FloatingActionButton(
                    onClick = { showQuickAddSheet = true },
                    containerColor = Running,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Inject Task")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(SurfaceDark)
                .padding(end = 16.dp, start = 8.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DateCalendar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                isCalendarVisible = uiState.isDateCalendarVisible,
                onShowCalendar = { viewModel.onEvent(TaskScreenEvent.ShowDateCalendar) },
                onHideCalendar = { viewModel.onEvent(TaskScreenEvent.HideDateCalendar) },
                selectedDate = selectedDate,
                onSelectedDateChange = {
                    viewModel.onEvent(TaskScreenEvent.OnDateSelected(it))
                }
            )
            HourBasedTimeline(
                tasks = uiState.tasks,
                now = currentTime,
                modifier = Modifier.weight(1f),
                selectedDate = selectedDate,
                onNavigateToTaskDetail = onNavigateToTaskDetail
            )
        }
    }
    // LaunchedEffect will be called on first composition
    // & when navigate between screens
    // & will not be called on pause screen
    LaunchedEffect(key1 = true) {
        Timber.d("LaunchedEffect(key1 = true) called")
        viewModel.eventFlow.collect { uiEvent ->
            when (uiEvent) {
                is TaskScreenViewModelUiEvent.SchedulePlanTaskAlarm -> {
                        TaskAlarmScheduler.scheduleTaskStart(context, uiEvent.task)
                }
            }
        }
    }
    if (showQuickAddSheet) {
        QuickTaskInjectBottomSheet(
            currentTime = currentTime,
            categories = categories,
            onDismiss = { showQuickAddSheet = false },
            onSave = { title, start, end, category ->
                // Fire an event to your TaskViewModel to save this new PromiseTask
                 viewModel.onEvent(TaskScreenEvent.InjectTask(title, start, end, category))
                showQuickAddSheet = false
            }
        )
    }
}

//@Composable
//fun TodayTimeline(tasks: List<PromiseTask>, dateCalendar: @Composable () -> Unit, now: LocalTime, modifier: Modifier = Modifier) {
//    LazyColumn(
//        modifier = modifier,
//        contentPadding = PaddingValues(bottom = 16.dp),
//        // cannot break the timeline line
////        verticalArrangement = Arrangement.spacedBy(16.dp)
//    ) {
//        item {
//            dateCalendar()
//        }
//        itemsIndexed(tasks) { idx, task ->
//            TodayTaskItem(
//                task = task,
//                now = now,
//                isFirst = idx == 0,
//                isLast = idx == tasks.lastIndex
//            )
//        }
//    }
//}
//
//@Composable
//fun TodayTaskItem(
//    task: PromiseTask,
//    now: LocalTime,
//    isFirst: Boolean,
//    isLast: Boolean,
//) {
//    Row(
//        verticalAlignment = Alignment.CenterVertically,
//        modifier = Modifier.height(IntrinsicSize.Min) // Ensures children match tallest child
//    ) {
//
//        val taskColor = when (task.status) {
//            TaskEndStatus.COMPLETED -> Completed
//            TaskEndStatus.UNFINISHED -> Unfinished
//            TaskEndStatus.BROKEN -> Broken
//            TaskEndStatus.NONE -> {
//                // Task RUNNING
//                if (task.isCurrent(now)) {
//                    Running
//                } else {
//                    NotStarted
//                }
//            }
//        }
//        // Timeline column with drawBehind for line
//        Box(
//            Modifier
//                .width(40.dp)
//                .fillMaxHeight()
//                .drawBehind {
//                    val centerX = size.width / 2
//                    val dotRadius = if (task.isCurrent(now)) 7.dp.toPx() else 5.dp.toPx()
//                    val centerY = size.height / 2
//                    val lineColor = Color(0xFF444444)
//                    // Make the line above the circle node
//                    if (!isFirst) {
//                        drawLine(
//                            color = lineColor,
//                            start = Offset(centerX, 0f),
//                            end = Offset(centerX, centerY - dotRadius),
//                            strokeWidth = 6f
//                        )
//                    }
//                    // Make the line below the circle node
//                    if (!isLast) {
//                        drawLine(
//                            color = lineColor,
//                            start = Offset(
//                                centerX,
//                                centerY + dotRadius
//                            ),
//                            end = Offset(centerX, size.height),
//                            strokeWidth = 6f
//                        )
//                    }
//                },
//            contentAlignment = Alignment.Center
//        ) {
//
//            Box(
//                Modifier
//                    .size(12.dp)
//                    .clip(CircleShape)
//                    .background(
//                        taskColor
//                    )
//            )
//        }
//        // Task card
//        Column(
//            Modifier
//                .weight(1f)
//                .padding(start = 8.dp)
//                .padding(vertical = 12.dp)
//                .clip(MaterialTheme.shapes.medium)
//                .background(
//                    taskColor
//                )
////                .border(
////                    width = if (task.isCurrent(now)) 1.dp else 0.dp,
////                    color = if (task.isCurrent(now)) Color(0xFF2D5BFF) else Color.Transparent,
////                    shape = MaterialTheme.shapes.medium
////                )
//                .padding(16.dp),
//            verticalArrangement = Arrangement.spacedBy(4.dp)
//        ) {
//            Text(
//                text = task.title,
//                color = White,
//                fontWeight = if (task.isCurrent(now)) FontWeight.Bold else FontWeight.Normal,
//                textDecoration = if (task.isPast(now)) TextDecoration.LineThrough else TextDecoration.None
//            )
//            Text(
//                text = "${task.startTime.formatTime()} – ${task.endTime.formatTime()}",
//                color = White,
//                style = MaterialTheme.typography.bodySmall
//            )
//            task.note?.let {
//                Text(
//                    text = it,
//                    color = White,
//                    style = MaterialTheme.typography.bodySmall
//                )
//            }
//        }
//    }
//}