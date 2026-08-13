// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.feature.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import dev.androidpods.app.R
import dev.androidpods.core.airpods.BatteryComponentState
import dev.androidpods.core.data.AirPodsRepositoryProvider

// Presentation only, same rule as Compose UI (§30): renders BatteryWidgetUiState, never touches
// Bluetooth or the repository beyond reading its state.
class BatteryWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // provideContent's lambda is the composition; a plain val read before it would only ever
        // run once per session and then be frozen for every later recomposition. collectAsState
        // is the live path (widget visible, state changes) -- observeWidgetUpdates' updateAll()
        // push is the complementary path for when no session exists yet (process revived without
        // a host asking), which makes provideGlance itself run again. Both are needed, not either/or.
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
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.widgetBackground)
            .padding(12.dp),
        verticalAlignment = Alignment.Vertical.CenterVertically,
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
    ) {
        when (state) {
            is BatteryWidgetUiState.NoData -> NoDataContent()
            is BatteryWidgetUiState.Battery -> BatteryContent(state)
        }
    }
}

@Composable
private fun NoDataContent() {
    Text(
        text = LocalContext.current.getString(R.string.widget_battery_no_data),
        style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant),
    )
}

@Composable
private fun BatteryContent(state: BatteryWidgetUiState.Battery) {
    val context = LocalContext.current
    Row(horizontalAlignment = Alignment.Horizontal.CenterHorizontally) {
        BatteryCell(context.getString(R.string.home_battery_left), state.left)
        BatteryCell(context.getString(R.string.home_battery_right), state.right)
        BatteryCell(context.getString(R.string.home_battery_case), state.case)
    }
}

@Composable
private fun BatteryCell(label: String, battery: BatteryComponentState) {
    Column(
        modifier = GlanceModifier.padding(horizontal = 6.dp),
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
    ) {
        Text(text = label, style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant))
        Text(text = "${battery.level}%", style = TextStyle(color = GlanceTheme.colors.onSurface))
    }
}
