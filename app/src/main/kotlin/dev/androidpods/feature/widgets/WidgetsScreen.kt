// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.feature.widgets

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.androidpods.app.R
import dev.androidpods.core.airpods.BatteryChargeStatus
import dev.androidpods.core.airpods.BatteryComponentState
import dev.androidpods.core.airpods.BatteryState
import dev.androidpods.core.data.AirPodsRepositoryProvider
import dev.androidpods.core.data.AirPodsState
import dev.androidpods.core.designsystem.AndroidpodsTheme

@Composable
fun WidgetsScreen(modifier: Modifier = Modifier) {
    val state by AirPodsRepositoryProvider.state.collectAsState()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val unsupportedMessage = stringResource(R.string.widgets_pin_unsupported)

    WidgetsScreenContent(
        state = state,
        onPinWidget = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val receiver = ComponentName(context, BatteryWidgetReceiver::class.java)
            if (appWidgetManager.isRequestPinAppWidgetSupported) {
                appWidgetManager.requestPinAppWidget(receiver, null, null)
            } else {
                Toast.makeText(context, unsupportedMessage, Toast.LENGTH_LONG).show()
            }
        },
        modifier = modifier,
    )
}

@Composable
internal fun WidgetsScreenContent(
    state: AirPodsState,
    onPinWidget: () -> Unit,
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
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.widgets_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 4.dp),
        )

        // Section 1: Live Interactive Widget Preview (Material 3 Expressive Pill)
        Text(
            text = stringResource(R.string.widgets_preview_header),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )

        // Pill Widget Preview Mockup matching reference image
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(144.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = 4.dp,
            shadowElevation = 8.dp,
        ) {
            val battery = state.battery
            if (battery != null) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    WidgetBatteryCard(
                        iconRes = R.drawable.ic_airpod_left,
                        battery = battery.left,
                        isCase = false,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    )
                    WidgetBatteryCard(
                        iconRes = R.drawable.ic_airpod_right,
                        battery = battery.right,
                        isCase = false,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    )
                    WidgetBatteryCard(
                        iconRes = R.drawable.ic_airpods_case,
                        battery = battery.case,
                        isCase = true,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_notification),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(32.dp),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.widget_battery_no_data),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }

        // Action Button: Pin Widget
        Button(
            onClick = onPinWidget,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
        ) {
            Text(
                text = stringResource(R.string.widgets_pin_button),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(vertical = 4.dp),
            )
        }

        // Section 2: Step-by-step Guide
        Text(
            text = stringResource(R.string.widgets_guide_header),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp),
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = stringResource(R.string.widgets_guide_step_1),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = stringResource(R.string.widgets_guide_step_2),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = stringResource(R.string.widgets_guide_step_3),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun WidgetBatteryCard(
    iconRes: Int,
    battery: BatteryComponentState,
    isCase: Boolean,
    modifier: Modifier = Modifier,
) {
    val isCharging = battery.status == BatteryChargeStatus.CHARGING || battery.status == BatteryChargeStatus.OPTIMIZED_CHARGING
    val isDisconnected = battery.status == BatteryChargeStatus.DISCONNECTED

    val containerColor = if (isCase) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }

    val contentColor = if (isCase) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(26.dp),
        color = containerColor,
        contentColor = contentColor,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(38.dp),
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isDisconnected) "--" else "${battery.level}%",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                )
                if (isCharging) {
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Charging",
                        tint = contentColor,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WidgetsScreenPreview() {
    AndroidpodsTheme {
        WidgetsScreenContent(
            state = AirPodsState.INITIAL.copy(
                battery = BatteryState(
                    left = BatteryComponentState(94, BatteryChargeStatus.NOT_CHARGING),
                    right = BatteryComponentState(86, BatteryChargeStatus.NOT_CHARGING),
                    case = BatteryComponentState(0, BatteryChargeStatus.DISCONNECTED),
                ),
            ),
            onPinWidget = {},
        )
    }
}
