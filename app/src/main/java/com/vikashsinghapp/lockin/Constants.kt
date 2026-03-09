package com.vikashsinghapp.lockin

import com.vikashsinghapp.lockin.presentation.navigation.Screen
import com.vikashsinghapp.lockin.presentation.navigation.Screen.JournalScreen
import com.vikashsinghapp.lockin.presentation.navigation.Screen.SettingsScreen
import com.vikashsinghapp.lockin.presentation.navigation.Screen.TomorrowFocusScreen

object Constants {
    const val TAG = "LockInApp"
    const val REQUEST_CODE_DRAW_OVER_APPS = 2894

    // Defining allScreens outside the sealed class ensures all objects are initialized before the list is created.
    val ALL_SCREENS = listOf(Screen.TaskScreen, JournalScreen, TomorrowFocusScreen, SettingsScreen)

}