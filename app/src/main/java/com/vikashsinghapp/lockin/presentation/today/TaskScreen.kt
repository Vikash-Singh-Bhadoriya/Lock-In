package com.vikashsinghapp.lockin.presentation.today

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.vikashsinghapp.lockin.data.entity.TaskEndStatus
import com.vikashsinghapp.lockin.presentation.today.component.DateCalendar
import com.vikashsinghapp.lockin.presentation.today.component.EmptyPlanState
import com.vikashsinghapp.lockin.presentation.today.component.HourBasedTimeline
import com.vikashsinghapp.lockin.presentation.tomorrow_focus.TemplateTaskBottomSheet
import com.vikashsinghapp.lockin.system.alarm.TaskAlarmScheduler
import com.vikashsinghapp.lockin.ui.theme.Running
import com.vikashsinghapp.lockin.ui.theme.SurfaceDark
import com.vikashsinghapp.lockin.ui.theme.spacing
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState,
    viewModel: TaskViewModel = hiltViewModel(),
    onNavigateToTaskDetail: (Long) -> Unit,
    onNavigateToActiveFocus: (Long) -> Unit,
    onNavigateToPlanner: (String) -> Unit,
    onOpenDrawer: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentTime by viewModel.currentTime.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    var showQuickAddSheet by remember { mutableStateOf(false) }
    val categories by viewModel.categories.collectAsState()

    val isViewedDateLocked = uiState.tasks.isNotEmpty()

    // Check if ANY task is currently actively running right now
    val isAnyTaskRunning = uiState.tasks.any { task ->
        task.isCurrent(currentTime) && task.status == TaskEndStatus.PENDING
    }

    val context = LocalContext.current

    // Only show FAB if looking at TODAY, no task is running, AND the plan is locked
    val showFab = !isAnyTaskRunning && selectedDate == LocalDate.now() && isViewedDateLocked

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Task",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (uiState.isDateCalendarVisible)
                            viewModel.onEvent(TaskScreenEvent.HideDateCalendar)
                        else
                            viewModel.onEvent(TaskScreenEvent.ShowDateCalendar)
                    }) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth, // Or Icons.Default.DateRange
                            contentDescription = "Select Date",
                            tint = Running // Use your brand blue to make it pop slightly
                        )
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
                    Icon(Icons.Default.Add, contentDescription = "Add Task")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(SurfaceDark)
                .padding(
                    end = MaterialTheme.spacing.large,
                    start = MaterialTheme.spacing.small,
                    bottom = MaterialTheme.spacing.medium
                ),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        ) {
            DateCalendar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.extraSmall),
                isCalendarVisible = uiState.isDateCalendarVisible,
                selectedDate = selectedDate,
                onSelectedDateChange = {
                    viewModel.onEvent(TaskScreenEvent.OnDateSelected(it))
                }
            )
            //The Dynamic Routing Logic
            val today = LocalDate.now()

            // Handle the split-second loading state to prevent flicker
            if (uiState.isLoading) {
                // Show a blank space (or a subtle CircularProgressIndicator) while Room fetches data
                Box(modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth())
            } else {
                when {
                    // 1. PAST DATES: Read-Only Archive
                    selectedDate.isBefore(today) -> {
                        HourBasedTimeline(
                            tasks = uiState.tasks,
                            now = currentTime,
                            modifier = Modifier.weight(1f),
                            selectedDate = selectedDate,
                            categories = categories,
                            onTaskClick = { clickedTask ->
                                onNavigateToTaskDetail(clickedTask.id) // Past tasks always go to detail
                            }
                        )
                    }
                    // 2. TODAY OR TOMORROW: Execution or Drafting
                    selectedDate.isEqual(today) || selectedDate.isEqual(today.plusDays(1)) -> {
                        if (isViewedDateLocked) {
                            HourBasedTimeline(
                                tasks = uiState.tasks,
                                now = currentTime,
                                modifier = Modifier.weight(1f),
                                selectedDate = selectedDate,
                                categories = categories,
                                onTaskClick = { clickedTask ->
                                    if (clickedTask.isCurrent(currentTime) && clickedTask.status == TaskEndStatus.PENDING) {
                                        onNavigateToActiveFocus(clickedTask.id)
                                    } else {
                                        onNavigateToTaskDetail(clickedTask.id)
                                    }
                                }
                            )
                        } else {
                            // Empty State! Route them to the planner with the specific date
                            EmptyPlanState(
                                modifier = Modifier.weight(1f),
                                date = selectedDate,
                                onPlanClick = {
                                    onNavigateToPlanner(selectedDate.toString())
                                }
                            )
                        }
                    }
                    // 3. FAR FUTURE: Blocked
                    selectedDate.isAfter(today.plusDays(1)) -> {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Focus on the present.\nYou can only plan up to one day in advance.",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
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

                is TaskScreenViewModelUiEvent.ShowSnackbar -> {
                    // Launch a new coroutine so it doesn't block the Flow!
                    launch {
                        snackbarHostState.showSnackbar(uiEvent.message)
                    }
                }

                is TaskScreenViewModelUiEvent.ValidationError -> {
                    // Launch a new coroutine so it doesn't block the Flow!
                    launch {
                        snackbarHostState.showSnackbar(uiEvent.message)
                    }
                }
            }
        }
    }
    if (showQuickAddSheet) {
        TemplateTaskBottomSheet(
            initialTask = null,
            categories = categories,
            onDismiss = { showQuickAddSheet = false },
            onSave = { title, start, end, category ->
                viewModel.validateAndInjectTask(title, start, end, category)
            },
            // Pass the viewmodel functions down for the category sheet
            onAddCategory = { viewModel.addCategory(it) },
            onEditCategory = { old, newName -> viewModel.editCategory(old, newName) },
            onDeleteCategory = { viewModel.deleteCategory(it) }

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