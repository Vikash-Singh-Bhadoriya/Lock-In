package com.vikashsinghapp.lockin.presentation.task_status

import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.vikashsinghapp.lockin.MainActivity
import com.vikashsinghapp.lockin.data.repository.AppPrefsRepository
import com.vikashsinghapp.lockin.system.alarm.AlarmPlayer
import com.vikashsinghapp.lockin.system.alarm.TaskAlarmScheduler
import com.vikashsinghapp.lockin.system.service.TaskExecutionService
import com.vikashsinghapp.lockin.system.service.TaskExecutionService.Companion.BLOCK_MARK_STATUS_SCREEN_NOTIFICATION_ID
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class TaskStatusActivity : ComponentActivity() {

    @Inject lateinit var alarmPlayer: AlarmPlayer
    @Inject lateinit var userPrefs: AppPrefsRepository

    private var submitted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- to wake the screen and bypass lock! ---
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            // Fallback for older Android versions
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        // Prevent back press
        onBackPressedDispatcher.addCallback(this) {
              // Do nothing
        }

        setContent {
            TaskStatusMarkScreen(
                taskId = intent.getLongExtra(
                    TaskAlarmScheduler.EXTRA_TASK_ID,
                    -1L
                ),
                onNavigateBack = {
                    submitted = true
                    alarmPlayer.stop()

                    lifecycleScope.launch {
                        userPrefs.clearPendingTask()
                    }
                    val manager =
                        getSystemService(NOTIFICATION_SERVICE) as NotificationManager

                    manager.cancel(BLOCK_MARK_STATUS_SCREEN_NOTIFICATION_ID)
                    goToMain()
                }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        // 🔇 Pause alarm while user is marking status
        alarmPlayer.pauseAlarm()
    }

    override fun onPause() {
        super.onPause()
        if (submitted) {
            Timber.d("STOPPING onPause TASKSTATUS ACTIVITY SERVICE")

            // WHEN I submit => I WANT TO stop THE FOREGROUND SERVICE
            val taskId = intent.getLongExtra(
                TaskAlarmScheduler.EXTRA_TASK_ID,
                -1L
            )
            if (taskId == -1L) return

            val serviceIntent = Intent(this, TaskExecutionService::class.java).apply {
                putExtra(TaskAlarmScheduler.EXTRA_TASK_ID, taskId)
            }
            stopService(serviceIntent)
        } else {
            // 🔊 Resume alarm ONLY if user escaped without submitting
            alarmPlayer.resumeAlarm()
        }
    }

    private fun goToMain() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
        finish()
    }

}
