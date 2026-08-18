// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Material 3 Expressive Status Bar Scrim.
 *
 * Renders a soft vertical gradient over the top status bar insets
 * to ensure system status bar icons (clock, battery, Wi-Fi) maintain high contrast
 * and legibility when content scrolls edge-to-edge underneath.
 */
@Composable
fun StatusBarScrim(
    modifier: Modifier = Modifier,
    scrimColor: Color = MaterialTheme.colorScheme.surface,
    extraHeight: Dp = 16.dp,
) {
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val totalHeight = statusBarHeight + extraHeight

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(totalHeight)
            .background(
                brush = Brush.verticalGradient(
                    0.0f to scrimColor.copy(alpha = 0.95f),
                    0.55f to scrimColor.copy(alpha = 0.70f),
                    1.0f to Color.Transparent,
                ),
            ),
    )
}
