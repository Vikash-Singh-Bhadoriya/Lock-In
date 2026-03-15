package com.vikashsinghapp.lockin.presentation.settings

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.vikashsinghapp.lockin.system.deviceadmin.LockInDeviceAdminReceiver
import com.vikashsinghapp.lockin.ui.theme.SurfaceDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onOpenDrawer: () -> Unit,
) {
    val isAdminActive by viewModel.isAdminActive.collectAsState()
    val showAdminDialog by viewModel.showAdminDialog.collectAsState()
    val context = LocalContext.current

    // Launcher: opens system Device Admin activation screen
    // and refreshes the toggle state when user comes back
    val adminLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.refreshAdminState()
    }

    // Show dialog if pending
    if (showAdminDialog) {
        DeviceAdminExplanationDialog(
            onConfirm = {
                viewModel.onDialogConfirmed()
                // Navigate to system Device Admin permission screen
                val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                    putExtra(
                        DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                        ComponentName(context, LockInDeviceAdminReceiver::class.java)
                    )
                    putExtra(
                        DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                        "LockIn needs Device Manager permission to prevent uninstall during task."
                    )
                }
                adminLauncher.launch(intent)
            },
            onDismiss = {
                viewModel.onDialogDismissed()
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = Color.White, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
                .padding(24.dp)
        ) {
            Spacer(Modifier.height(24.dp))

            // ── Prevent Uninstall Toggle ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Prevent App Uninstall",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = White
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "During running task  •  Requires Device Manager",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                Switch(
                    checked = isAdminActive,
                    onCheckedChange = { checked ->
                        viewModel.onToggleChanged(checked)
                    }
                )
            }

            // Warning hint shown when admin is active
            if (isAdminActive) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "⚠️ Device admin is active. To uninstall LockIn, turn this off first.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFFFA726) // Orange warning
                )
            }

            HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))
            Spacer(Modifier.height(24.dp))

            // ── Existing notification timing section ──
            NotificationTimingInfoScreen()
        }
    }
}

// ── Dialog ──────────────────────────────────────────────────────────────────

@Composable
private fun DeviceAdminExplanationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Prevent App Uninstall During Alarm",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "LockIn needs Device Manager permission to prevent uninstall during task.\n\n" +
                        "If you set this on, you can't uninstall LockIn. " +
                        "If you want to uninstall LockIn, please turn off this option first.\n\n" +
                        "(This needs Device Manager permission)",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun NotificationTimingInfoScreen() {
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
