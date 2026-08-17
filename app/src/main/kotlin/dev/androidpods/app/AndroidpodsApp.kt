// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.app

import android.app.Application
import dev.androidpods.core.bluetooth.resumeObservingAssociatedDevices
import dev.androidpods.core.data.AirPodsRepositoryProvider
import dev.androidpods.core.media.observeAutoPause
import dev.androidpods.feature.notifications.observeBatteryNotifications
import dev.androidpods.feature.notifications.observeConnectionNotifications
import dev.androidpods.feature.popup.observeBatteryPopup
import dev.androidpods.feature.widgets.observeWidgetUpdates
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AndroidpodsApp : Application() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        dev.androidpods.core.data.AppSettingsRepositoryProvider.get(this)
        resumeObservingAssociatedDevices(this)
        observeAutoPause(this, AirPodsRepositoryProvider.state, scope)
        observeWidgetUpdates(this, AirPodsRepositoryProvider.state, scope)
        observeBatteryNotifications(this, AirPodsRepositoryProvider.state, scope)
        observeConnectionNotifications(this, AirPodsRepositoryProvider.state, scope)
        observeBatteryPopup(this, AirPodsRepositoryProvider.state, scope)
        dev.androidpods.feature.tiles.observeTileUpdates(this, AirPodsRepositoryProvider.state, scope)
        dev.androidpods.core.telecom.CallGestureManagerProvider.get(this, scope)
    }
}
