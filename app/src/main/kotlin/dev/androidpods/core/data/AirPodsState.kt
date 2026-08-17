// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.data

import dev.androidpods.core.airpods.AirPodsCapabilities
import dev.androidpods.core.airpods.BatteryState
import dev.androidpods.core.airpods.EarDetectionState
import dev.androidpods.core.airpods.HeadGesturesState
import dev.androidpods.core.airpods.HoldDuration
import dev.androidpods.core.airpods.PressSpeed
import dev.androidpods.core.airpods.StemPressAndHoldAction
import dev.androidpods.core.bluetooth.AirPodsTransport

data class HeadOrientation(
    val pitch: Float,
    val yaw: Float,
    val roll: Float,
)

// The single authoritative state (PROJECT.md §10): Compose UI, widgets, Quick Settings and
// notifications all derive from this, never from a feature-local copy.
data class AirPodsState(
    val connection: AirPodsTransport.ConnectionState,
    val capabilities: AirPodsCapabilities,
    val battery: BatteryState?,
    val earDetection: EarDetectionState?,
    val stemLeftAction: StemPressAndHoldAction? = null,
    val stemRightAction: StemPressAndHoldAction? = null,
    val pressSpeed: PressSpeed? = null,
    val holdDuration: HoldDuration? = null,
    val headGesturesState: HeadGesturesState? = null,
    val headOrientation: HeadOrientation? = null,
    val motionStreamActive: Boolean = false,
) {
    companion object {
        val INITIAL = AirPodsState(
            connection = AirPodsTransport.ConnectionState.Disconnected,
            capabilities = AirPodsCapabilities.UNKNOWN,
            battery = null,
            earDetection = null,
            stemLeftAction = null,
            stemRightAction = null,
            pressSpeed = null,
            holdDuration = null,
            headGesturesState = null,
            headOrientation = null,
            motionStreamActive = false,
        )
    }
}
