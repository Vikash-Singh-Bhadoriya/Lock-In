package com.vikashsinghapp.lockin

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.vikashsinghapp.lockin.data.repository.PlanPrefsRepository
import com.vikashsinghapp.lockin.presentation.navigation.NavigationScaffold
import com.vikashsinghapp.lockin.presentation.task_status.TaskStatusActivity
import com.vikashsinghapp.lockin.system.alarm.TaskAlarmScheduler
import com.vikashsinghapp.lockin.ui.theme.LockInTheme
import com.vikashsinghapp.lockin.ui.theme.SurfaceDark
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var prefs: PlanPrefsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()

        lifecycleScope.launch {
            prefs.pendingTaskId.first()?.let { taskId ->
                launchTaskStatus(taskId)
            }
        }

//        // Prevent system from resizing the whole window
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            LockInTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = SurfaceDark
                ) {
                    val navController = rememberNavController()
                    NavigationScaffold(
                        navController = navController,
                        shouldShowPermissionRationale = { permission ->
                            ActivityCompat.shouldShowRequestPermissionRationale(
                                this@MainActivity,
                                permission
                            )
                        })
                }
            }
        }
    }

    private fun launchTaskStatus(taskId: Long) {
        startActivity(
            Intent(this, TaskStatusActivity::class.java).apply {
                putExtra(TaskAlarmScheduler.EXTRA_TASK_ID, taskId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        )
    }
}