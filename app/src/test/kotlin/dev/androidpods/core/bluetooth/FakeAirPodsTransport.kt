// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.bluetooth

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

// Real implementation of AirPodsTransport backed by in-memory state, not a mock -- exercises the
// callers' actual logic (session sequencing, decoding) against a transport that behaves like the
// real one without touching Bluetooth.
class FakeAirPodsTransport(
    private val connectOutcome: AirPodsTransport.ConnectionState = AirPodsTransport.ConnectionState.Connected,
) : AirPodsTransport {
    override val state = MutableStateFlow<AirPodsTransport.ConnectionState>(AirPodsTransport.ConnectionState.Disconnected)
    private val inbound = MutableSharedFlow<ByteArray>(extraBufferCapacity = 16)
    override val packets: Flow<ByteArray> = inbound

    val sent = mutableListOf<ByteArray>()

    override suspend fun connect() {
        state.value = connectOutcome
    }

    override suspend fun disconnect() {
        state.value = AirPodsTransport.ConnectionState.Disconnected
    }

    override suspend fun send(packet: ByteArray) {
        sent.add(packet)
    }

    suspend fun emit(packet: ByteArray) {
        inbound.emit(packet)
    }
}
