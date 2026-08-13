// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.feature.notifications

import android.content.Context
import dev.androidpods.core.data.AirPodsState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

// Wired once from AndroidpodsApp.onCreate() (same shape as observeAutoPause/observeWidgetUpdates)
// -- core/data must not import feature/notifications, so this observer lives here instead
// (PROJECT.md §11).
//
// Not unit-tested: Context/NotificationManager are framework calls (same precedent as
// observeAutoPause/observeWidgetUpdates). The mapping it pushes is
// BatteryNotificationContentTest's job, including the disconnect-cancels-the-notification case.
fun observeBatteryNotifications(context: Context, states: Flow<AirPodsState>, scope: CoroutineScope) {
    ensureBatteryNotificationChannel(context)
    states
        .map { it.toBatteryNotificationUiState() }
        .distinctUntilChanged()
        .onEach { updateBatteryNotification(context, it) }
        .launchIn(scope)
}

// The observer above only re-posts on a *state* change; the POST_NOTIFICATIONS grant that lands
// after the user taps Allow isn't one (distinctUntilChanged already remembered the pre-grant
// "cancelled" emission), so without this the notification would only appear at the next battery
// reading, which can be minutes away. Called from HomeScreen's permission-result callback with
// the state HomeScreen already has from its own collectAsState() -- keeps this file free of a
// direct AirPodsRepositoryProvider read, matching observeBatteryNotifications' own shape.
fun refreshBatteryNotification(context: Context, state: AirPodsState) {
    updateBatteryNotification(context, state.toBatteryNotificationUiState())
}
