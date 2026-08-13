// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.data

import dev.androidpods.core.airpods.AirPodsCapabilities
import dev.androidpods.core.airpods.BatteryState
import dev.androidpods.core.airpods.EarDetectionState
import dev.androidpods.core.bluetooth.AirPodsTransport

// The single authoritative state (PROJECT.md §10): Compose UI, widgets, Quick Settings and
// notifications all derive from this, never from a feature-local copy.
data class AirPodsState(
    val connection: AirPodsTransport.ConnectionState,
    val capabilities: AirPodsCapabilities,
    val battery: BatteryState?,
    val earDetection: EarDetectionState?,
) {
    companion object {
        val INITIAL = AirPodsState(
            connection = AirPodsTransport.ConnectionState.Disconnected,
            capabilities = AirPodsCapabilities.UNKNOWN,
            battery = null,
            earDetection = null,
        )
    }
}
