package com.vikashsinghapp.lockin.system.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.CountDownTimer
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.vikashsinghapp.lockin.Constants
import com.vikashsinghapp.lockin.Constants.TAG
import com.vikashsinghapp.lockin.LockInApp
import com.vikashsinghapp.lockin.LockInApp.Companion.CHANNEL_ID
import com.vikashsinghapp.lockin.MainActivity
import com.vikashsinghapp.lockin.R
import com.vikashsinghapp.lockin.data.entity.PromiseTask
import com.vikashsinghapp.lockin.data.repository.AppPrefsRepository
import com.vikashsinghapp.lockin.data.repository.PromiseTaskRepository
import com.vikashsinghapp.lockin.formatTime
import com.vikashsinghapp.lockin.presentation.task_distracted_reflection.TaskReflectionActivity
import com.vikashsinghapp.lockin.presentation.task_status.TaskStatusActivity
import com.vikashsinghapp.lockin.system.alarm.AlarmPlayer
import com.vikashsinghapp.lockin.system.alarm.TaskAlarmScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt

// The Android system stops a service only when memory is low and it must recover system resources for the activity that has user focus.
// If the service is bound to an activity that has user focus, it's less likely to be killed;
// if the service is declared to run in the FOREGROUND, it's rarely killed.

// Service must have no arguments in constructor
@AndroidEntryPoint
class TaskExecutionService : Service() {
// @ServiceScoped is for objects created INSIDE the service

    @Inject
    lateinit var repository: PromiseTaskRepository

    @Inject
    lateinit var alarmPlayer: AlarmPlayer

    @Inject
    lateinit var userPrefs: AppPrefsRepository


    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var countdownJob: Job? = null

    // context = this, inside local variable is NULL, bcs it is created before context is passed to the Service

    override fun onCreate() {
        super.onCreate()
        Timber.tag(TAG).d("TaskExecutionService onCreate!")

        // 1. Start foreground IMMEDIATELY with a placeholder notification
        startForeground(START_NOTIFICATION_ID, buildPlaceholderNotification().build())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

//        when (intent?.action) {
//            ACTION_BREAK_TASK -> {
//                Timber.tag(TAG).d("TaskExecutionService ACTION_BREAK_TASK called!")
//
//                val taskId = intent.getLongExtra(
//                    TaskAlarmScheduler.EXTRA_TASK_ID,
//                    -1L
//                )
//                serviceScope.launch {
//
//                    // Persist reality
//                    userPrefs.setPendingTask(taskId)
//
//                    // No need do vibration & sound => as user click just now
////                    alarmPlayer.start()
////                    vibrateFor2Seconds()
//                }
//
//                // Not show Break button => if not draw over other app permission
//                Timber.d("Settings.canDrawOverlays(this) : ${Settings.canDrawOverlays(this)}")
//                if (Settings.canDrawOverlays(this)) {
//                    startActivity(
//                        Intent(this, TaskReflectionActivity::class.java).apply {
//                            // CLEAR_TOP prevents opening multiple instances of the reflection screen
//                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
//                            putExtra(TaskAlarmScheduler.EXTRA_TASK_ID, taskId)
//                        }
//                    )
//                } else {
//                    // Fallback: Ask for permission if it was somehow revoked
//                    startActivity(
//                        Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
//                            data = "package:$packageName".toUri()
//                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                        }
//                    )
//                }
//                // Return START_STICKY to ensure the service stays alive and ticking
//                return START_STICKY
//            }
//        }


        // 1. Start foreground IMMEDIATELY with a placeholder notification
        startForeground(START_NOTIFICATION_ID, buildPlaceholderNotification().build())

        val taskId = intent?.getLongExtra(TaskAlarmScheduler.EXTRA_TASK_ID, -1L)
            ?: return START_NOT_STICKY

        countdownJob?.cancel()

        if (!::repository.isInitialized) {
            Timber.tag(TAG).d("TaskExecutionService Repository not injected!")
            stopSelf()
            return START_NOT_STICKY
        }
        countdownJob = serviceScope.launch {
            val task = repository.getTaskByIdOnce(taskId)
            Timber.tag(TAG)
                .d("TaskExecutionService onStartCommand Task : $task inside CoroutineScope ")
            task?.let {

                // --- Abort if task is already over! ---
                val endMillis = it.endTimePlan.toMs(it.planDate)
                if (endMillis <= System.currentTimeMillis()) {
                    Timber.tag(TAG).d("Task ${it.title} is already over. Aborting service.")
                    stopSelf()
                    return@launch
                }

                val pending = userPrefs.pendingTaskId.first()
                if (pending != null) {
                    Timber.e("BLOCKED: Pending task $pending not marked")

                    stopForeground(STOP_FOREGROUND_REMOVE)
                    showPendingBlockNotification(task)
                    stopSelf()
                    return@launch
                }

                // --- Vibrate to notify the user the task has started! ---
                vibrateFor2Seconds(500L)
                alarmPlayer.playNotificationSound()

                // --- Force open MainActivity over the Lock Screen! ---
                try {
                    val intent = Intent(this@TaskExecutionService, MainActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
//                        putExtra(Constants.NAVIGATE_TO_TIMELINE, true)
                        putExtra(Constants.NAVIGATE_TO_ACTIVE_FOCUS_TASK_ID, taskId)
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                    Timber.e(e, "Failed to launch MainActivity from background")
                }

                // Save the running state to DataStore right before starting the timer
                serviceScope.launch {
                    userPrefs.setActiveRunningTask(taskId)
                }
                startCountdown(it)
            } ?: stopSelf()
        }

        return START_STICKY
    }

    private fun mainActivityPendingIntent(taskId: Long = -1): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // Send the specific ID instead of a boolean
            putExtra(Constants.NAVIGATE_TO_ACTIVE_FOCUS_TASK_ID, taskId)
        }
        return PendingIntent.getActivity(
            this,
            taskId.toInt(), // Use the taskId as the request code to prevent overwrites
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun showPendingBlockNotification(task: PromiseTask) {
        val intent = Intent(this, TaskStatusActivity::class.java).apply {
            putExtra(TaskAlarmScheduler.EXTRA_TASK_ID, task.id)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        val pi = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, LockInApp.ALARM_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_app_notification)
            .setContentTitle("Action required")
            .setContentText("Mark your previous task \n${task.title} ${task.startTime.formatTime()} – ${(task.actualEndTime ?: task.endTimePlan).formatTime()}")
            .setOngoing(true)
            // We do not want notifications to flash when updated, or to continuously hog the status bar of the device,you must:
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setAutoCancel(false)
            .setContentIntent(pi)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()

        getSystemService(NotificationManager::class.java)
            .notify(BLOCK_MARK_STATUS_SCREEN_NOTIFICATION_ID, notification)
    }

    override fun onBind(intent: Intent?) = null

    override fun onDestroy() {
        countdownJob?.cancel()
//        alarmPlayer.stop()
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        const val START_NOTIFICATION_ID = 1999
        const val BLOCK_MARK_STATUS_SCREEN_NOTIFICATION_ID = 2000
//        const val ACTION_BREAK_TASK = "ACTION_BREAK_TASK"

    }

    fun taskStatusFullScreenPendingIntent(
        taskId: Long,
    ): PendingIntent {
        Timber.tag("TASK_ID").d("HII TASK ID $taskId")
        val intent = Intent(this, TaskStatusActivity::class.java).apply {
            putExtra(TaskAlarmScheduler.EXTRA_TASK_ID, taskId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
//            or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        return PendingIntent.getActivity(
            this,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun breakTaskPendingIntent(taskId: Long): PendingIntent {
        // Direct it straight to the Activity
        val intent = Intent(this, TaskReflectionActivity::class.java).apply {
            putExtra(TaskAlarmScheduler.EXTRA_TASK_ID, taskId)
            // CLEAR_TOP ensures it doesn't open multiple copies
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        // Use getActivity instead of getService.
        // Android will ALWAYS allow this to open because the user tapped the notification.
        return PendingIntent.getActivity(
            this,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    // Later use CountdownTimer, so do not manually need delay(1000)
    private fun startCountdown(task: PromiseTask) {

        val endMillis = task.endTimePlan.toMs(task.planDate)

        // Calculate duration based on when they ACTUALLY started
        val startToUse = task.actualStartTime ?: task.startTime
        val startMillis = startToUse.toMs(task.planDate)

        val totalDuration = endMillis - startMillis

        object : CountDownTimer(endMillis - System.currentTimeMillis(), 1000L) {
            override fun onTick(remainingMillis: Long) {

                val notification = updateNotification(task, totalDuration, remainingMillis)

                startForeground(START_NOTIFICATION_ID, notification)
//                    notificationManager.notify(START_NOTIFICATION_ID, notification)
            }

            override fun onFinish() {
                serviceScope.launch {
                    onTaskFinished(task)
                }
            }

        }.start()
    }

    private suspend fun onTaskFinished(task: PromiseTask) {
        Timber.tag("TASK_ID").d("startCountdown TASK ID ${task.id}")

        Timber.tag(TAG).d("HELLO    SHOWING markTaskStatusNotification")

        //Clear the running state because the timer hit 00:00 natively!
        userPrefs.clearActiveRunningTask()

        // 🔐 Persist reality FIRST
        userPrefs.setPendingTask(task.id)
        stopForeground(STOP_FOREGROUND_REMOVE)

        alarmPlayer.start()

        vibrateFor2Seconds()

        val markTaskStatusNotification = NotificationCompat.Builder(
            this@TaskExecutionService,
            LockInApp.ALARM_CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_app_notification)
            .setContentTitle("Task ended")
            .setContentText("Mark how this Task went")
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setFullScreenIntent(
                taskStatusFullScreenPendingIntent(task.id),
                true
            )
            .setOngoing(true)
            .setAutoCancel(false)
            .build()

        val manager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        manager.notify(
            BLOCK_MARK_STATUS_SCREEN_NOTIFICATION_ID,
            markTaskStatusNotification
        )

//        // SHOW Mark Task Status Screen over other apps
        // IF full screen intent fails, show an overlay
        if (Settings.canDrawOverlays(this)) {
            val intent = Intent(this, TaskStatusActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra(TaskAlarmScheduler.EXTRA_TASK_ID, task.id)
            }
            this.startActivity(intent)
        } else {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            intent.data = "package:$packageName".toUri()
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        // HOW TO get datastore in TaskExecutionService
        // will this HANDLER code work, bcs just after showing the notification, I am doing stopForeground, stopSelf

        Timber.d("userPrefs.autoDismissMinutes.first() : ${userPrefs.autoDismissMinutes.first()}")

        // Auto-stop after user-configured duration (30 min example)
        // THIS FOREGROUND SERVICE WILL RUN UNTIL Alarm Player Duration 30 min like expire
        android.os.Handler(Looper.getMainLooper()).postDelayed({
            alarmPlayer.stop()

            // after delay timer ends, when user open the app
            // => HOW I WILL KNOW THAT their is pending mark STATUS required
            stopSelf()
        }, userPrefs.autoDismissMinutes.first() * 60 * 1000L)

//        stopSelf()
    }

    private fun vibrateFor2Seconds(ms: Long = 2000L) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
        vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    fun buildPlaceholderNotification(): NotificationCompat.Builder {
        // 1. Check if the system is in Dark Mode
        val isDarkMode =
            (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

        // 2. Choose your accent color (Create these in your colors.xml if you haven't)
        // Note: You usually use the SAME accent color for both, but you can split them if needed.
        val accentColor = if (isDarkMode) {
            ContextCompat.getColor(this, R.color.running_blue_dark)
        } else {
            ContextCompat.getColor(this, R.color.running_blue_light)
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_app_notification)
            .setOngoing(true)
            // We do not want notifications to flash when updated, or to continuously hog the status bar of the device,you must:
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setAutoCancel(false)
            .setContentTitle("Starting task...")
            .setContentText("Preparing your focus session")
            .setPriority(NotificationCompat.PRIORITY_MAX) // MAX priority required for screen wake
            .setCategory(Notification.CATEGORY_ALARM) // ALARM category bypasses Do Not Disturb
//            .setPriority(NotificationCompat.PRIORITY_HIGH)
//            .setCategory(Notification.CATEGORY_SERVICE)
            .setColor(accentColor)
            .setContentIntent(mainActivityPendingIntent(-1)) // When click notification
            // --- This tells Android to pop this over the lock screen! ---
            .setFullScreenIntent(mainActivityPendingIntent(-1), true)
    }

    private fun updateNotification(
        task: PromiseTask,
        totalDuration: Long,
        remainingMillis: Long,
    ): Notification {
        Timber.tag(TAG).d("TaskExecutionService buildNotification Task : $task")

        val contentText =
            "${task.startTime.formatTime()} – ${task.endTimePlan.formatTime()}"

        val progress =
            remainingMillis / totalDuration.toFloat()

        return buildPlaceholderNotification()
            .setContentTitle("${task.title} ${remainingMillis.formatTimeLeft()}")
            .setContentText(contentText)
            .setProgress(100, (progress * 100).roundToInt(), false)
            .addAction(
                R.drawable.ic_notification_stop,
                "Break",
                breakTaskPendingIntent(task.id)
            )
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(mainActivityPendingIntent(task.id)) // When click notification
            // --- This tells Android to pop this over the lock screen! ---
            .setFullScreenIntent(mainActivityPendingIntent(task.id), true)


            // Remove Previous Start & Stop Actions
//            .clearActions()
//            .addAction(
//                R.drawable.notification_ic_stop,
//                context.getString(R.string.stop),
//                stopPendingIntent(context)
//            )
            .build()

    }


//    fun closeNotification() {
//        Timber.tag(Constants.TAG).d("WorkoutRunningNotification: close Notification")
//        NotificationManagerCompat.from(context).cancel(WORKOUT_RUNNING_NOTIFICATION_ID)
//    }
}

fun LocalTime.toMs(planDate: LocalDate) = this.atDate(planDate)
    .atZone(ZoneId.systemDefault())
    .toInstant()
    .toEpochMilli()

fun Long.formatTimeLeft(): String {
    val totalSeconds = this / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return when {
        hours > 0 -> String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
        minutes > 0 -> String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
        else -> String.format(Locale.getDefault(), "%d", seconds)
    }
}