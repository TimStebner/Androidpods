// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.feature.home

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.Hearing
import androidx.compose.material.icons.outlined.HearingDisabled
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.SpatialAudio
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.androidpods.app.R
import dev.androidpods.core.airpods.BatteryChargeStatus
import dev.androidpods.core.airpods.BatteryComponentState
import dev.androidpods.core.airpods.BatteryState
import dev.androidpods.core.airpods.CapabilityResolver
import dev.androidpods.core.airpods.EarDetectionState
import dev.androidpods.core.airpods.NoiseControlMode
import dev.androidpods.core.bluetooth.AirPodsTransport
import dev.androidpods.core.bluetooth.hasNotificationPermission
import dev.androidpods.core.bluetooth.resumeObservingAssociatedDevices
import dev.androidpods.core.data.AirPodsRepositoryProvider
import dev.androidpods.core.data.AirPodsState
import dev.androidpods.core.data.AppSettingsRepositoryProvider
import dev.androidpods.core.data.DataStoreTierProbeCache
import dev.androidpods.core.designsystem.AirPodsGeneration
import dev.androidpods.core.designsystem.AirPodsGenerationMorphBadge
import dev.androidpods.core.designsystem.AirPodsIllustration
import dev.androidpods.core.designsystem.AndroidpodsTheme
import dev.androidpods.core.designsystem.AudioWaveformVisualizer
import dev.androidpods.core.designsystem.ExpressiveScreenHeader
import dev.androidpods.core.designsystem.MorphingBadge
import dev.androidpods.core.designsystem.androidpodsReduceMotion
import dev.androidpods.core.designsystem.androidpodsSpatialSpec
import dev.androidpods.core.designsystem.generation
import dev.androidpods.core.designsystem.rememberAppHaptics
import dev.androidpods.feature.controls.ControlsSection
import dev.androidpods.feature.notifications.refreshBatteryNotification
import kotlinx.coroutines.launch

/**
 * Material 3 Expressive Home Screen (PROJECT.md §6, §9, §10, §11, §19).
 *
 * Implements the hero container card with animated audio waveform, three battery pillars
 * for Left/Case/Right, capability-filtered controls, and a prominent disconnected state.
 */
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onNavigateToControls: (ControlsSection) -> Unit = {},
) {
    val state by AirPodsRepositoryProvider.state.collectAsState()
    val settings by AppSettingsRepositoryProvider.settings.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> if (granted) refreshBatteryNotification(context, state) }

    LaunchedEffect(Unit) {
        if (!hasNotificationPermission(context)) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    HomeScreenContent(
        state = state,
        autoPauseActive = settings.autoPauseEnabled,
        onToggleAutoPause = { enabled ->
            scope.launch { AppSettingsRepositoryProvider.get(context).setAutoPause(enabled) }
        },
        onRetry = {
            scope.launch {
                DataStoreTierProbeCache(context).clear()
                resumeObservingAssociatedDevices(context)
            }
        },
        onNavigateToControls = onNavigateToControls,
        modifier = modifier,
    )
}

@Composable
internal fun HomeScreenContent(
    state: AirPodsState,
    autoPauseActive: Boolean = true,
    onToggleAutoPause: (Boolean) -> Unit = {},
    onRetry: () -> Unit = {},
    onNavigateToControls: (ControlsSection) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val haptics = rememberAppHaptics()
    val isConnected = state.connection == AirPodsTransport.ConnectionState.Connected

    val logoInteractionSource = remember { MutableInteractionSource() }
    val isLogoPressed by logoInteractionSource.collectIsPressedAsState()
    val logoPressScale by animateFloatAsState(
        targetValue = if (isLogoPressed) 0.88f else 1f,
        animationSpec = androidpodsSpatialSpec(),
        label = "logo-press-scale",
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .padding(bottom = 96.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 1. Brand Hero Header with Custom Androidpods App Icon
        ExpressiveScreenHeader(
            title = "Androidpods",
            subtitle = if (isConnected) "${state.capabilities.modelName} · Active" else "Ready to Connect",
            badgeContent = {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.Transparent,
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .size(48.dp)
                        .graphicsLayer {
                            scaleX = logoPressScale
                            scaleY = logoPressScale
                        }
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(
                            interactionSource = logoInteractionSource,
                            indication = null,
                        ) {
                            haptics.tick()
                        },
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_androidpods_logo),
                        contentDescription = "Androidpods Logo",
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            },
            trailingAction = {
                ConnectionBadge(state.connection)
            },
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Hero Device Card Container
        HeroDeviceCard(
            state = state,
            onRetry = onRetry,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Noise Control: ONLY shown if supported by the hardware (e.g. AirPods Pro / AirPods 4 with ANC)
        if (isConnected && state.capabilities.supportsNoiseControl) {
            NoiseControlSection(state = state)
            Spacer(modifier = Modifier.height(14.dp))
        }

        // 4. Feature Cards when Connected
        if (isConnected) {
            AutoPauseCard(
                active = autoPauseActive,
                onToggle = { active ->
                    haptics.confirm()
                    onToggleAutoPause(active)
                },
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (state.capabilities.supportsHeadGestures) {
                HeadGesturesQuickCard(
                    onClick = { onNavigateToControls(ControlsSection.HEAD_GESTURES) },
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            SpatialAudioQuickCard(
                state = state,
                onClick = { onNavigateToControls(ControlsSection.SPATIAL_MOTION) },
            )
        } else {
            // 5. Rich Disconnected / Failed State
            DisconnectedGuideCard(
                connection = state.connection,
                onRetry = onRetry,
            )
        }
    }
}

/**
 * Top Device Hero Card holding device title, refresh trigger, animated illustration & waveform,
 * and the 3 battery/status pillars for Left pod, Case, and Right pod.
 */
@Composable
private fun HeroDeviceCard(
    state: AirPodsState,
    onRetry: () -> Unit,
) {
    val haptics = rememberAppHaptics()
    val scope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }

    val refreshRotation by animateFloatAsState(
        targetValue = if (isRefreshing) 360f else 0f,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "refresh-rotation",
        finishedListener = { isRefreshing = false },
    )

    val isConnected = state.connection == AirPodsTransport.ConnectionState.Connected

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Header Row: Device Name & Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isConnected) state.capabilities.modelName else "AirPods",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = when (state.connection) {
                            AirPodsTransport.ConnectionState.Connected -> "L2CAP Tier B Active · 50Hz IMU"
                            AirPodsTransport.ConnectionState.Connecting -> "Connecting L2CAP channel…"
                            is AirPodsTransport.ConnectionState.Failed -> "Connection Failed"
                            AirPodsTransport.ConnectionState.Disconnected -> "Not Connected · Open Case"
                        },
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                IconButton(
                    onClick = {
                        haptics.tick()
                        isRefreshing = true
                        scope.launch { onRetry() }
                    },
                    modifier = Modifier.size(38.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = "Refresh connection",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(22.dp)
                            .rotate(refreshRotation),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Animated AirPods Illustration + Audio Waveform
            if (isConnected) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    AirPodsIllustration(
                        tint = MaterialTheme.colorScheme.primary,
                        pulsing = true,
                        generation = state.capabilities.generation,
                        modifier = Modifier.size(108.dp),
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                AudioWaveformVisualizer(
                    active = true,
                    heightDp = 36.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
            } else {
                AudioWaveformVisualizer(
                    active = false,
                    heightDp = 32.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3 Battery & Status Pillars: Left, Case, Right (Unified harmonious container)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                UnifiedBatteryPillar(
                    label = stringResource(R.string.home_battery_left),
                    icon = Icons.Outlined.Headphones,
                    battery = state.battery?.left,
                    inEar = state.earDetection?.leftInEar,
                    isCase = false,
                    isConnected = isConnected,
                    modifier = Modifier.weight(1f),
                )

                UnifiedBatteryPillar(
                    label = stringResource(R.string.home_battery_case),
                    icon = Icons.Outlined.Lock,
                    battery = state.battery?.case,
                    isCase = true,
                    isConnected = isConnected,
                    modifier = Modifier.weight(1f),
                )

                UnifiedBatteryPillar(
                    label = stringResource(R.string.home_battery_right),
                    icon = Icons.Outlined.Headphones,
                    battery = state.battery?.right,
                    inEar = state.earDetection?.rightInEar,
                    isCase = false,
                    isConnected = isConnected,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/**
 * Unified battery pillar displaying battery percentage, charging state, in-ear/case status,
 * and bottom action hint with spring physics interaction and harmonious Material 3 surfaces.
 */
@Composable
private fun UnifiedBatteryPillar(
    label: String,
    icon: ImageVector,
    battery: BatteryComponentState?,
    inEar: Boolean? = null,
    isCase: Boolean = false,
    isConnected: Boolean,
    modifier: Modifier = Modifier,
) {
    val haptics = rememberAppHaptics()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = androidpodsSpatialSpec(),
        label = "pillar-press-$label",
    )

    val animatedLevel by animateIntAsState(
        targetValue = battery?.level ?: 0,
        animationSpec = androidpodsSpatialSpec(),
        label = "pillar-level-$label",
    )

    val isCharging = battery?.status == BatteryChargeStatus.CHARGING

    Surface(
        modifier = modifier
            .alpha(if (isConnected) 1f else 0.55f)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = isConnected,
            ) { haptics.tick() },
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Unified top circular icon badge (same primaryContainer across Left, Case, Right!)
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(20.dp),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Percentage & Charging indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = if (isConnected && battery != null) "$animatedLevel%" else "--",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                    color = if (isCharging) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                )
                if (isConnected && isCharging) {
                    Icon(
                        imageVector = Icons.Outlined.Bolt,
                        contentDescription = "Charging",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Status tag
            val statusText = when {
                !isConnected -> "Offline"
                isCase -> if (isCharging) "Charging" else "Case Ready"
                inEar == true -> stringResource(R.string.home_status_in_ear)
                inEar == false -> stringResource(R.string.home_status_out_of_ear)
                isCharging -> stringResource(R.string.home_status_charging)
                else -> "Ready"
            }
            val isStatusActive = inEar == true || isCharging

            Text(
                text = statusText,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = if (isStatusActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Action Pill
            val actionText = when {
                isCase -> if (isCharging) "Charging" else "In Case"
                inEar == true -> stringResource(R.string.home_status_in_ear)
                else -> stringResource(R.string.home_action_press_stem)
            }

            Surface(
                shape = CircleShape,
                color = if (isStatusActive) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                },
            ) {
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isStatusActive) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
        }
    }
}

/**
 * Noise Control Section with 4 segmented options: Off, Transparency, Adaptive, ANC.
 * Only rendered for hardware that supports it.
 */
@Composable
private fun NoiseControlSection(
    state: AirPodsState,
) {
    val haptics = rememberAppHaptics()
    var selectedMode by remember { mutableStateOf(NoiseControlMode.OFF) }

    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = stringResource(R.string.home_noise_control_header),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                NoiseModePill(
                    icon = Icons.Outlined.HearingDisabled,
                    label = "Off",
                    selected = selectedMode == NoiseControlMode.OFF,
                    onClick = {
                        haptics.confirm()
                        selectedMode = NoiseControlMode.OFF
                    },
                    modifier = Modifier.weight(1f),
                )
                NoiseModePill(
                    icon = Icons.Outlined.Hearing,
                    label = "Transp.",
                    selected = selectedMode == NoiseControlMode.TRANSPARENCY,
                    onClick = {
                        haptics.confirm()
                        selectedMode = NoiseControlMode.TRANSPARENCY
                    },
                    modifier = Modifier.weight(1f),
                )
                NoiseModePill(
                    icon = Icons.Outlined.Tune,
                    label = "Adaptive",
                    selected = selectedMode == NoiseControlMode.ADAPTIVE,
                    onClick = {
                        haptics.confirm()
                        selectedMode = NoiseControlMode.ADAPTIVE
                    },
                    modifier = Modifier.weight(1f),
                )
                NoiseModePill(
                    icon = Icons.Outlined.GraphicEq,
                    label = "ANC",
                    selected = selectedMode == NoiseControlMode.NOISE_CANCELLATION,
                    onClick = {
                        haptics.confirm()
                        selectedMode = NoiseControlMode.NOISE_CANCELLATION
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun NoiseModePill(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor by animateColorAsState(
        targetValue = when {
            selected -> MaterialTheme.colorScheme.primary
            else -> Color.Transparent
        },
        animationSpec = androidpodsSpatialSpec(),
        label = "noise-pill-bg-$label",
    )
    val contentColor by animateColorAsState(
        targetValue = when {
            selected -> MaterialTheme.colorScheme.onPrimary
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = androidpodsSpatialSpec(),
        label = "noise-pill-fg-$label",
    )

    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = backgroundColor,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (selected) FontWeight.Black else FontWeight.SemiBold),
                color = contentColor,
                maxLines = 1,
            )
        }
    }
}

/**
 * Quick Action card for Automatic Media Pause with spring physics press.
 */
@Composable
private fun AutoPauseCard(
    active: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = androidpodsSpatialSpec(),
        label = "autopause-press-scale",
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.home_quick_auto_pause_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (active) stringResource(R.string.home_quick_auto_pause_active) else stringResource(R.string.home_quick_auto_pause_inactive),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = active,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                ),
            )
        }
    }
}

/**
 * Quick Action card displaying Head Gestures status with spring physics.
 */
@Composable
private fun HeadGesturesQuickCard(
    onClick: () -> Unit,
) {
    val haptics = rememberAppHaptics()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = androidpodsSpatialSpec(),
        label = "headgestures-press-scale",
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
            ) {
                haptics.tick()
                onClick()
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.size(42.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Face,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier
                            .padding(10.dp)
                            .size(22.dp),
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = stringResource(R.string.controls_head_gestures_header),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "Nod to answer, shake to decline calls",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                    contentDescription = "Open Setting",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(14.dp),
                )
            }
        }
    }
}

/**
 * Quick Action card displaying Spatial Motion stream status with spring physics.
 */
@Composable
private fun SpatialAudioQuickCard(
    state: AirPodsState,
    onClick: () -> Unit,
) {
    val haptics = rememberAppHaptics()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = androidpodsSpatialSpec(),
        label = "spatial-press-scale",
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
            ) {
                haptics.tick()
                onClick()
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(42.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.SpatialAudio,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier
                            .padding(10.dp)
                            .size(22.dp),
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = stringResource(R.string.spatial_motion_title),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = if (state.motionStreamActive) "50Hz 6-Axis Stream Active" else "Ready · 50Hz Head Tracking",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                    contentDescription = "Open Setting",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(14.dp),
                )
            }
        }
    }
}

/**
 * Rich Disconnected Guide Card with clear steps and prominent reconnect CTA button.
 */
@Composable
private fun DisconnectedGuideCard(
    connection: AirPodsTransport.ConnectionState,
    onRetry: () -> Unit,
) {
    val haptics = rememberAppHaptics()
    val isFailed = connection is AirPodsTransport.ConnectionState.Failed

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isFailed) {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            },
        ),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Surface(
                shape = CircleShape,
                color = if (isFailed) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(56.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Bluetooth,
                    contentDescription = null,
                    tint = if (isFailed) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .padding(14.dp)
                        .size(28.dp),
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = if (isFailed) "Connection Issue" else "AirPods Disconnected",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                color = if (isFailed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (connection is AirPodsTransport.ConnectionState.Failed) {
                    connection.reason
                } else {
                    "Open your AirPods case or put them into your ears to automatically connect."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    haptics.confirm()
                    onRetry()
                },
                shape = CircleShape,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.home_reconnect_button),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                )
            }
        }
    }
}

@Composable
private fun ConnectionBadge(connection: AirPodsTransport.ConnectionState) {
    val badgeColor by animateColorAsState(
        targetValue = when (connection) {
            AirPodsTransport.ConnectionState.Connected -> MaterialTheme.colorScheme.primaryContainer
            AirPodsTransport.ConnectionState.Connecting -> MaterialTheme.colorScheme.tertiaryContainer
            is AirPodsTransport.ConnectionState.Failed -> MaterialTheme.colorScheme.errorContainer
            AirPodsTransport.ConnectionState.Disconnected -> MaterialTheme.colorScheme.surfaceContainerHigh
        },
        animationSpec = androidpodsSpatialSpec(),
        label = "connection-badge-color",
    )
    val textColor = when (connection) {
        AirPodsTransport.ConnectionState.Connected -> MaterialTheme.colorScheme.onPrimaryContainer
        AirPodsTransport.ConnectionState.Connecting -> MaterialTheme.colorScheme.onTertiaryContainer
        is AirPodsTransport.ConnectionState.Failed -> MaterialTheme.colorScheme.onErrorContainer
        AirPodsTransport.ConnectionState.Disconnected -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val reduceMotion = androidpodsReduceMotion()
    val isConnected = connection == AirPodsTransport.ConnectionState.Connected
    val pulseTransition = rememberInfiniteTransition(label = "badge-pulse")
    val dotAlpha by pulseTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "badge-dot-alpha",
    )

    Surface(
        shape = CircleShape,
        color = badgeColor,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (isConnected) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = if (reduceMotion) 1f else dotAlpha),
                    modifier = Modifier.size(7.dp),
                ) {}
            }
            Text(
                text = connection.description(),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = textColor,
            )
        }
    }
}

@Composable
private fun AirPodsTransport.ConnectionState.description(): String = when (this) {
    AirPodsTransport.ConnectionState.Connected -> stringResource(R.string.home_connected_title)
    AirPodsTransport.ConnectionState.Connecting -> stringResource(R.string.home_connecting_title)
    is AirPodsTransport.ConnectionState.Failed -> stringResource(R.string.home_tier_b_unavailable_title)
    AirPodsTransport.ConnectionState.Disconnected -> stringResource(R.string.home_disconnected_title)
}

@Preview(name = "Connected Light Mode", showBackground = true)
@Composable
private fun HomeScreenConnectedPreview() {
    AndroidpodsTheme(darkTheme = false, dynamicColor = false) {
        HomeScreenContent(
            state = AirPodsState.INITIAL.copy(
                connection = AirPodsTransport.ConnectionState.Connected,
                capabilities = CapabilityResolver.resolve("A3050"),
                battery = BatteryState(
                    left = BatteryComponentState(94, BatteryChargeStatus.NOT_CHARGING),
                    right = BatteryComponentState(88, BatteryChargeStatus.NOT_CHARGING),
                    case = BatteryComponentState(76, BatteryChargeStatus.CHARGING),
                ),
                earDetection = EarDetectionState(leftInEar = true, rightInEar = true),
            ),
        )
    }
}

@Preview(name = "Connected Dark Mode", showBackground = true)
@Composable
private fun HomeScreenConnectedDarkPreview() {
    AndroidpodsTheme(darkTheme = true, dynamicColor = false) {
        HomeScreenContent(
            state = AirPodsState.INITIAL.copy(
                connection = AirPodsTransport.ConnectionState.Connected,
                capabilities = CapabilityResolver.resolve("A3050"),
                battery = BatteryState(
                    left = BatteryComponentState(94, BatteryChargeStatus.NOT_CHARGING),
                    right = BatteryComponentState(88, BatteryChargeStatus.NOT_CHARGING),
                    case = BatteryComponentState(76, BatteryChargeStatus.CHARGING),
                ),
                earDetection = EarDetectionState(leftInEar = true, rightInEar = true),
            ),
        )
    }
}
