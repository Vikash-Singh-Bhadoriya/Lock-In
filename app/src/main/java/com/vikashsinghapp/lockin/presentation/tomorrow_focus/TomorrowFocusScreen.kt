package com.vikashsinghapp.lockin.presentation.tomorrow_focus

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Reorder
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.vikashsinghapp.lockin.presentation.tomorrow_focus.component.PermissionDialog
import com.vikashsinghapp.lockin.presentation.tomorrow_focus.component.TimeField
import com.vikashsinghapp.lockin.presentation.tomorrow_focus.component.goToAppSettings
import com.vikashsinghapp.lockin.system.alarm.TaskAlarmScheduler
import com.vikashsinghapp.lockin.ui.theme.BackgroundDark
import com.vikashsinghapp.lockin.ui.theme.SurfaceDark
import sh.calvin.reorderable.DragGestureDetector
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import timber.log.Timber
import java.time.LocalTime

// This screen exists so the user can plan tomorrow in under 10 minutes.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TomorrowFocusScreen(
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState,
    viewModel: TomorrowFocusViewModel = hiltViewModel(),
    shouldShowPermissionRationale: (String) -> Boolean = { false },
    onNavigateUp: () -> Unit,
) {

    // Use localTasks for UI state
    val taskList = viewModel.localTasks

    val isLocked by viewModel.isLocked.collectAsState()
    val context = LocalContext.current
    val dialogQueue = viewModel.visiblePermissionDialogQueue

    val isPostNotificationsPermissionGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    // Post Notifications Permission
    val requestPostNotificationsPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    viewModel.onPostNotificationsPermissionPermissionResult(
                        permission = Manifest.permission.POST_NOTIFICATIONS, isGranted = isGranted
                    )
                }
            })

    BackHandler(true) {
        // show dialog when actual reordering happens => workouts list change
        if (viewModel.isReordering) {
            viewModel.onEvent(TomorrowFocusEvent.ReorderingChanged(false))
        } else {
            onNavigateUp()
        }
    }

//    Scaffold(
    // systemBarsPadding() <-- if not apply then the input bar is like 20.dp away from bottom when keyboard appear
//        modifier = modifier.fillMaxSize(), // Use fillMaxSize to own the window space => the system status bar also
//        containerColor = BackgroundDark,
//        topBar = {
//            // This Top App Bar includes the system status bar also.
//            // If I set the background color to red, the system status bar background color changes to red
//            TopAppBar(
//                title = { Text("Tomorrow's Focus", color = Color.White, fontSize = 24.sp) },
//                navigationIcon = {
//                    IconButton(onClick = {}) {
//                        Icon(Icons.Default.Menu, contentDescription = null, tint = Color.White)
//                    }
//                },
//                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark)
//            )
//        }
//    ) { innerPadding ->

    val lazyListState = rememberLazyListState()
    val reorderableLazyListState =
        rememberReorderableLazyListState(lazyListState, onMove = { from, to ->
            viewModel.onEvent(
                TomorrowFocusEvent.SwapTask(
                    fromIndex = from.index,
                    toIndex = to.index
                )
            )
        })

    Column(
        modifier = modifier
            .fillMaxSize()
//                .padding(innerPadding)
            .background(BackgroundDark)
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Start)
                .padding(start = 8.dp),
            text = "Plan your day with intention",
            color = Color.Gray,
            fontSize = 15.sp
        )

        LazyColumn(
            state = lazyListState,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(12.dp)
        ) {
            items(
                taskList,
                key = { it.id }
            ) { task ->

                ReorderableItem(
                    state = reorderableLazyListState,
                    key = task.id
                ) { isDragging ->
                    val elevation by animateDpAsState(
                        if (isDragging) 4.dp else 0.dp,
                        label = ""
                    )
                    TaskBlock(
                        modifier = modifier.shadow(elevation),
                        reorderModifier = if (viewModel.isReordering) Modifier
                            .draggableHandle(
                                onDragStarted = {
                                    Timber.tag("THAG").d("reorder onDragStarted: ${it}")
                                },
                                onDragStopped = {
                                    Timber.tag("THAG").d("reorder onDragStop")
                                },
                                dragGestureDetector = DragGestureDetector.LongPress
                            )
                        else Modifier,

                        isLocked = isLocked,
                        isReordering = viewModel.isReordering,
                        title = task.title,
                        startTime = task.startTime,
                        endTime = task.endTime,
                        onTitleChange = { title ->
                            viewModel.onEvent(TomorrowFocusEvent.OnTaskUpdate(task.copy(title = title)))
                        },
                        onStartTimeChange = { startTime ->
                            viewModel.onEvent(TomorrowFocusEvent.OnTaskUpdate(task.copy(startTime = startTime)))

                        },
                        onEndTimeChange = { endTime ->
                            viewModel.onEvent(TomorrowFocusEvent.OnTaskUpdate(task.copy(endTime = endTime)))
                        },
                        onDeleteTask = {
                            viewModel.onEvent(TomorrowFocusEvent.DeleteTask(task))
                        },
                        onDuplicateTask = {
                            viewModel.onEvent(TomorrowFocusEvent.DuplicateTask(task))
                        },
                        onReorderingStart = {
                            viewModel.onEvent(TomorrowFocusEvent.ReorderingChanged(true))
                        },
                    )
                }
            }
        }

        if (isLocked) {
            Text(
                modifier = Modifier.padding(vertical = 8.dp),
                text = "🔒 Plan locked",
                color = Color.White
            )
        } else if (viewModel.isReordering){
            Button(
                modifier = Modifier.fillMaxWidth(0.5f),
                onClick = {
                    viewModel.onEvent(TomorrowFocusEvent.ReorderingChanged(false))
                }
            ) {
                Text("DONE")
            }
        } else {

            TextButton(
                onClick = {
                    viewModel.onEvent(TomorrowFocusEvent.AddNewTask)
                }
            ) {
                Text("Add Task")
            }

            TextButton(
                onClick = {
//                    // I checked here only if PostNotifications Permission is given if user is on Tiramusu
//                    // So, I don't need to check later in IntervalTimerService
//                    if (isPostNotificationsPermissionGranted) {
//                        viewModel.onEvent(TomorrowFocusEvent.LockPlan)
//                    } else if (Build.VERSION.SDK_INT >= 33) {
//                        // this will be called here only, as we should request for permission when we need (to start timer)
//                        requestPostNotificationsPermissionLauncher.launch(
//                            Manifest.permission.POST_NOTIFICATIONS
//                        )
//                    }
                    viewModel.onEvent(TomorrowFocusEvent.ValidateAndRequestPermission)

                }
            ) {
                Text("Save Plan")
            }
        }
    }
//    if (isLocked) {
//        // cannot click on any of button on the screen when plan locked
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(Color.Transparent)
//                .clickable(enabled = false) {}
//        )
//    }
    // Show Educational UI
    // Which includes Rationale explain what the permission is for
    // & when user permanently decline the permission
    dialogQueue.reversed().forEach { permission ->
        PermissionDialog(
            isPermanentlyDeclined = !shouldShowPermissionRationale(
                permission
            ), onDismiss = viewModel::dismissDialog, onAllow = {
                viewModel.dismissDialog()
                requestPostNotificationsPermissionLauncher.launch(
                    permission
                )
            }, onGoToAppSettingsClick = {
                context.goToAppSettings()
            })
    }
    // LaunchedEffect will be called on first composition
    // & when navigate between screens
    // & will not be called on pause screen
    LaunchedEffect(key1 = true) {
        Timber.d("LaunchedEffect(key1 = true) called")
        viewModel.eventFlow.collect { uiEvent ->
            when (uiEvent) {
                is TomorrowFocusScreenViewModelUiEvent.ScheduleAllPlanTaskAlarm -> {
                    taskList.forEach { task ->
                        TaskAlarmScheduler.scheduleTaskStart(context, task)
                    }
                }

                is TomorrowFocusScreenViewModelUiEvent.ValidationError -> {
                    snackbarHostState.showSnackbar(uiEvent.message)
                }

                is TomorrowFocusScreenViewModelUiEvent.RequestNotificationPermission -> {
                    // I checked here only if PostNotifications Permission is given if user is on Tiramusu
                    // So, I don't need to check later in IntervalTimerService
                    if (isPostNotificationsPermissionGranted) {
                        viewModel.onEvent(TomorrowFocusEvent.SavePlanAfterPermissionGranted)
                    } else if (Build.VERSION.SDK_INT >= 33) {
                        // this will be called here only, as we should request for permission when we need (to start timer)
                        requestPostNotificationsPermissionLauncher.launch(
                            Manifest.permission.POST_NOTIFICATIONS
                        )
                    }
                }

                TomorrowFocusScreenViewModelUiEvent.ScrollToTop -> {
                    Timber.tag("THAGG")
                        .d("MyWorkoutsScreenViewModelUiEvent.ScrollToTop, animate scroll to 0")
                    lazyListState.animateScrollToItem(0)
                }
            }
        }
    }
}
//}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskBlock(
    modifier: Modifier = Modifier,
    reorderModifier: Modifier = Modifier,
    isLocked: Boolean,
    isReordering: Boolean,
    title: String,
    startTime: LocalTime,
    endTime: LocalTime,
    onTitleChange: (String) -> Unit,
    onStartTimeChange: (LocalTime) -> Unit,
    onEndTimeChange: (LocalTime) -> Unit,
    onDeleteTask: () -> Unit,
    onReorderingStart: () -> Unit,
    onDuplicateTask: () -> Unit,
) {

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .then(reorderModifier),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        var textField by rememberSaveable {
            mutableStateOf(title)
        }
        val keyboardController = LocalSoftwareKeyboardController.current
        val focusManager = LocalFocusManager.current


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            if (isLocked) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    text = "🔒 $title",
                    color = Color.White
                )
            } else {
                if (isReordering) {
                    Text(
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = 16.dp),
                        text = title,
                        color = Color.White
                    )

                    Icon(
                        modifier = Modifier
                            .padding(16.dp)
                            .size(16.dp),
                        imageVector = Icons.Default.Reorder,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                } else {
                    OutlinedTextField(
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .weight(1f),
                        value = textField,
                        onValueChange = {
                            textField = it
                            onTitleChange(textField)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedTextColor = White,
                            focusedTextColor = White
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            if (textField.isNotEmpty()) {
                                focusManager.clearFocus() // Remove focus from text field

                                onTitleChange(textField)
                                keyboardController?.hide() // Hide keyboard
                            }
                        }),
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                        //                color = Color.White,
                        //                fontSize = 16.sp,
                        //                fontWeight = FontWeight.Bold
                    )
                    EditDeleteDuplicateMoreOptionsDropDownMenu(
                        onDelete = onDeleteTask,
                        onReorder = onReorderingStart,
                        onDuplicate = onDuplicateTask
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TimeField(
                modifier = Modifier,
                time = startTime,
                onTimeChange = onStartTimeChange,
                enable = !(isLocked || isReordering)
            )
            Icon(
                Icons.Default.HorizontalRule,
                contentDescription = null,
                tint = if (isLocked) Color.Gray else Color.White
            )
            TimeField(
                modifier = Modifier,
                time = endTime,
                onTimeChange = onEndTimeChange,
                enable = !(isLocked || isReordering)
            )
        }
    }
}


@Composable
fun EditDeleteDuplicateMoreOptionsDropDownMenu(
    modifier: Modifier = Modifier,
    onDelete: () -> Unit,
    onReorder: () -> Unit,
    onDuplicate: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        var showMoreOptions by remember {
            mutableStateOf(false)
        }
        IconButton(
            onClick = {
                showMoreOptions = true
            }) {
            Icon(
                modifier = Modifier.size(16.dp),
                imageVector = Icons.Default.MoreVert,
                contentDescription = null,
                tint = White
            )
        }
        DropdownMenu(
            expanded = showMoreOptions,
            onDismissRequest = { showMoreOptions = false }
        ) {
            MoreOptionsItem(
                onClick = {
                    onDelete()
                    showMoreOptions = false
                },
                imageVector = Icons.Default.Delete,
                text = "Delete",
            )
            MoreOptionsItem(
                onClick = {
                    onReorder()
                    showMoreOptions = false
                },
                imageVector = Icons.Default.Reorder,
                text = "Reorder"
            )
            MoreOptionsItem(
                onClick = {
                    onDuplicate()
                    showMoreOptions = false
                },
                imageVector = Icons.Default.ContentCopy,
                text = "Duplicate"
            )
        }
    }
}

@Composable
fun MoreOptionsItem(
    onClick: () -> Unit,
    imageVector: ImageVector,
    text: String,
) {
    DropdownMenuItem(
        onClick = onClick,
        leadingIcon = {
            Icon(
                modifier = Modifier.size(24.dp),
                imageVector = imageVector,
                contentDescription = text,
                tint = MaterialTheme.colorScheme.onBackground
            )
        },
        text = {
            Text(
                modifier = Modifier.padding(start = 4.dp),
                text = text,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Normal),
                color = MaterialTheme.colorScheme.onBackground
            )
        })
}