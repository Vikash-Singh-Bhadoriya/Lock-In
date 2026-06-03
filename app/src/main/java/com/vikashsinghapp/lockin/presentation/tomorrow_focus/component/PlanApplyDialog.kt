package com.vikashsinghapp.lockin.presentation.tomorrow_focus.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.vikashsinghapp.lockin.ui.theme.Running
import com.vikashsinghapp.lockin.ui.theme.SurfaceDarkElevated

@Composable
fun PlanApplyDialog(
    onDismiss: () -> Unit,
    onReplace: () -> Unit,
    onAppend: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDarkElevated,
        title = { Text("Apply Template", color = Color.White) },
        text = { Text("You already have tasks planned for this day. Do you want to replace them completely, or append the template tasks to your existing plan?", color = Color.Gray) },
        confirmButton = {
            Button(
                onClick = onAppend,
                colors = ButtonDefaults.buttonColors(containerColor = Running)
            ) {
                Text("Append", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onReplace) {
                Text("Replace All", color = Color(0xFFCF6679))
            }
        }
    )
}