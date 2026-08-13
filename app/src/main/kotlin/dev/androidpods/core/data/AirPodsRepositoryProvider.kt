// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.data

import android.bluetooth.BluetoothDevice
import dev.androidpods.core.bluetooth.AapTransport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

// Process-lifetime home for the one AirPodsState (§10). A manual singleton, not Hilt: this is
// still the only injection site (AirPodsPresenceService) -- the plan defers Hilt to when a second
// one appears (plan file M0 "Verzicht"-Liste).
object AirPodsRepositoryProvider {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var repository: AirPodsRepository? = null

    val current: AirPodsRepository? get() = repository

    fun repositoryFor(device: BluetoothDevice): AirPodsRepository =
        repository ?: AirPodsRepository(AapTransport(device), scope).also { repository = it }
}
