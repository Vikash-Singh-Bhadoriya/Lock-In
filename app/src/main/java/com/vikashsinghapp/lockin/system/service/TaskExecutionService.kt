package com.vikashsinghapp.lockin.system.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.vikashsinghapp.lockin.Constants.TAG
import com.vikashsinghapp.lockin.LockInApp
import com.vikashsinghapp.lockin.LockInApp.Companion.CHANNEL_ID
import com.vikashsinghapp.lockin.R
import com.vikashsinghapp.lockin.data.entity.PromiseTask
import com.vikashsinghapp.lockin.data.repository.PlanPrefsRepository
import com.vikashsinghapp.lockin.data.repository.PromiseTaskRepository
import com.vikashsinghapp.lockin.formatTime
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
    lateinit var userPrefs: PlanPrefsRepository


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
            val task = repository.getTaskById(taskId)
            Timber.tag(TAG)
                .d("TaskExecutionService onStartCommand Task : $task inside CoroutineScope ")
            task?.let {
                startCountdown(it)
            } ?: stopSelf()
        }

        return START_STICKY
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

    }

    fun taskStatusFullScreenPendingIntent(
        taskId: Long,
    ): PendingIntent {
        Timber.tag("TASK_ID").d("HII TASK ID $taskId")
        val intent = Intent(this, TaskStatusActivity::class.java).apply {
            putExtra(TaskAlarmScheduler.EXTRA_TASK_ID, taskId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        return PendingIntent.getActivity(
            this,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }


    // Later use CountdownTimer, so do not manually need delay(1000)
    private fun startCountdown(task: PromiseTask) {

        val endMillis = task.endTime.toMs(task.planDate)
        val startMillis = task.startTime.toMs(task.planDate)

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
        stopForeground(STOP_FOREGROUND_REMOVE)

        alarmPlayer.start()


        val markTaskStatusNotification = NotificationCompat.Builder(
            this@TaskExecutionService,
            LockInApp.ALARM_CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_app_notification)
            .setContentTitle("Focus block ended")
            .setContentText("Mark how this block went")
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setFullScreenIntent(
                // TODO: is giving context = this@TaskExecutionService, is causing the problem?
                taskStatusFullScreenPendingIntent( task.id),
                true
            )
            .setOngoing(true)
            .setAutoCancel(false)
            .build()

        val manager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        manager.notify(
            BLOCK_MARK_STATUS_SCREEN_NOTIFICATION_ID,
            markTaskStatusNotification
        )

        // HOW TO get datastore in TaskExecutionService
        // will this HANDLER code work, bcs just after showing the notification, I am doing stopForeground, stopSelf

        Timber.d("userPrefs.autoDismissMinutes.first() : ${userPrefs.autoDismissMinutes.first()}")

        // Auto-stop after user-configured duration (30 min example)
        android.os.Handler(Looper.getMainLooper()).postDelayed({
            alarmPlayer.stop()

            // after delay timer ends, when user open the app
            // => HOW I WILL KNOW THAT their is pending mark STATUS required
            stopSelf()
        }, userPrefs.autoDismissMinutes.first() * 60000L)


//        // SHOW Mark Task Status Screen over other apps
//        if (Settings.canDrawOverlays(this)) {
//            val intent = Intent(this, TaskStatusActivity::class.java)
//            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//            startActivity(intent)
//        }

//        stopSelf()
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


    fun buildPlaceholderNotification() =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_app_notification)
            .setOngoing(true)
            // We do not want notifications to flash when updated, or to continuously hog the status bar of the device,you must:
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setAutoCancel(false)
            .setContentTitle("Starting task...")
            .setContentText("Preparing your focus session")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setColorized(true)

    private fun updateNotification(
        task: PromiseTask,
        totalDuration: Long,
        remainingMillis: Long,
    ): Notification {
        Timber.tag(TAG).d("TaskExecutionService buildNotification Task : $task")

        val contentText =
            "${task.startTime.formatTime()} – ${task.endTime.formatTime()}"

        val progress =
            remainingMillis / totalDuration.toFloat()


        return buildPlaceholderNotification()
            .setContentTitle("${task.title} ${remainingMillis.formatTimeLeft()}")
            .setContentText(contentText)
            .setProgress(100, (progress * 100).roundToInt(), false)
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