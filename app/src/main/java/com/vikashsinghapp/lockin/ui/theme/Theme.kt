package com.vikashsinghapp.lockin.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat

// 🟢 MODIFIED: We ONLY use DarkColors for LockIn to preserve the tactical cage aesthetic.
// No dynamic color, no light mode.
private val DarkColorScheme = darkColorScheme(
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceDarkElevated,
    primary = Running,
    error = Error
    // Add other mappings if needed, but these cover 90% of your M3 components
)

@Composable
fun LockInTheme(
    // We removed dynamicColor and darkTheme parameters because LockIn is strictly Dark Mode.
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat
                .getInsetsController(window, view)
                // needed as without it in Light Mode, the status bar content + background => whole black
                .isAppearanceLightStatusBars = false
        }
    }

    // Wrap MaterialTheme in our Custom Spacing Provider
    CompositionLocalProvider(LocalSpacing provides Spacing()) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography, // 🟢 Injects your new Type.kt
            content = content
        )
    }
}

// When ModalBottomSheet opens, it does not just draw a view on your screen; it creates an
// entirely new Android Window (a Dialog). This new Dialog Window completely ignores the
// LockInTheme you set in MainActivity and resets the status bar icons to their default state
@Composable
fun ForceWhiteStatusBarIcons() {
    val view = LocalView.current
    SideEffect {
        // This safely grabs the Bottom Sheet's invisible Dialog window
        val window = (view.parent as? DialogWindowProvider)?.window
        if (window != null) {
            // Force the icons to stay white in dark mode
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }
}