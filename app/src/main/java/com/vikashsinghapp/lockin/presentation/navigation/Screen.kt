package com.vikashsinghapp.lockin.presentation.navigation

sealed class Screen(val route: String, val screenName: String) {
    object JournalScreen : Screen("journal_screen", "Journal")
    object TomorrowFocusScreen : Screen("tomorrow_focus_screen", "Tomorrow's Focus")
    object TodayScreen : Screen("today_screen", "Today")
    object SettingsScreen : Screen("settings_screen", "Settings")
}