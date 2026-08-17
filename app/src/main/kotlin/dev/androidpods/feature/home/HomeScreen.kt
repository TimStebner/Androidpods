// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.feature.home

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import dev.androidpods.core.bluetooth.resumeObservingAssociatedDevices
import dev.androidpods.core.data.DataStoreTierProbeCache
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.androidpods.app.R
import dev.androidpods.core.airpods.BatteryChargeStatus
import dev.androidpods.core.airpods.BatteryComponentState
import dev.androidpods.core.airpods.BatteryState
import dev.androidpods.core.airpods.CapabilityResolver
import dev.androidpods.core.airpods.EarDetectionState
import dev.androidpods.core.bluetooth.AirPodsTransport
import dev.androidpods.core.bluetooth.hasNotificationPermission
import dev.androidpods.core.data.AirPodsRepositoryProvider
import dev.androidpods.core.data.AirPodsState
import dev.androidpods.core.data.AppSettingsRepositoryProvider
import dev.androidpods.core.designsystem.AirPodsIllustration
import dev.androidpods.core.designsystem.AndroidpodsTheme
import dev.androidpods.core.designsystem.androidpodsSpatialSpec
import dev.androidpods.feature.notifications.refreshBatteryNotification

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
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
        onRetry = {
            scope.launch {
                DataStoreTierProbeCache(context).clear()
                resumeObservingAssociatedDevices(context)
            }
        },
        modifier = modifier,
    )
}

@Composable
internal fun HomeScreenContent(
    state: AirPodsState,
    autoPauseActive: Boolean = true,
    onRetry: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .padding(bottom = 96.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = stringResource(R.string.home_device_title),
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    text = when (state.connection) {
                        AirPodsTransport.ConnectionState.Connected -> "AirPods 4 · Active"
                        AirPodsTransport.ConnectionState.Connecting -> "Connecting…"
                        is AirPodsTransport.ConnectionState.Failed -> "Connection Failed"
                        AirPodsTransport.ConnectionState.Disconnected -> "Disconnected"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            ConnectionBadge(state.connection)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Center Hero Illustration (PROJECT.md §19)
        ConnectionIllustration(state.connection)

        Spacer(modifier = Modifier.height(16.dp))

        when (val connection = state.connection) {
            AirPodsTransport.ConnectionState.Disconnected -> DisconnectedContent()
            AirPodsTransport.ConnectionState.Connecting -> ConnectingContent()
            AirPodsTransport.ConnectionState.Connected -> ConnectedContent(state, autoPauseActive)
            is AirPodsTransport.ConnectionState.Failed -> FailedContent(connection.reason, onRetry)
        }

        Spacer(modifier = Modifier.height(16.dp))
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

    Surface(
        shape = CircleShape,
        color = badgeColor,
    ) {
        Text(
            text = connection.description(),
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun ConnectionIllustration(connection: AirPodsTransport.ConnectionState) {
    val tint by animateColorAsState(
        targetValue = connection.tint(),
        animationSpec = androidpodsSpatialSpec(),
        label = "connection-illustration-tint",
    )
    val description = connection.description()
    AirPodsIllustration(
        tint = tint,
        pulsing = connection == AirPodsTransport.ConnectionState.Connecting,
        modifier = Modifier
            .padding(vertical = 12.dp)
            .semantics { contentDescription = description },
    )
}

@Composable
private fun AirPodsTransport.ConnectionState.tint(): Color = when (this) {
    AirPodsTransport.ConnectionState.Connected -> MaterialTheme.colorScheme.primary
    AirPodsTransport.ConnectionState.Connecting -> MaterialTheme.colorScheme.tertiary
    is AirPodsTransport.ConnectionState.Failed -> MaterialTheme.colorScheme.error
    AirPodsTransport.ConnectionState.Disconnected -> MaterialTheme.colorScheme.outline
}

@Composable
private fun AirPodsTransport.ConnectionState.description(): String = when (this) {
    AirPodsTransport.ConnectionState.Connected -> stringResource(R.string.home_connected_title)
    AirPodsTransport.ConnectionState.Connecting -> stringResource(R.string.home_connecting_title)
    is AirPodsTransport.ConnectionState.Failed -> stringResource(R.string.home_tier_b_unavailable_title)
    AirPodsTransport.ConnectionState.Disconnected -> stringResource(R.string.home_disconnected_title)
}

@Composable
private fun DisconnectedContent() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.home_disconnected_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(R.string.home_disconnected_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}

@Composable
private fun ConnectingContent() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.home_connecting_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "Establishing L2CAP session with AirPods…",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}

@Composable
private fun FailedContent(reason: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.home_tier_b_unavailable_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error,
            )
            Text(
                text = stringResource(R.string.home_tier_b_unavailable_body, reason),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRetry,
                shape = MaterialTheme.shapes.medium,
            ) {
                Text(stringResource(R.string.home_reconnect_button))
            }
        }
    }
}

@Composable
private fun ConnectedContent(state: AirPodsState, autoPauseActive: Boolean) {
    // Battery Cards Row
    state.battery?.let { battery ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BatteryCard(
                label = stringResource(R.string.home_battery_left),
                battery = battery.left,
                modifier = Modifier.weight(1f),
            )
            BatteryCard(
                label = stringResource(R.string.home_battery_right),
                battery = battery.right,
                modifier = Modifier.weight(1f),
            )
            BatteryCard(
                label = stringResource(R.string.home_battery_case),
                battery = battery.case,
                modifier = Modifier.weight(1f),
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // In-Ear Trageerkennung Card
    if (state.capabilities.supportsEarDetection) {
        state.earDetection?.let { earDetection ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Ear Detection",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        EarDetectionStatusItem(
                            label = stringResource(R.string.home_battery_left),
                            inEar = earDetection.leftInEar,
                            modifier = Modifier.weight(1f),
                        )
                        EarDetectionStatusItem(
                            label = stringResource(R.string.home_battery_right),
                            inEar = earDetection.rightInEar,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Quick Action Card: Auto-Pause Status
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = stringResource(R.string.home_quick_auto_pause_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = if (autoPauseActive) stringResource(R.string.home_quick_auto_pause_active) else stringResource(R.string.home_quick_auto_pause_inactive),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            Surface(
                shape = CircleShape,
                color = if (autoPauseActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
            ) {
                Text(
                    text = if (autoPauseActive) "Active" else "Off",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (autoPauseActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun BatteryCard(label: String, battery: BatteryComponentState, modifier: Modifier = Modifier) {
    val animatedLevel by animateIntAsState(
        targetValue = battery.level,
        animationSpec = androidpodsSpatialSpec(),
        label = "battery-level-$label",
    )

    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "$animatedLevel%",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(vertical = 4.dp),
            )
            Text(
                text = stringResource(battery.status.labelRes()),
                style = MaterialTheme.typography.labelSmall,
                color = if (battery.status == BatteryChargeStatus.CHARGING) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun BatteryChargeStatus.labelRes(): Int = when (this) {
    BatteryChargeStatus.CHARGING -> R.string.home_battery_charging
    BatteryChargeStatus.NOT_CHARGING -> R.string.home_battery_not_charging
    BatteryChargeStatus.OPTIMIZED_CHARGING -> R.string.home_battery_optimized_charging
    BatteryChargeStatus.DISCONNECTED -> R.string.home_battery_unavailable
}

@Composable
private fun EarDetectionStatusItem(label: String, inEar: Boolean, modifier: Modifier = Modifier) {
    val color by animateColorAsState(
        targetValue = if (inEar) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
        animationSpec = androidpodsSpatialSpec(),
        label = "ear-detection-color-$label",
    )

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = color,
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Surface(
                modifier = Modifier.size(12.dp),
                shape = CircleShape,
                color = if (inEar) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
            ) {}
            Text(
                text = "$label: ${stringResource(if (inEar) R.string.home_ear_detection_in_ear else R.string.home_ear_detection_out_of_ear)}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenConnectedPreview() {
    AndroidpodsTheme {
        HomeScreenContent(
            state = AirPodsState.INITIAL.copy(
                connection = AirPodsTransport.ConnectionState.Connected,
                capabilities = CapabilityResolver.resolve("A3050"),
                battery = BatteryState(
                    left = BatteryComponentState(95, BatteryChargeStatus.NOT_CHARGING),
                    right = BatteryComponentState(96, BatteryChargeStatus.NOT_CHARGING),
                    case = BatteryComponentState(60, BatteryChargeStatus.CHARGING),
                ),
                earDetection = EarDetectionState(leftInEar = true, rightInEar = true),
            ),
        )
    }
}
