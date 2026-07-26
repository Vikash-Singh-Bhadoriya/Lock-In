package com.vikashsinghapp.lockin.presentation.core

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * Hides the system navigation bar (Back / Home / Recents).
 * Re-applies on every configuration change because rotation resets system UI flags.
 */
@Composable
fun HideNavigationBar(enabled: Boolean = true) {
    val view = LocalView.current
    val configuration = LocalConfiguration.current

    LaunchedEffect(configuration, enabled) {
        if (enabled) {
            applyNavigationBarHidden(view)
        } else {
            showNavigationBar(view)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            showNavigationBar(view)
        }
    }
}

private fun applyNavigationBarHidden(view: android.view.View) {
    val activity = view.context as? Activity ?: return
    val controller = WindowCompat.getInsetsController(activity.window, view)
    controller.hide(WindowInsetsCompat.Type.navigationBars())
    controller.systemBarsBehavior =
        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
}

private fun showNavigationBar(view: android.view.View) {
    val activity = view.context as? Activity ?: return
    val controller = WindowCompat.getInsetsController(activity.window, view)
    controller.show(WindowInsetsCompat.Type.navigationBars())
}
