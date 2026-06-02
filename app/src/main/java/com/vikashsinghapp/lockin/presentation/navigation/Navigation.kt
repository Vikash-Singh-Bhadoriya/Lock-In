package com.vikashsinghapp.lockin.presentation.navigation

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.vikashsinghapp.lockin.presentation.active_focus.ActiveFocusScreen
import com.vikashsinghapp.lockin.presentation.active_focus.ActiveFocusUiEvent
import com.vikashsinghapp.lockin.presentation.active_focus.ActiveFocusViewModel
import com.vikashsinghapp.lockin.presentation.analytics.AnalyticsScreen
import com.vikashsinghapp.lockin.presentation.journal.JournalScreen
import com.vikashsinghapp.lockin.presentation.onboarding.OnboardingScreen
import com.vikashsinghapp.lockin.presentation.settings.SettingsScreen
import com.vikashsinghapp.lockin.presentation.settings.attribution.AttributionScreen
import com.vikashsinghapp.lockin.presentation.settings.faq.FAQScreen
import com.vikashsinghapp.lockin.presentation.settings.feedback.FeedbackScreen
import com.vikashsinghapp.lockin.presentation.task_detail.TaskDetailScreen
import com.vikashsinghapp.lockin.presentation.today.TaskScreen
import com.vikashsinghapp.lockin.presentation.tomorrow_focus.PlanEditorScreen
import com.vikashsinghapp.lockin.presentation.tomorrow_focus.TomorrowFocusScreen
import com.vikashsinghapp.lockin.system.alarm.TaskAlarmScheduler

@Composable
fun Navigation(
    // central API that keeps track of the back stack of composables
    modifier: Modifier = Modifier,
    navController: NavHostController,
    startDestination: String,
    onCompleteOnboarding: () -> Unit,
    snackbarHostState: SnackbarHostState,
    shouldShowPermissionRationale: (String) -> Boolean,
    onOpenDrawer: () -> Unit,
) {
    // navController must be link to a NavHost
    // which specifies the composable destinations(Screens), that you should be able to navigate
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination,
    ) {
        // composable that represents Screen.OnboardingScreen.route
        composable(route = Screen.OnboardingScreen.route) {
            OnboardingScreen(
                isReviewMode = false,
                onFinish = {
                    // Save it to DataStore and navigate to the app!
                    onCompleteOnboarding()
                    navController.navigate(Screen.TomorrowFocusScreen.route) {
                        // Pop the onboarding screen so they can't hit the back button to return to it
                        popUpTo(Screen.OnboardingScreen.route) { inclusive = true }
                    }
                }
            )
        }

        // in Jetpack Compose, defining two separate routes for the same screen is a common industry practice known as Intent-Based Routing.
        composable(route = Screen.OnboardingReviewScreen.route) {
            OnboardingScreen(
                isReviewMode = true,
                // The "Exit Behavior" is fundamentally different then normal Onboarding flow
                onFinish = {
                    // Simply pop the backstack to return to the Settings screen
                    navController.navigateUp()
                }
            )
        }
        composable(
            route = Screen.TemplateEditorScreen.route + "/{planId}",
            arguments = listOf(navArgument("planId") { type = NavType.LongType })
        ) { backStackEntry ->
            PlanEditorScreen(
                onBack = { navController.navigateUp() }
            )
        }
        // composable that represents Screen.HomeScreen.route
        composable(route = Screen.JournalScreen.route) {
            JournalScreen(onOpenDrawer = onOpenDrawer)
        }


        composable(route = Screen.SettingsScreen.route) {
            SettingsScreen(
                onOpenDrawer = onOpenDrawer,
                onNavigateToOnboardingReview = { navController.navigate(Screen.OnboardingReviewScreen.route) },
                onNavigateToFeedbackScreen = {
                    navController.navigate(Screen.FeedbackScreen.route)
                },
                onNavigateToFaq = {
                    navController.navigate(Screen.FAQScreen.route)
                },
                onNavigateToAttributions = {
                    navController.navigate(Screen.AttributionScreen.route)
                },
            )
        }
        composable(route = Screen.FeedbackScreen.route) {
            FeedbackScreen(
                onNavigateUp = { navController.navigateUp() },
            )
        }
        composable(route = Screen.AttributionScreen.route) {
            AttributionScreen(
                onNavigateUp = { navController.navigateUp() },
            )
        }
        composable(route = Screen.FAQScreen.route) {
            FAQScreen(
                onNavigateUp = { navController.navigateUp() },
                onNavigateToFeedback = {
                    navController.navigate(Screen.FeedbackScreen.route)
                }
            )
        }

        // composable that represents Screen.TaskScreen.route
        composable(
            route = Screen.TomorrowFocusScreen.route + "?date={dateString}",
            arguments = listOf(
                navArgument("dateString") {
                    type = NavType.StringType
                    defaultValue = java.time.LocalDate.now().toString() // Default to today
                }
            )
        ) { entry ->
            TomorrowFocusScreen(
                shouldShowPermissionRationale = shouldShowPermissionRationale,
                snackbarHostState = snackbarHostState,
                onNavigateUp = { navController.navigateUp() },
                onOpenDrawer = onOpenDrawer,
                onNavigateToTemplateEditor = {
                    navController.navigate(Screen.TemplateEditorScreen.route + "/$it")
                },
                onNavigateToTimeline = {
                    navController.navigate(Screen.TaskScreen.route) {
                        popUpTo(Screen.TomorrowFocusScreen.route) { inclusive = true }
                    }
                }
            )
        }
        // composable that represents Screen.TaskScreen.route
        composable(
            route = Screen.TaskScreen.route,
        ) { entry ->
            TaskScreen(
                snackbarHostState = snackbarHostState,
                onOpenDrawer = onOpenDrawer,
                onNavigateToTaskDetail = { taskId ->
                    navController.navigate(Screen.TaskDetailScreen.route + "/$taskId")
                },
                onNavigateToActiveFocus = { taskId ->
                    navController.navigate(Screen.ActiveFocusScreen.route + "/$taskId")
                },
                onNavigateToPlanner = { dateStr ->
                    navController.navigate(Screen.TomorrowFocusScreen.route + "?date=$dateStr")
                },
            )
        }
        composable(
            route = Screen.TaskDetailScreen.route + "/{taskId}", // Append the argument to the route
            arguments = listOf(
                navArgument("taskId") { type = NavType.LongType }
            )
        ) { entry ->
            // The ViewModel will automatically find "taskId" in SavedStateHandle via Hilt
            TaskDetailScreen(snackbarHostState, onBack = {
                navController.navigateUp()
            })
        }
        composable(route = Screen.AnalyticsScreen.route) {
            AnalyticsScreen(
                onOpenDrawer = onOpenDrawer
            )
        }
        composable(
            route = Screen.ActiveFocusScreen.route + "/{taskId}",
            arguments = listOf(navArgument("taskId") { type = NavType.LongType })
        ) { entry ->
            val context = LocalContext.current
            val viewModel: ActiveFocusViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()

            // 🟢 THE TRAP: Disable the hardware/swipe back button!
            BackHandler(true) {
                Toast.makeText(context, "You must hold END EARLY to leave.", Toast.LENGTH_SHORT).show()
            }

            // 🟢 Listen for ViewModel Navigation Events
            LaunchedEffect(key1 = true) {
                viewModel.eventFlow.collect { event ->
                    when (event) {
                        is ActiveFocusUiEvent.CancelAlarm -> {
                            TaskAlarmScheduler.cancelTaskAlarm(context, event.taskId)
                        }
                        is ActiveFocusUiEvent.NavigateToReflection -> {
                            // 1. Launch the Heavy "Penalty Box" Activity
                            val intent = android.content.Intent(context, com.vikashsinghapp.lockin.presentation.task_distracted_reflection.TaskReflectionActivity::class.java).apply {
                                putExtra(TaskAlarmScheduler.EXTRA_TASK_ID, event.taskId)
                                // We are quitting mid-task, so this is false
                                putExtra(TaskAlarmScheduler.EXTRA_IS_PRE_START_DELAY, false)
                            }
                            context.startActivity(intent)

                            // 2. Pop the Active Focus screen so they can't swipe back to it!
                            // 🟢 MODIFIED: Explicitly route to TaskScreen and clear the backstack (0 = root)
                            navController.navigate(Screen.TaskScreen.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                        is ActiveFocusUiEvent.NavigateToSuccess -> {
                            // 1. Pop back to the Timeline
                            // Explicitly route to TaskScreen and clear the backstack
                            navController.navigate(Screen.TaskScreen.route) {
                                popUpTo(0) { inclusive = true }
                            }

                            // 2. Give them their dopamine hit!
                            snackbarHostState.showSnackbar("Target reached! Time's up. 🎯")
                        }
                    }
                }
            }

            ActiveFocusScreen(
                taskTitle = uiState.task?.title ?: "",
                timeRemainingFormatted = uiState.timeRemainingFormatted,
                progressPercentage = uiState.progressPercentage,
                journalMessages = uiState.journalMessages,
                onAddLog = { viewModel.addJournalLog(it) },
                onEndEarlySession = { viewModel.onEndEarly() },
                themeColorValue = uiState.themeColorValue
            )
        }
    }
}