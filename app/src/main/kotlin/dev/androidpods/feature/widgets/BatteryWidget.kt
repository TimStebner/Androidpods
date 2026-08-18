// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.feature.widgets

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import dev.androidpods.app.MainActivity
import dev.androidpods.app.R
import dev.androidpods.core.airpods.BatteryChargeStatus
import dev.androidpods.core.airpods.BatteryComponentState
import dev.androidpods.core.data.AirPodsRepositoryProvider

/**
 * Material 3 Expressive Battery Glance Widget (PROJECT.md §6, §11, §30).
 *
 * Displays three distinct tonal status cards for Left, Right, and Case with
 * bold typography, battery levels, and charging indicators.
 */
class BatteryWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val state by AirPodsRepositoryProvider.state.collectAsState()
            GlanceTheme {
                BatteryWidgetContent(state.toBatteryWidgetUiState())
            }
        }
    }
}

@Composable
private fun BatteryWidgetContent(state: BatteryWidgetUiState) {
    val context = LocalContext.current
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(32.dp)
            .background(GlanceTheme.colors.surface)
            .clickable(actionStartActivity(intent))
            .padding(8.dp),
        contentAlignment = Alignment.Center,
    ) {
        when (state) {
            is BatteryWidgetUiState.NoData -> NoDataContent()
            is BatteryWidgetUiState.Battery -> BatteryContent(state)
        }
    }
}

@Composable
private fun NoDataContent() {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.Vertical.CenterVertically,
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
    ) {
        Image(
            provider = ImageProvider(R.drawable.ic_notification),
            contentDescription = null,
            modifier = GlanceModifier.size(28.dp),
            colorFilter = ColorFilter.tint(GlanceTheme.colors.primary),
        )
        Spacer(modifier = GlanceModifier.height(6.dp))
        Text(
            text = LocalContext.current.getString(R.string.widget_battery_no_data),
            style = TextStyle(
                color = GlanceTheme.colors.onSurface,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
        Spacer(modifier = GlanceModifier.height(2.dp))
        Text(
            text = LocalContext.current.getString(R.string.widget_tap_to_open),
            style = TextStyle(
                color = GlanceTheme.colors.onSurfaceVariant,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
    }
}

@Composable
private fun BatteryContent(state: BatteryWidgetUiState.Battery) {
    Row(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.Vertical.CenterVertically,
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
    ) {
        // Left AirPod Card
        BatteryCard(
            label = LocalContext.current.getString(R.string.home_battery_left),
            iconRes = R.drawable.ic_airpod_left,
            battery = state.left,
            isCase = false,
            modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
        )
        Spacer(modifier = GlanceModifier.width(6.dp))
        // Case Card (Center)
        BatteryCard(
            label = LocalContext.current.getString(R.string.home_battery_case),
            iconRes = R.drawable.ic_airpods_case,
            battery = state.case,
            isCase = true,
            modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
        )
        Spacer(modifier = GlanceModifier.width(6.dp))
        // Right AirPod Card
        BatteryCard(
            label = LocalContext.current.getString(R.string.home_battery_right),
            iconRes = R.drawable.ic_airpod_right,
            battery = state.right,
            isCase = false,
            modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
        )
    }
}

@Composable
private fun BatteryCard(
    label: String,
    iconRes: Int,
    battery: BatteryComponentState,
    isCase: Boolean,
    modifier: GlanceModifier = GlanceModifier,
) {
    val isCharging = battery.status == BatteryChargeStatus.CHARGING || battery.status == BatteryChargeStatus.OPTIMIZED_CHARGING
    val isDisconnected = battery.status == BatteryChargeStatus.DISCONNECTED

    val cardBackground = if (isCase) {
        GlanceTheme.colors.tertiaryContainer
    } else {
        GlanceTheme.colors.primaryContainer
    }

    val contentColor = if (isCase) {
        GlanceTheme.colors.onTertiaryContainer
    } else {
        GlanceTheme.colors.onPrimaryContainer
    }

    Column(
        modifier = modifier
            .cornerRadius(24.dp)
            .background(cardBackground)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Vertical.CenterVertically,
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
    ) {
        Image(
            provider = ImageProvider(iconRes),
            contentDescription = null,
            modifier = GlanceModifier.size(30.dp),
            colorFilter = ColorFilter.tint(contentColor),
        )
        Spacer(modifier = GlanceModifier.height(4.dp))
        Text(
            text = label,
            style = TextStyle(
                color = contentColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
            ),
            maxLines = 1,
        )
        Spacer(modifier = GlanceModifier.height(2.dp))
        val text = when {
            isDisconnected -> "--"
            isCharging -> "${battery.level}% ⚡"
            else -> "${battery.level}%"
        }
        Text(
            text = text,
            style = TextStyle(
                color = contentColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            ),
            maxLines = 1,
        )
    }
}
