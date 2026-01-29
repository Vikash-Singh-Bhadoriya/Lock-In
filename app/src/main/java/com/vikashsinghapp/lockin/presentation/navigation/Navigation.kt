package com.vikashsinghapp.lockin.presentation.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.vikashsinghapp.lockin.presentation.journal.JournalScreen
import com.vikashsinghapp.lockin.presentation.tomorrow_focus.TomorrowFocusScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Navigation(
    // central API that keeps track of the back stack of composables
    modifier: Modifier = Modifier,
    navController: NavHostController,
    startDestination: String = Screen.JournalScreen.route,
) {
    // navController must be link to a NavHost
    // which specifies the composable destinations(Screens), that you should be able to navigate
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination,
    ) {
        // composable that represents Screen.HomeScreen.route
        composable(route = Screen.JournalScreen.route) {
            JournalScreen()
        }
        // composable that represents Screen.TaskScreen.route
        composable(
            route = Screen.TomorrowFocusScreen.route,
        ) { entry ->
            TomorrowFocusScreen()
        }
    }
}