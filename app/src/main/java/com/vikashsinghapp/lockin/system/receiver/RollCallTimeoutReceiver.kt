package com.vikashsinghapp.lockin.system.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.vikashsinghapp.lockin.LockInApp
import com.vikashsinghapp.lockin.R
import com.vikashsinghapp.lockin.data.entity.TaskEndStatus
import com.vikashsinghapp.lockin.data.repository.PromiseTaskRepository
import com.vikashsinghapp.lockin.system.alarm.AlarmIdFactory
import com.vikashsinghapp.lockin.system.alarm.TaskAlarmScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

// This will run in the background exactly 5 minutes after the Roll Call starts.
@AndroidEntryPoint
class RollCallTimeoutReceiver : BroadcastReceiver() {

    @Inject lateinit var repository: PromiseTaskRepository

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(TaskAlarmScheduler.EXTRA_TASK_ID, -1L)
        val taskTitle = intent.getStringExtra(TaskAlarmScheduler.EXTRA_TASK_TITLE) ?: "Focus Session"
        if (taskId == -1L) return

        CoroutineScope(Dispatchers.IO).launch {
            val task = repository.getTaskById(taskId).first()

            // The ultimate check: Did they actually start the task?
            // If actualStartTime is still null, it means they swiped the app away or ignored it!
            if (task != null && task.actualStartTime == null && task.status == TaskEndStatus.PENDING) {

                // 1. Mark as Broken (The Punishment)
                repository.updateTask(task.copy(status = TaskEndStatus.BROKEN))

                // 2. Drop a silent notification of shame/failure
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val notification = NotificationCompat.Builder(context, LockInApp.ALARM_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_app_notification)
                    .setContentTitle("Session Missed")
                    .setContentText("You failed to report for '$taskTitle'.")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .build()

                notificationManager.notify(AlarmIdFactory.getMissedSessionNotificationId(taskId), notification)
            }
        }
    }
}