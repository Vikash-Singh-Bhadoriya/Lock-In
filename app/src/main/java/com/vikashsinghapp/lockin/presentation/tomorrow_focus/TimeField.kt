package com.vikashsinghapp.lockin.presentation.tomorrow_focus

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.vikashsinghapp.lockin.R
import com.vikashsinghapp.lockin.formatTime
import com.vikashsinghapp.lockin.ui.theme.BackgroundDark
import java.time.LocalTime


@ExperimentalMaterial3Api
@Composable
fun TimeField(
    modifier: Modifier = Modifier,
    time: LocalTime,
    onTimeChange: (LocalTime) -> Unit,
    isLocked: Boolean,
) {
    var isDialogVisible by rememberSaveable { mutableStateOf(false) }
    // Dialog should not be used inside Scaffold, Column => apply paddding etc => size change
    if (isDialogVisible) {
        TimePickerInputDialog(
            time = time,
            onTimeChange = onTimeChange,
            hideTimeDialog = {
                isDialogVisible = false
            },
        )
    }
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(BackgroundDark)
            .padding(8.dp)
            .clickable {
                isDialogVisible = true
            },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = time.formatTime(),
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = if(isLocked) Color.Gray else Color.White,
        )
        Icon(
            modifier = Modifier.size(16.dp),
            painter = painterResource(R.drawable.ic_schedule),
            contentDescription = null,
            tint = if(isLocked) Color.Gray else Color.White,
        )
    }
}

//@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerInputDialog(
    time: LocalTime,
    onTimeChange: (LocalTime) -> Unit,
    hideTimeDialog: () -> Unit,
) {
    val timePickerState = rememberTimePickerState(
        initialHour = time.hour,
        initialMinute = time.minute,
        is24Hour = false
    )
    var showingPicker by remember { mutableStateOf(true) } // false => showInput
    val configuration = LocalConfiguration.current
    TimePickerDialog(
        title = if (showingPicker) {
            "Select Time "
        } else {
            "Enter Time"
        },
        onCancel = { hideTimeDialog() },
        onConfirm = {
            val localTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
            onTimeChange(localTime)
            hideTimeDialog()
        },
        toggle = {
            if (configuration.screenHeightDp > 400) {
                IconButton(onClick = { showingPicker = !showingPicker }) {
                    Icon(
                        painter = painterResource(
                            id = if (showingPicker) {
                                R.drawable.ic_keyboard
                            } else {
                                R.drawable.ic_schedule
                            }
                        ),
                        contentDescription = if (showingPicker) {
                            "Switch to Text Input"
                        } else {
                            "Switch to Touch Input"
                        },
                        tint = Color.Gray
                    )
                }
            }
        }
    ) {
        if (showingPicker && configuration.screenHeightDp > 400) {
            TimePicker(
                modifier = Modifier.padding(16.dp),
                state = timePickerState,
                colors = TimePickerDefaults.colors(),
                layoutType = TimePickerDefaults.layoutType()
            )
        } else {
            TimeInput(
                modifier = Modifier.padding(16.dp),
                state = timePickerState,
                colors = TimePickerDefaults.colors(),
            )
        }
    }
}

//@Composable
//fun Dp.dpToPx() = with(LocalDensity.current) { this@dpToPx.toPx() }

@Composable
fun Int.pxToDp() = with(LocalDensity.current) { this@pxToDp.toDp() }

// Compose Official Time Picker Dialog is not included in the M3 package YET
// So, I make it myself by seeing sample of Time Picker in official compose on github & cs.android (same code, sample, etc) => MORE THAN DOCUMENTATION
// https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/material3/material3/samples/src/main/java/androidx/compose/material3/samples/TimePickerSamples.kt
// https://github.com/androidx/androidx/blob/androidx-main/compose/material3/material3/samples/src/main/java/androidx/compose/material3/samples/TimePickerSamples.kt
@Composable
fun TimePickerDialog(
    title: String = "Select Time",
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    toggle: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        ),
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier = Modifier
                .width(IntrinsicSize.Min)
                .height(IntrinsicSize.Min)
                .background(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surface
                ),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    text = title,
                    style = MaterialTheme.typography.labelMedium
                )
                content()
                Row(
                    modifier = Modifier
                        .height(40.dp)
                        .fillMaxWidth()
                ) {
                    toggle()
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(
                        onClick = onCancel
                    ) { Text("Cancel") }
                    TextButton(
                        onClick = onConfirm
                    ) { Text("OK") }
                }
            }
        }
    }
}