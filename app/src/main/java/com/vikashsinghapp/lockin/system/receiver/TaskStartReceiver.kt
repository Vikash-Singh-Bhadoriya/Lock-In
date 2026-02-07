package com.vikashsinghapp.lockin.system.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.vikashsinghapp.lockin.system.alarm.TaskAlarmScheduler
import com.vikashsinghapp.lockin.system.service.TaskExecutionService
import timber.log.Timber

class TaskStartReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Timber.tag("ALARM_DEBUG").d(
            "Alarm fired at ${System.currentTimeMillis()}"
        )

        val taskId = intent.getLongExtra(
            TaskAlarmScheduler.EXTRA_TASK_ID,
            -1L
        )
        if (taskId == -1L) return

        val serviceIntent = Intent(context, TaskExecutionService::class.java).apply {
            putExtra(TaskAlarmScheduler.EXTRA_TASK_ID, taskId)
        }
        context.startForegroundService(serviceIntent)
    }
}