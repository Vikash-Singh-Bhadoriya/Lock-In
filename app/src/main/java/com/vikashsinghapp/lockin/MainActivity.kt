package com.vikashsinghapp.lockin

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.vikashsinghapp.lockin.data.repository.AppPrefsRepository
import com.vikashsinghapp.lockin.presentation.navigation.NavigationScaffold
import com.vikashsinghapp.lockin.presentation.navigation.Screen
import com.vikashsinghapp.lockin.presentation.task_status.TaskStatusActivity
import com.vikashsinghapp.lockin.system.alarm.TaskAlarmScheduler
import com.vikashsinghapp.lockin.ui.theme.LockInTheme
import com.vikashsinghapp.lockin.ui.theme.SurfaceDark
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var prefs: AppPrefsRepository

    // State to hold our start destination
    private var startDestination by mutableStateOf<String?>(null)

    // State to control the splash screen
    private var keepSplashOnScreen = true

    override fun onCreate(savedInstanceState: Bundle?) {

        // It must be called BEFORE super.onCreate() if you are using certain older Android versions,
        // but standard practice is right before setContent.
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()

        // --- to wake the screen and bypass phone lock! ---
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)

            // --- FIX: Request the OS to dismiss the lock screen ---
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            // Fallback for older Android versions
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        // Keep the splash screen visible until we read the DataStore
        splashScreen.setKeepOnScreenCondition { keepSplashOnScreen }

        lifecycleScope.launch {
            // 0. CHECK FOR ACTIVE ROLL CALL (The Hijack)
            val activeRollCallId = prefs.activeRollCallId.first()
            val activeRollCallTimeout = prefs.activeRollCallTimeout.first()

            // If an ID exists AND the timer hasn't expired yet
            if (activeRollCallId != -1L && activeRollCallTimeout > System.currentTimeMillis()) {
                val activeTitle = prefs.activeRollCallTitle.first()

                // Route them straight to the punishment
                val intent = Intent(this@MainActivity, com.vikashsinghapp.lockin.presentation.roll_call.TaskRollCallActivity::class.java).apply {
                    putExtra(TaskAlarmScheduler.EXTRA_TASK_ID, activeRollCallId)
                    putExtra(TaskAlarmScheduler.EXTRA_TASK_TITLE, activeTitle)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish() // Kill MainActivity so standard routing stops
                return@launch
            } else {
                prefs.clearActiveRollCall()
            }

            // 1. Read Onboarding State
            val onboardingCompleted = prefs.isOnboardingCompleted.first()

            // 2. Check if the app was opened via the Notification Click
            val clickedFromNightly = intent.getBooleanExtra(Constants.NAVIGATE_TO_ANALYTICS, false)

            // Read the active running task from DataStore (Catches Recent Apps clear)
            val activeRunningTaskId = prefs.activeRunningTaskId.first()

            // Check if the app was opened via Notification Click or Resume Button
            val activeFocusIntentTaskId = intent.getLongExtra(Constants.NAVIGATE_TO_ACTIVE_FOCUS_TASK_ID, -1L)

            // The target ID is either passed explicitly via Intent, OR pulled from DataStore memory
            val targetActiveFocusId = if (activeFocusIntentTaskId != -1L) activeFocusIntentTaskId else activeRunningTaskId

            startDestination = when {
                !onboardingCompleted -> Screen.OnboardingScreen.route
                clickedFromNightly -> Screen.AnalyticsScreen.route
                targetActiveFocusId != -1L -> Screen.ActiveFocusScreen.route + "/$targetActiveFocusId"
                else -> Screen.TaskScreen.route
            }

            // 2. DataStore is read, dismiss splash screen!
            keepSplashOnScreen = false

            // 3. Handle pending tasks
            prefs.pendingTaskId.first()?.let { taskId ->
                launchTaskStatus(taskId)
            }
            // 4. Schedule both reminders on boot (using their saved preferences!)
            val isReminderEnabled = prefs.isReminderEnabled.first()
            if (isReminderEnabled) {
                val hour = prefs.reminderHour.first()
                val minute = prefs.reminderMinute.first()
                TaskAlarmScheduler.scheduleDailyReminder(this@MainActivity, hour, minute)
            }

            // Assuming you add this to DataStore later, but hardcoding Nightly is fine for now
            TaskAlarmScheduler.scheduleNightlyReport(this@MainActivity, 22, 0)
        }
        // Prevent system from resizing the whole window
        // It tells the app to draw behind the system bars
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            LockInTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = SurfaceDark
                ) {
                    val navController = rememberNavController()

                    // Only build the Navigation when we actually have a destination
                    // app will stay completely invisible behind the Splash Screen until it is 100% sure where to send the user.
                    startDestination?.let { destination ->
                        NavigationScaffold(
                            navController = navController,
                            startDestination = destination,
                            onCompleteOnboarding = {
                                lifecycleScope.launch { prefs.completeOnboarding() }
                            },
                            shouldShowPermissionRationale = { permission ->
                                ActivityCompat.shouldShowRequestPermissionRationale(
                                    this@MainActivity,
                                    permission
                                )
                            })
                    }
                }
            }
        }
    }

    private fun launchTaskStatus(taskId: Long) {
        startActivity(
            Intent(this, TaskStatusActivity::class.java).apply {
                putExtra(TaskAlarmScheduler.EXTRA_TASK_ID, taskId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        )
    }
}