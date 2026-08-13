// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.data

// In-memory TierProbeCache, not a mock -- exercises AirPodsRepository's actual caching logic
// (same precedent as FakeAirPodsTransport). Mirrors DataStoreTierProbeCache's confirm-twice
// policy for a false result (see TierProbeCache's doc comment).
class FakeTierProbeCache : TierProbeCache {
    // 1 = supported, -1 = one failure seen, -2 = confirmed unsupported.
    private val states = mutableMapOf<String, Int>()

    override suspend fun tierBSupported(deviceAddress: String): Boolean? = when (states[deviceAddress]) {
        1 -> true
        -2 -> false
        else -> null
    }

    override suspend fun recordProbeResult(deviceAddress: String, supported: Boolean) {
        states[deviceAddress] = if (supported) {
            1
        } else {
            if (states[deviceAddress] == -1) -2 else -1
        }
    }
}
