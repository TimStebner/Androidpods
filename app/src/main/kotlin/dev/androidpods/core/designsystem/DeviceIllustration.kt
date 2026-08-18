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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import dev.androidpods.core.airpods.AirPodsCapabilities

/**
 * Resolves the [AirPodsGeneration] from [AirPodsCapabilities.modelName].
 */
val AirPodsCapabilities.generation: AirPodsGeneration
    get() = when {
        modelName.contains("Pro", ignoreCase = true) -> AirPodsGeneration.PRO
        modelName.contains("Max", ignoreCase = true) -> AirPodsGeneration.MAX
        modelName.contains("3", ignoreCase = true) -> AirPodsGeneration.GEN_3
        modelName.contains("1", ignoreCase = true) || modelName.contains("2", ignoreCase = true) -> AirPodsGeneration.GEN_1_2
        else -> AirPodsGeneration.GEN_4
    }

/**
 * Material 3 Expressive Animated AirPods Hero Illustration.
 *
 * Dynamically displays the exact hardware silhouette (AirPods 4, AirPods Pro,
 * AirPods 1-2, AirPods 3, AirPods Max) corresponding to the connected device,
 * with smooth entrance physics and subtle ambient breathing pulse.
 */
@Composable
fun AirPodsIllustration(
    tint: Color,
    modifier: Modifier = Modifier,
    pulsing: Boolean = false,
    generation: AirPodsGeneration = AirPodsGeneration.GEN_4,
    accentTint: Color = tint.copy(alpha = 0.65f),
) {
    val reduceMotion = androidpodsReduceMotion()
    val pulse: androidx.compose.runtime.State<Float>?
    if (pulsing && !reduceMotion) {
        val transition = rememberInfiniteTransition(label = "airpods-illustration-pulse")
        pulse = transition.animateFloat(
            initialValue = 0.94f,
            targetValue = 1.02f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "airpods-illustration-pulse-value",
        )
    } else {
        pulse = null
    }

    val leftPath = remember { Path() }
    val rightPath = remember { Path() }
    val tipLeftPath = remember { Path() }
    val tipRightPath = remember { Path() }

    Canvas(
        modifier
            .size(136.dp)
            .graphicsLayer {
                val scale = pulse?.value ?: 1f
                scaleX = scale
                scaleY = scale
            },
    ) {
        when (generation) {
            AirPodsGeneration.GEN_4 -> drawAirPodPairGen4(tint, accentTint, leftPath, rightPath)
            AirPodsGeneration.PRO -> drawAirPodPairPro(tint, accentTint, leftPath, rightPath, tipLeftPath, tipRightPath)
            AirPodsGeneration.GEN_1_2 -> drawAirPodPairGen1(tint, accentTint, leftPath, rightPath)
            AirPodsGeneration.GEN_3 -> drawAirPodPairGen3(tint, accentTint, leftPath, rightPath)
            AirPodsGeneration.MAX -> drawAirPodsMaxSilhouette(tint, accentTint, leftPath)
        }
    }
}

/**
 * Draws a mirrored pair of AirPods 4 with compact stems, acoustic cavity, and port notch.
 */
private fun DrawScope.drawAirPodPairGen4(primaryColor: Color, accentColor: Color, leftPath: Path, rightPath: Path) {
    val w = size.width
    val h = size.height

    // Left Bud
    drawSingleAirPodGen4(primaryColor, accentColor, isLeft = true, offsetCenterX = w * 0.34f, headPath = leftPath)
    // Right Bud
    drawSingleAirPodGen4(primaryColor, accentColor, isLeft = false, offsetCenterX = w * 0.66f, headPath = rightPath)
}

private fun DrawScope.drawSingleAirPodGen4(
    primaryColor: Color,
    accentColor: Color,
    isLeft: Boolean,
    offsetCenterX: Float,
    headPath: Path,
) {
    val w = size.width
    val h = size.height
    val stemWidth = w * 0.12f
    val stemHeight = h * 0.38f
    val stemLeft = if (isLeft) offsetCenterX - stemWidth * 0.1f else offsetCenterX - stemWidth * 0.9f
    val stemTop = h * 0.44f

    drawRoundRect(
        color = primaryColor,
        topLeft = Offset(stemLeft, stemTop),
        size = Size(stemWidth, stemHeight),
        cornerRadius = CornerRadius(stemWidth / 2f, stemWidth / 2f),
    )

    // Pod head
    headPath.rewind()
    if (isLeft) {
        headPath.moveTo(stemLeft + stemWidth * 0.95f, stemTop + stemHeight * 0.12f)
        headPath.cubicTo(
            stemLeft + stemWidth * 0.95f, h * 0.16f,
            offsetCenterX - w * 0.22f, h * 0.18f,
            offsetCenterX - w * 0.22f, h * 0.38f,
        )
        headPath.cubicTo(
            offsetCenterX - w * 0.22f, h * 0.52f,
            stemLeft - w * 0.04f, stemTop + stemHeight * 0.24f,
            stemLeft + stemWidth * 0.25f, stemTop + stemHeight * 0.12f,
        )
        headPath.close()
    } else {
        headPath.moveTo(stemLeft + stemWidth * 0.05f, stemTop + stemHeight * 0.12f)
        headPath.cubicTo(
            stemLeft + stemWidth * 0.05f, h * 0.16f,
            offsetCenterX + w * 0.22f, h * 0.18f,
            offsetCenterX + w * 0.22f, h * 0.38f,
        )
        headPath.cubicTo(
            offsetCenterX + w * 0.22f, h * 0.52f,
            stemLeft + stemWidth + w * 0.04f, stemTop + stemHeight * 0.24f,
            stemLeft + stemWidth * 0.75f, stemTop + stemHeight * 0.12f,
        )
        headPath.close()
    }
    drawPath(headPath, color = primaryColor, style = Fill)

    // Acoustic port notch
    val portX = if (isLeft) offsetCenterX - w * 0.16f else offsetCenterX + w * 0.04f
    drawRoundRect(
        color = accentColor,
        topLeft = Offset(portX, h * 0.28f),
        size = Size(w * 0.12f, h * 0.08f),
        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
    )
}

/**
 * Draws a mirrored pair of AirPods Pro with short stems, silicone ear tips, and black ANC mesh vents.
 */
private fun DrawScope.drawAirPodPairPro(
    primaryColor: Color,
    accentColor: Color,
    leftPath: Path,
    rightPath: Path,
    tipLeftPath: Path,
    tipRightPath: Path,
) {
    val w = size.width

    // Left Pro Bud
    drawSingleAirPodPro(primaryColor, accentColor, isLeft = true, offsetCenterX = w * 0.34f, bodyPath = leftPath, tipPath = tipLeftPath)
    // Right Pro Bud
    drawSingleAirPodPro(primaryColor, accentColor, isLeft = false, offsetCenterX = w * 0.66f, bodyPath = rightPath, tipPath = tipRightPath)
}

private fun DrawScope.drawSingleAirPodPro(
    primaryColor: Color,
    accentColor: Color,
    isLeft: Boolean,
    offsetCenterX: Float,
    bodyPath: Path,
    tipPath: Path,
) {
    val w = size.width
    val h = size.height
    val stemWidth = w * 0.13f
    val stemHeight = h * 0.34f
    val stemLeft = if (isLeft) offsetCenterX else offsetCenterX - stemWidth
    val stemTop = h * 0.48f

    // Short stem
    drawRoundRect(
        color = primaryColor,
        topLeft = Offset(stemLeft, stemTop),
        size = Size(stemWidth, stemHeight),
        cornerRadius = CornerRadius(stemWidth / 2f, stemWidth / 2f),
    )

    // Body
    bodyPath.rewind()
    if (isLeft) {
        bodyPath.moveTo(stemLeft + stemWidth, stemTop + stemHeight * 0.1f)
        bodyPath.cubicTo(
            stemLeft + stemWidth, h * 0.20f,
            offsetCenterX - w * 0.16f, h * 0.20f,
            offsetCenterX - w * 0.16f, h * 0.42f,
        )
        bodyPath.cubicTo(
            offsetCenterX - w * 0.16f, h * 0.54f,
            stemLeft, stemTop + stemHeight * 0.2f,
            stemLeft + stemWidth * 0.3f, stemTop + stemHeight * 0.1f,
        )
        bodyPath.close()
    } else {
        bodyPath.moveTo(stemLeft, stemTop + stemHeight * 0.1f)
        bodyPath.cubicTo(
            stemLeft, h * 0.20f,
            offsetCenterX + w * 0.16f, h * 0.20f,
            offsetCenterX + w * 0.16f, h * 0.42f,
        )
        bodyPath.cubicTo(
            offsetCenterX + w * 0.16f, h * 0.54f,
            stemLeft + stemWidth, stemTop + stemHeight * 0.2f,
            stemLeft + stemWidth * 0.7f, stemTop + stemHeight * 0.1f,
        )
        bodyPath.close()
    }
    drawPath(bodyPath, color = primaryColor, style = Fill)

    // Silicone Ear Tip
    tipPath.rewind()
    if (isLeft) {
        tipPath.moveTo(offsetCenterX - w * 0.14f, h * 0.28f)
        tipPath.cubicTo(
            offsetCenterX - w * 0.26f, h * 0.26f,
            offsetCenterX - w * 0.26f, h * 0.44f,
            offsetCenterX - w * 0.14f, h * 0.42f,
        )
        tipPath.close()
    } else {
        tipPath.moveTo(offsetCenterX + w * 0.14f, h * 0.28f)
        tipPath.cubicTo(
            offsetCenterX + w * 0.26f, h * 0.26f,
            offsetCenterX + w * 0.26f, h * 0.44f,
            offsetCenterX + w * 0.14f, h * 0.42f,
        )
        tipPath.close()
    }
    drawPath(tipPath, color = accentColor, style = Fill)

    // Black ANC mesh vent
    val meshX = if (isLeft) stemLeft + stemWidth * 0.25f else stemLeft - stemWidth * 0.25f
    drawOval(
        color = primaryColor.copy(alpha = 0.5f),
        topLeft = Offset(meshX, h * 0.27f),
        size = Size(w * 0.10f, h * 0.06f),
    )
}

/**
 * Draws a mirrored pair of AirPods 1 & 2 with classic long straight stems and round heads.
 */
private fun DrawScope.drawAirPodPairGen1(primaryColor: Color, accentColor: Color, leftPath: Path, rightPath: Path) {
    val w = size.width

    drawSingleAirPodGen1(primaryColor, accentColor, isLeft = true, offsetCenterX = w * 0.34f, headPath = leftPath)
    drawSingleAirPodGen1(primaryColor, accentColor, isLeft = false, offsetCenterX = w * 0.66f, headPath = rightPath)
}

private fun DrawScope.drawSingleAirPodGen1(
    primaryColor: Color,
    accentColor: Color,
    isLeft: Boolean,
    offsetCenterX: Float,
    headPath: Path,
) {
    val w = size.width
    val h = size.height
    val stemWidth = w * 0.11f
    val stemHeight = h * 0.52f
    val stemLeft = if (isLeft) offsetCenterX else offsetCenterX - stemWidth
    val stemTop = h * 0.34f

    // Long straight stem
    drawRoundRect(
        color = primaryColor,
        topLeft = Offset(stemLeft, stemTop),
        size = Size(stemWidth, stemHeight),
        cornerRadius = CornerRadius(stemWidth / 2f, stemWidth / 2f),
    )

    // Round open ear head
    headPath.rewind()
    if (isLeft) {
        headPath.moveTo(stemLeft + stemWidth * 0.8f, stemTop + stemHeight * 0.15f)
        headPath.cubicTo(
            stemLeft + stemWidth * 0.8f, h * 0.10f,
            offsetCenterX - w * 0.20f, h * 0.10f,
            offsetCenterX - w * 0.20f, h * 0.28f,
        )
        headPath.cubicTo(
            offsetCenterX - w * 0.20f, h * 0.42f,
            stemLeft, stemTop + stemHeight * 0.24f,
            stemLeft + stemWidth * 0.2f, stemTop + stemHeight * 0.15f,
        )
        headPath.close()
    } else {
        headPath.moveTo(stemLeft + stemWidth * 0.2f, stemTop + stemHeight * 0.15f)
        headPath.cubicTo(
            stemLeft + stemWidth * 0.2f, h * 0.10f,
            offsetCenterX + w * 0.20f, h * 0.10f,
            offsetCenterX + w * 0.20f, h * 0.28f,
        )
        headPath.cubicTo(
            offsetCenterX + w * 0.20f, h * 0.42f,
            stemLeft + stemWidth, stemTop + stemHeight * 0.24f,
            stemLeft + stemWidth * 0.8f, stemTop + stemHeight * 0.15f,
        )
        headPath.close()
    }
    drawPath(headPath, color = primaryColor, style = Fill)

    // Chrome bottom ring
    val ringH = h * 0.05f
    drawRoundRect(
        color = accentColor,
        topLeft = Offset(stemLeft, stemTop + stemHeight - ringH),
        size = Size(stemWidth, ringH),
        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
    )
}

/**
 * Draws a mirrored pair of AirPods 3 with medium angled stems and contoured heads.
 */
private fun DrawScope.drawAirPodPairGen3(primaryColor: Color, accentColor: Color, leftPath: Path, rightPath: Path) {
    val w = size.width

    drawSingleAirPodGen3(primaryColor, accentColor, isLeft = true, offsetCenterX = w * 0.34f, headPath = leftPath)
    drawSingleAirPodGen3(primaryColor, accentColor, isLeft = false, offsetCenterX = w * 0.66f, headPath = rightPath)
}

private fun DrawScope.drawSingleAirPodGen3(
    primaryColor: Color,
    accentColor: Color,
    isLeft: Boolean,
    offsetCenterX: Float,
    headPath: Path,
) {
    val w = size.width
    val h = size.height
    val stemWidth = w * 0.12f
    val stemHeight = h * 0.40f
    val stemLeft = if (isLeft) offsetCenterX - stemWidth * 0.1f else offsetCenterX - stemWidth * 0.9f
    val stemTop = h * 0.42f

    drawRoundRect(
        color = primaryColor,
        topLeft = Offset(stemLeft, stemTop),
        size = Size(stemWidth, stemHeight),
        cornerRadius = CornerRadius(stemWidth / 2f, stemWidth / 2f),
    )

    headPath.rewind()
    if (isLeft) {
        headPath.moveTo(stemLeft + stemWidth * 0.9f, stemTop + stemHeight * 0.15f)
        headPath.cubicTo(
            stemLeft + stemWidth * 0.9f, h * 0.14f,
            offsetCenterX - w * 0.22f, h * 0.16f,
            offsetCenterX - w * 0.22f, h * 0.36f,
        )
        headPath.cubicTo(
            offsetCenterX - w * 0.22f, h * 0.50f,
            stemLeft - w * 0.04f, stemTop + stemHeight * 0.26f,
            stemLeft + stemWidth * 0.3f, stemTop + stemHeight * 0.15f,
        )
        headPath.close()
    } else {
        headPath.moveTo(stemLeft + stemWidth * 0.1f, stemTop + stemHeight * 0.15f)
        headPath.cubicTo(
            stemLeft + stemWidth * 0.1f, h * 0.14f,
            offsetCenterX + w * 0.22f, h * 0.16f,
            offsetCenterX + w * 0.22f, h * 0.36f,
        )
        headPath.cubicTo(
            offsetCenterX + w * 0.22f, h * 0.50f,
            stemLeft + stemWidth + w * 0.04f, stemTop + stemHeight * 0.26f,
            stemLeft + stemWidth * 0.7f, stemTop + stemHeight * 0.15f,
        )
        headPath.close()
    }
    drawPath(headPath, color = primaryColor, style = Fill)

    // Top acoustic mesh vent
    val ventX = if (isLeft) offsetCenterX - w * 0.16f else offsetCenterX + w * 0.04f
    drawOval(
        color = accentColor,
        topLeft = Offset(ventX, h * 0.22f),
        size = Size(w * 0.12f, h * 0.08f),
    )
}
