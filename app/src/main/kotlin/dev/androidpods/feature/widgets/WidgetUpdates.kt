// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.feature.widgets

import android.content.Context
import androidx.glance.appwidget.updateAll
import dev.androidpods.core.data.AirPodsState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

// BatteryWidget.provideGlance's collectAsState() only updates a session that's already alive.
// If the process was restarted by AirPodsPresenceService rather than by the widget host, no
// Glance session exists yet, so nothing is collecting -- updateAll() is what starts one (it
// re-invokes provideGlance), which is what makes the widget render at all after a cold restart.
// Wired once from AndroidpodsApp.onCreate() (same shape as observeAutoPause) -- core/data must
// not import feature/widgets, so this observer lives here instead (PROJECT.md §11).
//
// Not unit-tested: Context and GlanceAppWidget.updateAll are framework calls (same precedent as
// observeAutoPause). The mapping it pushes is BatteryWidgetContentTest's job.
fun observeWidgetUpdates(context: Context, states: Flow<AirPodsState>, scope: CoroutineScope) {
    states
        .map { it.toBatteryWidgetUiState() }
        .distinctUntilChanged()
        .onEach { BatteryWidget().updateAll(context) }
        .launchIn(scope)
}
