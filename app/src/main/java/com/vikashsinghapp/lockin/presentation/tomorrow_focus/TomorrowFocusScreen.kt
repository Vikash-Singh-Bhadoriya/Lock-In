package com.vikashsinghapp.lockin.presentation.tomorrow_focus

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.vikashsinghapp.lockin.ui.theme.BackgroundDark
import com.vikashsinghapp.lockin.ui.theme.SurfaceDark
import java.time.LocalTime

// This screen exists so the user can plan tomorrow in under 10 minutes.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TomorrowFocusScreen(
    modifier: Modifier = Modifier,
    viewModel: TomorrowFocusViewModel = hiltViewModel(),
) {

    val taskList by viewModel.promiseTasks.collectAsState()

    var isLocked by remember { mutableStateOf(false) }

    Scaffold(
        // systemBarsPadding() <-- if not apply then the input bar is like 20.dp away from bottom when keyboard appear
        modifier = modifier.fillMaxSize(), // Use fillMaxSize to own the window space => the system status bar also
        containerColor = BackgroundDark,
        topBar = {
            // This Top App Bar includes the system status bar also.
            // If I set the background color to red, the system status bar background color changes to red
            TopAppBar(
                title = { Text("Tomorrow's Focus", color = Color.White, fontSize = 24.sp) },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Menu, contentDescription = null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
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
            val listState = rememberLazyListState()

            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(12.dp)
            ) {
                items(taskList, key = { it.id }) { task ->
                    TaskBlock(
                        modifier = modifier,
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
                        }, onDeleteTask = {
                            viewModel.onEvent(TomorrowFocusEvent.DeleteTask(task))
                        },
                        isLocked = isLocked
                    )
                }
            }

            if (!isLocked) {
                TextButton(
                    onClick = {
                        viewModel.onEvent(TomorrowFocusEvent.AddNewTask)
                    }
                ) {
                    Text("Add Task")
                }

                TextButton(
                    onClick = {
                        isLocked = true
                    }
                ) {
                    Text("Save Plan")
                }
            } else {
                Text(
                    modifier = Modifier.padding(vertical = 8.dp),
                    text = "🔒 Plan locked",
                    color = Color.White
                )
            }
        }
        if (isLocked) {
            // cannot click on any of button on the screen when plan locked
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
                    .clickable(enabled = false) {}
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskBlock(
    modifier: Modifier = Modifier,
    isLocked: Boolean,
    title: String,
    startTime: LocalTime,
    endTime: LocalTime,
    onTitleChange: (String) -> Unit,
    onStartTimeChange: (LocalTime) -> Unit,
    onEndTimeChange: (LocalTime) -> Unit,
    onDeleteTask: () -> Unit,
) {

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark),
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
                Text(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), text = "🔒 $title", color = Color.White)
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
                    colors = OutlinedTextFieldDefaults.colors(),
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

                IconButton(onClick = onDeleteTask) {
                    Icon(
                        modifier = Modifier.size(16.dp),
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = Color.Gray
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
                isLocked = isLocked
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
                isLocked = isLocked
            )
        }
    }
}