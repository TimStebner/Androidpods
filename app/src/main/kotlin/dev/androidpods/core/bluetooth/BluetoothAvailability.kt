// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

sealed interface BluetoothAvailability {
    // No Bluetooth radio at all. Distinct from Disabled: a device without an adapter never
    // recovers, and §26 wants the UI to say so honestly instead of showing a perpetual
    // "turn on Bluetooth" prompt.
    data object Unsupported : BluetoothAvailability
    data object Disabled : BluetoothAvailability
    data object Enabled : BluetoothAvailability
}

// Bluetooth-on/off as a StateFlow-friendly source (PROJECT.md §11 Transport layer): no polling,
// just the adapter's current state plus ACTION_STATE_CHANGED.
fun bluetoothAvailability(context: Context): Flow<BluetoothAvailability> = callbackFlow {
    val adapter = context.getSystemService<BluetoothManager>()?.adapter
    if (adapter == null) {
        trySend(BluetoothAvailability.Unsupported)
        awaitClose {}
        return@callbackFlow
    }

    fun availabilityFor(state: Int) =
        if (state == BluetoothAdapter.STATE_ON) BluetoothAvailability.Enabled else BluetoothAvailability.Disabled

    trySend(availabilityFor(adapter.state))

    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
            trySend(availabilityFor(state))
        }
    }
    ContextCompat.registerReceiver(
        context,
        receiver,
        IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED),
        ContextCompat.RECEIVER_NOT_EXPORTED,
    )
    awaitClose { context.unregisterReceiver(receiver) }
}
