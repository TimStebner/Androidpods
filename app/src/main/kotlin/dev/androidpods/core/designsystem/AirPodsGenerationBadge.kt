// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.designsystem

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The 5 iconic hardware generations of Apple AirPods.
 */
enum class AirPodsGeneration {
    GEN_4,
    PRO,
    GEN_3,
    GEN_1_2,
    MAX;

    fun next(): AirPodsGeneration {
        val entries = entries
        val nextIndex = (ordinal + 1) % entries.size
        return entries[nextIndex]
    }
}

/**
 * Interactive Material 3 Expressive Badge that cycles and morphs through all
 * 5 iconic AirPods generations (AirPods 1/2, AirPods 3, AirPods 4, AirPods Pro, AirPods Max)
 * on tap with spring physics and tactile haptic feedback.
 */
@Composable
fun AirPodsGenerationMorphBadge(
    modifier: Modifier = Modifier,
    sizeDp: Dp = 48.dp,
    initialGeneration: AirPodsGeneration = AirPodsGeneration.GEN_4,
    onGenerationChanged: ((AirPodsGeneration) -> Unit)? = null,
) {
    val haptics = rememberAppHaptics()
    var currentGen by remember { mutableIntStateOf(initialGeneration.ordinal) }
    val generation = AirPodsGeneration.entries[currentGen]

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = androidpodsSpatialSpec(),
        label = "airpods-badge-press-scale",
    )

    Surface(
        modifier = modifier
            .size(sizeDp)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
            ) {
                haptics.tick()
                val nextIndex = (currentGen + 1) % AirPodsGeneration.entries.size
                currentGen = nextIndex
                onGenerationChanged?.invoke(AirPodsGeneration.entries[nextIndex])
            },
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        shadowElevation = 3.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedContent(
                targetState = generation,
                transitionSpec = {
                    (fadeIn(animationSpec = androidx.compose.animation.core.spring(dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy, stiffness = androidx.compose.animation.core.Spring.StiffnessMedium)) +
                        scaleIn(initialScale = 0.7f, animationSpec = androidx.compose.animation.core.spring(dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy, stiffness = androidx.compose.animation.core.Spring.StiffnessMedium)))
                        .togetherWith(
                            fadeOut(animationSpec = androidx.compose.animation.core.spring(dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy, stiffness = androidx.compose.animation.core.Spring.StiffnessMedium)) +
                                scaleOut(targetScale = 1.25f, animationSpec = androidx.compose.animation.core.spring(dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy, stiffness = androidx.compose.animation.core.Spring.StiffnessMedium)),
                        )
                },
                label = "airpods-generation-morph",
            ) { gen ->
                val primaryColor = MaterialTheme.colorScheme.onPrimaryContainer
                val accentColor = MaterialTheme.colorScheme.primary

                Canvas(modifier = Modifier.fillMaxSize()) {
                    when (gen) {
                        AirPodsGeneration.GEN_1_2 -> drawAirPodsGen1Silhouette(primaryColor, accentColor)
                        AirPodsGeneration.GEN_3 -> drawAirPodsGen3Silhouette(primaryColor, accentColor)
                        AirPodsGeneration.GEN_4 -> drawAirPodsGen4Silhouette(primaryColor, accentColor)
                        AirPodsGeneration.PRO -> drawAirPodsProSilhouette(primaryColor, accentColor)
                        AirPodsGeneration.MAX -> drawAirPodsMaxSilhouette(primaryColor, accentColor)
                    }
                }
            }
        }
    }
}

/**
 * Silhouette for AirPods 1 & 2: Classic long straight stem and rounded pod head.
 */
private fun DrawScope.drawAirPodsGen1Silhouette(primaryColor: Color, accentColor: Color) {
    val w = size.width
    val h = size.height

    // Long straight stem
    val stemWidth = w * 0.16f
    val stemHeight = h * 0.58f
    val stemLeft = w * 0.52f
    val stemTop = h * 0.34f
    drawRoundRect(
        color = primaryColor,
        topLeft = Offset(stemLeft, stemTop),
        size = Size(stemWidth, stemHeight),
        cornerRadius = CornerRadius(stemWidth / 2f, stemWidth / 2f),
    )

    // Round open ear head
    val headPath = Path().apply {
        moveTo(stemLeft + stemWidth * 0.8f, stemTop + stemHeight * 0.2f)
        cubicTo(
            stemLeft + stemWidth * 0.8f, h * 0.08f,
            w * 0.22f, h * 0.08f,
            w * 0.22f, h * 0.28f,
        )
        cubicTo(
            w * 0.22f, h * 0.44f,
            stemLeft, stemTop + stemHeight * 0.3f,
            stemLeft + stemWidth * 0.2f, stemTop + stemHeight * 0.2f,
        )
        close()
    }
    drawPath(headPath, color = primaryColor, style = Fill)

    // Chrome bottom ring
    drawRoundRect(
        color = accentColor,
        topLeft = Offset(stemLeft, stemTop + stemHeight - h * 0.06f),
        size = Size(stemWidth, h * 0.06f),
        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
    )
}

/**
 * Silhouette for AirPods 3: Shorter angled stem with larger acoustic body.
 */
private fun DrawScope.drawAirPodsGen3Silhouette(primaryColor: Color, accentColor: Color) {
    val w = size.width
    val h = size.height

    // Medium angled stem
    val stemWidth = w * 0.18f
    val stemHeight = h * 0.44f
    val stemLeft = w * 0.50f
    val stemTop = h * 0.46f
    drawRoundRect(
        color = primaryColor,
        topLeft = Offset(stemLeft, stemTop),
        size = Size(stemWidth, stemHeight),
        cornerRadius = CornerRadius(stemWidth / 2f, stemWidth / 2f),
    )

    // Ergonomic angled acoustic head
    val headPath = Path().apply {
        moveTo(stemLeft + stemWidth * 0.9f, stemTop + stemHeight * 0.15f)
        cubicTo(
            stemLeft + stemWidth * 0.9f, h * 0.12f,
            w * 0.16f, h * 0.14f,
            w * 0.16f, h * 0.36f,
        )
        cubicTo(
            w * 0.16f, h * 0.54f,
            stemLeft - w * 0.06f, stemTop + stemHeight * 0.3f,
            stemLeft + stemWidth * 0.3f, stemTop + stemHeight * 0.15f,
        )
        close()
    }
    drawPath(headPath, color = primaryColor, style = Fill)

    // Top acoustic mesh vent
    drawOval(
        color = accentColor,
        topLeft = Offset(w * 0.28f, h * 0.20f),
        size = Size(w * 0.20f, h * 0.10f),
    )
}

/**
 * Silhouette for AirPods 4: Modern refined ultra-compact stem with acoustic cavity.
 */
private fun DrawScope.drawAirPodsGen4Silhouette(primaryColor: Color, accentColor: Color) {
    val w = size.width
    val h = size.height

    // Short refined stem with force sensor groove
    val stemWidth = w * 0.19f
    val stemHeight = h * 0.42f
    val stemLeft = w * 0.48f
    val stemTop = h * 0.48f
    drawRoundRect(
        color = primaryColor,
        topLeft = Offset(stemLeft, stemTop),
        size = Size(stemWidth, stemHeight),
        cornerRadius = CornerRadius(stemWidth / 2f, stemWidth / 2f),
    )

    // Ultra-ergonomic pod head
    val headPath = Path().apply {
        moveTo(stemLeft + stemWidth * 0.95f, stemTop + stemHeight * 0.1f)
        cubicTo(
            stemLeft + stemWidth * 0.95f, h * 0.14f,
            w * 0.18f, h * 0.16f,
            w * 0.18f, h * 0.38f,
        )
        cubicTo(
            w * 0.18f, h * 0.56f,
            stemLeft, stemTop + stemHeight * 0.2f,
            stemLeft + stemWidth * 0.3f, stemTop + stemHeight * 0.1f,
        )
        close()
    }
    drawPath(headPath, color = primaryColor, style = Fill)

    // Distinct side acoustic port notch
    drawRoundRect(
        color = accentColor,
        topLeft = Offset(w * 0.24f, h * 0.28f),
        size = Size(w * 0.18f, h * 0.11f),
        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
    )
}

/**
 * Silhouette for AirPods Pro: Short stem + silicone in-ear ear tip and black mesh vents.
 */
private fun DrawScope.drawAirPodsProSilhouette(primaryColor: Color, accentColor: Color) {
    val w = size.width
    val h = size.height

    // Short stem
    val stemWidth = w * 0.19f
    val stemHeight = h * 0.40f
    val stemLeft = w * 0.52f
    val stemTop = h * 0.50f
    drawRoundRect(
        color = primaryColor,
        topLeft = Offset(stemLeft, stemTop),
        size = Size(stemWidth, stemHeight),
        cornerRadius = CornerRadius(stemWidth / 2f, stemWidth / 2f),
    )

    // Pod body
    val bodyPath = Path().apply {
        moveTo(stemLeft + stemWidth, stemTop + stemHeight * 0.1f)
        cubicTo(
            stemLeft + stemWidth, h * 0.18f,
            w * 0.28f, h * 0.18f,
            w * 0.28f, h * 0.42f,
        )
        cubicTo(
            w * 0.28f, h * 0.56f,
            stemLeft, stemTop + stemHeight * 0.2f,
            stemLeft + stemWidth * 0.3f, stemTop + stemHeight * 0.1f,
        )
        close()
    }
    drawPath(bodyPath, color = primaryColor, style = Fill)

    // Distinct Silicone Ear Tip (angled on the left)
    val tipPath = Path().apply {
        moveTo(w * 0.30f, h * 0.28f)
        cubicTo(
            w * 0.10f, h * 0.24f,
            w * 0.10f, h * 0.46f,
            w * 0.30f, h * 0.44f,
        )
        close()
    }
    drawPath(tipPath, color = accentColor, style = Fill)

    // Black ANC mesh vent on back of pod head
    drawOval(
        color = primaryColor.copy(alpha = 0.5f),
        topLeft = Offset(stemLeft + stemWidth * 0.4f, h * 0.26f),
        size = Size(w * 0.14f, h * 0.08f),
    )
}

/**
 * Silhouette for AirPods Max: Over-ear headphone ear-cup and headband canopy.
 */
internal fun DrawScope.drawAirPodsMaxSilhouette(primaryColor: Color, accentColor: Color) {
    val w = size.width
    val h = size.height

    // Headband Mesh Canopy Arc
    val headbandPath = Path().apply {
        moveTo(w * 0.25f, h * 0.55f)
        cubicTo(
            w * 0.25f, h * 0.12f,
            w * 0.75f, h * 0.12f,
            w * 0.75f, h * 0.55f,
        )
    }
    drawPath(
        path = headbandPath,
        color = primaryColor,
        style = Stroke(width = 3.5.dp.toPx()),
    )

    // Left Ear Cup (Anodized aluminum pill)
    drawRoundRect(
        color = accentColor,
        topLeft = Offset(w * 0.14f, h * 0.45f),
        size = Size(w * 0.24f, h * 0.46f),
        cornerRadius = CornerRadius(w * 0.12f, w * 0.12f),
    )

    // Right Ear Cup (Anodized aluminum pill)
    drawRoundRect(
        color = accentColor,
        topLeft = Offset(w * 0.62f, h * 0.45f),
        size = Size(w * 0.24f, h * 0.46f),
        cornerRadius = CornerRadius(w * 0.12f, w * 0.12f),
    )
}
