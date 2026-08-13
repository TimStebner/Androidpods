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
    private val tierProbeCache: TierProbeCache,
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
        // §13.6: a confirmed-unsupported cache entry skips the guarded PSM 0x1001 attempt (and
        // its retry backoff, §14 battery policy) instead of re-probing a build already known to
        // reject it. See TierProbeCache's doc comment for why "confirmed" requires two failures.
        if (tierProbeCache.tierBSupported(transport.deviceAddress) == false) {
            _state.update {
                it.copy(connection = AirPodsTransport.ConnectionState.Failed(TIER_B_CACHED_UNAVAILABLE_REASON))
            }
            return
        }

        transport.connect()
        val supported = transport.state.value == AirPodsTransport.ConnectionState.Connected
        tierProbeCache.recordProbeResult(transport.deviceAddress, supported)
        // A failed transport.connect() already recorded itself as Failed (§2.6: honest failure,
        // not a crash) -- starting the AAP session on top of a socket that doesn't exist would
        // throw instead.
        if (supported) {
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

    private companion object {
        // Exact wording from PROJECT.md §13.6's own example of an honest Tier B failure.
        const val TIER_B_CACHED_UNAVAILABLE_REASON = "this Android build does not allow the AirPods control channel"
    }
}
