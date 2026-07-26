package com.vikashsinghapp.lockin.presentation.journal

import android.content.ClipData
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.vikashsinghapp.lockin.ui.theme.BackgroundDark
import com.vikashsinghapp.lockin.ui.theme.SurfaceDark
import com.vikashsinghapp.lockin.ui.theme.SurfaceDarkElevated
import com.vikashsinghapp.lockin.ui.theme.spacing
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(
    modifier: Modifier = Modifier,
    viewModel: JournalViewModel = hiltViewModel(),
    onOpenDrawer: () -> Unit,
) {
    val messages by viewModel.filteredMessages.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val activeCategory by viewModel.selectedCategory.collectAsState()

    val selectedMessageIds by viewModel.selectedMessages.collectAsState()
    val isSelectionMode = selectedMessageIds.isNotEmpty()

    var inputText by rememberSaveable { mutableStateOf("") }
    var showExportMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundDark,
        topBar = {
            // ---  DYNAMIC TOP BAR ---
            if (isSelectionMode) {
                TopAppBar(
                    title = { Text("${selectedMessageIds.size}", color = Color.White, style = MaterialTheme.typography.titleMedium) },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color.White)
                        }
                    },
                    actions = {
                        // Copy Button
                        IconButton(onClick = {
                            coroutineScope.launch {
                                // Combine all selected text
                                val textToCopy = messages
                                    .filter { selectedMessageIds.contains(it.message.id) }
                                    .joinToString("\n\n") { it.message.content }

                                val clipData = ClipData.newPlainText("Journal Logs", textToCopy)
                                clipboard.setClipEntry(ClipEntry(clipData))
                                Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                                viewModel.clearSelection()
                            }
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color.White)
                        }

                        // Delete Button
                        IconButton(onClick = { viewModel.deleteSelectedMessages() }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E1E)) // Slightly lighter dark for selection mode
                )
            } else {
                TopAppBar(
                    title = { Text("Journal", color = Color.White, style = MaterialTheme.typography.titleMedium) },
                    navigationIcon = {
                        IconButton(onClick = onOpenDrawer) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = Color.White)
                        }
                    },
                    actions = {
                        Row(modifier = Modifier.padding(end = MaterialTheme.spacing.small)) {
                            IconButton(onClick = { showExportMenu = true }) {
                                Icon(
                                    Icons.Default.Share,
                                    contentDescription = "Export",
                                    tint = Color.White
                                )
                            }
                            DropdownMenu(
                                expanded = showExportMenu,
                                onDismissRequest = { showExportMenu = false },
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Export All") },
                                    onClick = {
                                        showExportMenu = false
                                        viewModel.exportJournals(context, true)
                                    }
                                )
                                if (activeCategory != "All") {
                                    DropdownMenuItem(
                                        text = { Text("Export '$activeCategory'") },
                                        onClick = {
                                            showExportMenu = false
                                            viewModel.exportJournals(context, false)
                                        }
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
                )
            }
        }
    ) { innerPadding ->

        // Since the NavHost passes the innerPadding via the modifier,
        // we just use fillMaxSize() and imePadding() to handle the keyboard
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark)
                .padding(innerPadding)
                .imePadding()
        ) {
            val listState = rememberLazyListState()

            // Auto-scroll ONLY when a new message is added AND user was at bottom
            LaunchedEffect(messages.size) {
                if (messages.isNotEmpty()) {
                    // do not do animateScrollTo => kind of freeze when at the top & add message, scroll laggingly
                    listState.scrollToItem(messages.lastIndex)
                }
            }

//        // 1. Category Filter Strip
//        LazyRow(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//            items(categories) { cat ->
//                FilterChip(
//                    selected = cat.id == activeCategoryId,
//                    onClick = { viewModel.setCategoryId(cat.id) },
//                    label = { Text(cat.name, color = White) }
//                )
//            }
//        }
            /*
                        When the keyboard opens:
                        The semantic intent (“user was at bottom”) is lost
                        We must capture intent BEFORE resize
                        Then restore scroll AFTER resize
                        */
            // Detect if user is already near bottom
            var userWasAtBottom by remember { mutableStateOf(true) }

            LaunchedEffect(listState) {
                snapshotFlow {
                    val lastVisible =
                        listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
                    lastVisible == messages.lastIndex
                }.collect { atBottom ->
                    userWasAtBottom = atBottom
                }
            }
//            LaunchedEffect(messages.size) {
//                if (messages.isNotEmpty() && isAtBottom.value) {
//                    listState.animateScrollToItem(messages.lastIndex)
//                }
//            }
            // 1. Category Filter Strip
            if (categories.size > 1) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceDark),
                    contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.large, vertical = MaterialTheme.spacing.small),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                ) {
                    items(categories) { cat ->
                        val isSelected = cat == activeCategory
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) Color.White else SurfaceDarkElevated)
                                .clickable { viewModel.setCategory(cat) }
                                .padding(horizontal = MaterialTheme.spacing.large, vertical = MaterialTheme.spacing.small)
                        ) {
                            Text(
                                text = cat.uppercase(),
                                color = if (isSelected) Color.Black else Color.White,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }

            // 3. THE LIST: weight(1f) tells the list to take all available space
            // but shrink when the keyboard (IME) pushes the input bar up.
            if (messages.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.spacing.massive),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(SurfaceDarkElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Empty Journal",
                                tint = Color.Gray,
                                modifier = Modifier.size(MaterialTheme.spacing.iconSizeLarge)
                            )
                        }
                        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
                        Text(
                            text = "No logs yet",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
                        Text(
                            text = "Start typing below to capture your thoughts, log your progress, or document happening.",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                MessageList(
                    modifier = Modifier.weight(1f),
//                    .border(5.dp, Color.Blue)
                    selectedIds = selectedMessageIds,
                    isSelectionMode = isSelectionMode,
                    onToggleSelection = { viewModel.toggleMessageSelection(it) },
                    messages = messages,
                    listState = listState,
                )
            }
            // detect open keyboard
            val imeBottom = WindowInsets.ime.getBottom(LocalDensity.current)
            LaunchedEffect(imeBottom) {
                // Only scroll to bottom if keyboard is open and user was at bottom
                // not scrolling when not at bottom
                if (imeBottom > 0 && messages.isNotEmpty() && userWasAtBottom) {
                    listState.scrollToItem(messages.lastIndex)
                }
            }

            // 4. THE INPUT: imePadding() makes this bar "stick" to the top of the keyboard. =>
            // without it the message input just show statically at the bottom of screen
            MessageInputBar(
                modifier = Modifier
//                    .border(2.dp, Error)
                    .imePadding(),
                text = inputText,
                onTextChange = { inputText = it },
                onSend = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendMessage(inputText.trim())
                        inputText = ""
                    }
                }
            )
        }
    }
}