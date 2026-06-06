package com.vikashsinghapp.lockin.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// 1. Define your standard tactical spacing tokens based on your existing UI
data class Spacing(
    val default: Dp = 0.dp,
    val extraSmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 12.dp,
    val large: Dp = 16.dp,       // Your standard screen padding
    val extraLarge: Dp = 20.dp,  // Used heavily in your bottom sheets
    val huge: Dp = 24.dp,
    val massive: Dp = 32.dp,

    // Component specific sizes (Optional but very helpful for consistency)
    val buttonHeight: Dp = 56.dp,
    val iconSizeSmall: Dp = 16.dp,
    val iconSizeMedium: Dp = 24.dp,
    val iconSizeLarge: Dp = 32.dp
)

// 2. Create the CompositionLocal
val LocalSpacing = compositionLocalOf { Spacing() }

// 3. Create the extension property on MaterialTheme for easy access
val MaterialTheme.spacing: Spacing
    @Composable
    @ReadOnlyComposable
    get() = LocalSpacing.current