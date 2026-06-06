package com.vikashsinghapp.lockin.presentation.roll_call

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vikashsinghapp.lockin.ui.theme.BackgroundDark
import com.vikashsinghapp.lockin.ui.theme.Running
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun TaskRollCallScreen(
    taskTitle: String,
    timeoutEpochMillis: Long, // Receive the absolute time here
    onLockIn: () -> Unit,
    onDelayRequested: () -> Unit,
) {
    // Dynamically calculate seconds remaining based on the system clock
    var secondsRemaining by remember {
        mutableLongStateOf((timeoutEpochMillis - System.currentTimeMillis()) / 1000L)
    }

    LaunchedEffect(timeoutEpochMillis) {
        while (secondsRemaining > 0) {
            delay(1000)
            secondsRemaining = (timeoutEpochMillis - System.currentTimeMillis()) / 1000L
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "ROLL CALL",
            color = Running,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 4.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = taskTitle,
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your scheduled task is starting.",
            color = Color.Gray,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Big, inviting Lock In button
        Button(
            onClick = onLockIn,
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Running),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text("Lock In 🎯", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Visual Countdown Timer
        val minutes = maxOf(0, secondsRemaining / 60)
        val seconds = maxOf(0, secondsRemaining % 60)

        Text(
            text = "Awaiting presence... ${
                String.format(
                    Locale.getDefault(),
                    "%02d:%02d",
                    minutes,
                    seconds
                )
            }",
            color = if (secondsRemaining < 60) Color.Red else Color.LightGray,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Small, text-only delay button (High visual friction)
        Text(
            text = "I can't start right now.",
            color = Color.DarkGray,
            fontSize = 14.sp,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { onDelayRequested() }
                .padding(8.dp)
        )
    }
}