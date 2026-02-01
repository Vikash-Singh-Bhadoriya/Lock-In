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
import com.vikashsinghapp.lockin.system.alarm.TaskAlarmScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.ZoneId
import javax.inject.Inject

// The Android system stops a service only when memory is low and it must recover system resources for the activity that has user focus.
// If the service is bound to an activity that has user focus, it's less likely to be killed;
// if the service is declared to run in the FOREGROUND, it's rarely killed.

// Service must have no arguments in constructor
@AndroidEntryPoint
class TaskExecutionService : Service() {
// @ServiceScoped is for objects created INSIDE the service

    @Inject
    lateinit var repository: PromiseTaskRepository

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

        val taskId = intent?.getLongExtra(
            TaskAlarmScheduler.EXTRA_TASK_ID,
            -1L
        ) ?: return START_NOT_STICKY

        CoroutineScope(Dispatchers.IO).launch {
            val task = repository.getTaskById(taskId)
            Timber.tag(TAG).d( "TaskExecutionService onStartCommand Task : ${task} inside CoroutineScope ")
            task?.let {
                startForeground(
                    NOTIFICATION_ID,
                    buildNotification(it)
                )
                scheduleEnd(task)
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?) = null

    companion object {
        const val NOTIFICATION_ID = 1001
    }


    private fun scheduleEnd(task: PromiseTask) {
        val delay = task.endTime.atDate(task.planDate)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli() - System.currentTimeMillis()

        CoroutineScope(Dispatchers.Main).launch {
            delay(delay)
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun buildNotification(task: PromiseTask): Notification {
//        val minutes = (remainingMillis / 1000) / 60
//        val seconds = (remainingMillis / 1000) % 60
//        val timeLeft = String.format("%02d:%02d", minutes, seconds)
        Timber.tag(TAG).d( "TaskExecutionService buildNotification Task : ${task}")
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setOngoing(true)
            // We do not want notifications to flash when updated, or to continuously hog the status bar of the device,you must:
            .setOnlyAlertOnce(true)
            .setAutoCancel(false)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(task.title)
//            .setContentText("${task.startTime.formatTime()} – ${task.endTime.formatTime()}       $timeLeft")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(Notification.CATEGORY_SERVICE)
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