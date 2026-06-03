package com.vikashsinghapp.lockin.presentation.tomorrow_focus

import CategorySelectionAndManageSheet
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.vikashsinghapp.lockin.data.entity.CategoryEntity
import com.vikashsinghapp.lockin.data.entity.TemplateTaskEntity
import com.vikashsinghapp.lockin.presentation.tomorrow_focus.component.TaskCard
import com.vikashsinghapp.lockin.presentation.tomorrow_focus.component.TimeField
import com.vikashsinghapp.lockin.ui.theme.BackgroundDark
import com.vikashsinghapp.lockin.ui.theme.Error
import com.vikashsinghapp.lockin.ui.theme.ForceWhiteStatusBarIcons
import com.vikashsinghapp.lockin.ui.theme.Running
import com.vikashsinghapp.lockin.ui.theme.SurfaceDark
import com.vikashsinghapp.lockin.ui.theme.SurfaceDarkElevated
import com.vikashsinghapp.lockin.ui.theme.spacing
import sh.calvin.reorderable.DragGestureDetector
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import timber.log.Timber
import java.time.LocalTime


// The distinct Indigo color to differentiate from the normal Plan Screen
val IndigoTemplate = Color(0xFF3F51B5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanEditorScreen(
    onBack: () -> Unit,
    viewModel: PlanEditorViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val categories by viewModel.categories.collectAsState()

    // You will use this state to show your existing AddTaskBottomSheet!
    var isSheetOpen by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<TemplateTaskEntity?>(null) } // Tracks if we are adding or editing

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = { Text("Plan Editor", color = Color.White, style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.saveTemplate(onSaved = onBack) },
                        // Prevent saving a template without a name
                        enabled = uiState.name.isNotBlank()
                    ) {
                        Text("Save", color = if (uiState.name.isNotBlank()) Color.White else Color.Gray, style = MaterialTheme.typography.titleSmall)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = IndigoTemplate)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    taskToEdit = null // Null means "Create New"
                    isSheetOpen = true
                },
                containerColor = IndigoTemplate,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task to Plan")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Distinct Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF283593))
                    .padding(MaterialTheme.spacing.small),
                contentAlignment = Alignment.Center
            ) {
                Text("Changes here do not affect today's live plan.", color = Color.White, style = MaterialTheme.typography.labelLarge)
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(MaterialTheme.spacing.massive)
                )
            } else {
                val lazyListState = rememberLazyListState()
                val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
                    Timber.d("index from $from to $to")
                    // Cast the key to Long because your task.id is a Long
                    viewModel.reorderTasksByKey(from.key as Long, to.key as Long)
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = lazyListState,
                    contentPadding = PaddingValues(MaterialTheme.spacing.extraLarge)
                ) {
                    // Template Name Input
                    item {
                        OutlinedTextField(
                            value = uiState.name,
                            onValueChange = { viewModel.updateName(it) },
                            label = { Text("Plan Name", color = Color.Gray) },
                            placeholder = { Text("e.g. Weekends Plan", color = Color.DarkGray) },
                            textStyle = MaterialTheme.typography.titleMedium.copy(color = Color.White),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = IndigoTemplate,
                                unfocusedBorderColor = Color.DarkGray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                        )

                        Spacer(Modifier.height(MaterialTheme.spacing.huge))
                        Text("Plan Tasks", color = Color.White, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(MaterialTheme.spacing.large))
                    }

                    // Task List
                    if (uiState.tasks.isEmpty()) {
                        item {
                            Text("No tasks in this plan yet. Tap + to add.", color = Color.Gray, modifier = Modifier.padding(top = MaterialTheme.spacing.extraLarge))
                        }
                    } else {
                        items(uiState.tasks, key = {
                            it.id
                        }) { task ->

                            ReorderableItem(state = reorderableState, key = task.id) { isDragging ->
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

                                TaskCard(
                                    modifier = Modifier.shadow(elevation),
                                    title = task.title,
                                    category = task.category,
                                    categoryColor = accentColor,
                                    startTime = task.startTime,
                                    endTime = task.endTimePlan,
                                    isLocked = false, // Always false in the Plan Editor
                                    onCardClick = {
                                        taskToEdit = task
                                        isSheetOpen = true
                                    },
                                    onDuplicateClick = { viewModel.duplicateTask(task) },
                                    onDeleteClick = { viewModel.removeTask(task) },
                                    dragModifier = Modifier.draggableHandle(
                                        dragGestureDetector = DragGestureDetector.Press
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
        // --- The actual Bottom Sheet implementation ---
        if (isSheetOpen) {
            TemplateTaskBottomSheet(
                initialTask = taskToEdit,
                categories = categories, // Passing DB categories down!
                onDismiss = { isSheetOpen = false },
                onSave = { title, start, end, category ->
                    viewModel.validateAndSaveTask(
                        id = taskToEdit?.id,
                        title = title,
                        startTime = start,
                        endTimePlan = end,
                        category = category
                    )
                },
                // Pass the viewmodel functions down for the category sheet
                onAddCategory = { viewModel.addCategory(it) },
                onEditCategory = { old, newName -> viewModel.editCategory(old, newName) },
                onDeleteCategory = { viewModel.deleteCategory(it) }
            )
        }
    }
}

// ── Dedicated Template Task Bottom Sheet ────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateTaskBottomSheet(
    initialTask: TemplateTaskEntity?,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (String, LocalTime, LocalTime, String) -> String?,
    onAddCategory: (CategoryEntity) -> Unit,
    onEditCategory: (CategoryEntity, CategoryEntity) -> Unit,
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

    val LuminousIndigo = Color(0xFF8C9EFF)

    // State for opening the nested category sheet
    var showCategorySheet by remember { mutableStateOf(false) }

    // Hold the error message locally
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
                        // 🟢 Run the save function and check for errors
                        errorMessage = onSave(title, startTime, endTime, category)

                        if (errorMessage == null) {
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
                        modifier = Modifier.size(MaterialTheme.spacing.iconSizeLarge)
                    )
                }
            }
            // 🟢 Show the error message right below the title row
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = Error,
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