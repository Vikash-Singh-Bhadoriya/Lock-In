package com.vikashsinghapp.lockin.presentation.settings.faq

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpCenter
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.EditOff
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PauseCircleFilled
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vikashsinghapp.lockin.ui.theme.BackgroundDark
import com.vikashsinghapp.lockin.ui.theme.Running
import com.vikashsinghapp.lockin.ui.theme.SurfaceDark
import com.vikashsinghapp.lockin.ui.theme.SurfaceDarkElevated

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FAQScreen(
    onNavigateUp: () -> Unit,
    onNavigateToFeedback: () -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = { Text("FAQ", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Feedback Prompt Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Running)
                    .clickable { onNavigateToFeedback() }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.AutoMirrored.Filled.HelpCenter, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
                Text("Still have problems? Contact Us", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // LockIn Specific FAQs
            FaqQuestion(
                icon = Icons.Default.NotificationsOff,
                title = "Why didn't my alarm ring exactly on time?"
            ) {
                Text("Some Android devices (like Vivo, Oppo, and Xiaomi) aggressively kill background processes to save battery. This can delay the alarm by 10–60 seconds.", color = Color.LightGray, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                Text("Fix: Go to your phone's App Settings for LockIn, enable 'Autostart', and set Battery Optimization to 'Unrestricted'.", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }

            FaqQuestion(
                icon = Icons.Default.PauseCircleFilled,
                title = "Can I pause a focus session?"
            ) {
                Text("No. LockIn is built on strict accountability. Life doesn't pause, and neither does your focus block. If you need to step away, you must hit 'Break' and type out the reason why.", color = Color.LightGray, fontSize = 14.sp)
            }

            FaqQuestion(
                icon = Icons.Default.EditOff,
                title = "How do I edit my plan after it's locked?"
            ) {
                Text("You can't. Once you hit 'LOCK IN SCHEDULE', your schedule is locked for the day. This creates psychological friction and stops you from endlessly rearranging your schedule to procrastinate.", color = Color.LightGray, fontSize = 14.sp)
            }

            FaqQuestion(
                icon = Icons.Default.DeleteForever,
                title = "I can't uninstall the app?"
            ) {
                Text("If you enabled 'Prevent App Uninstall' in settings, LockIn acts as a Device Administrator to stop you from rage-deleting the app during a moment of weakness.", color = Color.LightGray, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                Text("To uninstall: Open LockIn Settings, toggle 'Prevent App Uninstall' off, and then uninstall normally.", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun FaqQuestion(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    dropDownContent: @Composable ColumnScope.() -> Unit,
) {
    var isContentVisible by rememberSaveable { mutableStateOf(false) } // Default closed looks cleaner

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDarkElevated)
            // 1. Clickable must be before padding so the ripple fills the whole card
            .clickable { isContentVisible = !isContentVisible }
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(24.dp))
                Text(text = title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, overflow = TextOverflow.Clip)
            }
            Icon(
                modifier = Modifier
                    .size(24.dp)
                    .rotate(if (isContentVisible) 180F else 0F),
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Expand",
                tint = Color.Gray
            )
        }
        // It shrinks the height without fading, keeping the text inside until the box fully closes.
        AnimatedVisibility(
            visible = isContentVisible,
            enter = expandVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ),
            exit = shrinkVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 40.dp)
            ) {
                dropDownContent()
            }
        }
    }
}