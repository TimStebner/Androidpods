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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Material 3 Expressive Minimalist Left AirPod with 3D spatial perspective
 * floating physics (pitch, yaw, levitation) and tactile spring touch reaction.
 */
@Composable
fun AnimatedLeftAirPod(
    tint: Color = MaterialTheme.colorScheme.primary,
    accentTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    inEar: Boolean? = null,
    isCharging: Boolean = false,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 92.dp,
    onClick: (() -> Unit)? = null,
) {
    AnimatedAirPodExpressiveItem(
        tint = tint,
        accentTint = accentTint,
        isLeft = true,
        inEar = inEar,
        isCharging = isCharging,
        modifier = modifier,
        sizeDp = sizeDp,
        onClick = onClick,
    )
}

/**
 * Material 3 Expressive Minimalist Right AirPod with mirrored 3D spatial perspective
 * floating physics and tactile spring touch reaction.
 */
@Composable
fun AnimatedRightAirPod(
    tint: Color = MaterialTheme.colorScheme.primary,
    accentTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    inEar: Boolean? = null,
    isCharging: Boolean = false,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 92.dp,
    onClick: (() -> Unit)? = null,
) {
    AnimatedAirPodExpressiveItem(
        tint = tint,
        accentTint = accentTint,
        isLeft = false,
        inEar = inEar,
        isCharging = isCharging,
        modifier = modifier,
        sizeDp = sizeDp,
        onClick = onClick,
    )
}

@Composable
private fun AnimatedAirPodExpressiveItem(
    tint: Color,
    accentTint: Color,
    isLeft: Boolean,
    inEar: Boolean?,
    isCharging: Boolean,
    modifier: Modifier,
    sizeDp: Dp,
    onClick: (() -> Unit)?,
) {
    val haptics = rememberAppHaptics()
    val density = LocalDensity.current.density
    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entered = true }

    val entryScale by animateFloatAsState(
        targetValue = if (entered) 1f else 0.4f,
        animationSpec = androidpodsSpatialSpec(),
        label = "m3-airpod-entry-scale",
    )
    val entryOffsetY by animateFloatAsState(
        targetValue = if (entered) 0f else 28f,
        animationSpec = androidpodsSpatialSpec(),
        label = "m3-airpod-entry-y",
    )

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = androidpodsSpatialSpec(),
        label = "m3-airpod-press-scale",
    )
    val pressPitch by animateFloatAsState(
        targetValue = if (isPressed) (if (isLeft) 16f else -16f) else 0f,
        animationSpec = androidpodsSpatialSpec(),
        label = "m3-airpod-press-pitch",
    )

    val reduceMotion = androidpodsReduceMotion()
    val floatTransition = rememberInfiniteTransition(label = "m3-airpod-3d-motion")

    // 1. Smooth 3D Levitation Float
    val floatY by floatTransition.animateFloat(
        initialValue = if (isLeft) -4.5f else 4.5f,
        targetValue = if (isLeft) 4.5f else -4.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "m3-float-y",
    )

    // 2. Smooth 3D Yaw Rotation (perspective turn)
    val yawAngle by floatTransition.animateFloat(
        initialValue = if (isLeft) -12f else 12f,
        targetValue = if (isLeft) 8f else -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "m3-yaw-angle",
    )

    // 3. Smooth 3D Pitch Tilt
    val pitchAngle by floatTransition.animateFloat(
        initialValue = if (isLeft) 6f else -5f,
        targetValue = if (isLeft) -5f else 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "m3-pitch-angle",
    )

    val activeFloatY = if (!reduceMotion && entered) floatY else 0f
    val activeYaw = if (!reduceMotion && entered) yawAngle else 0f
    val activePitch = if (!reduceMotion && entered) pitchAngle + pressPitch else pressPitch

    Box(
        modifier = modifier
            .size(sizeDp)
            .graphicsLayer {
                cameraDistance = 14f * density
                scaleX = entryScale * pressScale
                scaleY = entryScale * pressScale
                translationY = entryOffsetY + activeFloatY
                rotationX = activePitch
                rotationY = activeYaw
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
            ) {
                haptics.tick()
                onClick?.invoke()
            },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawExpressiveM3AirPod(
                color = tint,
                accentColor = accentTint,
                isLeft = isLeft,
                inEar = inEar == true,
            )
        }
    }
}

/**
 * Material 3 Expressive Minimalist AirPods Charging Case with clean tonal styling,
 * active pulsating LED status indicator, and 3D perspective pitch.
 */
@Composable
fun AnimatedAirPodsCase(
    tint: Color = MaterialTheme.colorScheme.primary,
    accentTint: Color = MaterialTheme.colorScheme.outlineVariant,
    batteryLevel: Int? = null,
    isCharging: Boolean = false,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 102.dp,
    onClick: (() -> Unit)? = null,
) {
    val haptics = rememberAppHaptics()
    val density = LocalDensity.current.density
    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entered = true }

    val entryScale by animateFloatAsState(
        targetValue = if (entered) 1f else 0.5f,
        animationSpec = androidpodsSpatialSpec(),
        label = "m3-case-entry-scale",
    )

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = androidpodsSpatialSpec(),
        label = "m3-case-press-scale",
    )
    val pressPitch by animateFloatAsState(
        targetValue = if (isPressed) 12f else 0f,
        animationSpec = androidpodsSpatialSpec(),
        label = "m3-case-press-pitch",
    )

    val reduceMotion = androidpodsReduceMotion()
    val glowTransition = rememberInfiniteTransition(label = "m3-case-led-glow")
    val ledGlowAlpha by glowTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "m3-led-glow-alpha",
    )

    val caseFloatTransition = rememberInfiniteTransition(label = "m3-case-float")
    val caseYaw by caseFloatTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "m3-case-yaw",
    )

    val ledColor = when {
        isCharging -> Color(0xFF22C55E) // Bright Emerald Green charging
        batteryLevel == null -> Color(0xFF38BDF8) // Cyan detecting
        batteryLevel <= 20 -> Color(0xFFF59E0B) // Warm Amber low battery
        else -> Color(0xFF22C55E) // Green charged
    }

    val activeCaseYaw = if (!reduceMotion && entered) caseYaw else 0f
    val activeCasePitch = 5f + pressPitch

    Box(
        modifier = modifier
            .size(sizeDp)
            .graphicsLayer {
                cameraDistance = 14f * density
                scaleX = entryScale * pressScale
                scaleY = entryScale * pressScale
                rotationX = activeCasePitch
                rotationY = activeCaseYaw
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
            ) {
                haptics.tick()
                onClick?.invoke()
            },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawExpressiveM3Case(
                bodyColor = tint,
                seamColor = accentTint,
                ledColor = ledColor,
                ledAlpha = if (reduceMotion) 1f else ledGlowAlpha,
                isCharging = isCharging,
            )
        }
    }
}

/**
 * Composed Hero Trio placing Left Pod, Case, and Right Pod in a clean,
 * harmonious Material 3 Expressive floating layout.
 */
@Composable
fun AirPodsExpressiveTrio(
    tint: Color = MaterialTheme.colorScheme.primary,
    accentTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    leftInEar: Boolean? = null,
    rightInEar: Boolean? = null,
    leftCharging: Boolean = false,
    rightCharging: Boolean = false,
    caseCharging: Boolean = false,
    caseBatteryLevel: Int? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(132.dp)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AnimatedLeftAirPod(
            tint = tint,
            accentTint = accentTint,
            inEar = leftInEar,
            isCharging = leftCharging,
            sizeDp = 88.dp,
        )

        AnimatedAirPodsCase(
            tint = tint,
            accentTint = accentTint,
            batteryLevel = caseBatteryLevel,
            isCharging = caseCharging,
            sizeDp = 100.dp,
        )

        AnimatedRightAirPod(
            tint = tint,
            accentTint = accentTint,
            inEar = rightInEar,
            isCharging = rightCharging,
            sizeDp = 88.dp,
        )
    }
}

/**
 * Clean, minimalist Material 3 Expressive AirPod vector silhouette
 * with iconic ergonomic contours, acoustic mesh notch, and chrome base ring.
 */
private fun DrawScope.drawExpressiveM3AirPod(
    color: Color,
    accentColor: Color,
    isLeft: Boolean,
    inEar: Boolean,
) {
    val w = size.width
    val h = size.height

    val stemWidth = w * 0.18f
    val stemHeight = h * 0.44f
    val stemLeft = if (isLeft) w * 0.48f else w * 0.34f
    val stemTop = h * 0.46f

    // 1. Ergonomic Sculpted Head Path (AirPods 4 / Pro Curvature)
    val headPath = Path().apply {
        if (isLeft) {
            moveTo(stemLeft + stemWidth * 0.95f, stemTop + stemHeight * 0.12f)
            cubicTo(
                stemLeft + stemWidth * 0.95f, h * 0.15f,
                w * 0.17f, h * 0.17f,
                w * 0.17f, h * 0.38f,
            )
            cubicTo(
                w * 0.17f, h * 0.54f,
                stemLeft - w * 0.04f, stemTop + stemHeight * 0.25f,
                stemLeft + stemWidth * 0.25f, stemTop + stemHeight * 0.12f,
            )
            close()
        } else {
            moveTo(stemLeft + stemWidth * 0.05f, stemTop + stemHeight * 0.12f)
            cubicTo(
                stemLeft + stemWidth * 0.05f, h * 0.15f,
                w * 0.83f, h * 0.17f,
                w * 0.83f, h * 0.38f,
            )
            cubicTo(
                w * 0.83f, h * 0.54f,
                stemLeft + stemWidth + w * 0.04f, stemTop + stemHeight * 0.25f,
                stemLeft + stemWidth * 0.75f, stemTop + stemHeight * 0.12f,
            )
            close()
        }
    }

    // Tonal body fill with subtle top specular sheen
    val headBrush = Brush.linearGradient(
        colors = listOf(
            color.copy(alpha = 0.95f),
            color.copy(alpha = 0.80f),
        ),
        start = Offset(w / 2f, 0f),
        end = Offset(w / 2f, h),
    )
    drawPath(path = headPath, brush = headBrush, style = Fill)

    // 2. Sculpted Cylindrical Stem
    drawRoundRect(
        color = color,
        topLeft = Offset(stemLeft, stemTop),
        size = Size(stemWidth, stemHeight),
        cornerRadius = CornerRadius(stemWidth / 2f, stemWidth / 2f),
    )

    // 3. Iconic Acoustic Speaker Port Notch
    val portX = if (isLeft) w * 0.24f else w * 0.58f
    val portY = h * 0.28f
    drawRoundRect(
        color = accentColor.copy(alpha = 0.85f),
        topLeft = Offset(portX, portY),
        size = Size(w * 0.18f, h * 0.11f),
        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
    )

    // 4. Chrome Bottom Stem Ring
    val chromeHeight = h * 0.055f
    val chromeTop = stemTop + stemHeight - chromeHeight
    drawRoundRect(
        color = accentColor.copy(alpha = 0.75f),
        topLeft = Offset(stemLeft, chromeTop),
        size = Size(stemWidth, chromeHeight),
        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
    )

    // 5. In-Ear Status Dot
    if (inEar) {
        val dotX = if (isLeft) w * 0.32f else w * 0.68f
        drawCircle(
            color = Color(0xFF22C55E),
            radius = 3.5.dp.toPx(),
            center = Offset(dotX, h * 0.10f),
        )
    }
}

/**
 * Clean, minimalist Material 3 Expressive Charging Case
 * with clean squircle body, lid cut seam, and pulsating LED light.
 */
private fun DrawScope.drawExpressiveM3Case(
    bodyColor: Color,
    seamColor: Color,
    ledColor: Color,
    ledAlpha: Float,
    isCharging: Boolean,
) {
    val w = size.width
    val h = size.height

    val caseWidth = w * 0.78f
    val caseHeight = h * 0.84f
    val caseLeft = (w - caseWidth) / 2f
    val caseTop = (h - caseHeight) / 2f
    val cornerRadius = CornerRadius(caseWidth * 0.36f, caseWidth * 0.36f)

    // Subtle charging aura
    if (isCharging) {
        drawRoundRect(
            brush = Brush.radialGradient(
                colors = listOf(ledColor.copy(alpha = 0.22f * ledAlpha), Color.Transparent),
                center = Offset(w / 2f, h / 2f),
                radius = w * 0.50f,
            ),
            topLeft = Offset(0f, 0f),
            size = size,
            cornerRadius = cornerRadius,
        )
    }

    // Case body fill
    drawRoundRect(
        color = bodyColor.copy(alpha = 0.20f),
        topLeft = Offset(caseLeft, caseTop),
        size = Size(caseWidth, caseHeight),
        cornerRadius = cornerRadius,
    )

    // Case outer contour
    drawRoundRect(
        color = bodyColor,
        topLeft = Offset(caseLeft, caseTop),
        size = Size(caseWidth, caseHeight),
        cornerRadius = cornerRadius,
        style = Stroke(width = 2.dp.toPx()),
    )

    // Lid horizontal cut seam
    val lidY = caseTop + caseHeight * 0.32f
    drawLine(
        color = seamColor.copy(alpha = 0.60f),
        start = Offset(caseLeft + 2.dp.toPx(), lidY),
        end = Offset(caseLeft + caseWidth - 2.dp.toPx(), lidY),
        strokeWidth = 1.4.dp.toPx(),
    )

    // Front LED Status Light (Pulsating Glow)
    val ledCenter = Offset(w / 2f, caseTop + caseHeight * 0.58f)
    val ledRadius = 3.dp.toPx()

    // Outer glow
    drawCircle(
        color = ledColor.copy(alpha = 0.35f * ledAlpha),
        radius = ledRadius * 2.8f,
        center = ledCenter,
    )
    // Luminous core
    drawCircle(
        color = ledColor.copy(alpha = ledAlpha),
        radius = ledRadius,
        center = ledCenter,
    )
}
