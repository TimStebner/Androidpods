// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.feature.controls

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.androidpods.app.R
import dev.androidpods.core.airpods.HoldDuration
import dev.androidpods.core.airpods.PressSpeed
import dev.androidpods.core.designsystem.ExpressiveScreenHeader
import dev.androidpods.core.bluetooth.AirPodsTransport
import dev.androidpods.core.data.AirPodsRepositoryProvider
import dev.androidpods.core.data.AirPodsState
import dev.androidpods.core.data.AppSettingsRepositoryProvider
import dev.androidpods.core.designsystem.AndroidpodsTheme
import dev.androidpods.core.designsystem.androidpodsSpatialSpec
import kotlinx.coroutines.launch

@Composable
fun ControlsScreen(modifier: Modifier = Modifier) {
    val state by AirPodsRepositoryProvider.state.collectAsState()
    val settings by AppSettingsRepositoryProvider.settings.collectAsState()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val settingsRepo = AppSettingsRepositoryProvider.get(context)

    ControlsScreenContent(
        state = state,
        autoPauseEnabled = settings.autoPauseEnabled,
        onAutoPauseChanged = { enabled ->
            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
            scope.launch { settingsRepo.setAutoPause(enabled) }
        },
        autoResumeEnabled = settings.autoResumeEnabled,
        onAutoResumeChanged = { enabled ->
            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
            scope.launch { settingsRepo.setAutoResume(enabled) }
        },
        assistantTriggerEnabled = settings.assistantTriggerEnabled,
        onAssistantTriggerChanged = { enabled ->
            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
            scope.launch {
                settingsRepo.setAssistantTrigger(enabled)
                AirPodsRepositoryProvider.current?.setAssistantTriggerEnabled(enabled)
            }
        },
        headGesturesEnabled = settings.headGesturesEnabled,
        onHeadGesturesChanged = { enabled ->
            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
            scope.launch {
                settingsRepo.setHeadGestures(enabled)
                AirPodsRepositoryProvider.current?.setHeadGesturesEnabled(enabled)
            }
        },
        pressSpeed = settings.pressSpeed,
        onPressSpeedChanged = { speed ->
            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
            scope.launch {
                settingsRepo.setPressSpeed(speed)
                AirPodsRepositoryProvider.current?.setPressSpeed(speed)
            }
        },
        holdDuration = settings.holdDuration,
        onHoldDurationChanged = { duration ->
            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
            scope.launch {
                settingsRepo.setHoldDuration(duration)
                AirPodsRepositoryProvider.current?.setHoldDuration(duration)
            }
        },
        modifier = modifier,
    )
}

@Composable
internal fun ControlsScreenContent(
    state: AirPodsState,
    autoPauseEnabled: Boolean,
    onAutoPauseChanged: (Boolean) -> Unit,
    autoResumeEnabled: Boolean,
    onAutoResumeChanged: (Boolean) -> Unit,
    assistantTriggerEnabled: Boolean = true,
    onAssistantTriggerChanged: (Boolean) -> Unit = {},
    headGesturesEnabled: Boolean = true,
    onHeadGesturesChanged: (Boolean) -> Unit = {},
    pressSpeed: PressSpeed = PressSpeed.DEFAULT,
    onPressSpeedChanged: (PressSpeed) -> Unit = {},
    holdDuration: HoldDuration = HoldDuration.DEFAULT,
    onHoldDurationChanged: (HoldDuration) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    var hasCallPerms by remember { mutableStateOf(dev.androidpods.core.bluetooth.hasCallPermissions(context)) }
    val callPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { _ ->
        hasCallPerms = dev.androidpods.core.bluetooth.hasCallPermissions(context)
        dev.androidpods.core.telecom.CallGestureManagerProvider.registerIfPossible()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .padding(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ExpressiveScreenHeader(
            title = stringResource(R.string.controls_title),
            subtitle = "Custom Gestures & Audio Tuning",
            icon = Icons.Default.Tune,
            iconBadgeColor = MaterialTheme.colorScheme.primaryContainer,
            iconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )

        // Section 1: Ear Detection & Auto-Pause/Resume
        Text(
            text = stringResource(R.string.controls_ear_detection_header),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
            color = MaterialTheme.colorScheme.primary,
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Auto-Pause
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                        Text(
                            text = stringResource(R.string.controls_auto_pause_title),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = stringResource(R.string.controls_auto_pause_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                    Switch(
                        checked = autoPauseEnabled,
                        onCheckedChange = onAutoPauseChanged,
                    )
                }

                // Auto-Resume
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                        Text(
                            text = stringResource(R.string.controls_auto_resume_title),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = stringResource(R.string.controls_auto_resume_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                    Switch(
                        checked = autoResumeEnabled,
                        onCheckedChange = onAutoResumeChanged,
                    )
                }

                if (state.connection is AirPodsTransport.ConnectionState.Connected && state.capabilities.supportsEarDetection) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Current Sensor State",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        val leftIn = state.earDetection?.leftInEar == true
                        val rightIn = state.earDetection?.rightInEar == true
                        SensorStatusPill(label = stringResource(R.string.home_battery_left), inEar = leftIn, modifier = Modifier.weight(1f))
                        SensorStatusPill(label = stringResource(R.string.home_battery_right), inEar = rightIn, modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Section: Spatial Audio & Head Motion Tracking (Milestone 7)
        if (state.capabilities.supportsHeadGestures) {
            Text(
                text = stringResource(R.string.spatial_motion_header),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )

            dev.androidpods.feature.spatial.SpatialMotionCard(state = state)
        }

        // Section: Find My & Audio Chime (Milestone 7)
        if (state.capabilities.supportsEarbudChime) {
            Text(
                text = stringResource(R.string.findmy_header),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )

            dev.androidpods.feature.findmy.FindMyCard(state = state)
        }

        // Section 2: Head Gestures (AirPods 4 & AirPods Pro 2)
        if (state.capabilities.supportsHeadGestures) {
            Text(
                text = stringResource(R.string.controls_head_gestures_header),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            ) {
                val supportsHeadGestures = state.capabilities.supportsHeadGestures
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                            Text(
                                text = stringResource(R.string.controls_head_gestures_title),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            )
                            Text(
                                text = stringResource(
                                    if (supportsHeadGestures) R.string.controls_head_gestures_desc else R.string.controls_head_gestures_unsupported,
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                        Switch(
                            checked = if (supportsHeadGestures) headGesturesEnabled else false,
                            onCheckedChange = if (supportsHeadGestures) { enabled ->
                                if (enabled && !hasCallPerms) {
                                    callPermLauncher.launch(dev.androidpods.core.bluetooth.CALL_PERMISSIONS)
                                }
                                onHeadGesturesChanged(enabled)
                            } else null,
                            enabled = supportsHeadGestures,
                        )
                    }

                    if (supportsHeadGestures && headGesturesEnabled) {
                        if (!hasCallPerms) {
                            Button(
                                onClick = { callPermLauncher.launch(dev.androidpods.core.bluetooth.CALL_PERMISSIONS) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                ),
                            ) {
                                Text(stringResource(R.string.controls_head_gestures_grant_btn))
                            }
                        } else {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(
                                    text = stringResource(R.string.controls_head_gestures_ready),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 3: Accessibility & Gesture Timing (Press Speed & Hold Duration)
        if (state.capabilities.supportsPressSpeed) {
            Text(
                text = stringResource(R.string.controls_timing_header),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    // Press Speed
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.controls_press_speed_title),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        )
                        Text(
                            text = stringResource(R.string.controls_press_speed_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            SegmentOption(
                                label = stringResource(R.string.controls_press_speed_default),
                                selected = pressSpeed == PressSpeed.DEFAULT,
                                onClick = { onPressSpeedChanged(PressSpeed.DEFAULT) },
                                modifier = Modifier.weight(1f),
                            )
                            SegmentOption(
                                label = stringResource(R.string.controls_press_speed_slow),
                                selected = pressSpeed == PressSpeed.SLOW,
                                onClick = { onPressSpeedChanged(PressSpeed.SLOW) },
                                modifier = Modifier.weight(1f),
                            )
                            SegmentOption(
                                label = stringResource(R.string.controls_press_speed_slowest),
                                selected = pressSpeed == PressSpeed.SLOWEST,
                                onClick = { onPressSpeedChanged(PressSpeed.SLOWEST) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }

                    // Press & Hold Duration
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.controls_hold_duration_title),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        )
                        Text(
                            text = stringResource(R.string.controls_hold_duration_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            SegmentOption(
                                label = stringResource(R.string.controls_hold_duration_default),
                                selected = holdDuration == HoldDuration.DEFAULT,
                                onClick = { onHoldDurationChanged(HoldDuration.DEFAULT) },
                                modifier = Modifier.weight(1f),
                            )
                            SegmentOption(
                                label = stringResource(R.string.controls_hold_duration_short),
                                selected = holdDuration == HoldDuration.SHORT,
                                onClick = { onHoldDurationChanged(HoldDuration.SHORT) },
                                modifier = Modifier.weight(1f),
                            )
                            SegmentOption(
                                label = stringResource(R.string.controls_hold_duration_long),
                                selected = holdDuration == HoldDuration.LONG,
                                onClick = { onHoldDurationChanged(HoldDuration.LONG) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        }

        // Section 4: Digital Assistant (Gemini / Google Assistant)
        if (state.capabilities.supportsStemConfiguration) {
            Text(
                text = stringResource(R.string.controls_assistant_header),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            ) {
                val context = LocalContext.current
                val isSupported = state.capabilities.supportsStemConfiguration
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                            Text(
                                text = stringResource(R.string.controls_assistant_title),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            )
                            Text(
                                text = stringResource(
                                    if (isSupported) R.string.controls_assistant_desc_supported else R.string.controls_assistant_desc_unsupported,
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                        Switch(
                            checked = if (isSupported) assistantTriggerEnabled else false,
                            onCheckedChange = if (isSupported) onAssistantTriggerChanged else null,
                            enabled = isSupported,
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Settings.ACTION_VOICE_INPUT_SETTINGS).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            if (intent.resolveActivity(context.packageManager) != null) {
                                context.startActivity(intent)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.controls_assistant_settings_button))
                    }
                }
            }
        }

        // Section 5: Noise Control (Only if supported by hardware)
        if (state.capabilities.supportsNoiseControl) {
            Text(
                text = stringResource(R.string.controls_noise_header),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = stringResource(R.string.controls_noise_gated_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        NoiseModeChip(
                            label = stringResource(R.string.controls_noise_off),
                            selected = true,
                            modifier = Modifier.weight(1f),
                        )
                        NoiseModeChip(
                            label = stringResource(R.string.controls_noise_transparency),
                            selected = false,
                            modifier = Modifier.weight(1f),
                        )
                        NoiseModeChip(
                            label = stringResource(R.string.controls_noise_adaptive),
                            selected = false,
                            modifier = Modifier.weight(1f),
                        )
                        NoiseModeChip(
                            label = stringResource(R.string.controls_noise_anc),
                            selected = false,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }

        // Section 6: Device Information
        Text(
            text = stringResource(R.string.controls_info_header),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                InfoRow(
                    label = stringResource(R.string.controls_model_label),
                    value = state.capabilities.modelName,
                )
                InfoRow(
                    label = stringResource(R.string.controls_transport_label),
                    value = stringResource(R.string.controls_transport_tier_b),
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SegmentOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
        animationSpec = androidpodsSpatialSpec(),
        label = "segment-bg",
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        animationSpec = androidpodsSpatialSpec(),
        label = "segment-text",
    )

    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = CircleShape,
        color = bgColor,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = textColor,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 4.dp),
            maxLines = 1,
        )
    }
}

@Composable
private fun SensorStatusPill(
    label: String,
    inEar: Boolean,
    modifier: Modifier = Modifier,
) {
    val pillBg by animateColorAsState(
        targetValue = if (inEar) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
        animationSpec = androidpodsSpatialSpec(),
        label = "sensor-pill-bg",
    )
    val pillText = if (inEar) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = pillBg,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = CircleShape,
                color = if (inEar) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(8.dp),
            ) {}
            Text(
                text = "$label: ${if (inEar) "In Ear" else "Out"}",
                style = MaterialTheme.typography.labelMedium,
                color = pillText,
                modifier = Modifier.padding(start = 6.dp),
            )
        }
    }
}

@Composable
private fun NoiseModeChip(label: String, selected: Boolean, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            maxLines = 1,
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Preview(showBackground = true)
@Composable
private fun ControlsScreenPreview() {
    AndroidpodsTheme {
        ControlsScreenContent(
            state = AirPodsState.INITIAL,
            autoPauseEnabled = true,
            onAutoPauseChanged = {},
            autoResumeEnabled = true,
            onAutoResumeChanged = {},
        )
    }
}
