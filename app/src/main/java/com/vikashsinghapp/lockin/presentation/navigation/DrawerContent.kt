package com.vikashsinghapp.lockin.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vikashsinghapp.lockin.Constants
import com.vikashsinghapp.lockin.Constants.TAG
import com.vikashsinghapp.lockin.ui.theme.SurfaceDarkElevated
import timber.log.Timber

@Composable
fun DrawerContent(
    currentRoute: String,
    onDestinationClicked: (Screen) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(0.5f)
            .background(SurfaceDarkElevated)
            .padding(top = 48.dp)
    ) {
        Constants.ALL_SCREENS.forEach { screen ->
            Timber.tag(TAG).d("NAVIGATION DrawerContent screen : $screen screen.route: ${screen.route}")
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDestinationClicked(screen) }
                    .padding(vertical = 14.dp)
                    .padding(start = 24.dp),
                text = screen.screenName,
                fontWeight = if(screen.route == currentRoute) FontWeight.Bold else FontWeight.Normal,
                color = White
            )
        }
    }
}
