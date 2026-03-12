package com.vikashsinghapp.lockin.presentation.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.vikashsinghapp.lockin.presentation.journal.JournalScreen
import com.vikashsinghapp.lockin.presentation.settings.SettingsScreen
import com.vikashsinghapp.lockin.presentation.task_detail.TaskDetailScreen
import com.vikashsinghapp.lockin.presentation.today.TaskScreen
import com.vikashsinghapp.lockin.presentation.tomorrow_focus.TomorrowFocusScreen

@Composable
fun Navigation(
    // central API that keeps track of the back stack of composables
    modifier: Modifier = Modifier,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    startDestination: String = Screen.TaskScreen.route,
    shouldShowPermissionRationale: (String) -> Boolean,
    onOpenDrawer: () -> Unit
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
            JournalScreen(onOpenDrawer = onOpenDrawer)
        }
        // composable that represents Screen.HomeScreen.route
        composable(route = Screen.SettingsScreen.route) {
            SettingsScreen(onOpenDrawer = onOpenDrawer)
        }
        // composable that represents Screen.TaskScreen.route
        composable(
            route = Screen.TomorrowFocusScreen.route,
        ) { entry ->
            TomorrowFocusScreen(
                shouldShowPermissionRationale = shouldShowPermissionRationale,
                snackbarHostState = snackbarHostState,
                onNavigateUp = { navController.navigateUp() },
                onOpenDrawer = onOpenDrawer
            )
        }
        // composable that represents Screen.TaskScreen.route
        composable(
            route = Screen.TaskScreen.route,
        ) { entry ->
            TaskScreen(onNavigateToTaskDetail = { taskId ->
                navController.navigate(Screen.TaskDetailScreen.route + "/$taskId")
            }, onOpenDrawer = onOpenDrawer)
        }
        composable(
            route = Screen.TaskDetailScreen.route + "/{taskId}", // Append the argument to the route
            arguments = listOf(
                navArgument("taskId") { type = NavType.LongType }
            )
        ) { entry ->
            // The ViewModel will automatically find "taskId" in SavedStateHandle via Hilt
            TaskDetailScreen(onBack = {
                navController.navigateUp()
            })
        }
    }
}