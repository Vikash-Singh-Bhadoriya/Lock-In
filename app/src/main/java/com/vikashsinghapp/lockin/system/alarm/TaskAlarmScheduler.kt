package com.vikashsinghapp.lockin.system.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.vikashsinghapp.lockin.Constants.TAG
import com.vikashsinghapp.lockin.data.entity.PromiseTask
import com.vikashsinghapp.lockin.system.receiver.TaskStartReceiver
import timber.log.Timber
import java.time.ZoneId

object TaskAlarmScheduler {

    fun scheduleTaskStart(context: Context, task: PromiseTask) {

        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Android 12+ exact alarm check
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                // For v1: just return silently
                // Later: show dialog directing to settings
                Timber.tag(TAG).d("!alarmManager.canScheduleExactAlarms() => SO RETURNING")
                return
            }
        }

        val intent = Intent(context, TaskStartReceiver::class.java).apply {
            putExtra(EXTRA_TASK_ID, task.id)

            // only pass id's & then Load task from storage inside Service
//            putExtra(EXTRA_TASK, task)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.toInt(), // stable unique requestCode
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAtMillis = task.startTime
            .atDate(task.planDate)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    }

    const val EXTRA_TASK_ID = "EXTRA_TASK_ID"
}
