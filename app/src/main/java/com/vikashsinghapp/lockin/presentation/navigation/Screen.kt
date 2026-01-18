package com.vikashsinghapp.lockin.presentation.navigation

sealed class Screen(val route: String) {
    object JournalScreen: Screen("journal_screen")
    object TomorrowFocusScreen: Screen("tomorrow_focus_screen")
}
