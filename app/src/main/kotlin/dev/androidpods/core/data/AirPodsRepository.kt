// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.data

import dev.androidpods.core.airpods.AapEvent
import dev.androidpods.core.airpods.AapSession
import dev.androidpods.core.airpods.BatteryChargeStatus
import dev.androidpods.core.airpods.BatteryComponentState
import dev.androidpods.core.airpods.BatteryState
import dev.androidpods.core.airpods.CapabilityResolver
import dev.androidpods.core.airpods.HeadGesturesState
import dev.androidpods.core.airpods.HoldDuration
import dev.androidpods.core.airpods.PressSpeed
import dev.androidpods.core.airpods.StemPressAndHoldAction
import dev.androidpods.core.bluetooth.AirPodsTransport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// The only layer allowed to expose AirPods write operations (PROJECT.md §11, §33).
class AirPodsRepository(
    private val transport: AirPodsTransport,
    scope: CoroutineScope,
    private val tierProbeCache: TierProbeCache,
) {
    private val session = AapSession(transport)
    val events: Flow<AapEvent> = session.events
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
        if (_state.value.connection == AirPodsTransport.ConnectionState.Connected ||
            _state.value.connection == AirPodsTransport.ConnectionState.Connecting
        ) {
            return
        }

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
        if (supported) {
            tierProbeCache.recordProbeResult(transport.deviceAddress, true)
            session.start()
        } else if (transport.state.value is AirPodsTransport.ConnectionState.Failed) {
            tierProbeCache.recordProbeResult(transport.deviceAddress, false)
        }
    }

    suspend fun disconnect() = transport.disconnect()

    suspend fun setAssistantTriggerEnabled(enabled: Boolean) {
        if (transport.state.value == AirPodsTransport.ConnectionState.Connected) {
            session.setAssistantTriggerEnabled(enabled)
        }
    }

    suspend fun setPressSpeed(speed: PressSpeed) {
        if (transport.state.value == AirPodsTransport.ConnectionState.Connected) {
            session.setPressSpeed(speed)
        }
    }

    suspend fun setHoldDuration(duration: HoldDuration) {
        if (transport.state.value == AirPodsTransport.ConnectionState.Connected) {
            session.setHoldDuration(duration)
        }
    }

    suspend fun setHeadGesturesEnabled(enabled: Boolean) {
        if (transport.state.value == AirPodsTransport.ConnectionState.Connected) {
            session.setHeadGesturesEnabled(enabled)
        }
    }

    private var motionStreamRequested: Boolean = false

    suspend fun startMotionStream() {
        if (transport.state.value == AirPodsTransport.ConnectionState.Connected) {
            motionStreamRequested = true
            session.startMotionStream()
            _state.update { it.copy(motionStreamActive = true) }
        }
    }

    suspend fun stopMotionStream() {
        if (transport.state.value == AirPodsTransport.ConnectionState.Connected) {
            motionStreamRequested = false
            session.stopMotionStream()
            _state.update { it.copy(motionStreamActive = false, headOrientation = null) }
        }
    }

    private var lastKnownLeft: BatteryComponentState? = null
    private var lastKnownRight: BatteryComponentState? = null
    private var lastKnownCase: BatteryComponentState? = null

    private fun mergeBatteryEvent(event: AapEvent.Battery): BatteryState {
        val rawLeft = event.state.left
        val effectiveLeft = if (rawLeft.status != BatteryChargeStatus.DISCONNECTED && rawLeft.level > 0) {
            lastKnownLeft = rawLeft
            rawLeft
        } else {
            lastKnownLeft?.copy(status = BatteryChargeStatus.DISCONNECTED) ?: rawLeft
        }

        val rawRight = event.state.right
        val effectiveRight = if (rawRight.status != BatteryChargeStatus.DISCONNECTED && rawRight.level > 0) {
            lastKnownRight = rawRight
            rawRight
        } else {
            lastKnownRight?.copy(status = BatteryChargeStatus.DISCONNECTED) ?: rawRight
        }

        val rawCase = event.state.case
        val effectiveCase = if (rawCase.status != BatteryChargeStatus.DISCONNECTED && rawCase.level > 0) {
            lastKnownCase = rawCase
            rawCase
        } else {
            lastKnownCase?.copy(status = BatteryChargeStatus.DISCONNECTED) ?: rawCase
        }

        return BatteryState(left = effectiveLeft, right = effectiveRight, case = effectiveCase)
    }

    private fun AirPodsState.reduce(event: AapEvent): AirPodsState = when (event) {
        is AapEvent.Battery -> copy(battery = mergeBatteryEvent(event))
        is AapEvent.EarDetection -> copy(earDetection = event.state)
        is AapEvent.DeviceInfo -> copy(capabilities = CapabilityResolver.resolve(event.info.modelNumber))
        is AapEvent.StemConfig -> if (event.isLeft) copy(stemLeftAction = event.action) else copy(stemRightAction = event.action)
        is AapEvent.PressSpeedConfig -> copy(pressSpeed = event.speed)
        is AapEvent.HoldDurationConfig -> copy(holdDuration = event.duration)
        is AapEvent.HeadGesturesConfig -> copy(headGesturesState = event.state)
        is AapEvent.HeadMotion -> if (motionStreamRequested) {
            copy(
                headOrientation = HeadOrientation(pitch = event.pitch, yaw = event.yaw, roll = event.roll),
                motionStreamActive = true,
            )
        } else this
        AapEvent.Unrecognized -> this
    }

    private companion object {
        // Exact wording from PROJECT.md §13.6's own example of an honest Tier B failure.
        const val TIER_B_CACHED_UNAVAILABLE_REASON = "this Android build does not allow the AirPods control channel"
    }
}
