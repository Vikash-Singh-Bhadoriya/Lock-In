package com.vikashsinghapp.lockin.presentation.tomorrow_focus.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PermissionDialog(
    modifier: Modifier = Modifier,
    onAllow: () -> Unit,
    onDismiss: () -> Unit,
    isPermanentlyDeclined: Boolean,
    onGoToAppSettingsClick: () -> Unit
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = {
            onDismiss()
        },
        title = {
            Text(
                modifier = Modifier.padding(vertical = 4.dp),
                text = "Notification Permission Required",
                style = MaterialTheme.typography.headlineMedium
            )
        },
        text = {
            Column {
                Text(
                    text = if (isPermanentlyDeclined) {
                        "It seems you permanently declined notification permission.\n\n" +
                                "Cannot save Plan if permission is not granted. Go to app settings, then permissions, and then click 'Allow notifications' to grant it."
                    } else {
                        "In order to get notifications, allow notification permission.\n\n" +
                                "We will only use this permission for showing task progress and marking task status notifications."
                    },
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onDismiss()
            }) {
                Text(
                    "Cancel",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(vertical = 5.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (isPermanentlyDeclined) {
                        onGoToAppSettingsClick()
                        onDismiss()
                    } else {
                        onAllow()
                    }
                }
            ) {
                Text(
                        if (isPermanentlyDeclined) "App Settings" else "Allow",
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 5.dp)
                )
            }
        },
//        properties = DialogProperties(
//            dismissOnBackPress = false,
//            dismissOnClickOutside = false
//        )
    )
}