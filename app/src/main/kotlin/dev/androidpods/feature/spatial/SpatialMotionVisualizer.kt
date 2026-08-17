// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.feature.spatial

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.androidpods.app.R
import dev.androidpods.core.bluetooth.AirPodsTransport
import dev.androidpods.core.data.AirPodsRepositoryProvider
import dev.androidpods.core.data.AirPodsState
import dev.androidpods.core.designsystem.androidpodsSpatialSpec
import dev.androidpods.core.gestures.HeadGesture
import dev.androidpods.core.gestures.HeadGestureDetector
import kotlinx.coroutines.launch

@Composable
fun SpatialMotionCard(
    state: AirPodsState,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val isConnected = state.connection is AirPodsTransport.ConnectionState.Connected
    val isStreaming = state.motionStreamActive
    val orientation = state.headOrientation

    // Gesture detector instance for real-time visual feedback
    val detector = remember { HeadGestureDetector(pitchNodThreshold = 15f, yawShakeThreshold = 20f) }
    var detectedGesture by remember { mutableStateOf(HeadGesture.NONE) }

    LaunchedEffect(orientation) {
        if (orientation != null) {
            val gesture = detector.onSample(orientation.pitch, orientation.yaw, orientation.roll)
            if (gesture != HeadGesture.NONE) {
                detectedGesture = gesture
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        }
    }

    // Auto-stop stream when leaving this composable
    DisposableEffect(Unit) {
        onDispose {
            if (isStreaming) {
                scope.launch {
                    AirPodsRepositoryProvider.current?.stopMotionStream()
                }
            }
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Header with Stream Toggle Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Text(
                        text = stringResource(R.string.spatial_motion_title),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = stringResource(R.string.spatial_motion_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = if (isStreaming) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                ) {
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                            scope.launch {
                                if (isStreaming) {
                                    AirPodsRepositoryProvider.current?.stopMotionStream()
                                } else {
                                    AirPodsRepositoryProvider.current?.startMotionStream()
                                }
                            }
                        },
                        enabled = isConnected,
                    ) {
                        Icon(
                            imageVector = if (isStreaming) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = if (isStreaming) "Stop Motion Stream" else "Start Motion Stream",
                            tint = if (isStreaming) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }

            if (isStreaming) {
                val currentPitch = orientation?.pitch ?: 0f
                val currentYaw = orientation?.yaw ?: 0f
                val currentRoll = orientation?.roll ?: 0f

                // 3D Head Visualizer Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(MaterialTheme.shapes.large)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    contentAlignment = Alignment.Center,
                ) {
                    val animatedPitch by animateFloatAsState(
                        targetValue = currentPitch,
                        animationSpec = androidpodsSpatialSpec(),
                        label = "pitch-anim",
                    )
                    val animatedYaw by animateFloatAsState(
                        targetValue = currentYaw,
                        animationSpec = androidpodsSpatialSpec(),
                        label = "yaw-anim",
                    )
                    val animatedRoll by animateFloatAsState(
                        targetValue = currentRoll,
                        animationSpec = androidpodsSpatialSpec(),
                        label = "roll-anim",
                    )

                    HeadOrientation3DView(
                        pitch = animatedPitch,
                        yaw = animatedYaw,
                        roll = animatedRoll,
                        modifier = Modifier.size(160.dp),
                    )

                    // Detected Gesture Badge
                    androidx.compose.animation.AnimatedVisibility(
                        visible = detectedGesture != HeadGesture.NONE,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 12.dp),
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                        ) {
                            Text(
                                text = when (detectedGesture) {
                                    HeadGesture.NOD -> "✨ Nod Detected (Yes)"
                                    HeadGesture.SHAKE -> "❌ Shake Detected (No)"
                                    HeadGesture.NONE -> ""
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            )
                        }
                    }
                }

                // Telemetry Readings (Pitch, Yaw, Roll)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OrientationPill(
                        label = "Pitch",
                        value = if (orientation != null) "%.1f°".format(orientation.pitch) else "--",
                        modifier = Modifier.weight(1f),
                    )
                    OrientationPill(
                        label = "Yaw",
                        value = if (orientation != null) "%.1f°".format(orientation.yaw) else "--",
                        modifier = Modifier.weight(1f),
                    )
                    OrientationPill(
                        label = "Roll",
                        value = if (orientation != null) "%.1f°".format(orientation.roll) else "--",
                        modifier = Modifier.weight(1f),
                    )
                }
            } else {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = if (isConnected) {
                            stringResource(R.string.spatial_motion_idle_hint)
                        } else {
                            stringResource(R.string.spatial_motion_disconnected_hint)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun HeadOrientation3DView(
    pitch: Float,
    yaw: Float,
    roll: Float,
    modifier: Modifier = Modifier,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.tertiary
    val surfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .graphicsLayer {
                rotationX = -pitch
                rotationY = yaw
                rotationZ = -roll
                cameraDistance = 12f * density
            },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(140.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension / 2.5f

            // Reference Orbital Ring (Spatial Frame)
            drawCircle(
                color = surfaceVariant.copy(alpha = 0.25f),
                radius = radius * 1.2f,
                center = center,
                style = Stroke(width = 2.dp.toPx()),
            )

            // Head / Sphere Gradient
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.9f),
                        primaryColor.copy(alpha = 0.4f),
                        Color.Transparent,
                    ),
                    center = center - Offset(radius * 0.3f, radius * 0.3f),
                    radius = radius * 1.3f,
                ),
                radius = radius,
                center = center,
            )

            // Front Nose/Facing Indicator
            drawCircle(
                color = secondaryColor,
                radius = 8.dp.toPx(),
                center = center + Offset(0f, -radius * 0.7f),
            )

            // Left Ear Marker
            drawCircle(
                color = primaryColor,
                radius = 6.dp.toPx(),
                center = center + Offset(-radius * 0.9f, 0f),
            )

            // Right Ear Marker
            drawCircle(
                color = primaryColor,
                radius = 6.dp.toPx(),
                center = center + Offset(radius * 0.9f, 0f),
            )
        }
    }
}

@Composable
private fun OrientationPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
