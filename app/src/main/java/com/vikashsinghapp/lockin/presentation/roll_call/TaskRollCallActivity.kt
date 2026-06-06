package com.vikashsinghapp.lockin.presentation.roll_call

import android.app.KeyguardManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.vikashsinghapp.lockin.LockInApp
import com.vikashsinghapp.lockin.MainActivity
import com.vikashsinghapp.lockin.R
import com.vikashsinghapp.lockin.data.entity.TaskEndStatus
import com.vikashsinghapp.lockin.data.repository.AppPrefsRepository
import com.vikashsinghapp.lockin.data.repository.PromiseTaskRepository
import com.vikashsinghapp.lockin.presentation.task_distracted_reflection.TaskReflectionActivity
import com.vikashsinghapp.lockin.system.alarm.AlarmIdFactory
import com.vikashsinghapp.lockin.system.alarm.AlarmPlayer
import com.vikashsinghapp.lockin.system.alarm.TaskAlarmScheduler
import com.vikashsinghapp.lockin.system.receiver.RollCallTimeoutReceiver
import com.vikashsinghapp.lockin.system.service.TaskExecutionService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalTime
import javax.inject.Inject

@AndroidEntryPoint
class TaskRollCallActivity : ComponentActivity() {

    @Inject
    lateinit var alarmPlayer: AlarmPlayer

    @Inject
    lateinit var repository: PromiseTaskRepository

    @Inject
    lateinit var prefs: AppPrefsRepository

    private var taskId: Long = -1L
    private var taskTitle: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- Wake the screen and bypass lock ---
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        taskId = intent.getLongExtra(TaskAlarmScheduler.EXTRA_TASK_ID, -1L)
        Timber.d("ERROR: TaskRollCallActivity onCreate TASK ID : $taskId.")

        // 🟢 FAIL-SAFE
        if (taskId == -1L) {
            Timber.e("Ghost Roll Call alarm received. Aborting.")
            finish()
            return
        }

        taskTitle = intent.getStringExtra(TaskAlarmScheduler.EXTRA_TASK_TITLE) ?: "Focus Task"

        lifecycleScope.launch {
            // 1. ASK THE SOURCE OF TRUTH (DataStore)
            var currentTimeout = prefs.activeRollCallTimeout.first()

            // 2. Determine if we need to start a NEW Roll Call
            val isNewRollCall = currentTimeout == -1L || currentTimeout < System.currentTimeMillis()

            if (isNewRollCall) {
                currentTimeout = System.currentTimeMillis() + (5 * 60 * 1000L)

                // SAVE TO THE SOURCE OF TRUTH
                prefs.setActiveRollCall(taskId, taskTitle, currentTimeout)

                // ONLY trigger these if it's a fresh Roll Call!
                showRollCallNotification(currentTimeout)
                scheduleDeadMansSwitch()

            }

            // 2. Start the Hard Timeout based on the absolute time remaining
            val timeRemaining = currentTimeout - System.currentTimeMillis()

            // 🟢 Always start the alarm if time remains, even on reopen!
            if (timeRemaining > 0) {
                // ---  Trigger heavy haptic thud the second this screen opens ---
                triggerHeavyHaptic()
                alarmPlayer.start()
            }
            val timeoutJob = lifecycleScope.launch {
                if (timeRemaining > 0) {
                    delay(timeRemaining)
                    alarmPlayer.stop()
                    cancelRollCallNotification()
                    markTaskAsMissed()
                    finish()
                } else {
                    // They opened it after it already expired
                    markTaskAsMissed()
                    finish()
                }
            }

            setContent {
                TaskRollCallScreen(
                    taskTitle = taskTitle,
                    timeoutEpochMillis = currentTimeout,
                    onLockIn = {
                        timeoutJob.cancel()
                        alarmPlayer.stop()
                        cancelRollCallNotification()
                        cancelDeadMansSwitch()
                        startStrictFocusSession()
                    },
                    onDelayRequested = {
                        timeoutJob.cancel()
                        alarmPlayer.stop()
                        cancelRollCallNotification()
                        openReflectionForDelay()
                    }
                )
            }
        }
    }

    // ==========================================
    private fun showRollCallNotification(timeoutEpochMillis: Long) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Hide the old T-5 Nudge Notification
        manager.cancel(AlarmIdFactory.getPrepNudgeNotificationId(taskId))

        // If they click the notification, bring them exactly back here with the SAME timeout
        val tapIntent = Intent(this, TaskRollCallActivity::class.java).apply {
            putExtra(TaskAlarmScheduler.EXTRA_TASK_ID, taskId)
            putExtra(TaskAlarmScheduler.EXTRA_TASK_TITLE, taskTitle)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            taskId.toInt(),
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, LockInApp.ALARM_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_app_notification)
            .setContentTitle("Roll Call: $taskTitle")
            .setContentText("Awaiting your presence...")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(true) // Cannot swipe it away!
            .setUsesChronometer(true) // Android handles the ticking natively
            .setChronometerCountDown(true)
            .setWhen(timeoutEpochMillis) // Counts down to this exact epoch
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(AlarmIdFactory.getRollCallNotificationId(taskId), notification)
    }

    // ==========================================
    private fun cancelRollCallNotification() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(AlarmIdFactory.getRollCallNotificationId(taskId))
    }

    private fun startStrictFocusSession() {
        lifecycleScope.launch {
            prefs.clearActiveRollCall()

            // Update actual start time in DB so analytics are 100% accurate!
            val task = repository.getTaskById(taskId).first()
            task?.let {
                repository.updateTask(it.copy(actualStartTime = LocalTime.now()))
            }

            // Fire up your strict foreground service exactly as before
            val serviceIntent =
                Intent(this@TaskRollCallActivity, TaskExecutionService::class.java).apply {
                    putExtra(TaskAlarmScheduler.EXTRA_TASK_ID, taskId)
                }
            ContextCompat.startForegroundService(this@TaskRollCallActivity, serviceIntent)
            finish()
        }
    }

    private fun openReflectionForDelay() {
        Timber.d("ERROR: openReflectionForDelay TASK ID : $taskId")
        // Send them to the reflection screen to justify their delay
        val intent = Intent(this, TaskReflectionActivity::class.java).apply {
            putExtra(TaskAlarmScheduler.EXTRA_TASK_ID, taskId)

            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(intent)
        // TODO: problem => finishing just after startActivity. so it show start activity. but kill app within a second
        finish()
    }

    private fun markTaskAsMissed() {
        lifecycleScope.launch {
            prefs.clearActiveRollCall()

            // Log this as a BROKEN task so their efficiency score takes a hit
            val task = repository.getTaskById(taskId).first()
            task?.let {
                repository.updateTask(it.copy(status = TaskEndStatus.BROKEN))
                // TODO: option to reschedule missed session

                //  Fire the Missed Session Notification
                val notificationManager =
                    getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager

                // Intent to open the app so they can see the damage
                val tapIntent = Intent(
                    this@TaskRollCallActivity,
                    MainActivity::class.java
                ).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                val pendingIntent = PendingIntent.getActivity(
                    this@TaskRollCallActivity,
                    AlarmIdFactory.getMissedSessionTapId(taskId),
                    tapIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )

                val notification = NotificationCompat.Builder(
                    this@TaskRollCallActivity,
                    LockInApp.ALARM_CHANNEL_ID
                )
                    .setSmallIcon(R.drawable.ic_app_notification)
                    .setContentTitle("Session Missed: ${task.title}")
                    .setContentText("You failed to report for duty. Tap to review your day.")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .build()

                notificationManager.notify(
                    AlarmIdFactory.getMissedSessionNotificationId(taskId),
                    notification
                )
            }
        }
    }

    private fun scheduleDeadMansSwitch() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        val intent = Intent(this, RollCallTimeoutReceiver::class.java).apply {
            putExtra(TaskAlarmScheduler.EXTRA_TASK_ID, taskId)
            putExtra(
                TaskAlarmScheduler.EXTRA_TASK_TITLE,
                intent.getStringExtra(TaskAlarmScheduler.EXTRA_TASK_TITLE)
            )
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            AlarmIdFactory.getDeadMansSwitchId(taskId),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Fire exactly 5 minutes from right now
        val timeoutMillis = System.currentTimeMillis() + (5 * 60 * 1000L)
        alarmManager.setExactAndAllowWhileIdle(
            android.app.AlarmManager.RTC_WAKEUP,
            timeoutMillis,
            pendingIntent
        )
    }

    private fun cancelDeadMansSwitch() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        val intent = Intent(this, RollCallTimeoutReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            AlarmIdFactory.getDeadMansSwitchId(taskId),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
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

    override fun onDestroy() {
        super.onDestroy()
        // Stop the loud sound if the Activity is destroyed (swiped away)
        alarmPlayer.stop()
        // Notice we DO NOT cancel the DeadMansSwitch here!
        // It will trigger in 5 minutes and fail them.
    }
}