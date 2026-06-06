package com.vikashsinghapp.lockin.presentation.onboarding

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.vikashsinghapp.lockin.R
import com.vikashsinghapp.lockin.presentation.tomorrow_focus.component.goToAppSettings
import com.vikashsinghapp.lockin.ui.theme.BackgroundDark
import com.vikashsinghapp.lockin.ui.theme.Running
import com.vikashsinghapp.lockin.ui.theme.SurfaceDarkElevated
import com.vikashsinghapp.lockin.ui.theme.spacing
import kotlinx.coroutines.launch

data class OnboardingPage(
    val title: String,
    val description: String,
    val lottieRes: Int
)

@Composable
fun OnboardingScreen(
    isReviewMode: Boolean = false,
    onFinish: () -> Unit // Call this to navigate to Home/Plan screen and save state in DataStore
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val pages = rememberSaveable {
        listOf(
            OnboardingPage(
                title = "Stop Lying to Yourself",
                description = "Regular to-do lists let you ignore tasks without consequence. We track your planned time vs. actual time so you face your real habits.",
                lottieRes = R.raw.lottie_calendar
            ),
            OnboardingPage(
                title = "Un-ignorable Focus",
                description = "When a focus session starts, the notification is locked. You either complete the work, or hit 'Break'. No escaping reality.",
                lottieRes = R.raw.lottie_lock
            ),
            OnboardingPage(
                title = "Own Your Distractions",
                description = "Want to scroll reels? You have to hit 'Break' and type out your exact excuse. Face the friction, stay accountable, and build discipline.",
                lottieRes = R.raw.lottie_notification
            ),
            OnboardingPage(
                title = "Let's Get to Work",
                description = if (isReviewMode) {
                    "You know the rules. No excuses, no swiping away. Plan your day, lock it in, and execute."
                } else {
                    "To keep you strictly accountable, LockIn needs to fire alarms at the exact minute. Please allow the required permissions below."
                },
                lottieRes = R.raw.lottie_alarm
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val powerManager = remember { context.getSystemService(Context.POWER_SERVICE) as PowerManager }
    val lifecycleOwner = LocalLifecycleOwner.current

    // States for permissions
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    var isBatteryUnrestricted by remember {
        mutableStateOf(powerManager.isIgnoringBatteryOptimizations(context.packageName))
    }

    // Tracks if they clicked the notification button and were denied
    var notificationDenialCount by remember { mutableIntStateOf(0) }

    // VIVO/XIAOMI WORKAROUND: Tracks if they actively clicked the battery button
    var hasAttemptedBatteryFix by remember { mutableStateOf(false) }

    // Permission Launcher for Android 13+ Notifications
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (!isGranted) {
            notificationDenialCount++ // Increment if they deny it or if system auto-blocks it
        }
    }
    // Re-check permissions every time the app comes back to the foreground
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val granted = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                    hasNotificationPermission = granted
                    if (granted) notificationDenialCount = 0 // Reset if they fixed it in settings
                }
                isBatteryUnrestricted =
                    powerManager.isIgnoringBatteryOptimizations(context.packageName)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        containerColor = BackgroundDark,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.spacing.huge),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Dot Indicators
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(bottom = MaterialTheme.spacing.huge)
                ) {
                    repeat(pages.size) { iteration ->
                        val color = if (pagerState.currentPage == iteration) Running else Color.DarkGray
                        Box(
                            modifier = Modifier
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(color)
                                .size(if (pagerState.currentPage == iteration) 10.dp else 8.dp)
                        )
                    }
                }

                // Bottom Buttons
                if (pagerState.currentPage == pages.lastIndex) {
                    // FINAL PAGE: Show setup buttons

                    // --- Only show permissions if NOT in review mode ---
                    if (!isReviewMode) {

                        // 1. Notification Button (Hides when granted)
                        AnimatedVisibility(
                            visible = !hasNotificationPermission,
                            exit = shrinkVertically(animationSpec = tween(300))
                        ) {
                            Column {
                                Button(
                                    onClick = {
                                        if (notificationDenialCount >= 1) {
                                            // If system is blocking the prompt, send them to settings
                                            context.goToAppSettings()
                                        } else {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(MaterialTheme.spacing.buttonHeight),
                                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceDarkElevated),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = if (notificationDenialCount >= 1) "Open Settings to Allow Notifications" else "1. Allow Notifications",
                                        color = if (notificationDenialCount >= 1) Color(0xFFFF5252) else Color.White,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                }
                                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
                            }
                        }

                        // 2. Battery Button (Hides if API says true OR if user attempted it)
                        AnimatedVisibility(
                            visible = !isBatteryUnrestricted && !hasAttemptedBatteryFix,
                            exit = shrinkVertically(animationSpec = tween(300))
                        ) {
                            Column {
                                Button(
                                    onClick = {
                                        hasAttemptedBatteryFix =
                                            true // VIVO FIX: Hide it once they try
                                        val intent =
                                            Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier.fillMaxWidth().height(MaterialTheme.spacing.buttonHeight),
                                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceDarkElevated),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        "2. Disable Battery Restrictions",
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                }
                                Spacer(modifier = Modifier.height(MaterialTheme.spacing.huge))
                            }
                        }

                        if (hasNotificationPermission && (isBatteryUnrestricted || hasAttemptedBatteryFix)) {
                            Spacer(modifier = Modifier.height(MaterialTheme.spacing.huge))
                        }
                    } else {
                        // Just a spacer for review mode to keep the button from hugging the dots
                        Spacer(modifier = Modifier.height(MaterialTheme.spacing.huge))
                    }

                    // 3. Start Button (Disabled until notifications are granted)
                    Button(
                        onClick = onFinish,
                        enabled = isReviewMode || hasNotificationPermission, // Hard block on Notifications
                        modifier = Modifier.fillMaxWidth().height(MaterialTheme.spacing.buttonHeight),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Running,
                            disabledContainerColor = SurfaceDarkElevated // Faded look when disabled
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = if (isReviewMode) "Got it!" else if (hasNotificationPermission) "Start Locking In" else "Grant Permissions to Start",
                            color = if (isReviewMode || hasNotificationPermission) Color.White else Color.Gray,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                } else {
                    // NORMAL PAGES: Next & Skip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pages.lastIndex)
                            }
                        }) {
                            Text("Skip", color = Color.Gray, style = MaterialTheme.typography.titleSmall)
                        }

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Running),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Next", color = Color.White, style = MaterialTheme.typography.titleSmall)
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) { position ->
            PagerScreen(page = pages[position])
        }
    }
}

@Composable
fun PagerScreen(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.massive),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Lottie Animation
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(page.lottieRes))
        val progress by animateLottieCompositionAsState(
            composition = composition,
            iterations = LottieConstants.IterateForever
        )

        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier
                .size(250.dp)
                .padding(bottom = MaterialTheme.spacing.massive)
        )

        // Title
        Text(
            text = page.title,
            color = Color.White,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

        // Description
        Text(
            text = page.description,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}