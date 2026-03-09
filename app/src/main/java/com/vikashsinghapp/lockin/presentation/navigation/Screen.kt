package com.vikashsinghapp.lockin.presentation.navigation

sealed class Screen(val route: String, val screenName: String) {
    object JournalScreen : Screen("journal_screen", "Journal")
    object TomorrowFocusScreen : Screen("tomorrow_focus_screen", "Plan")
    object TaskScreen : Screen("task_screen", "Task")
    object SettingsScreen : Screen("settings_screen", "Settings")
}