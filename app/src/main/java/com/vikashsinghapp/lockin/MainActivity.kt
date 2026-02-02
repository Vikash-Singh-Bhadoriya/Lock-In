package com.vikashsinghapp.lockin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.vikashsinghapp.lockin.presentation.navigation.NavigationScaffold
import com.vikashsinghapp.lockin.ui.theme.LockInTheme
import com.vikashsinghapp.lockin.ui.theme.SurfaceDark
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()

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
}