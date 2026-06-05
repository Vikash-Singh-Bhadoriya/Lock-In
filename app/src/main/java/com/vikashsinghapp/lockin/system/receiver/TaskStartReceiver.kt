package com.vikashsinghapp.lockin.system.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.vikashsinghapp.lockin.presentation.roll_call.TaskRollCallActivity
import com.vikashsinghapp.lockin.system.alarm.TaskAlarmScheduler
import timber.log.Timber

// Why I need a receiver to start activity or service, can't directly do??
// Since Android 10, Google strictly prohibits starting an Activity directly from the background.
// If you point an alarm directly to an Activity, Android will block it.
// Pointing it to a BroadcastReceiver first acts as a legal "bridge" to wake the device
// and launch the Full Screen Intent.
class TaskStartReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Timber.tag("ALARM_DEBUG").d(
            "Alarm fired at ${System.currentTimeMillis()}"
        )

        val taskId = intent.getLongExtra(
            TaskAlarmScheduler.EXTRA_TASK_ID,
            -1L
        )
        Timber.d("ERROR: TaskStartReceiver TASK ID : $taskId.")

        val taskTitle = intent.getStringExtra(TaskAlarmScheduler.EXTRA_TASK_TITLE)
        if (taskId == -1L) return

        // Launch the Roll Call instead of the Service
        val rollCallIntent = Intent(context, TaskRollCallActivity::class.java).apply {
            putExtra(TaskAlarmScheduler.EXTRA_TASK_ID, taskId)
            putExtra(TaskAlarmScheduler.EXTRA_TASK_TITLE, taskTitle)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        context.startActivity(rollCallIntent)
    }
}