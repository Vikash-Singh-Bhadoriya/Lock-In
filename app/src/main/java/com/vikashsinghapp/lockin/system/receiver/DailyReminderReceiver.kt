package com.vikashsinghapp.lockin.system.receiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.vikashsinghapp.lockin.LockInApp
import com.vikashsinghapp.lockin.MainActivity
import com.vikashsinghapp.lockin.R
import com.vikashsinghapp.lockin.data.repository.AppPrefsRepository
import com.vikashsinghapp.lockin.system.alarm.TaskAlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DailyReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 1. Intent to open the Plan Screen
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, tapIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // 2. Build the "Nudge"
        val notification = NotificationCompat.Builder(context, LockInApp.ALARM_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_app_notification) // Your icon
            .setContentTitle("Lock in your day 🎯")
            .setContentText("Tap to build your routine and set your focus blocks.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(3000, notification) // Unique ID for this specific nudge

        // 3. Reschedule for tomorrow!
        val prefs = AppPrefsRepository(context)
        CoroutineScope(Dispatchers.IO).launch {
            val isEnabled = prefs.isReminderEnabled.first()
            if (isEnabled) {
                val hour = prefs.reminderHour.first()
                val minute = prefs.reminderMinute.first()
                TaskAlarmScheduler.scheduleDailyReminder(context, hour, minute)
            }
        }
    }
}