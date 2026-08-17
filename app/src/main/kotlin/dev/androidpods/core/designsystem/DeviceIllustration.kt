// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.designsystem

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

// PROJECT.md §19 "connection experience": a device illustration that plays the role a text glyph
// placeholder used to (see the ponytail: comment this replaced in HomeScreen.kt), plus a short
// connection animation. Domain-free on purpose (PROJECT.md §11 presentation layer / §12 module
// boundaries): it only knows a tint and a pulsing flag, not connection states -- callers in
// feature/home map AirPodsState to those.
@Composable
fun AirPodsIllustration(tint: Color, modifier: Modifier = Modifier, pulsing: Boolean = false) {
    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { appeared = true }
    val entryScale by animateFloatAsState(
        targetValue = if (appeared) 1f else 0.85f,
        animationSpec = androidpodsSpatialSpec(),
        label = "airpods-illustration-entry",
    )

    val pulseScale = if (pulsing && !androidpodsReduceMotion()) {
        val transition = rememberInfiniteTransition(label = "airpods-illustration-pulse")
        val pulse by transition.animateFloat(
            initialValue = 0.92f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "airpods-illustration-pulse-value",
        )
        pulse
    } else {
        1f
    }

    Canvas(
        modifier
            .size(148.dp)
            .graphicsLayer {
                scaleX = entryScale * pulseScale
                scaleY = entryScale * pulseScale
            },
    ) {
        drawAirPod(tint, mirror = false)
        drawAirPod(tint, mirror = true)
    }
}

// One AirPod: a bud (circle) with a stem (rounded rect) angled outward, mirrored for the other
// side. Deliberately simple silhouette, not a rendered asset -- boring shapes read clearly at
// small sizes and cost nothing to theme (PROJECT.md §6.2 dynamic color just flows through `tint`).
private fun DrawScope.drawAirPod(color: Color, mirror: Boolean) {
    val direction = if (mirror) 1f else -1f
    val headRadius = size.minDimension * 0.16f
    val headCenter = Offset(
        x = size.width / 2f + direction * size.width * 0.16f,
        y = size.height * 0.32f,
    )
    rotate(degrees = direction * 14f, pivot = headCenter) {
        drawCircle(color = color, radius = headRadius, center = headCenter)
        drawRoundRect(
            color = color,
            topLeft = Offset(headCenter.x - headRadius * 0.42f, headCenter.y + headRadius * 0.3f),
            size = Size(headRadius * 0.84f, headRadius * 2.6f),
            cornerRadius = CornerRadius(headRadius * 0.42f),
        )
    }
}
