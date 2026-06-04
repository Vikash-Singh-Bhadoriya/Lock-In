package com.vikashsinghapp.lockin.presentation.settings.attribution

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vikashsinghapp.lockin.ui.theme.BackgroundDark
import com.vikashsinghapp.lockin.ui.theme.Running
import com.vikashsinghapp.lockin.ui.theme.SurfaceDark
import com.vikashsinghapp.lockin.ui.theme.SurfaceDarkElevated

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttributionScreen(onNavigateUp: () -> Unit) {
    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Attributions",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Animations & Graphics",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            // Wrap them in your premium elevated cards
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDarkElevated, MaterialTheme.shapes.medium)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AttributionStatement(
                    "Calendar",
                    "Paresh Deshpande",
                    "https://lottiefiles.com/vh625mqztg"
                )
                HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.5f))
                AttributionStatement("Notification", "Théo", "https://lottiefiles.com/n9frq64ttp")
                HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.5f))
                AttributionStatement("Alarm", "엄도연", "https://lottiefiles.com/qwohy5cvty")
                HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.5f))
                AttributionStatement(
                    "Notification Locked",
                    "Unibondwallet",
                    "https://lottiefiles.com/df5hcm6yok"
                )
                HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.5f))
                AttributionStatement(
                    "Plan Lock",
                    "Richa Gupta",
                    "https://lottiefiles.com/dmkbmrp6fj"
                )
            }
        }
    }
}

@Composable
fun AttributionStatement(
    animationName: String,
    creatorName: String,
    creatorUrl: String,
    websiteUrl: String = "https://lottiefiles.com"
) {
    // 1. Define how your links should look (LockIn Blue + Underline)
    val linkStyle = TextLinkStyles(
        style = SpanStyle(
            color = Running, // Your LockIn brand color!
            textDecoration = TextDecoration.Underline
        )
    )

    // 2. Build the string with native LinkAnnotations
    val annotatedString = buildAnnotatedString {
        withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.Bold)) {
            append("$animationName ")
        }

        withStyle(SpanStyle(color = Color.LightGray)) {
            append("animation made by ")
        }

        // Native clickable URL block
        withLink(LinkAnnotation.Url(url = creatorUrl, styles = linkStyle)) {
            append(creatorName)
        }

        withStyle(SpanStyle(color = Color.LightGray)) {
            append(" from ")
        }

        // Native clickable URL block
        withLink(LinkAnnotation.Url(url = websiteUrl, styles = linkStyle)) {
            append("LottieFiles")
        }
    }

    // 3. Use the standard Text composable! It handles the clicks automatically.
    Text(
        text = annotatedString,
        modifier = Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp)
    )
}