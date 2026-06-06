package com.vikashsinghapp.lockin.presentation.analytics

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.vikashsinghapp.lockin.domain.DailyEfficiencyReport
import com.vikashsinghapp.lockin.formatDate
import com.vikashsinghapp.lockin.ui.theme.BackgroundDark
import com.vikashsinghapp.lockin.ui.theme.Broken
import com.vikashsinghapp.lockin.ui.theme.Completed
import com.vikashsinghapp.lockin.ui.theme.Running
import com.vikashsinghapp.lockin.ui.theme.SurfaceDark
import com.vikashsinghapp.lockin.ui.theme.SurfaceDarkElevated
import com.vikashsinghapp.lockin.ui.theme.Unfinished
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = hiltViewModel(),
    onOpenDrawer: () -> Unit
) {
    val report by viewModel.report.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = { Text("Performance", color = Color.White, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- DATE NAVIGATOR ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(SurfaceDarkElevated)
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.changeDate(-1) }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous", tint = Color.White)
                }

                Text(
                    text = if (selectedDate == LocalDate.now()) "Today" else selectedDate.formatDate(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                IconButton(
                    onClick = { viewModel.changeDate(1) },
                    enabled = selectedDate.isBefore(LocalDate.now()) // Can't see future analytics
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next",
                        tint = if (selectedDate.isBefore(LocalDate.now())) Color.White else Color.Transparent
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (report != null) {
                // --- BIG HERO CARD ---
                EfficiencyHeroCard(report!!)

                Spacer(modifier = Modifier.height(24.dp))

                // --- TOTAL FOCUS TIME ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceDarkElevated)
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total Deep Work", color = Color.Gray, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))

                        val hours = report!!.totalFocusMinutes / 60
                        val mins = report!!.totalFocusMinutes % 60
                        val timeString = if (hours > 0) "${hours}h ${mins}m" else "${mins}m"

                        Text(timeString, color = Running, fontSize = 28.sp, fontWeight = FontWeight.Black)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- STATS GRID ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatBox(modifier = Modifier.weight(1f), title = "Completed", count = report!!.completed, color = Completed)
                    StatBox(modifier = Modifier.weight(1f), title = "Unfinished", count = report!!.unfinished, color = Unfinished)
                    StatBox(modifier = Modifier.weight(1f), title = "Broken", count = report!!.broken, color = Broken)
                }

                Spacer(modifier = Modifier.height(32.dp))

            } else {
                // --- EMPTY STATE ---
                Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No completed tasks yet.", color = Color.Gray, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Lock in and get to work.", color = Color.DarkGray, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun EfficiencyHeroCard(report: DailyEfficiencyReport) {
    // Animate the progress bar filling up when the screen opens
    var animationPlayed by remember { mutableStateOf(false) }
    val currentPercentage by animateFloatAsState(
        targetValue = if (animationPlayed) report.efficiencyPercentage / 100f else 0f,
        animationSpec = tween(durationMillis = 1500),
        label = "progress"
    )

    LaunchedEffect(report) {
        animationPlayed = false // Reset animation when data changes
        kotlinx.coroutines.delay(100)
        animationPlayed = true
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
            // Background Track
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.fillMaxSize(),
                color = SurfaceDarkElevated,
                strokeWidth = 16.dp,
            )
            // Actual Progress
            CircularProgressIndicator(
                progress = { currentPercentage },
                modifier = Modifier.fillMaxSize(),
                color = Running, // Bright Blue
                strokeCap = StrokeCap.Round,
                strokeWidth = 15.dp,
            )
            // Big Number in the middle
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${report.efficiencyPercentage}%",
                    color = Color.White,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black
                )
                Text("Efficiency", color = Color.Gray, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(text = report.title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = report.message, color = Color.Gray, fontSize = 15.sp)
    }
}

@Composable
fun StatBox(modifier: Modifier = Modifier, title: String, count: Int, color: Color) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceDarkElevated)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = count.toString(), color = color, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = title, color = Color.LightGray, fontSize = 12.sp)
    }
}