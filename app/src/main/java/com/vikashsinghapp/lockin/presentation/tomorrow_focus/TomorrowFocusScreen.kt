package com.vikashsinghapp.lockin.presentation.tomorrow_focus

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Reorder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.vikashsinghapp.lockin.data.entity.CategoryEntity
import com.vikashsinghapp.lockin.presentation.tomorrow_focus.component.PermissionDialog
import com.vikashsinghapp.lockin.presentation.tomorrow_focus.component.StartAddingTaskGraphic
import com.vikashsinghapp.lockin.presentation.tomorrow_focus.component.TimeField
import com.vikashsinghapp.lockin.presentation.tomorrow_focus.component.goToAppSettings
import com.vikashsinghapp.lockin.system.alarm.TaskAlarmScheduler
import com.vikashsinghapp.lockin.ui.theme.BackgroundDark
import com.vikashsinghapp.lockin.ui.theme.Running
import com.vikashsinghapp.lockin.ui.theme.SurfaceDark
import com.vikashsinghapp.lockin.ui.theme.SurfaceDarkElevated
import sh.calvin.reorderable.DragGestureDetector
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import timber.log.Timber
import java.time.LocalTime

// This screen exists so the user can plan tomorrow in under 10 minutes.
@SuppressLint("InlinedApi")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TomorrowFocusScreen(
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState,
    viewModel: TomorrowFocusViewModel = hiltViewModel(),
    shouldShowPermissionRationale: (String) -> Boolean = { false },
    onNavigateUp: () -> Unit,
    onOpenDrawer: () -> Unit,
) {

    // Use localTasks for UI state
    val taskList = viewModel.localTasks

    val isLocked by viewModel.isLocked.collectAsState()
    val categories by viewModel.categories.collectAsState()
    var showCategoryManager by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val dialogQueue = viewModel.visiblePermissionDialogQueue

//    val isDisplayOverOtherAppsGranted by remember {
//        mutableStateOf(
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                ContextCompat.checkSelfPermission(
//                    context, Manifest.permission.USE_FULL_SCREEN_INTENT
//                ) == PackageManager.PERMISSION_GRANTED
//            } else {
//                true
//            }
//        )
//    }

    // Post Notifications Permission
    val requestPostNotificationsPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted ->
                if (isGranted) {
                    // INSTANTLY trigger the save once they hit "Allow"
                    viewModel.onEvent(TomorrowFocusEvent.SavePlanAfterPermissionGranted)
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        viewModel.onPostNotificationsPermissionPermissionResult(
                            permission = Manifest.permission.POST_NOTIFICATIONS,
                            isGranted = false
                        )
                    }
                }
            })

//    // Post Notifications Permission
//    val requestDisplayOverOtherAppsPermissionLauncher =
//        rememberLauncherForActivityResult(
//            contract = ActivityResultContracts.RequestPermission(),
//            onResult = { isGranted ->
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                    viewModel.onDisplayOverOtherAppsPermissionPermissionResult(
//                        permission = Manifest.permission.USE_FULL_SCREEN_INTENT, isGranted = isGranted
//                    )
//                }
//            })

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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Plan", color = Color.White, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BackgroundDark)
                .padding(horizontal = 8.dp, vertical = 16.dp),
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

            if (taskList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    StartAddingTaskGraphic()
                }
            } else {
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
                                            Timber.tag("THAG").d("reorder onDragStarted: $it")
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
                                category = task.category,
                                startTime = task.startTime,
                                endTime = task.endTimePlan,
                                onTitleChange = { title ->
                                    viewModel.onEvent(
                                        TomorrowFocusEvent.OnTaskUpdate(
                                            task.copy(
                                                title = title
                                            )
                                        )
                                    )
                                },
                                onCategoryChange = { category ->
                                    viewModel.onEvent(
                                        TomorrowFocusEvent.OnTaskUpdate(
                                            task.copy(
                                                category = category
                                            )
                                        )
                                    )
                                },
                                onStartTimeChange = { startTime ->
                                    viewModel.onEvent(
                                        TomorrowFocusEvent.OnTaskUpdate(
                                            task.copy(
                                                startTime = startTime
                                            )
                                        )
                                    )

                                },
                                onEndTimeChange = { endTime ->
                                    viewModel.onEvent(
                                        TomorrowFocusEvent.OnTaskUpdate(
                                            task.copy(
                                                endTimePlan = endTime
                                            )
                                        )
                                    )
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
                                categories = categories,
                                onOpenCategoryManager = {
                                    showCategoryManager = true
                                } // Open global manager
                            )
                        }
                    }
                }
            }

            if (isLocked) {
                Text(
                    modifier = Modifier.padding(vertical = 8.dp),
                    text = "🔒 Plan locked",
                    color = White
                )
            } else if (viewModel.isReordering) {
                Button(
                    modifier = Modifier.fillMaxWidth(0.5f),
                    onClick = {
                        viewModel.onEvent(TomorrowFocusEvent.ReorderingChanged(false))
                    }
                ) {
                    Text("DONE")
                }
            } else {

//            Button(
//                modifier = Modifier.fillMaxWidth(0.65f),
//                onClick = {
//                    viewModel.onEvent(TomorrowFocusEvent.AddNewTask)
//                },
//            ) {
//                Text(
//                    text = "Add Task",
//                    color = White,
//                )
//            }
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(0.65f),
                    onClick = { viewModel.onEvent(TomorrowFocusEvent.AddNewTask) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = White),
                    border = BorderStroke(
                        1.dp,
                        Color.Gray
                    ) // Sleek border instead of solid dull fill
                ) {
                    Text(
                        text = "Add Task",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    modifier = Modifier.fillMaxWidth(0.65f),
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

                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Running, // Vibrant Brand Blue
                        contentColor = White
                    )
                ) {
                    Text(
                        text = "Save Plan",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Show Category Manager Dialog
        if (showCategoryManager) {
            CategoryManagerDialog(
                categories = categories,
                onDismiss = { showCategoryManager = false },
                onAdd = { newCategory -> viewModel.addCategory(newCategory) },
                onEdit = { updatedCategory -> viewModel.editCategory(updatedCategory) },
                onDelete = { categoryToDelete -> viewModel.deleteCategory(categoryToDelete) }
            )
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

                    is TomorrowFocusScreenViewModelUiEvent.RequestPermissions -> {
                        // 1. Check permission dynamically right now
                        val hasNotificationPermission =
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) == PackageManager.PERMISSION_GRANTED
                            } else {
                                true
                            }

                        // I checked here only if PostNotifications Permission is given if user is on Tiramusu
                        // So, I don't need to check later in IntervalTimerService
                        if (hasNotificationPermission) {
//                        val intent = Intent(
//                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
//                            "package:${context.packageName}".toUri()
//                        )
//                        context.startActivity(intent, REQUEST_CODE_DRAW_OVER_APPS)
                            viewModel.onEvent(TomorrowFocusEvent.SavePlanAfterPermissionGranted)
                        } else {
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskBlock(
    modifier: Modifier = Modifier,
    reorderModifier: Modifier = Modifier,
    isLocked: Boolean,
    isReordering: Boolean,
    title: String,
    category: String,
    startTime: LocalTime,
    endTime: LocalTime,
    onTitleChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onStartTimeChange: (LocalTime) -> Unit,
    onEndTimeChange: (LocalTime) -> Unit,
    onDeleteTask: () -> Unit,
    onReorderingStart: () -> Unit,
    onDuplicateTask: () -> Unit,
    categories: List<CategoryEntity>,
    onOpenCategoryManager: () -> Unit, // ADDED for the global manage dialog
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var textField by rememberSaveable { mutableStateOf(title) }
//    var selectedCategory by rememberSaveable { mutableStateOf(category) }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .then(reorderModifier),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.Start
    ) {
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
                    color = White
                )
            } else {
                if (isReordering) {
                    Text(
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = 16.dp),
                        text = title,
                        color = White
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
                                focusManager.clearFocus()
                                onTitleChange(textField)
                                keyboardController?.hide()
                            }
                        }),
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    )

                    EditDeleteDuplicateMoreOptionsDropDownMenu(
                        onDelete = onDeleteTask,
                        onReorder = onReorderingStart,
                        onDuplicate = onDuplicateTask
                    )
                }
            }
        }
        var showCategoryDropdown by remember { mutableStateOf(false) }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, start = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // The Button with the Dropdown Arrow
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF173393))
                    .clickable(enabled = !isLocked && !isReordering) { showCategoryDropdown = true }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = category.ifBlank { "Uncategorized" }.uppercase(),
                        color = White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    if (!isLocked && !isReordering) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select Category",
                            tint = White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Show the dialog only when clicked
        if (showCategoryDropdown) {
            CategorySelectionDialog(
                categories = categories, // Pass this down from the ViewModel
                currentCategory = category,
                onSelect = { selectedCat ->
                    onCategoryChange(selectedCat) // Update the task via ViewModel event
                    showCategoryDropdown = false
                },
                onDismiss = { showCategoryDropdown = false },
                onManageClick = {
                    showCategoryDropdown = false
                    onOpenCategoryManager() // Trigger the global manage dialog from the parent screen
                }
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TimeField(
                modifier = Modifier.weight(1f),
                time = startTime,
                onTimeChange = onStartTimeChange,
                enable = !(isLocked || isReordering)
            )
//            Icon(
//                Icons.Default.HorizontalRule,
//                contentDescription = null,
//                tint = if (isLocked) Color.Gray else White
//            )
            Text("to", color = Color.Gray)
            TimeField(
                modifier = Modifier.weight(1f),
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
        modifier = modifier,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelectionDialog(
    categories: List<CategoryEntity>,
    currentCategory: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
    onManageClick: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 48.dp, start = 16.dp, end = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Select Category",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = onManageClick) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Manage",
                        tint = Color.Gray
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    val isSelected = cat.name == currentCategory
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelect(cat.name) },
                        label = {
                            Text(
                                cat.name,
                                color = if (isSelected) Color.Black else Color.White
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color.White,
                            containerColor = SurfaceDarkElevated
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryManagerDialog(
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit,
    onEdit: (CategoryEntity) -> Unit,
    onDelete: (CategoryEntity) -> Unit,
) {
    var categoryInputText by remember { mutableStateOf("") }
    var editingCategory by remember { mutableStateOf<CategoryEntity?>(null) } // Tracks edit state

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = { Text("Manage Categories", color = White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Add New Category
                OutlinedTextField(
                    value = categoryInputText,
                    onValueChange = { categoryInputText = it },
                    placeholder = {
                        Text(
                            if (editingCategory != null) "Update category" else "New category name...",
                            color = Color.Gray
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = White,
                        unfocusedTextColor = White
                    ),
                    trailingIcon = {
                        if (editingCategory != null) {
                            // EDIT MODE: Show Cancel & Save icons
                            Row {
                                IconButton(onClick = {
                                    editingCategory = null
                                    categoryInputText = ""
                                }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Cancel",
                                        tint = Color.Gray
                                    )
                                }
                                IconButton(onClick = {
                                    if (categoryInputText.isNotBlank()) {
                                        // Fire the edit callback with the updated name
                                        onEdit(editingCategory!!.copy(name = categoryInputText.trim()))
                                        editingCategory = null
                                        categoryInputText = ""
                                    }
                                }) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Save",
                                        tint = Running
                                    )
                                }
                            }
                        } else {
                            // ADD MODE: Show standard Add icon
                            IconButton(onClick = {
                                if (categoryInputText.isNotBlank()) {
                                    onAdd(categoryInputText.trim())
                                    categoryInputText = ""
                                }
                            }) {
                                Icon(Icons.Default.Add, contentDescription = "Add", tint = Running)
                            }
                        }
                    }
                )

                // List Existing Categories
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { cat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SurfaceDarkElevated, RoundedCornerShape(8.dp))
                                .padding(start = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = cat.name, color = White)
                            Row {
                                IconButton(onClick = {
                                    // Trigger Edit Mode
                                    editingCategory = cat
                                    categoryInputText = cat.name
                                }) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Edit",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                IconButton(onClick = { onDelete(cat) }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done", color = Running)
            }
        }
    )
}