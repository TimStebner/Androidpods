// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.feature.widgets

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
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
import dev.androidpods.core.designsystem.ExpressiveScreenHeader
import dev.androidpods.core.designsystem.androidpodsSpatialSpec
import dev.androidpods.core.designsystem.rememberAppHaptics

@Composable
fun WidgetsScreen(modifier: Modifier = Modifier) {
    val state by AirPodsRepositoryProvider.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val haptics = rememberAppHaptics()
    val unsupportedMessage = stringResource(R.string.widgets_pin_unsupported)

    WidgetsScreenContent(
        state = state,
        onPinWidget = {
            haptics.confirm()
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
    val haptics = rememberAppHaptics()

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val previewPressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = androidpodsSpatialSpec(),
        label = "widget-preview-scale",
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .padding(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ExpressiveScreenHeader(
            title = stringResource(R.string.widgets_title),
            subtitle = stringResource(R.string.widgets_subtitle),
            icon = Icons.Default.Widgets,
            iconBadgeColor = MaterialTheme.colorScheme.primaryContainer,
            iconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )

        // Section 1: Live Interactive Widget Preview (Material 3 Expressive Pill)
        Text(
            text = stringResource(R.string.widgets_preview_header),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
            color = MaterialTheme.colorScheme.primary,
        )

        // Pill Widget Preview with spring physics interaction
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(152.dp)
                .graphicsLayer {
                    scaleX = previewPressScale
                    scaleY = previewPressScale
                }
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                ) {
                    haptics.tick()
                },
            shape = RoundedCornerShape(36.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            tonalElevation = 4.dp,
            shadowElevation = 8.dp,
        ) {
            val battery = state.battery
            if (battery != null) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    WidgetBatteryCard(
                        label = stringResource(R.string.home_battery_left),
                        iconRes = R.drawable.ic_airpod_left,
                        battery = battery.left,
                        isCase = false,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    )
                    WidgetBatteryCard(
                        label = stringResource(R.string.home_battery_case),
                        iconRes = R.drawable.ic_airpods_case,
                        battery = battery.case,
                        isCase = true,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    )
                    WidgetBatteryCard(
                        label = stringResource(R.string.home_battery_right),
                        iconRes = R.drawable.ic_airpod_right,
                        battery = battery.right,
                        isCase = false,
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
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(34.dp),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.widget_battery_no_data),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.widgets_preview_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // Action Button: Pin Widget
        Button(
            onClick = onPinWidget,
            modifier = Modifier.fillMaxWidth(),
            shape = CircleShape,
        ) {
            Icon(
                imageVector = Icons.Outlined.TouchApp,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.widgets_pin_button),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(vertical = 4.dp),
            )
        }

        // Section 2: Step-by-step Guide
        Text(
            text = stringResource(R.string.widgets_guide_header),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp),
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                GuideStepRow(step = 1, text = stringResource(R.string.widgets_guide_step_1))
                GuideStepRow(step = 2, text = stringResource(R.string.widgets_guide_step_2))
                GuideStepRow(step = 3, text = stringResource(R.string.widgets_guide_step_3))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun GuideStepRow(
    step: Int,
    text: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(28.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "$step",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun WidgetBatteryCard(
    label: String,
    iconRes: Int,
    battery: BatteryComponentState,
    isCase: Boolean,
    modifier: Modifier = Modifier,
) {
    val haptics = rememberAppHaptics()
    val isCharging = battery.status == BatteryChargeStatus.CHARGING || battery.status == BatteryChargeStatus.OPTIMIZED_CHARGING
    val isDisconnected = battery.status == BatteryChargeStatus.DISCONNECTED

    val cardInteractionSource = remember { MutableInteractionSource() }
    val isCardPressed by cardInteractionSource.collectIsPressedAsState()
    val cardPressScale by animateFloatAsState(
        targetValue = if (isCardPressed) 0.93f else 1f,
        animationSpec = androidpodsSpatialSpec(),
        label = "card-press-$label",
    )

    val animatedLevel by animateIntAsState(
        targetValue = battery.level,
        animationSpec = androidpodsSpatialSpec(),
        label = "widget-level-$label",
    )

    val containerColor = if (isCase) {
        MaterialTheme.colorScheme.tertiaryContainer
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }

    val contentColor = if (isCase) {
        MaterialTheme.colorScheme.onTertiaryContainer
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }

    Surface(
        modifier = modifier
            .graphicsLayer {
                scaleX = cardPressScale
                scaleY = cardPressScale
            }
            .clickable(
                interactionSource = cardInteractionSource,
                indication = null,
            ) { haptics.tick() },
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
                modifier = Modifier.size(34.dp),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = contentColor.copy(alpha = 0.85f),
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isDisconnected) "--" else "$animatedLevel%",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                    color = contentColor,
                )
                if (isCharging) {
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = stringResource(R.string.charging_description),
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
