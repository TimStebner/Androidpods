// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.bluetooth

import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

// Real implementation of AirPodsTransport backed by in-memory state, not a mock -- exercises the
// callers' actual logic (session sequencing, decoding) against a transport that behaves like the
// real one without touching Bluetooth.
class FakeAirPodsTransport(
    private val connectOutcome: AirPodsTransport.ConnectionState = AirPodsTransport.ConnectionState.Connected,
    override val deviceAddress: String = "02:00:00:00:00:01",
) : AirPodsTransport {
    override val state = MutableStateFlow<AirPodsTransport.ConnectionState>(AirPodsTransport.ConnectionState.Disconnected)
    private val inbound = MutableSharedFlow<ByteArray>(extraBufferCapacity = 16)
    override val packets: Flow<ByteArray> = inbound

    val sent = mutableListOf<ByteArray>()
    var sendFailure: IOException? = null
    var connectCallCount = 0
        private set

    override suspend fun connect() {
        connectCallCount++
        state.value = connectOutcome
    }

    override suspend fun disconnect() {
        state.value = AirPodsTransport.ConnectionState.Disconnected
    }

    override suspend fun send(packet: ByteArray) {
        sendFailure?.let { throw it }
        sent.add(packet)
    }

    suspend fun emit(packet: ByteArray) {
        inbound.emit(packet)
    }
}
