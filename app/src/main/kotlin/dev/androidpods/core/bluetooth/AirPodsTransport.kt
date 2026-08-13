// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.bluetooth

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

// Everything above the protocol layer talks to AirPods through this interface, never through a
// concrete transport (PROJECT.md §11). Tier A (`AdvertisementSource`, BLE advertisement parsing)
// and Tier B (`AapTransport`, the L2CAP session) are separate implementations scheduled for M2b
// and M2 respectively (plan Teil B). This is socket-lifecycle-and-bytes only -- no packet parsing
// here, that's `core.airpods`'s job (§11).
interface AirPodsTransport {
    val deviceAddress: String
    val state: StateFlow<ConnectionState>
    val packets: Flow<ByteArray>

    suspend fun connect()
    suspend fun disconnect()
    suspend fun send(packet: ByteArray)

    sealed interface ConnectionState {
        data object Disconnected : ConnectionState
        data object Connecting : ConnectionState
        data object Connected : ConnectionState
        data class Failed(val reason: String) : ConnectionState
    }
}
