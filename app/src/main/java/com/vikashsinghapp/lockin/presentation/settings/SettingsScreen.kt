package com.vikashsinghapp.lockin.presentation.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen() {
    NotificationTimingInfoScreen()
}

@Composable
fun NotificationTimingInfoScreen() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = "Notification Timing on Your Device",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = White
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Lock-In aims to show session notifications at the exact scheduled time. However, some devices (e.g. Vivo, Oppo, Xiaomi) may delay notifications by 10–60 seconds due to battery and background restrictions, even if battery optimization is disabled.",
            style = MaterialTheme.typography.bodyMedium, color = White
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "What you can do:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold, color = White
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "• Whitelist Lock-In in your device's battery and background settings.\n• Enable 'Autostart' or 'Run in background' for Lock-In.\n• Disable 'App battery saver' for Lock-In.",
            style = MaterialTheme.typography.bodyMedium, color = White
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Note: This is a device limitation. Exact timing is guaranteed only on standard Android devices (e.g. Motorola, Pixel, Samsung).",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}
