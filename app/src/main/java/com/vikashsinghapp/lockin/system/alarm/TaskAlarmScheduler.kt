package com.vikashsinghapp.lockin.system.alarm

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.vikashsinghapp.lockin.Constants
import com.vikashsinghapp.lockin.data.entity.PromiseTask
import com.vikashsinghapp.lockin.system.alarm.AlarmIdFactory.getPrepNudgeAlarmId
import com.vikashsinghapp.lockin.system.receiver.DailyReminderReceiver
import com.vikashsinghapp.lockin.system.receiver.NightlyReportReceiver
import com.vikashsinghapp.lockin.system.receiver.TaskPrepReceiver
import com.vikashsinghapp.lockin.system.receiver.TaskStartReceiver
import timber.log.Timber
import java.time.ZoneId
import java.util.Calendar

object TaskAlarmScheduler {

    @SuppressLint("MissingPermission")
    fun scheduleTaskStart(context: Context, task: PromiseTask) {

        Timber.Forest.tag(Constants.TAG).d("ERROR : task ID: ${task.id} Task ${task.title} is in the past. Skipping start alarm.")

        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Android 12+ exact alarm check
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                // For v1: just return silently
                // Later: show dialog directing to settings
                Timber.Forest.tag(Constants.TAG).d("!alarmManager.canScheduleExactAlarms() => SO RETURNING")
                return
            }
        }

        val triggerAtMillis = task.startTime
            .atDate(task.planDate)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        // --- If the task END time is in the past, do not schedule anything! ---
        val endMillis =
            task.endTimePlan.atDate(task.planDate).atZone(ZoneId.systemDefault()).toInstant()
                .toEpochMilli()

        // If they update the time to the past, cancel the phantom alarm!
        if (endMillis <= System.currentTimeMillis()) {
            cancelTaskAlarm(context, task.id)
            Timber.Forest.tag(Constants.TAG).d("Task ${task.title} is in the past. Skipping start alarm.")
            return
        }

        // The Time-Travel Guardrail (Catch Ongoing Tasks)
        if (triggerAtMillis <= System.currentTimeMillis()) {
            Timber.d("Task start time is in the past. Skipping alarm schedule to prevent instant Roll Call.")

            // Because we already verified endMillis > now, this task is happening RIGHT NOW!
            // Start the service immediately to show the running notification and trap them in the Active Focus screen.
            val serviceIntent = Intent(context, com.vikashsinghapp.lockin.system.service.TaskExecutionService::class.java).apply {
                putExtra(EXTRA_TASK_ID, task.id)
            }
            androidx.core.content.ContextCompat.startForegroundService(context, serviceIntent)

            return // Prevent the standard future-alarms from being set below
        }

        // ==========================================
        // 1. SCHEDULE THE T-5 PREP NUDGE (SILENT)
        // ==========================================
        val prepTriggerAtMillis = triggerAtMillis - (5 * 60 * 1000)

        // Only schedule the nudge if T-5 is actually in the future!
        // (If they plan a task for 2 minutes from now, skip the nudge)
        if (prepTriggerAtMillis > System.currentTimeMillis()) {

            val prepIntent = Intent(context, TaskPrepReceiver::class.java).apply {
                putExtra(EXTRA_TASK_ID, task.id)
                putExtra(EXTRA_TASK_TITLE, task.title)
            }

            // TODO: see requestCode not collide
            val prepPendingIntent = PendingIntent.getBroadcast(
                context,
                AlarmIdFactory.getPrepNudgeAlarmId(task.id), // Unique request code offset
                prepIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Use ExactAndAllowWhileIdle for the quiet nudge
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                prepTriggerAtMillis,
                prepPendingIntent
            )
        }

        // ==========================================
        // 2. SCHEDULE THE T-0 ROLL CALL (LOUD)
        // ==========================================
        val intent = Intent(context, TaskStartReceiver::class.java).apply {
            putExtra(EXTRA_TASK_ID, task.id)
            // only pass id's & then Load task from storage inside Service

            putExtra(EXTRA_TASK_TITLE, task.title) // Make sure to pass the title to the receiver too!
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            AlarmIdFactory.getMainAlarmId(task.id),  // stable unique requestCode
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // --- Use setAlarmClock to bypass Doze Mode ---
        val alarmClockInfo = AlarmManager.AlarmClockInfo(
            triggerAtMillis,
            pendingIntent // You can pass a separate pending intent here to open the app if they tap the upcoming alarm icon
        )

        alarmManager.setAlarmClock(
            alarmClockInfo,
            pendingIntent
        )
    }

    // when user change start/end Time from Task Detail Screen => cancel the previous & schedule new alarm
    fun cancelTaskAlarm(context: Context, taskId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // 1. Cancel the main T-0 Alarm
        val mainIntent = Intent(context, TaskStartReceiver::class.java).apply {
            putExtra(EXTRA_TASK_ID, taskId)
        }
        val mainPendingIntent = PendingIntent.getBroadcast(
            context,
            AlarmIdFactory.getMainAlarmId(taskId),
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(mainPendingIntent)
        // However, canceling an alarm does not delete the PendingIntent from the Android system cache.
        // So it will use this pending intent only which does not have task id => destory old pending intent
        mainPendingIntent.cancel() // 🟢 DESTROY THE PENDING INTENT

        // 2. Cancel the T-5 Prep Nudge!
        val prepIntent = Intent(context, TaskPrepReceiver::class.java).apply {
            putExtra(EXTRA_TASK_ID, taskId) // Add extra here too
        }
        val prepPendingIntent = PendingIntent.getBroadcast(
            context,
            getPrepNudgeAlarmId(taskId), // Must match the unique offset used in scheduleTaskStart
            prepIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(prepPendingIntent)
        prepPendingIntent.cancel() // 🟢 DESTROY THE PENDING INTENT

        Timber.tag(Constants.TAG).d("Canceled alarm for task $taskId")
    }

    // When the app opens, or when the user changes the time in Settings,
    // just call TaskAlarmScheduler.scheduleDailyReminder(context, hour, minute).
    // Because the request code 999 is static, scheduling it again automatically overwrites the old time.
    @SuppressLint("MissingPermission")
    fun scheduleDailyReminder(context: Context, hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, DailyReminderReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            AlarmIdFactory.DAILY_REMINDER_ID, // Unique request code for the daily reminder
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Calculate the next occurrence of this time
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        // If the scheduled time has already passed today, push it to tomorrow
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        // Use setExactAndAllowWhileIdle so it fires even if the phone is asleep
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            return
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }


    // when user change start/end Time from Task Detail Screen => cancel the previous & schedule new alarm
    fun cancelDailyReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, DailyReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            999,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
        Timber.tag(Constants.TAG).d("Canceled alarm for task 999")
    }

    @SuppressLint("MissingPermission")
    fun scheduleNightlyReport(context: Context, hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NightlyReportReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, AlarmIdFactory.NIGHTLY_REPORT_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) return

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }

    const val EXTRA_TASK_ID = "EXTRA_TASK_ID"
    const val EXTRA_TASK_TITLE = "EXTRA_TASK_TITLE"
    const val EXTRA_IS_PRE_START_DELAY = "EXTRA_IS_PRE_START_DELAY"
    const val EXTRA_NOTIFICATION_ID = "EXTRA_NOTIFICATION_ID"
}