package com.vikashsinghapp.lockin.presentation.task_distracted_reflection

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.vikashsinghapp.lockin.Constants
import com.vikashsinghapp.lockin.Constants.TAG
import com.vikashsinghapp.lockin.MainActivity
import com.vikashsinghapp.lockin.R
import com.vikashsinghapp.lockin.data.repository.AppPrefsRepository
import com.vikashsinghapp.lockin.data.repository.PromiseTaskRepository
import com.vikashsinghapp.lockin.system.alarm.AlarmIdFactory
import com.vikashsinghapp.lockin.system.alarm.TaskAlarmScheduler
import com.vikashsinghapp.lockin.system.receiver.RollCallTimeoutReceiver
import com.vikashsinghapp.lockin.system.service.TaskExecutionService
import com.vikashsinghapp.lockin.system.service.TaskExecutionService.Companion.BLOCK_MARK_STATUS_SCREEN_NOTIFICATION_ID
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class TaskReflectionActivity : ComponentActivity() {

    @Inject
    lateinit var userPrefs: AppPrefsRepository
    @Inject
    lateinit var taskRepository: PromiseTaskRepository

    private var isPreStartDelay = false
    private var taskId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- to wake the screen and bypass phone lock! ---
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

        // ---  Trigger heavy haptic thud the second this screen opens ---
        triggerHeavyHaptic()

        // Fire the one-shot audio
        playBreakSound()

        Timber.tag(TAG).d("TaskReflectionActivity Created")

        // Prevent back press
        onBackPressedDispatcher.addCallback(this) {
            // Do nothing
        }

        taskId = intent.getLongExtra(TaskAlarmScheduler.EXTRA_TASK_ID, -1L)

        // 🟢 FAIL-SAFE: If the OS hands us a ghost intent, abort immediately!
        if (taskId == -1L) {
            Timber.e("Ghost intent received. Task ID is missing. Aborting.")
            finish()
            return
        }

        // 1. Check if the Intent explicitly told us it's a pre-start delay (From T-5 Nudge)
        val isIntentPreStart = intent.getBooleanExtra(TaskAlarmScheduler.EXTRA_IS_PRE_START_DELAY, false)

        lifecycleScope.launch {
            // 2. Check if a Roll Call is currently active (From T-0 Roll Call Screen)
            val activeRollCallId = userPrefs.activeRollCallId.first()

            // 🟢 Determine the exact context
            val reflectionMode = when {
                activeRollCallId == taskId -> ReflectionMode.ROLL_CALL_DELAY
                isIntentPreStart -> ReflectionMode.NUDGE_DELAY
                else -> ReflectionMode.MID_TASK_BREAK
            }

            // Keep this for your onBlockEnd logic
            isPreStartDelay = reflectionMode != ReflectionMode.MID_TASK_BREAK

            setContent {
                TaskDistractedReflectionScreen(
                    taskId = taskId,
                    mode = reflectionMode,
                    onBlockEnd = {
                        if (isPreStartDelay) {
                            // 1. THIS WAS A RESCHEDULE! Cancel the Dead Man's Switch trap.
                            cancelDeadMansSwitch()

                            // Clear the Roll Call from DataStore since they delayed it
                            lifecycleScope.launch { userPrefs.clearActiveRollCall() }

                            // 🟢 Hide the T-5 Nudge Notification
                            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                            manager.cancel(AlarmIdFactory.getPrepNudgeNotificationId(taskId))


                            // 2. Reschedule the alarms for the new time!
                            lifecycleScope.launch {
                                val updatedTask = taskRepository.getTaskById(taskId).first() // Fetch the newly saved times
                                updatedTask?.let {
                                    TaskAlarmScheduler.scheduleTaskStart(this@TaskReflectionActivity, it)
                                }
                            }
                        } else {
                            // 🟢 Regular mid-task break end. STOP THE SERVICE HERE!
                            Timber.d("User ended task. Stopping TaskExecutionService.")
                            val serviceIntent =
                                Intent(this@TaskReflectionActivity, TaskExecutionService::class.java).apply {
                                    putExtra(TaskAlarmScheduler.EXTRA_TASK_ID, taskId)
                                }
                            stopService(serviceIntent)

                            lifecycleScope.launch {
                                userPrefs.clearPendingTask()
                                // Clear the cage state so they can return to the normal app!
                                userPrefs.clearActiveRunningTask()
                            }
                        }
                        val manager =
                            getSystemService(NOTIFICATION_SERVICE) as NotificationManager

                        manager.cancel(BLOCK_MARK_STATUS_SCREEN_NOTIFICATION_ID)
                        // Pass -1 so it routes to the standard Timeline
                        goToMain(-1L)
                    },
                    onBlockResume = {
                        // We are clearing Pending Task here also bcs User resume the task
                        // so no need to mark status, bcs it will only happen when user finish the task
                        Timber.d("User resuming task. Leaving TaskExecutionService running.")
                        lifecycleScope.launch {
                            userPrefs.clearPendingTask()
                        }
                        // Pass the taskId so MainActivity throws them right back into the timer!
                        goToMain(taskId)
                    }
                )
            }
        }
    }

    override fun onPause() {
        super.onPause()
    }

    // Accept an optional resumeTaskId to route the user
    private fun goToMain(resumeTaskId: Long = -1L) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            // If we are resuming, tell MainActivity exactly where to go
            if (resumeTaskId != -1L) {
                putExtra(Constants.NAVIGATE_TO_ACTIVE_FOCUS_TASK_ID, resumeTaskId)
            }
        }
        startActivity(intent)
        finish()
    }

    private fun triggerHeavyHaptic() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        // A short, sharp 100ms vibration to mimic a heavy button press
        vibrator.vibrate(VibrationEffect.createOneShot(100L, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    private fun playBreakSound() {
        try {
            // Create a temporary MediaPlayer for the one-shot sound
            val mediaPlayer = MediaPlayer.create(this, R.raw.lockin_buzz).apply {
                setVolume(0.10f, 0.10f) // 25% volume for both left and right channels
            }

            // Crucial: Set it to release its memory the exact millisecond the sound finishes playing
            mediaPlayer.setOnCompletionListener { player ->
                player.release()
            }

            mediaPlayer.start()
        } catch (e: Exception) {
            Timber.e(e, "Failed to play break sound")
        }
    }


    private fun cancelDeadMansSwitch() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        val intent = Intent(this, RollCallTimeoutReceiver::class.java)
        val pendingIntent = android.app.PendingIntent.getBroadcast(
            this,
            AlarmIdFactory.getDeadMansSwitchId(taskId),
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel() // Always a good habit to cancel it here too
    }
}
