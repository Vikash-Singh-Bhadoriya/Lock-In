package com.vikashsinghapp.lockin.system.receiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.vikashsinghapp.lockin.LockInApp
import com.vikashsinghapp.lockin.R
import com.vikashsinghapp.lockin.presentation.task_distracted_reflection.TaskReflectionActivity
import com.vikashsinghapp.lockin.system.alarm.AlarmIdFactory
import com.vikashsinghapp.lockin.system.alarm.TaskAlarmScheduler
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TaskPrepReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(TaskAlarmScheduler.EXTRA_TASK_ID, -1L)

        // 🟢 FAIL-SAFE: Do not show a Nudge if the ID is missing
        if (taskId == -1L) return

        val taskTitle = intent.getStringExtra(TaskAlarmScheduler.EXTRA_TASK_TITLE) ?: "Focus Task"

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // ACTION 1: "I'm Ready" -> Just dismisses the notification. The loud Roll Call will still happen at T-0.

        val readyIntent = PendingIntent.getBroadcast(
            context,
            AlarmIdFactory.getPrepReadyActionId(taskId),
            Intent(context, TaskReadyReceiver::class.java).apply { // 🟢 UPDATED
                putExtra(TaskAlarmScheduler.EXTRA_TASK_ID, taskId)
            }
            , PendingIntent.FLAG_IMMUTABLE
        )

        // ACTION 2: "I need to delay" -> The Friction Trap. Opens the app immediately.
        val delayIntent = Intent(context, TaskReflectionActivity::class.java).apply {
            putExtra(TaskAlarmScheduler.EXTRA_TASK_ID, taskId)
            putExtra(TaskAlarmScheduler.EXTRA_IS_PRE_START_DELAY, true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }


        val delayPendingIntent = PendingIntent.getActivity(
            context,
            AlarmIdFactory.getPrepDelayActionId(taskId),
            delayIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build the Nudge Notification
        val notification = NotificationCompat.Builder(context, LockInApp.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_app_notification)
            .setContentTitle("Time to lock in: $taskTitle")
            .setContentText("Starts in 5 minutes. Get ready.")
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Pops up over other apps
            .setAutoCancel(true)
            .addAction(0, "I'm Ready", readyIntent)
            .addAction(0, "I need to delay", delayPendingIntent)
            .build()

        notificationManager.notify(AlarmIdFactory.getPrepNudgeNotificationId(taskId), notification)
    }
}