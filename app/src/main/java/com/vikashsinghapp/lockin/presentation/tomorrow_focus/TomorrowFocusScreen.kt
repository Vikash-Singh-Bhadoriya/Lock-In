package com.vikashsinghapp.lockin.presentation.tomorrow_focus

import CategorySelectionAndManageSheet
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.vikashsinghapp.lockin.data.entity.CategoryEntity
import com.vikashsinghapp.lockin.data.entity.PromiseTask
import com.vikashsinghapp.lockin.data.entity.TemplateWithTasks
import com.vikashsinghapp.lockin.presentation.tomorrow_focus.component.PermissionDialog
import com.vikashsinghapp.lockin.presentation.tomorrow_focus.component.PlanApplyDialog
import com.vikashsinghapp.lockin.presentation.tomorrow_focus.component.PlanHubBottomSheet
import com.vikashsinghapp.lockin.presentation.tomorrow_focus.component.StartAddingTaskGraphic
import com.vikashsinghapp.lockin.presentation.tomorrow_focus.component.TaskCard
import com.vikashsinghapp.lockin.presentation.tomorrow_focus.component.TimeField
import com.vikashsinghapp.lockin.presentation.tomorrow_focus.component.goToAppSettings
import com.vikashsinghapp.lockin.system.alarm.TaskAlarmScheduler
import com.vikashsinghapp.lockin.ui.theme.BackgroundDark
import com.vikashsinghapp.lockin.ui.theme.Error
import com.vikashsinghapp.lockin.ui.theme.ForceWhiteStatusBarIcons
import com.vikashsinghapp.lockin.ui.theme.LuminousIndigo
import com.vikashsinghapp.lockin.ui.theme.Running
import com.vikashsinghapp.lockin.ui.theme.SurfaceDark
import com.vikashsinghapp.lockin.ui.theme.SurfaceDarkElevated
import com.vikashsinghapp.lockin.ui.theme.spacing
import kotlinx.coroutines.launch
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
    onNavigateToTemplateEditor: (Long) -> Unit,
    onNavigateToTimeline: () -> Unit,
) {

    // Use localTasks for UI state
    val taskList = viewModel.localTasks

    val isLocked by viewModel.isLocked.collectAsState()
    val categories by viewModel.categories.collectAsState()
//    var showCategoryManager by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val dialogQueue = viewModel.visiblePermissionDialogQueue

    var isSheetOpen by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<PromiseTask?>(null) }

    // Tracks the navigation action they attempted (Back vs. Drawer)
    var pendingNavAction by remember { mutableStateOf<(() -> Unit)?>(null) }

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

    val templates by viewModel.templatesWithTasks.collectAsState()
    var showTemplateHub by remember { mutableStateOf(false) }
    var pendingTemplateToApply by remember { mutableStateOf<TemplateWithTasks?>(null) }
//    val tooltipState = rememberTooltipState(isPersistent = true)

//// Automatically show the tooltip the first time this screen is opened
//    LaunchedEffect(Unit) {
//        // Wait for the initial layout pass to finish before showing the popup
//        kotlinx.coroutines.delay(500)
//        tooltipState.show()
//    }

    // Post Notifications Permission
    val requestPostNotificationsPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted ->
                viewModel.onPostNotificationsPermissionResult(isGranted)
            }
        )

    // The Overlay Permission Launcher (Opens Android Settings)
    val requestOverlayPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        // When they return from Android Settings, check if they actually flipped the switch
        viewModel.onOverlayPermissionResult(Settings.canDrawOverlays(context))
    }

    // Intercept hardware back button
    BackHandler(true) {
        if (taskList.isNotEmpty() && !isLocked) {
            pendingNavAction = { onNavigateUp() }
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
                title = { Text("Plan", color = Color.White, style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    // Intercept drawer menu
                    IconButton(onClick = {
                        if (taskList.isNotEmpty() && !isLocked) {
                            pendingNavAction = { onOpenDrawer() }
                        } else {
                            onOpenDrawer()
                        }
                    }) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = Color.White)
                    }
                }, actions = {
                    // Only show the template button if the plan isn't locked and we aren't reordering
                    if (!isLocked
//                        && !viewModel.isReordering
                    ) {
//                        TooltipBox(
//                            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
//                                TooltipAnchorPosition.Below
//                            ),
//                            tooltip = {
//                                PlainTooltip {
//                                    Text("Save time! Load a daily routine instantly.")
//                                }
//                            },
//                            state = tooltipState
//                        ) {
                        IconButton(onClick = {
                            showTemplateHub = true
//                                coroutineScope.launch { tooltipState.dismiss() } // Hide tooltip once clicked
                        }) {
                            Icon(
                                Icons.AutoMirrored.Filled.LibraryBooks,
                                contentDescription = "Templates",
                                tint = Color.White
                            )
                        }
//                        }
                    }
                },
                // ------------------------------------------------
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)

            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BackgroundDark)
                .padding(horizontal = MaterialTheme.spacing.small, vertical = MaterialTheme.spacing.large),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Start)
                    .padding(start = MaterialTheme.spacing.small),
                text = "Plan your day with intention",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyLarge
            )

            if (viewModel.isLoading) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.CircularProgressIndicator(color = Color.White)
                }
            } else if (taskList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(bottom = MaterialTheme.spacing.large),
                    contentAlignment = Alignment.Center
                ) {
                    StartAddingTaskGraphic()
                }
            } else {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
                    contentPadding = PaddingValues(MaterialTheme.spacing.medium)
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
                                if (isDragging) 8.dp else 0.dp,
                                label = ""
                            )
                            // 1. Find the matching category object from the ViewModel's state
                            val matchedCategory = categories.find { it.name == task.category }

                            // 2. Extract the Long value (fallback to DarkGray if deleted/uncategorized)
                            val colorValueLong = matchedCategory?.colorValue ?: 0xFF444444

                            // 3. Convert Long to Compose Color
                            val accentColor = Color(colorValueLong)

                            // --- THE REUSABLE UI COMPONENT ---
                            TaskCard(
                                modifier = Modifier.shadow(elevation),
                                title = task.title,
                                category = task.category,
                                categoryColor = accentColor,
                                startTime = task.startTime,
                                endTime = task.endTimePlan,
                                isLocked = isLocked,
                                onCardClick = {
                                    taskToEdit = task
                                    isSheetOpen = true
                                },
                                onDuplicateClick = {
                                    viewModel.onEvent(
                                        TomorrowFocusEvent.DuplicateTask(
                                            task
                                        )
                                    )
                                },
                                onDeleteClick = {
                                    viewModel.onEvent(
                                        TomorrowFocusEvent.DeleteTask(
                                            task
                                        )
                                    )
                                },
                                dragModifier = Modifier.draggableHandle(
                                    dragGestureDetector = DragGestureDetector.Press
                                )
                            )
                        }
                    }
                }
            }

            if (isLocked) {
                // --- LOCKED DASHBOARD INDICATOR ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BackgroundDark), // Matches the screen background
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // A subtle, glowing top border to separate it from the list
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Running.copy(alpha = 0.3f))
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = MaterialTheme.spacing.extraLarge),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // A tiny, glowing status dot instead of a clunky padlock
                        Box(
                            modifier = Modifier
                                .size(MaterialTheme.spacing.small)
                                .clip(RoundedCornerShape(50))
                                .background(Running)
                                .shadow(8.dp, spotColor = Running) // Glow effect
                        )
                        Spacer(Modifier.width(MaterialTheme.spacing.medium))
                        Text(
                            text = "PLAN LOCKED",
                            color = Color.Gray,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 4.sp // Wide tracking = Premium/Cinematic
                            )
                        )
                    }
                }
            } else {
                // --- THE NEW 70/30 BOTTOM BAR ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        // Add a dark background to the bottom bar so it acts like a dock
                        .background(BackgroundDark)
                        .padding(horizontal = MaterialTheme.spacing.extraLarge, vertical = MaterialTheme.spacing.large),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. The 70% "Save Plan" Button
                    Button(
                        modifier = Modifier
                            .weight(1f) // Takes up all remaining space (the 70%)
                            .height(MaterialTheme.spacing.buttonHeight), // Standard premium button height
                        enabled = taskList.isNotEmpty(),
                        onClick = {
                            viewModel.onEvent(TomorrowFocusEvent.ValidateAndRequestPermission)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Running, // Vibrant Brand Blue
                            contentColor = Color.White,
                            disabledContainerColor = SurfaceDarkElevated,
                            disabledContentColor = Color.Gray
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("LOCK IN SCHEDULE 🔒", style = MaterialTheme.typography.titleSmall)
                    }

                    // 2. The 30% "Add Task" Button (Styled like a modern FAB, but inline)
                    Box(
                        modifier = Modifier
                            .size(MaterialTheme.spacing.buttonHeight) // Perfect square that matches the button height
                            .clip(RoundedCornerShape(16.dp)) // Matches the Save button's corners
                            .background(SurfaceDarkElevated)
                            .border(1.dp, Running, RoundedCornerShape(16.dp))
                            .clickable {
                                // --- OPEN SHEET IN ADD MODE ---
                                taskToEdit = null
                                isSheetOpen = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Task",
                            tint = Running,
                            modifier = Modifier.size(MaterialTheme.spacing.iconSizeLarge)
                        )
                    }
                }

            }
        }

//        // Show Category Manager Dialog
//        if (showCategoryManager) {
//            CategoryManagerSheet(
//                categories = categories,
//                onDismiss = { showCategoryManager = false },
//                onAdd = { newCategory -> viewModel.addCategory(newCategory) },
//                onEdit = { oldCat, newName -> viewModel.editCategory(oldCat, newName) },
//                onDelete = { categoryToDelete -> viewModel.deleteCategory(categoryToDelete) }
//            )
//        }
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
        // 1. Standard Dialog Queue (For Notifications)
        dialogQueue.reversed().forEach { permission ->
            PermissionDialog(
                isPermanentlyDeclined = !shouldShowPermissionRationale(
                    permission
                ), onDismiss = viewModel::dismissDialog,
                onAllow = {
                    viewModel.dismissDialog()
                    requestPostNotificationsPermissionLauncher.launch(
                        permission
                    )
                }, onGoToAppSettingsClick = {
                    context.goToAppSettings()
                })
        }

        // 2. Special Dialog (For Overlay)
        if (viewModel.showOverlayDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissOverlayDialog() },
                title = {
                    Text(
                        "Bring you back to focus",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        "To pull you out of distractions, LockIn needs permission to display your Roll Call over other apps when it's time to work.",
                        color = Color.LightGray
                    )
                },
                containerColor = SurfaceDarkElevated,
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.dismissOverlayDialog()
                            // Launch the specific Settings intent for Overlay
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                "package:${context.packageName}".toUri()
                            )
                            requestOverlayPermissionLauncher.launch(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Running)
                    ) {
                        Text("Go to Settings")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissOverlayDialog() }) {
                        Text("Cancel", color = Color.Gray)
                    }
                }
            )
        }

        // --- Bottom Sheet & Replace Dialogs ---
        if (showTemplateHub) {
            PlanHubBottomSheet(
                templates = templates, // Passes the data to the Bottom Sheet
                onDismiss = { showTemplateHub = false },
                onCreateNewTemplate = {
                    showTemplateHub = false
                    onNavigateToTemplateEditor(-1L) // ID -1 means "New"
                },
                onEditTemplate = { templateId ->
                    showTemplateHub = false
                    onNavigateToTemplateEditor(templateId) // Pass existing ID
                },
                onApplyTemplate = { templateData ->
                    if (taskList.isEmpty()) {
                        // Safe to apply directly!
                        viewModel.onEvent(
                            TomorrowFocusEvent.ApplyTemplate(
                                templateData.tasks,
                                isReplace = true
                            )
                        )
                        showTemplateHub = false
                    } else {
                        // Show Append/Replace Dialog
                        pendingTemplateToApply = templateData
                    }
                }
            )
        }

        if (pendingTemplateToApply != null) {
            PlanApplyDialog(
                onDismiss = { pendingTemplateToApply = null },
                onReplace = {
                    viewModel.onEvent(
                        TomorrowFocusEvent.ApplyTemplate(
                            pendingTemplateToApply!!.tasks,
                            isReplace = true
                        )
                    )
                    pendingTemplateToApply = null
                    showTemplateHub = false
                },
                onAppend = {
                    viewModel.onEvent(
                        TomorrowFocusEvent.ApplyTemplate(
                            pendingTemplateToApply!!.tasks,
                            isReplace = false
                        )
                    )
                    pendingTemplateToApply = null
                    showTemplateHub = false
                }
            )
        }
        // The Unsaved Changes Dialog
        if (pendingNavAction != null) {
            AlertDialog(
                onDismissRequest = { pendingNavAction = null },
                containerColor = SurfaceDarkElevated,
                title = {
                    Text("Unsaved Tasks", color = Color.White, fontWeight = FontWeight.Bold)
                },
                text = {
                    Text("You have un-saved tasks. Do you want to Lock In your schedule, or discard these tasks?", color = Color.LightGray)
                },
                confirmButton = {
                    Button(
                        onClick = {
                            pendingNavAction = null // Dismiss dialog
                            // Trigger the Lock In sequence!
                            viewModel.onEvent(TomorrowFocusEvent.ValidateAndRequestPermission)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Running)
                    ) {
                        Text("Lock In", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            // Execute the navigation they originally requested (discarding the screen)
                            val action = pendingNavAction
                            pendingNavAction = null
                            action?.invoke()
                        }
                    ) {
                        Text("Discard", color = Error)
                    }
                }
            )
        }

        // --- THE UNIFIED ADD/EDIT TASK SHEET ---
        if (isSheetOpen) {
            PlanTaskBottomSheet(
                initialTask = taskToEdit,
                categories = categories,
                onDismiss = { isSheetOpen = false },
                onSave = { title, start, end, category ->
                    return@PlanTaskBottomSheet viewModel.validateAndSaveTask(
                        taskToEdit?.id,
                        title,
                        start,
                        end,
                        category
                    )
                },
                onAddCategory = { viewModel.addCategory(it) },
                onEditCategory = { old, newName -> viewModel.editCategory(old, newName) },
                onDeleteCategory = { viewModel.deleteCategory(it) }
            )
        }

        // LaunchedEffect will be called on first composition
        // & when navigate between screens
        // & will not be called on pause screen
        LaunchedEffect(key1 = true) {
            Timber.d("LaunchedEffect(key1 = true) called")
            viewModel.eventFlow.collect { uiEvent ->
                when (uiEvent) {
                    is TomorrowFocusScreenViewModelUiEvent.NavigateToTimeline -> {
                        onNavigateToTimeline()
                    }

                    is TomorrowFocusScreenViewModelUiEvent.ScheduleAllPlanTaskAlarm -> {
                        uiEvent.tasks.forEach { task ->
                            TaskAlarmScheduler.scheduleTaskStart(context, task)
                        }
                    }

                    is TomorrowFocusScreenViewModelUiEvent.ValidationError -> {
                        // Launch a new coroutine so it doesn't block the Flow!
                        launch {
                            snackbarHostState.showSnackbar(uiEvent.message)
                        }
                    }

                    is TomorrowFocusScreenViewModelUiEvent.CheckNotificationPermission -> {
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
                            // Already have it? Move directly to Stage 2
                            viewModel.onPostNotificationsPermissionResult(true)
                        } else {
                            // Ask for it
                            // this will be called here only, as we should request for permission when we need (to start timer)
                            requestPostNotificationsPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }

                    is TomorrowFocusScreenViewModelUiEvent.CheckOverlayPermission -> {
                        if (Settings.canDrawOverlays(context)) {
                            // Already have it? We are done! Save the plan.
                            viewModel.onOverlayPermissionResult(true)
                        } else {
                            // Show our custom explanation dialog before booting them to Settings
                            viewModel.allowOverlayDialog()
                        }
                    }

                    TomorrowFocusScreenViewModelUiEvent.ScrollToTop -> {
                        Timber.tag("THAGG")
                            .d("MyWorkoutsScreenViewModelUiEvent.ScrollToTop, animate scroll to 0")
                        lazyListState.animateScrollToItem(0)
                    }

                    is TomorrowFocusScreenViewModelUiEvent.ShowSnackbar -> {
                        // Launch a new coroutine so it doesn't block the Flow!
                        launch {
                            snackbarHostState.showSnackbar(uiEvent.message)
                        }
                    }
                }
            }
        }
    }
}

//@Composable
//fun MoreOptionsItem(
//    onClick: () -> Unit,
//    imageVector: ImageVector,
//    text: String,
//) {
//    DropdownMenuItem(
//        onClick = onClick,
//        leadingIcon = {
//            Icon(
//                modifier = Modifier.size(24.dp),
//                imageVector = imageVector,
//                contentDescription = text,
//                tint = MaterialTheme.colorScheme.onBackground
//            )
//        },
//        text = {
//            Text(
//                modifier = Modifier.padding(start = 4.dp),
//                text = text,
//                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Normal),
//                color = MaterialTheme.colorScheme.onBackground
//            )
//        })
//}

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun CategorySelectionDialog(
//    categories: List<CategoryEntity>,
//    currentCategory: String,
//    onSelect: (String) -> Unit,
//    onDismiss: () -> Unit,
//    onManageClick: () -> Unit,
//) {
//    ModalBottomSheet(
//        onDismissRequest = onDismiss,
//        containerColor = SurfaceDark,
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(bottom = 48.dp, start = 16.dp, end = 16.dp)
//        ) {
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text(
//                    "Select Category",
//                    color = Color.White,
//                    fontSize = 18.sp,
//                    fontWeight = FontWeight.Bold
//                )
//
//                IconButton(onClick = onManageClick) {
//                    Icon(
//                        Icons.Default.Settings,
//                        contentDescription = "Manage",
//                        tint = Color.Gray
//                    )
//                }
//            }
//            Spacer(modifier = Modifier.height(16.dp))
//
//            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//                items(categories) { cat ->
//                    val isSelected = cat.name == currentCategory
//                    FilterChip(
//                        selected = isSelected,
//                        onClick = { onSelect(cat.name) },
//                        label = {
//                            Text(
//                                cat.name,
//                                color = if (isSelected) Color.Black else Color.White
//                            )
//                        },
//                        colors = FilterChipDefaults.filterChipColors(
//                            selectedContainerColor = Color.White,
//                            containerColor = SurfaceDarkElevated
//                        )
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun CategoryManagerDialog(
//    categories: List<CategoryEntity>,
//    onDismiss: () -> Unit,
//    onAdd: (String) -> Unit,
//    onEdit: (oldCategory: CategoryEntity, newName: String) -> Unit,
//    onDelete: (CategoryEntity) -> Unit,
//) {
//    var categoryInputText by remember { mutableStateOf("") }
//    var editingCategory by remember { mutableStateOf<CategoryEntity?>(null) } // Tracks edit state
//
//    AlertDialog(
//        onDismissRequest = onDismiss,
//        containerColor = SurfaceDark,
//        title = { Text("Manage Categories", color = White) },
//        text = {
//            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
//                // Add New Category
//                OutlinedTextField(
//                    value = categoryInputText,
//                    onValueChange = { categoryInputText = it },
//                    placeholder = {
//                        Text(
//                            if (editingCategory != null) "Update category" else "New category name...",
//                            color = Color.Gray
//                        )
//                    },
//                    colors = OutlinedTextFieldDefaults.colors(
//                        focusedTextColor = White,
//                        unfocusedTextColor = White
//                    ),
//                    trailingIcon = {
//                        if (editingCategory != null) {
//                            // EDIT MODE: Show Cancel & Save icons
//                            Row {
//                                IconButton(onClick = {
//                                    editingCategory = null
//                                    categoryInputText = ""
//                                }) {
//                                    Icon(
//                                        Icons.Default.Close,
//                                        contentDescription = "Cancel",
//                                        tint = Color.Gray
//                                    )
//                                }
//                                IconButton(onClick = {
//                                    if (categoryInputText.isNotBlank()) {
//                                        // --- Pass the exact old object and the new string ---
//                                        onEdit(editingCategory!!, categoryInputText.trim())
//                                        editingCategory = null
//                                        categoryInputText = ""
//                                    }
//                                }) {
//                                    Icon(
//                                        Icons.Default.Check,
//                                        contentDescription = "Save",
//                                        tint = Running
//                                    )
//                                }
//                            }
//                        } else {
//                            // ADD MODE: Show standard Add icon
//                            IconButton(onClick = {
//                                if (categoryInputText.isNotBlank()) {
//                                    onAdd(categoryInputText.trim())
//                                    categoryInputText = ""
//                                }
//                            }) {
//                                Icon(Icons.Default.Add, contentDescription = "Add", tint = Running)
//                            }
//                        }
//                    }
//                )
//
//                // List Existing Categories
//                LazyColumn(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .heightIn(max = 200.dp),
//                    verticalArrangement = Arrangement.spacedBy(8.dp)
//                ) {
//                    items(categories) { cat ->
//                        Row(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .background(SurfaceDarkElevated, RoundedCornerShape(8.dp))
//                                .padding(start = 12.dp),
//                            horizontalArrangement = Arrangement.SpaceBetween,
//                            verticalAlignment = Alignment.CenterVertically
//                        ) {
//                            Text(text = cat.name, color = White)
//                            Row {
//                                IconButton(onClick = {
//                                    // Trigger Edit Mode
//                                    editingCategory = cat
//                                    categoryInputText = cat.name
//                                }) {
//                                    Icon(
//                                        Icons.Default.Edit,
//                                        contentDescription = "Edit",
//                                        tint = Color.Gray,
//                                        modifier = Modifier.size(20.dp)
//                                    )
//                                }
//                                IconButton(onClick = { onDelete(cat) }) {
//                                    Icon(
//                                        Icons.Default.Delete,
//                                        contentDescription = "Delete",
//                                        tint = Color.Gray,
//                                        modifier = Modifier.size(20.dp)
//                                    )
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        },
//        confirmButton = {
//            TextButton(onClick = onDismiss) {
//                Text("Done", color = Running)
//            }
//        }
//    )
//}


// ── Dedicated Template Task Bottom Sheet ────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanTaskBottomSheet(
    initialTask: PromiseTask?,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (String, LocalTime, LocalTime, String) -> String?,
    onAddCategory: (CategoryEntity) -> Unit,
    onEditCategory: (oldCategory: CategoryEntity, newCategory: CategoryEntity) -> Unit,
    onDeleteCategory: (CategoryEntity) -> Unit,
) {
    // Populate with existing data if editing, or defaults if creating
    var title by remember(initialTask) { mutableStateOf(initialTask?.title ?: "") }
    var category by remember(initialTask) { mutableStateOf(initialTask?.category ?: "") }
    var startTime by remember(initialTask) {
        mutableStateOf(
            initialTask?.startTime ?: LocalTime.now().plusMinutes(10)
        )
    }
    var endTime by remember(initialTask) {
        mutableStateOf(
            initialTask?.endTimePlan ?: LocalTime.now().plusMinutes(70)
        )
    }

    // State for opening the nested category sheet
    var showCategorySheet by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
    ) {
        ForceWhiteStatusBarIcons()

        val keyboardController = LocalSoftwareKeyboardController.current
        val focusManager = LocalFocusManager.current
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                // ---  Add verticalScroll to prevent any future clipping! ---
                .verticalScroll(scrollState)
                // Reduced bottom padding since the giant button is gone
                .padding(start = MaterialTheme.spacing.huge, end = MaterialTheme.spacing.huge, bottom = MaterialTheme.spacing.huge),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (initialTask == null) "Add Task" else "Edit Task",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge
                )

                // The Sleek Checkmark Save Action
                IconButton(
                    onClick = {
                        focusManager.clearFocus()
                        keyboardController?.hide()

                        // Run the save function and check for errors
                        val error = onSave(title, startTime, endTime, category)
                        if (error != null) {
                            errorMessage = error // Keep sheet open and show error
                        } else {
                            onDismiss() // Validation passed, close sheet
                        }
                    },
                    enabled = title.isNotBlank(),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Running,
                        disabledContainerColor = Color(0xFF333333)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Save Task",
                        tint = if (title.isNotBlank()) White else Color.LightGray,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = Error, // Use your red error color
                                       style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.padding(bottom = MaterialTheme.spacing.small)

                )
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Task Name", color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = LuminousIndigo, // Matches the bright accent
                    unfocusedBorderColor = Color.DarkGray
                ),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if (title.isNotEmpty()) {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    }
                }),
            )

            // --- Dynamically find the category color ---
            val matchedCategory = categories.find { it.name.equals(category, ignoreCase = true) }
            val displayColor = matchedCategory?.let { Color(it.colorValue) } ?: Color.Gray

            // iOS-Style Full Width Category Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (category.isBlank()) Running.copy(alpha = 0.15f) else displayColor.copy(
                            alpha = 0.15f
                        )
                    )
                    .clickable { showCategorySheet = true }
                    .padding(MaterialTheme.spacing.large),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Category", color = Color.LightGray, style = MaterialTheme.typography.bodyLarge)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (category.isNotBlank()) category.uppercase() else "SELECT CATEGORY",
                        color = if (category.isNotBlank()) displayColor else Running,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(Modifier.width(MaterialTheme.spacing.small))
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = if (category.isNotBlank()) displayColor else Running,
                    )
                }
            }

            // Grouped Time Window Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceDarkElevated)
                    .padding(MaterialTheme.spacing.large)
            ) {
                Text("Time Window", color = Color.LightGray, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(MaterialTheme.spacing.medium))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TimeField(
                        modifier = Modifier.weight(1f),
                        time = startTime,
                        onTimeChange = { startTime = it },
                        enable = true,
                        bgColor = BackgroundDark,
                        borderColor = Color.DarkGray
                    )
                    Text("to", color = Color.Gray, modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium))
                    TimeField(
                        modifier = Modifier.weight(1f),
                        time = endTime,
                        onTimeChange = { endTime = it },
                        enable = true,
                        bgColor = BackgroundDark,
                        borderColor = Color.DarkGray
                    )
                }
            }

            // --- Mount the Unified Category Sheet over the Bottom Sheet ---
            if (showCategorySheet) {
                CategorySelectionAndManageSheet(
                    categories = categories,
                    currentCategory = category,
                    onDismiss = { showCategorySheet = false },
                    onSelect = {
                        category = it
                        showCategorySheet = false
                    },
                    onAdd = onAddCategory,
                    onEdit = onEditCategory,
                    onDelete = onDeleteCategory
                )
            }
        }
    }
}