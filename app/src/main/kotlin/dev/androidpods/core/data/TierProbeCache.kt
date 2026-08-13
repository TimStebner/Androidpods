// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.data

// PROJECT.md §13.6: Tier B availability must never be inferred from Build.VERSION -- it's a
// runtime probe result, cached per device address together with the OS build fingerprint so a
// build upgrade is naturally a cache miss (no separate invalidation step needed).
interface TierProbeCache {
    suspend fun tierBSupported(deviceAddress: String): Boolean?
    suspend fun recordProbeResult(deviceAddress: String, supported: Boolean)
}
