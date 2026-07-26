package com.vikashsinghapp.lockin.presentation.active_focus

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vikashsinghapp.lockin.data.entity.JournalMessage
import com.vikashsinghapp.lockin.presentation.core.AutoResizeText
import com.vikashsinghapp.lockin.presentation.core.HideNavigationBar
import com.vikashsinghapp.lockin.presentation.journal.JournalLogCardSimple
import com.vikashsinghapp.lockin.presentation.journal.toTimeString
import com.vikashsinghapp.lockin.presentation.task_detail.JournalInputBar
import com.vikashsinghapp.lockin.ui.theme.BackgroundDark
import com.vikashsinghapp.lockin.ui.theme.Running
import com.vikashsinghapp.lockin.ui.theme.SurfaceDarkElevated
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ActiveFocusScreen(
    taskTitle: String,
    timeRemainingFormatted: String, // e.g. "01:45:22"
    progressPercentage: Float, // 0.0f to 1.0f
    themeColorValue: Long?,
    journalMessages: List<JournalMessage>,
    onAddLog: (String) -> Unit,
    onEndEarlySession: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val context = LocalContext.current

    val themeColor = themeColorValue?.let { Color(it) } ?: Running

    if (isLandscape) {
        // ==========================================
        // LANDSCAPE: The Desk Clock Mode
        // ==========================================
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            Toast.makeText(
                                context,
                                "Rotate to portrait to unlock.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            // 2-Pixel Progress Line at the very top
            LinearProgressIndicator(
                progress = { progressPercentage },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .align(Alignment.TopCenter),
                color = themeColor,
                trackColor = Color.Transparent,
                strokeCap = StrokeCap.Round
            )

            // Massive Cinematic Timer
            AutoResizeText(
                text = timeRemainingFormatted,
                color = themeColor, // Color Coded
                style = TextStyle(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp
                )
            )
        }
    } else {
        // ==========================================
        // PORTRAIT: The Terminal Dashboard
        // ==========================================
        var logInput by remember { mutableStateOf("") }

        Scaffold(
            containerColor = BackgroundDark,
            contentWindowInsets = WindowInsets(0.dp),
            bottomBar = {
                Box(modifier = Modifier.consumeWindowInsets(WindowInsets.navigationBars)) {
                    JournalInputBar(
                        value = logInput,
                        onValueChange = { logInput = it },
                        onSend = {
                            onAddLog(it)
                            logInput = ""
                        },
                        hintText = "Message",
                    )
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 12.dp),
            ) {
                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = taskTitle.uppercase(),
                        color = Color(0xFF8696A0),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.weight(1f),
                    )
                    GhostHoldToEndEarlyButton(onEndEarly = onEndEarlySession)
                }

                Spacer(Modifier.height(24.dp))

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                ) {
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.size(220.dp),
                        color = SurfaceDarkElevated,
                        strokeWidth = 14.dp,
                    )
                    CircularProgressIndicator(
                        progress = { progressPercentage },
                        modifier = Modifier.size(220.dp),
                        color = themeColor,
                        trackColor = Color.Transparent,
                        strokeCap = StrokeCap.Round,
                        strokeWidth = 13.dp,
                    )
                    Text(
                        text = timeRemainingFormatted,
                        color = themeColor,
                        fontSize = 46.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    "Session Logs",
                    color = Color(0xFF8696A0),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp),
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(BackgroundDark),
                ) {
                    if (journalMessages.isEmpty()) {
                        Text(
                            text = "No notes yet. Log progress below.",
                            color = Color(0xFF8696A0),
                            fontSize = 14.sp,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp),
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            items(journalMessages, key = { it.id }) { msg ->
                                JournalLogCardSimple(
                                    text = msg.content,
                                    timestamp = msg.timestamp.toTimeString(),
                                    borderColor = Running,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// THE 3-SECOND GHOST BUTTON
// ==========================================
@Composable
fun GhostHoldToEndEarlyButton(onEndEarly: () -> Unit) {
    var holdProgress by remember { mutableFloatStateOf(0f) }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = 1.dp,
                color = if (holdProgress > 0f) Color.Red.copy(alpha = holdProgress) else Color.DarkGray,
                shape = RoundedCornerShape(8.dp)
            )
            .background(if (holdProgress > 0f) Color.Red.copy(alpha = 0.1f) else Color.Transparent)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        val holdJob = coroutineScope.launch {
                            // Loop for 3 seconds (3000ms), updating every 30ms (100 steps)
                            for (i in 1..100) {
                                delay(30)
                                holdProgress = i / 100f
                            }
                            // If they hold for the full 3 seconds, trigger the forfeit!
                            onEndEarly()
                            holdProgress = 0f
                        }

                        // Wait for them to release their finger
                        tryAwaitRelease()

                        // If they release early, cancel the job and reset the red ring
                        holdJob.cancel()
                        holdProgress = 0f
                    }
                )
            }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (holdProgress > 0f) "HOLDING..." else "HOLD TO END EARLY",
            color = if (holdProgress > 0f) Color.Red else Color.DarkGray,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}