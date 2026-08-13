// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.feature.home

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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
import dev.androidpods.core.designsystem.AndroidpodsTheme
import dev.androidpods.core.designsystem.androidpodsSpatialSpec
import dev.androidpods.feature.notifications.refreshBatteryNotification

// Presentation only: no Bluetooth access, no protocol calls here (PROJECT.md §30). Renders
// AirPodsState, never infers it (§9) -- e.g. the ear-detection row only appears when
// state.capabilities.supportsEarDetection is true.
@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    val state by AirPodsRepositoryProvider.state.collectAsState()
    val context = LocalContext.current
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> if (granted) refreshBatteryNotification(context) }
    // The battery notification (feature.notifications) is posted from AndroidpodsApp regardless
    // of whether this screen is open, but a runtime permission can only be requested from a
    // foreground Activity -- this is the one place that's guaranteed to run once a device is
    // set up, and denying it must not block using the app (§23).
    LaunchedEffect(Unit) {
        if (!hasNotificationPermission(context)) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    HomeScreenContent(state = state, modifier = modifier)
}

@Composable
private fun HomeScreenContent(state: AirPodsState, modifier: Modifier = Modifier) {
    Scaffold(modifier = modifier.fillMaxSize()) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when (val connection = state.connection) {
                AirPodsTransport.ConnectionState.Disconnected -> DisconnectedContent()
                AirPodsTransport.ConnectionState.Connecting -> ConnectingContent()
                AirPodsTransport.ConnectionState.Connected -> ConnectedContent(state)
                is AirPodsTransport.ConnectionState.Failed -> FailedContent(connection.reason)
            }
        }
    }
}

@Composable
private fun DisconnectedContent() {
    StatusIndicator(glyph = "✕")
    Text(
        text = stringResource(R.string.home_disconnected_title),
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.padding(top = 24.dp),
    )
    Text(
        text = stringResource(R.string.home_disconnected_body),
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(top = 8.dp),
    )
}

@Composable
private fun ConnectingContent() {
    StatusIndicator(glyph = "…")
    Text(
        text = stringResource(R.string.home_connecting_title),
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.padding(top = 24.dp),
    )
}

// A failed Tier B probe degrades honestly instead of showing a dead control (§2.6/§13.6).
@Composable
private fun FailedContent(reason: String) {
    StatusIndicator(glyph = "!")
    Text(
        text = stringResource(R.string.home_tier_b_unavailable_title),
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.padding(top = 24.dp),
    )
    Text(
        text = stringResource(R.string.home_tier_b_unavailable_body, reason),
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(top = 8.dp),
    )
}

@Composable
private fun ConnectedContent(state: AirPodsState) {
    state.battery?.let { battery ->
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            BatteryColumn(stringResource(R.string.home_battery_left), battery.left)
            BatteryColumn(stringResource(R.string.home_battery_right), battery.right)
            BatteryColumn(stringResource(R.string.home_battery_case), battery.case)
        }
    }
    if (state.capabilities.supportsEarDetection) {
        state.earDetection?.let { earDetection ->
            EarDetectionRow(earDetection, modifier = Modifier.padding(top = 24.dp))
        }
    }
}

@Composable
private fun BatteryColumn(label: String, battery: BatteryComponentState) {
    val animatedLevel by animateIntAsState(
        targetValue = battery.level,
        animationSpec = androidpodsSpatialSpec(),
        label = "battery-level-$label",
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelMedium)
        Text(text = "$animatedLevel%", style = MaterialTheme.typography.headlineSmall)
        Text(text = stringResource(battery.status.labelRes()), style = MaterialTheme.typography.bodySmall)
    }
}

private fun BatteryChargeStatus.labelRes(): Int = when (this) {
    BatteryChargeStatus.CHARGING -> R.string.home_battery_charging
    BatteryChargeStatus.NOT_CHARGING -> R.string.home_battery_not_charging
    BatteryChargeStatus.OPTIMIZED_CHARGING -> R.string.home_battery_optimized_charging
    BatteryChargeStatus.DISCONNECTED -> R.string.home_battery_unavailable
}

@Composable
private fun EarDetectionRow(earDetection: EarDetectionState, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
        EarDetectionIndicator(stringResource(R.string.home_battery_left), earDetection.leftInEar)
        EarDetectionIndicator(stringResource(R.string.home_battery_right), earDetection.rightInEar)
    }
}

@Composable
private fun EarDetectionIndicator(label: String, inEar: Boolean) {
    val color by animateColorAsState(
        targetValue = if (inEar) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = androidpodsSpatialSpec(),
        label = "ear-detection-color-$label",
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(modifier = Modifier.size(16.dp), shape = CircleShape, color = color) {}
        Text(
            text = stringResource(if (inEar) R.string.home_ear_detection_in_ear else R.string.home_ear_detection_out_of_ear),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

// A minimal expressive-motion proof: the indicator eases its size with the theme's motion
// scheme rather than a hardcoded animation spec.
@Composable
private fun StatusIndicator(glyph: String) {
    var expanded by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { expanded = true }
    val size by animateDpAsState(
        targetValue = if (expanded) 96.dp else 88.dp,
        animationSpec = androidpodsSpatialSpec(),
        label = "status-indicator-size",
    )
    Surface(
        modifier = Modifier.size(size),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // ponytail: text glyph placeholder, add material-icons-extended when the design
            // system needs its first real icon set.
            Text(
                text = glyph,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenDisconnectedPreview() {
    AndroidpodsTheme {
        HomeScreenContent(state = AirPodsState.INITIAL)
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

@Preview(showBackground = true)
@Composable
private fun HomeScreenFailedPreview() {
    AndroidpodsTheme {
        HomeScreenContent(
            state = AirPodsState.INITIAL.copy(
                connection = AirPodsTransport.ConnectionState.Failed("ACL connection failed"),
            ),
        )
    }
}
