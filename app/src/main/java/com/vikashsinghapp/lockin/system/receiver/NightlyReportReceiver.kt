package com.vikashsinghapp.lockin.system.receiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.vikashsinghapp.lockin.Constants
import com.vikashsinghapp.lockin.LockInApp
import com.vikashsinghapp.lockin.MainActivity
import com.vikashsinghapp.lockin.R
import com.vikashsinghapp.lockin.data.repository.PromiseTaskRepository
import com.vikashsinghapp.lockin.domain.generateNightlyReport
import com.vikashsinghapp.lockin.system.alarm.TaskAlarmScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

// This receiver will run silently in the background at 10:00 PM, crunch your math, and fire the notification.
@AndroidEntryPoint
class NightlyReportReceiver : BroadcastReceiver() {

    @Inject lateinit var repository: PromiseTaskRepository

    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        CoroutineScope(Dispatchers.IO).launch {
            // 1. Fetch today's tasks and calculate math
            val tasks = repository.getAllTasksOnce(LocalDate.now())
            val report = generateNightlyReport(tasks) ?: return@launch // Don't show if no tasks planned

            // 2. Intent to open Analytics Screen
            val tapIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra(Constants.NAVIGATE_TO_ANALYTICS, true) // Specific route extra
            }

            val pendingIntent = PendingIntent.getActivity(
                context, 1, tapIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            // 3. Build the "Dopamine" Notification
            val notification = NotificationCompat.Builder(context, LockInApp.ALARM_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_app_notification)
                .setContentTitle("Day Complete: ${report.efficiencyPercentage}% Efficiency")
                .setContentText("${report.title} — ${report.message}")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

            notificationManager.notify(4000, notification)

            // 4. Reschedule for tomorrow night automatically
            TaskAlarmScheduler.scheduleNightlyReport(context, 22, 0) // Default 10 PM
        }
    }
}