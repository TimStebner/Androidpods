// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.feature.popup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import dev.androidpods.app.R
import dev.androidpods.core.airpods.BatteryChargeStatus
import dev.androidpods.core.airpods.BatteryComponentState
import dev.androidpods.core.bluetooth.AirPodsTransport
import dev.androidpods.core.data.AirPodsState
import dev.androidpods.core.designsystem.AirPodsExpressiveTrio
import dev.androidpods.core.designsystem.androidpodsSpatialSpec
import dev.androidpods.core.designsystem.rememberAppHaptics
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Material 3 Expressive AirPods Battery Pop-Up Content.
 *
 * Provides a floating, spring-animated bottom sheet card with:
 * - Animated Hero Trio (Left AirPod, Charging Case with active LED, Right AirPod)
 * - Live battery gauges and charging indicators
 * - Physics-based drag-to-dismiss and interactive touch reactions
 */
@Composable
fun AirPodsBatteryPopupContent(
    state: AirPodsState,
    onDismiss: () -> Unit,
    onOpenApp: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    autoDismissSeconds: Int = 8,
) {
    val haptics = rememberAppHaptics()
    val scope = rememberCoroutineScope()
    var offsetY by remember { mutableFloatStateOf(0f) }
    var isVisible by remember { mutableStateOf(false) }

    fun dismissWithAnimation() {
        if (!isVisible) return
        isVisible = false
        scope.launch {
            delay(280)
            onDismiss()
        }
    }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    // Auto-dismiss countdown
    if (autoDismissSeconds > 0) {
        LaunchedEffect(Unit) {
            delay(autoDismissSeconds * 1000L)
            dismissWithAnimation()
        }
    }

    val isConnected = state.connection == AirPodsTransport.ConnectionState.Connected
    val modelName = if (isConnected) state.capabilities.modelName else "AirPods"

    val dragSpec = androidpodsSpatialSpec<Float>()
    val animatedOffsetY by animateFloatAsState(
        targetValue = offsetY,
        animationSpec = dragSpec,
        label = "popup-drag-offset-y",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                haptics.tick()
                dismissWithAnimation()
            },
        contentAlignment = Alignment.BottomCenter,
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = androidpodsSpatialSpec(),
            ) + fadeIn(animationSpec = androidpodsSpatialSpec()),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = androidpodsSpatialSpec(),
            ) + fadeOut(animationSpec = androidpodsSpatialSpec()),
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset { IntOffset(0, animatedOffsetY.roundToInt()) }
                    .draggable(
                        orientation = Orientation.Vertical,
                        state = rememberDraggableState { delta ->
                            val newOffset = offsetY + delta
                            if (newOffset >= 0f) {
                                offsetY = newOffset
                            }
                        },
                        onDragStopped = { velocity ->
                            if (offsetY > 160f || velocity > 800f) {
                                haptics.tick()
                                dismissWithAnimation()
                            } else {
                                offsetY = 0f
                            }
                        },
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {
                        // Prevent dismiss when tapping inside the card
                    }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .navigationBarsPadding(),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // 1. Top Drag Handle
                    Surface(
                        modifier = Modifier.size(width = 36.dp, height = 4.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                    ) {}

                    Spacer(modifier = Modifier.height(14.dp))

                    // 2. Header Row: Model name & Status Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                text = modelName,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = when (state.connection) {
                                    AirPodsTransport.ConnectionState.Connected -> "Connected · 50Hz Spatial Active"
                                    AirPodsTransport.ConnectionState.Connecting -> "Connecting…"
                                    else -> "Ready to Connect"
                                },
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = if (isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        // Close Icon Button
                        IconButton(
                            onClick = {
                                haptics.tick()
                                dismissWithAnimation()
                            },
                            modifier = Modifier.size(36.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Close Pop-up",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3. Centerpiece Hero Trio (Left Pod, Case, Right Pod)
                    AirPodsExpressiveTrio(
                        tint = MaterialTheme.colorScheme.primary,
                        accentTint = MaterialTheme.colorScheme.onSurfaceVariant,
                        leftInEar = state.earDetection?.leftInEar,
                        rightInEar = state.earDetection?.rightInEar,
                        leftCharging = state.battery?.left?.status == BatteryChargeStatus.CHARGING,
                        rightCharging = state.battery?.right?.status == BatteryChargeStatus.CHARGING,
                        caseCharging = state.battery?.case?.status == BatteryChargeStatus.CHARGING,
                        caseBatteryLevel = state.battery?.case?.level,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // 4. Trio of Battery Gauges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        PopupBatteryGauge(
                            label = stringResource(R.string.home_battery_left),
                            icon = Icons.Outlined.Headphones,
                            battery = state.battery?.left,
                            inEar = state.earDetection?.leftInEar,
                            isConnected = isConnected,
                            modifier = Modifier.weight(1f),
                        )

                        PopupBatteryGauge(
                            label = stringResource(R.string.home_battery_case),
                            icon = Icons.Outlined.Lock,
                            battery = state.battery?.case,
                            isConnected = isConnected,
                            modifier = Modifier.weight(1f),
                        )

                        PopupBatteryGauge(
                            label = stringResource(R.string.home_battery_right),
                            icon = Icons.Outlined.Headphones,
                            battery = state.battery?.right,
                            inEar = state.earDetection?.rightInEar,
                            isConnected = isConnected,
                            modifier = Modifier.weight(1f),
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 5. Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        if (onOpenApp != null) {
                            FilledTonalButton(
                                onClick = {
                                    haptics.tick()
                                    onOpenApp()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(24.dp),
                            ) {
                                Text(
                                    text = "Open App",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                )
                            }
                        }

                        Button(
                            onClick = {
                                haptics.confirm()
                                dismissWithAnimation()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                        ) {
                            Text(
                                text = "Done",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black),
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Expressive individual battery gauge card with animated level counter,
 * charging bolt, in-ear indicator, and fill progress bar.
 */
@Composable
private fun PopupBatteryGauge(
    label: String,
    icon: ImageVector,
    battery: BatteryComponentState?,
    inEar: Boolean? = null,
    isConnected: Boolean,
    modifier: Modifier = Modifier,
) {
    val haptics = rememberAppHaptics()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = androidpodsSpatialSpec(),
        label = "popup-gauge-press-$label",
    )

    val animatedLevel by animateIntAsState(
        targetValue = battery?.level ?: 0,
        animationSpec = androidpodsSpatialSpec(),
        label = "popup-gauge-level-$label",
    )

    val isCharging = battery?.status == BatteryChargeStatus.CHARGING
    val levelFraction = ((battery?.level ?: 0) / 100f).coerceIn(0f, 1f)

    Surface(
        modifier = modifier
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = isConnected,
            ) { haptics.tick() },
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Icon Badge
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .padding(6.dp)
                        .size(18.dp),
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Percentage & Charging Bolt
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = if (isConnected && battery != null) "$animatedLevel%" else "--",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = if (isCharging) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                )
                if (isConnected && isCharging) {
                    Icon(
                        imageVector = Icons.Outlined.Bolt,
                        contentDescription = "Charging",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Linear Progress Pill
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(if (isConnected && battery != null) levelFraction else 0f)
                        .clip(CircleShape)
                        .background(
                            when {
                                isCharging -> MaterialTheme.colorScheme.primary
                                (battery?.level ?: 0) <= 20 -> Color(0xFFF59E0B)
                                else -> MaterialTheme.colorScheme.primary
                            },
                        ),
                )
            }

            if (inEar != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (inEar) "In Ear" else "Out",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = if (inEar) Color(0xFF22C55E) else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
