package com.vikashsinghapp.lockin.presentation.tomorrow_focus

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vikashsinghapp.lockin.data.entity.PromiseTask
import com.vikashsinghapp.lockin.ui.theme.BackgroundDark
import com.vikashsinghapp.lockin.ui.theme.SurfaceDark

// This screen exists so the user can plan tomorrow in under 10 minutes.
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TomorrowFocusScreen(modifier: Modifier = Modifier) {

    val taskList = remember {
        listOf(
            PromiseTask(
                1,
                "Morning Routine",
                System.currentTimeMillis(),
                System.currentTimeMillis() + 100000
            ),
            PromiseTask(
                2,
                "Deep Work",
                System.currentTimeMillis() + 2390,
                System.currentTimeMillis() + 70000
            ),
            PromiseTask(
                3,
                "DSA",
                System.currentTimeMillis() + 1000,
                System.currentTimeMillis() + 10000
            ),
            PromiseTask(
                4,
                "Workout",
                System.currentTimeMillis() + 9004,
                System.currentTimeMillis() + 50000
            ),
        )
    }
    val listState = rememberLazyListState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Tomorrow's Focus",
            color = Color.White,
            fontSize = 28.sp
        )
        Text(
            "Plan your day with intention",
            color = Color.Gray,
            fontSize = 16.sp
        )

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(taskList, key = { it.id }) { task ->
                TaskBlock(
                    modifier = modifier,
                    title = task.title,
                    startTime = task.startTime,
                    endTime = task.endTime,
                    onTitleChange = {},
                    onStartTimeChange = {

                    },
                    onEndTimeChange = {

                    }, onDeleteTask = {

                    }
                )
            }
        }

        TextButton(
            onClick = {

            }
        ) { Text("Add Task") }

        TextButton(
            onClick = {

            }
        ) { Text("Save Plan") }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskBlock(
    modifier: Modifier = Modifier,
    title: String,
    startTime: Long,
    endTime: Long,
    onTitleChange: (String) -> Unit,
    onStartTimeChange: (Long) -> Unit,
    onEndTimeChange: (Long) -> Unit,
    onDeleteTask: () -> Unit,
) {

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(0.92f))
            .background(SurfaceDark)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onDeleteTask) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = null,
                    tint = Color.Gray
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TimeField(
                modifier = Modifier,
                time = startTime,
            ) {

            }
            TimeField(
                modifier = Modifier,
                time = endTime,
            ) {

            }
        }

    }
}