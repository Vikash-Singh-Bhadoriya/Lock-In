package com.vikashsinghapp.lockin.system.service

import android.app.Notification
import android.app.Service
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.vikashsinghapp.lockin.Constants.TAG
import com.vikashsinghapp.lockin.LockInApp.Companion.CHANNEL_ID
import com.vikashsinghapp.lockin.R
import com.vikashsinghapp.lockin.data.entity.PromiseTask
import com.vikashsinghapp.lockin.data.repository.PromiseTaskRepository
import com.vikashsinghapp.lockin.formatTime
import com.vikashsinghapp.lockin.system.alarm.TaskAlarmScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    private var countdownJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        Timber.tag(TAG).d("TaskExecutionService onCreate!")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!::repository.isInitialized) {
            Timber.tag(TAG).d("TaskExecutionService Repository not injected!")
            stopSelf()
            return START_NOT_STICKY
        }
        // 1. Start foreground IMMEDIATELY with a placeholder notification
        startForeground(NOTIFICATION_ID, buildPlaceholderNotification())

        val taskId = intent?.getLongExtra(
            TaskAlarmScheduler.EXTRA_TASK_ID,
            -1L
        ) ?: return START_NOT_STICKY

        countdownJob?.cancel()
        countdownJob = CoroutineScope(Dispatchers.IO).launch {
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
        super.onDestroy()
    }

    companion object {
        const val NOTIFICATION_ID = 1001
    }

    // Later use CountdownTimer, so do not manually need delay(1000)
    private suspend fun startCountdown(task: PromiseTask) {
        withContext(Dispatchers.Main) {
            val totalDuration =
                task.endTime.toMs(task.planDate) - task.startTime.toMs(task.planDate)

            while (true) {
                val remainingMillis = task.endTime.atDate(task.planDate)
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli() - System.currentTimeMillis()

                if (remainingMillis <= 0) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                    break
                }

                val notification = buildNotification(task, totalDuration, remainingMillis)
                startForeground(NOTIFICATION_ID, notification)
                delay(1000)
            }
        }
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


    private fun buildPlaceholderNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setAutoCancel(false)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Starting task...")
            .setContentText("Preparing your focus session")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
    }

    private fun buildNotification(
        task: PromiseTask,
        totalDuration: Long,
        remainingMillis: Long,
    ): Notification {
        Timber.tag(TAG).d("TaskExecutionService buildNotification Task : $task")

        val contentText =
            "${task.startTime.formatTime()} – ${task.endTime.formatTime()}"

        val progress =
            remainingMillis / totalDuration.toFloat()


        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setOngoing(true)
            // We do not want notifications to flash when updated, or to continuously hog the status bar of the device,you must:
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setAutoCancel(false)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("${task.title} ${remainingMillis.formatTimeLeft()}")
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(Notification.CATEGORY_SERVICE)
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