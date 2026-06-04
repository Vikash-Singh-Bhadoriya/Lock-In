package com.vikashsinghapp.lockin.presentation.settings

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.vikashsinghapp.lockin.R
import com.vikashsinghapp.lockin.formatTime
import com.vikashsinghapp.lockin.presentation.tomorrow_focus.component.TimePickerInputDialog
import com.vikashsinghapp.lockin.system.alarm.TaskAlarmScheduler
import com.vikashsinghapp.lockin.system.deviceadmin.LockInDeviceAdminReceiver
import com.vikashsinghapp.lockin.ui.theme.ForceWhiteStatusBarIcons
import com.vikashsinghapp.lockin.ui.theme.Running
import com.vikashsinghapp.lockin.ui.theme.SurfaceDark
import com.vikashsinghapp.lockin.ui.theme.SurfaceDarkElevated
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onOpenDrawer: () -> Unit,
    onNavigateToOnboardingReview: () -> Unit,
    onNavigateToFaq: () -> Unit,
    onNavigateToFeedbackScreen: () -> Unit,
    onNavigateToAttributions: () -> Unit
) {
    val isAdminActive by viewModel.isAdminActive.collectAsState()
    val showAdminDialog by viewModel.showAdminDialog.collectAsState()
    val context = LocalContext.current

    // Overlay States
    var showNotificationSheet by remember { mutableStateOf(false) }
    var showAboutSheet by remember { mutableStateOf(false) }
//    var showAttributionDialog by remember { mutableStateOf(false) }

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

    val isReminderEnabled by viewModel.isReminderEnabled.collectAsState()
    val reminderHour by viewModel.reminderHour.collectAsState()
    val reminderMinute by viewModel.reminderMinute.collectAsState()
    var showTimePicker by remember { mutableStateOf(false) }

    val reminderTimeText = LocalTime.of(reminderHour, reminderMinute).formatTime()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = White, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // --- CARD 1: SETUP & PREFERENCES ---
            SettingsGroup(title = "SETUP & PREFERENCES") {
                SettingsRow(
                    icon = Icons.Default.PlayCircleOutline,
                    title = "How to use LockIn",
                    onClick = onNavigateToOnboardingReview
                )
                HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.5f), modifier = Modifier.padding(start = 48.dp))
                SettingsRow(
                    icon = Icons.Default.Build,
                    title = "Notification Fixes",
                    subtitle = "For Vivo, Oppo, Xiaomi devices",
                    onClick = { showNotificationSheet = true }
                )
                HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.5f), modifier = Modifier.padding(start = 48.dp))
                SettingsRow(
                    icon = Icons.Default.Security,
                    title = "Prevent App Uninstall",
                    subtitle = "Requires Device Manager",
                    rightElement = {
                        Switch(
                            checked = isAdminActive,
                            onCheckedChange = { checked -> viewModel.onToggleChanged(checked) },
                            colors = SwitchDefaults.colors(checkedTrackColor = Running)
                        )
                    },
                    onClick = { viewModel.onToggleChanged(!isAdminActive) }
                )
                SettingsRow(
                    icon = Icons.Default.Schedule,
                    title = "Planning Reminder",
                    subtitle = if (isReminderEnabled) "Daily at $reminderTimeText" else "Disabled",
                    rightElement = {
                        Switch(
                            checked = isReminderEnabled,
                            onCheckedChange = { checked ->
                                viewModel.setReminderEnabled(checked)
                                if (checked) {
                                    TaskAlarmScheduler.scheduleDailyReminder(context, reminderHour, reminderMinute)
                                } else {
                                    // You need a cancel function in TaskAlarmScheduler for ID 999
                                    TaskAlarmScheduler.cancelDailyReminder(context)
                                }
                            },
                            colors = SwitchDefaults.colors(checkedTrackColor = Running)
                        )
                    },
                    onClick = { if (isReminderEnabled) showTimePicker = true }
                )

                // Looks like security warning, if user want to delete app => can do from settings
//                // Active Admin Warning
//                if (isAdminActive) {
//                    Text(
//                        modifier = Modifier.padding(start = 48.dp, bottom = 12.dp, end = 16.dp),
//                        text = "⚠️ Device admin is active. To uninstall LockIn, turn this off first.",
//                        style = MaterialTheme.typography.bodySmall,
//                        color = Color(0xFFFFA726),
//                    )
//                }
            }

            // --- CARD 2: SUPPORT ---
            SettingsGroup(title = "SUPPORT") {
                SettingsRow(
                    icon = Icons.Default.Share,
                    title = "Share LockIn",
                    subtitle = "Help others build discipline",
                    onClick = { context.shareApp() }
                )
                HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.5f), modifier = Modifier.padding(start = 48.dp))
                SettingsRow(
                    icon = Icons.AutoMirrored.Filled.HelpOutline,
                    title = "FAQ",
                    onClick = onNavigateToFaq
                )
                HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.5f), modifier = Modifier.padding(start = 48.dp))
                SettingsRow(
                    icon = Icons.Default.ChatBubbleOutline,
                    title = "Send Feedback",
                    subtitle = "Bugs or suggestions",
                    onClick = onNavigateToFeedbackScreen
                )
            }

            // --- CARD 3: LEGAL ---
            SettingsGroup(title = "LEGAL") {
                SettingsRow(
                    icon = Icons.Default.Info,
                    title = "About LockIn",
                    onClick = { showAboutSheet = true }
                )
            }

            Spacer(Modifier.height(32.dp))
        }

        // --- OVERLAYS ---
        if (showNotificationSheet) {
            ModalBottomSheet(
                onDismissRequest = { showNotificationSheet = false },
                containerColor = SurfaceDark,
            ) {
                ForceWhiteStatusBarIcons()
                NotificationTimingSheetContent()
            }
        }

        if (showAboutSheet) {
            ModalBottomSheet(
                onDismissRequest = { showAboutSheet = false },
                containerColor = SurfaceDark
            ) {
                ForceWhiteStatusBarIcons()
                AboutLockInSheetContent(
                    context = context,
                    onShowAttributions = {
                        showAboutSheet = false
                        onNavigateToAttributions()
                    }
                )
            }
        }

        if (showTimePicker) {
            TimePickerInputDialog(
                time = LocalTime.of(reminderHour, reminderMinute),
                onTimeChange = { newTime ->
                    viewModel.setReminderTime(newTime.hour, newTime.minute)
                    TaskAlarmScheduler.scheduleDailyReminder(context, newTime.hour, newTime.minute)
                    showTimePicker = false
                },
                hideTimeDialog = { showTimePicker = false }
            )
        }

//        if (showAttributionDialog) {
//            AlertDialog(
//                onDismissRequest = { showAttributionDialog = false },
//                containerColor = SurfaceDarkElevated,
//                title = { Text("Attributions", color = White, fontWeight = FontWeight.Bold) },
//                text = {
//                    Text(
//                        "Animations provided by LottieFiles.\n\n" +
//                                "• Calendar by Paresh Deshpande\n" +
//                                "• Notification by Théo\n" +
//                                "• Alarm by 엄도연\n" +
//                                "• Lock by Théo",
//                        color = Color.LightGray,
//                        lineHeight = 22.sp
//                    )
//                },
//                confirmButton = {
//                    TextButton(onClick = { showAttributionDialog = false }) {
//                        Text("Close", color = Running)
//                    }
//                }
//            )
//        }
    }
}

// ── Premium UI Components ──────────────────────────────────────────────────

@Composable
fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            color = Color.Gray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceDarkElevated),
            content = content
        )
    }
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    rightElement: (@Composable () -> Unit)? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            if (subtitle != null) {
                Text(text = subtitle, color = Color.Gray, fontSize = 13.sp)
            }
        }
        if (rightElement != null) {
            rightElement()
        } else {
            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(16.dp))
        }
    }
}

// ── Sheet Contents ─────────────────────────────────────────────────────────

@Composable
fun NotificationTimingSheetContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, bottom = 48.dp)
    ) {
        Text(text = "Notification Timing Fixes", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = White)
        Spacer(Modifier.height(16.dp))
        Text(
            text = "LockIn aims to show session notifications exactly on time. However, some devices (Vivo, Oppo, Xiaomi) may delay them by 10–60 seconds due to aggressive background restrictions.",
            style = MaterialTheme.typography.bodyMedium, color = Color.LightGray
        )
        Spacer(Modifier.height(24.dp))
        Text("What you can do in your Phone Settings:", fontWeight = FontWeight.Bold, color = White)
        Spacer(Modifier.height(8.dp))
        Text("• Whitelist LockIn in battery settings.\n• Enable 'Autostart' or 'Run in background'.\n• Disable 'App battery saver'.", style = MaterialTheme.typography.bodyMedium, color = Color.LightGray)
        Spacer(Modifier.height(24.dp))
        Text("Note: Exact timing is guaranteed automatically only on standard Android devices (Motorola, Pixel, Samsung).", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
    }
}

@Composable
fun AboutLockInSheetContent(context: Context, onShowAttributions: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_app_notification), // or ic_launcher_round
            contentDescription = "LockIn Logo",
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
        )
        Spacer(Modifier.height(8.dp))
        Text("LockIn", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = White)
        Text("Version 1.0.0", fontSize = 14.sp, color = Color.Gray)
        Spacer(Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceDarkElevated)
        ) {
            SettingsRow(icon = Icons.Default.Description, title = "Privacy Policy") {
                context.startActivity(Intent(Intent.ACTION_VIEW,
                    "https://your-privacy-policy-link.com".toUri()))
            }
            HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.5f), modifier = Modifier.padding(start = 48.dp))
            SettingsRow(icon = Icons.Default.Gavel, title = "Terms of Service") {
                context.startActivity(Intent(Intent.ACTION_VIEW, "https://your-tos-link.com".toUri()))
            }
            HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.5f), modifier = Modifier.padding(start = 48.dp))
            SettingsRow(icon = Icons.Default.Palette, title = "Attributions", onClick = onShowAttributions)
        }
    }
}

// ── Dialog ──────────────────────────────────────────────────────────────────
@Composable
private fun DeviceAdminExplanationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDarkElevated,
        title = { Text("Prevent App Uninstall", color = White, fontWeight = FontWeight.Bold) },
        text = {
            Text(
                "LockIn needs Device Manager permission to prevent uninstall during a task.\n\n" +
                        "If enabled, you cannot uninstall LockIn. To uninstall later, turn this option off first.",
                color = Color.LightGray
            )
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Allow", color = Running) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Gray) } }
    )
}