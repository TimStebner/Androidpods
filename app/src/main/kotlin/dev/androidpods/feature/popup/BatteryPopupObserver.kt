// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.feature.popup

import android.content.Context
import dev.androidpods.app.MainActivity
import dev.androidpods.core.bluetooth.AirPodsTransport
import dev.androidpods.core.data.AirPodsState
import dev.androidpods.core.data.AppSettingsRepositoryProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

/**
 * Observes AirPods connection state changes and triggers the Material 3 Expressive
 * Battery Pop-up when AirPods connect (if enabled in AppSettings and the app is not already open).
 */
fun observeBatteryPopup(context: Context, states: Flow<AirPodsState>, scope: CoroutineScope) {
    states
        .map { it.connection }
        .distinctUntilChanged()
        .filter { it == AirPodsTransport.ConnectionState.Connected }
        .onEach {
            val isEnabled = AppSettingsRepositoryProvider.settings.value.batteryPopupEnabled
            if (isEnabled && !MainActivity.isForeground) {
                runCatching {
                    BatteryPopupActivity.start(context)
                }
            }
        }
        .launchIn(scope)
}
