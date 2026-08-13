// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.data

import dev.androidpods.core.airpods.AapEvent
import dev.androidpods.core.airpods.AapSession
import dev.androidpods.core.airpods.CapabilityResolver
import dev.androidpods.core.bluetooth.AirPodsTransport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// The only layer allowed to expose AirPods write operations (PROJECT.md §11). M2 is read-only --
// connect() only establishes the session, no configuration command exists yet (§2.6/§33).
class AirPodsRepository(
    private val transport: AirPodsTransport,
    scope: CoroutineScope,
) {
    private val session = AapSession(transport)
    private val _state = MutableStateFlow(AirPodsState.INITIAL)
    val state: StateFlow<AirPodsState> = _state.asStateFlow()

    init {
        scope.launch {
            transport.state.collect { connection ->
                _state.update { it.copy(connection = connection) }
            }
        }
        scope.launch {
            session.events.collect { event -> _state.update { it.reduce(event) } }
        }
    }

    suspend fun connect() {
        transport.connect()
        // A failed transport.connect() already recorded itself as Failed (§2.6: honest failure,
        // not a crash) -- starting the AAP session on top of a socket that doesn't exist would
        // throw instead.
        if (transport.state.value == AirPodsTransport.ConnectionState.Connected) {
            session.start()
        }
    }

    suspend fun disconnect() = transport.disconnect()

    private fun AirPodsState.reduce(event: AapEvent): AirPodsState = when (event) {
        is AapEvent.Battery -> copy(battery = event.state)
        is AapEvent.EarDetection -> copy(earDetection = event.state)
        is AapEvent.DeviceInfo -> copy(capabilities = CapabilityResolver.resolve(event.info.modelNumber))
        AapEvent.Unrecognized -> this
    }
}
