package com.vikashsinghapp.lockin.presentation.today.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

// The Beautiful Empty State
@Composable
fun EmptyPlanState(
    modifier: Modifier = Modifier,
    date: LocalDate,
    onPlanClick: () -> Unit
) {
    val isToday = date.isEqual(LocalDate.now())
    val title = if (isToday) "No Active Contract" else "Tomorrow is Unwritten"
    val subtitle = if (isToday) "Draft your schedule and lock in your focus for today." else "Prepare your execution plan for tomorrow."
    val buttonText = if (isToday) "Plan Your Day" else "Draft Tomorrow's Plan"

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Placeholder for your Lottie Animation
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(com.vikashsinghapp.lockin.ui.theme.SurfaceDarkElevated),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = com.vikashsinghapp.lockin.ui.theme.Running,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(Modifier.height(24.dp))

        Text(text = title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            text = subtitle,
            color = Color.Gray,
            fontSize = 14.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(Modifier.height(32.dp))

        androidx.compose.material3.Button(
            onClick = onPlanClick,
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = com.vikashsinghapp.lockin.ui.theme.Running
            ),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            modifier = Modifier.height(56.dp).padding(horizontal = 32.dp).fillMaxWidth()
        ) {
            Text(buttonText, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}