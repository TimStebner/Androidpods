// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.designsystem

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Curated sequence of Material 3 Expressive shapes for dynamic morphing.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
val OnboardingMorphPolygons: List<RoundedPolygon> = listOf(
    MaterialShapes.Sunny,
    MaterialShapes.Cookie4Sided,
    MaterialShapes.Clover4Leaf,
    MaterialShapes.Pill,
    MaterialShapes.Burst,
    MaterialShapes.Arch,
    MaterialShapes.Boom,
    MaterialShapes.Heart,
    MaterialShapes.ClamShell,
)

/**
 * Converts a [Morph] at the specified [progress] into a Compose [Path].
 */
fun Morph.toComposePath(progress: Float, path: Path = Path()): Path {
    path.rewind()
    var first = true
    forEachCubic(progress) { cubic ->
        if (first) {
            path.moveTo(cubic.anchor0X, cubic.anchor0Y)
            first = false
        }
        path.cubicTo(
            cubic.control0X,
            cubic.control0Y,
            cubic.control1X,
            cubic.control1Y,
            cubic.anchor1X,
            cubic.anchor1Y,
        )
    }
    path.close()
    return path
}

/**
 * Calculates a normalized bounding [Rect] for this [Morph].
 */
fun Morph.calculateBoundsRect(): Rect {
    val bounds = FloatArray(4)
    calculateBounds(bounds)
    return Rect(bounds[0], bounds[1], bounds[2], bounds[3])
}

/**
 * Draws a morphing shape within the canvas bounds, centered and scaled to fit.
 */
fun DrawScope.drawMorphShape(
    morph: Morph,
    progress: Float,
    color: Color,
    path: Path = Path(),
    rotationDegrees: Float = 0f,
    strokeWidth: Float? = null,
) {
    val morphPath = morph.toComposePath(progress, path)
    val bounds = morph.calculateBoundsRect()
    val boundsWidth = bounds.width.coerceAtLeast(0.001f)
    val boundsHeight = bounds.height.coerceAtLeast(0.001f)

    val scaleFactor = minOf(size.width / boundsWidth, size.height / boundsHeight) * 0.9f
    val centerX = size.width / 2f
    val centerY = size.height / 2f
    val boundsCenterX = bounds.center.x
    val boundsCenterY = bounds.center.y

    rotate(rotationDegrees, pivot = Offset(centerX, centerY)) {
        translate(
            left = centerX - boundsCenterX * scaleFactor,
            top = centerY - boundsCenterY * scaleFactor,
        ) {
            scale(scaleFactor, pivot = Offset(0f, 0f)) {
                if (strokeWidth != null && strokeWidth > 0f) {
                    drawPath(
                        path = morphPath,
                        color = color,
                        style = Stroke(width = strokeWidth / scaleFactor),
                    )
                } else {
                    drawPath(
                        path = morphPath,
                        color = color,
                        style = Fill,
                    )
                }
            }
        }
    }
}

/**
 * A rich, multi-layered Material 3 Expressive Hero unit featuring shape morphing
 * and interactive tactile haptic response.
 */
@Composable
fun MorphingShapeHero(
    modifier: Modifier = Modifier,
    sizeDp: Dp = 220.dp,
    shapes: List<RoundedPolygon> = OnboardingMorphPolygons,
    haptics: AppHaptics = rememberAppHaptics(),
    content: @Composable () -> Unit = {},
) {
    require(shapes.size >= 2) { "At least two shapes are required for morphing." }

    var currentShapeIndex by remember { mutableIntStateOf(0) }
    val nextShapeIndex = (currentShapeIndex + 1) % shapes.size
    val morphProgress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val reduceMotion = androidpodsReduceMotion()

    // Smooth continuous auto-morph loop unless reduced motion is active
    LaunchedEffect(currentShapeIndex, reduceMotion) {
        if (!reduceMotion) {
            delay(2800L)
            morphProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            )
            currentShapeIndex = nextShapeIndex
            morphProgress.snapTo(0f)
        }
    }

    // Subtle breathing rotation
    val infiniteTransition = rememberInfiniteTransition(label = "hero-rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "hero-rotation-val",
    )

    val currentMorph = remember(currentShapeIndex, nextShapeIndex) {
        Morph(shapes[currentShapeIndex], shapes[nextShapeIndex])
    }

    // Secondary accent aura morph (offset shape)
    val accentMorph = remember(currentShapeIndex, shapes) {
        val accentStart = (currentShapeIndex + 2) % shapes.size
        val accentEnd = (currentShapeIndex + 3) % shapes.size
        Morph(shapes[accentStart], shapes[accentEnd])
    }

    val primaryContainerColor = MaterialTheme.colorScheme.primaryContainer
    val tertiaryContainerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f)
    val outlineColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)

    val reusablePath = remember { Path() }

    Box(
        modifier = modifier
            .size(sizeDp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                // Interactive manual tap morph with spring physics and tactile tick!
                haptics.tick()
                scope.launch {
                    morphProgress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
                    )
                    currentShapeIndex = nextShapeIndex
                    morphProgress.snapTo(0f)
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val rot = if (reduceMotion) 0f else rotation

            // 1. Outer Accent Aura Shape
            drawMorphShape(
                morph = accentMorph,
                progress = morphProgress.value,
                color = tertiaryContainerColor,
                path = reusablePath,
                rotationDegrees = -rot * 1.5f,
            )

            // 2. Main Bold Container Shape
            drawMorphShape(
                morph = currentMorph,
                progress = morphProgress.value,
                color = primaryContainerColor,
                path = reusablePath,
                rotationDegrees = rot,
            )

            // 3. Subtle Tonal Border Outline for crisp shape definition
            drawMorphShape(
                morph = currentMorph,
                progress = morphProgress.value,
                color = outlineColor,
                path = reusablePath,
                rotationDegrees = rot,
                strokeWidth = 3.dp.toPx(),
            )
        }

        // Center Content (e.g. AirPods illustration or badges)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}
