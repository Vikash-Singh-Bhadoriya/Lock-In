package com.vikashsinghapp.lockin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.vikashsinghapp.lockin.presentation.journal.JournalScreen
import com.vikashsinghapp.lockin.ui.theme.LockInTheme
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LockInTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    JournalScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}