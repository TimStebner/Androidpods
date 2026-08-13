// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.data

// In-memory TierProbeCache, not a mock -- exercises AirPodsRepository's actual caching logic
// (same precedent as FakeAirPodsTransport).
class FakeTierProbeCache : TierProbeCache {
    private val results = mutableMapOf<String, Boolean>()

    override suspend fun tierBSupported(deviceAddress: String): Boolean? = results[deviceAddress]

    override suspend fun recordProbeResult(deviceAddress: String, supported: Boolean) {
        results[deviceAddress] = supported
    }
}
