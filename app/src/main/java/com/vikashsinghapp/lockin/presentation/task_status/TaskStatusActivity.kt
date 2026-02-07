package com.vikashsinghapp.lockin.presentation.task_status

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import com.vikashsinghapp.lockin.MainActivity
import com.vikashsinghapp.lockin.system.alarm.AlarmPlayer
import com.vikashsinghapp.lockin.system.alarm.TaskAlarmScheduler
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TaskStatusActivity : ComponentActivity() {

    @Inject lateinit var alarmPlayer: AlarmPlayer

    private var submitted = false

    @RequiresApi(Build.VERSION_CODES.O_MR1)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setShowWhenLocked(true)
        setTurnScreenOn(true)

        // Prevent back press
        onBackPressedDispatcher.addCallback(this) {
              // Do nothing
        }

        // Reduce alarm volume while marking
        alarmPlayer.reduceVolume()

        setContent {
            TaskStatusMarkScreen(
                taskId = intent.getLongExtra(
                    TaskAlarmScheduler.EXTRA_TASK_ID,
                    -1L
                ),
                onNavigateBack = {
                    submitted = true
                    alarmPlayer.stop()
                    goToMain()
                }
            )
        }
    }

    override fun onStop() {
        super.onStop()
        if (!submitted) {
            alarmPlayer.restoreVolume()
        }
    }

    private fun goToMain() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
        finish()
    }

}
