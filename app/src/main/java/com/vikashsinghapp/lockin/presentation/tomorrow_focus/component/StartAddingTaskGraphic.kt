package com.vikashsinghapp.lockin.presentation.tomorrow_focus.component


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.vikashsinghapp.lockin.R

@Composable
fun BoxScope.StartAddingTaskGraphic() {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.animation_lockin))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
    )
    Column(
        modifier = Modifier
            .align(Alignment.Center)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        LottieAnimation(
            modifier = Modifier.fillMaxWidth().weight(1f),
            composition = composition,
            progress = {
                progress
            },
            contentScale = ContentScale.FillWidth
        )
        Text(
            modifier = Modifier.padding(14.dp),
            text = "Tap '+' to add today's tasks",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = White
        )
    }
}
