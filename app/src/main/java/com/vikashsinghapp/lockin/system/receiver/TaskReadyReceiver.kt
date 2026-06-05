package com.vikashsinghapp.lockin.system.receiver

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.vikashsinghapp.lockin.data.repository.PromiseTaskRepository
import com.vikashsinghapp.lockin.system.alarm.AlarmIdFactory
import com.vikashsinghapp.lockin.system.alarm.TaskAlarmScheduler
import com.vikashsinghapp.lockin.system.service.TaskExecutionService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

@AndroidEntryPoint
class TaskReadyReceiver : BroadcastReceiver() {

    @Inject lateinit var repository: PromiseTaskRepository

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(TaskAlarmScheduler.EXTRA_TASK_ID, -1L)
        if (taskId == -1L) return

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // 1. Hide the Nudge Notification
        notificationManager.cancel(AlarmIdFactory.getPrepNudgeNotificationId(taskId))

        // 2. Cancel the upcoming T-0 Loud Roll Call Alarm (Reward them for being early!)
        val mainIntent = Intent(context, TaskStartReceiver::class.java).apply {
            putExtra(TaskAlarmScheduler.EXTRA_TASK_ID, taskId)
        }
        val mainPendingIntent = PendingIntent.getBroadcast(
            context,
            AlarmIdFactory.getMainAlarmId(taskId),
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(mainPendingIntent)
        mainPendingIntent.cancel()

        // 3. Update the Database & Start the Service
        val pendingResult = goAsync() // Tells Android to keep the receiver alive for Coroutines
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val task = repository.getTaskById(taskId).first()
                if (task != null) {
                    // Shift actual start time to RIGHT NOW
                    repository.updateTask(task.copy(
                        actualStartTime = LocalTime.now()  // 🟢 Log the actual start
                    ))

                    // Start the strict focus session
                    val serviceIntent = Intent(context, TaskExecutionService::class.java).apply {
                        putExtra(TaskAlarmScheduler.EXTRA_TASK_ID, taskId)
                    }
                    // Android 12+ allows starting Foreground Services directly from Notification Actions!
                    ContextCompat.startForegroundService(context, serviceIntent)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}