// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.designsystem

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

/**
 * An animated audio waveform visualizer that breathes and ripples gently
 * when the connection is active.
 */
@Composable
fun AudioWaveformVisualizer(
    modifier: Modifier = Modifier,
    active: Boolean = true,
    barCount: Int = 28,
    heightDp: Dp = 38.dp,
    color: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
) {
    val reduceMotion = androidpodsReduceMotion()

    val infiniteTransition = rememberInfiniteTransition(label = "waveform-anim")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "waveform-phase",
    )

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "waveform-pulse",
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(heightDp),
    ) {
        val totalWidth = size.width
        val canvasHeight = size.height
        val barWidth = (totalWidth / (barCount * 1.8f)).coerceIn(2.5f, 6.5f)
        val availableGap = (totalWidth - (barCount * barWidth)) / (barCount - 1).coerceAtLeast(1)

        val baseHeights = FloatArray(barCount) { index ->
            // Symmetrical envelope curve (tapered at ends, fuller in the middle/lobes)
            val normalizedX = index.toFloat() / (barCount - 1).toFloat() // 0f..1f
            val envelope = sin(normalizedX * PI).toFloat()
            // Multiple harmonic peaks
            val harmonic = (sin(normalizedX * 4 * PI + 0.5f) * 0.35f + 0.65f).toFloat()
            (envelope * harmonic).coerceIn(0.15f, 1f)
        }

        for (i in 0 until barCount) {
            val normalizedX = i.toFloat() / (barCount - 1).toFloat()
            val animatedHeightMultiplier = if (active && !reduceMotion) {
                val wave = sin(normalizedX * 3 * PI + phase).toFloat() * 0.35f + 0.65f
                wave * pulse
            } else if (active) {
                1f
            } else {
                0.2f
            }

            val barHeight = (canvasHeight * baseHeights[i] * animatedHeightMultiplier)
                .coerceIn(canvasHeight * 0.12f, canvasHeight * 0.95f)

            val x = i * (barWidth + availableGap)
            val y = (canvasHeight - barHeight) / 2f

            drawRoundRect(
                color = color,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f),
            )
        }
    }
}
