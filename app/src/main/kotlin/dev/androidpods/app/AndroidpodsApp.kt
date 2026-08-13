// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.app

import android.app.Application
import dev.androidpods.core.bluetooth.resumeObservingAssociatedDevices
import dev.androidpods.core.data.AirPodsRepositoryProvider
import dev.androidpods.core.media.observeAutoPause
import dev.androidpods.feature.notifications.observeBatteryNotifications
import dev.androidpods.feature.widgets.observeWidgetUpdates
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AndroidpodsApp : Application() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        resumeObservingAssociatedDevices(this)
        observeAutoPause(this, AirPodsRepositoryProvider.state, scope)
        observeWidgetUpdates(this, AirPodsRepositoryProvider.state, scope)
        observeBatteryNotifications(this, AirPodsRepositoryProvider.state, scope)
    }
}
