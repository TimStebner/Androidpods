// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.companion.CompanionDeviceManager
import android.companion.CompanionDeviceService
import android.companion.DevicePresenceEvent
import androidx.core.content.getSystemService
import dev.androidpods.core.data.AirPodsRepositoryProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// The wake trigger for Tier B session work (M2, PROJECT.md §13.4 point 1): the system binds this
// service only while the associated device is nearby or profile-connected, so nothing here polls
// or scans on its own. `onDevicePresenceEvent` already distinguishes BLE proximity (Tier A
// signal) from an ACL/profile connection (Tier B signal) instead of the deprecated single
// onDeviceAppeared/onDeviceDisappeared callbacks.
//
// No state is kept here (§10: one state machine, owned by the repository layer) -- this only
// resolves the BluetoothDevice for the association and hands off to AirPodsRepositoryProvider,
// the single owner of nearby/connected state.
class AirPodsPresenceService : CompanionDeviceService() {
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onDevicePresenceEvent(event: DevicePresenceEvent) {
        when (event.event) {
            DevicePresenceEvent.EVENT_BT_CONNECTED -> {
                val device = resolveBluetoothDevice(event.associationId) ?: return
                scope.launch { AirPodsRepositoryProvider.repositoryFor(device).connect() }
            }
            DevicePresenceEvent.EVENT_BT_DISCONNECTED -> {
                scope.launch { AirPodsRepositoryProvider.current?.disconnect() }
            }
        }
    }

    private fun resolveBluetoothDevice(associationId: Int): BluetoothDevice? {
        val manager = getSystemService<CompanionDeviceManager>() ?: return null
        val mac = manager.myAssociations.firstOrNull { it.id == associationId }?.deviceMacAddress
            ?: return null
        return getSystemService<BluetoothManager>()?.adapter?.getRemoteDevice(mac.toString())
    }
}
