// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.data

// PROJECT.md §13.6: Tier B availability must never be inferred from Build.VERSION -- it's a
// runtime probe result, cached per device address together with the OS build fingerprint so a
// build upgrade is naturally a cache miss (no separate invalidation step needed).
//
// tierBSupported() only returns false once two *consecutive* failed probes have been recorded --
// a single IOException from AapTransport.connect() can't be told apart from a transient
// PAGE_TIMEOUT (ADR-0001) at this layer, and a false negative here would permanently disable
// Tier B for this build with no way back. Any success immediately resets the streak and is
// trusted on its own, since a working connection is unambiguous. Implementations must apply this
// same policy (see DataStoreTierProbeCache).
interface TierProbeCache {
    suspend fun tierBSupported(deviceAddress: String): Boolean?
    suspend fun recordProbeResult(deviceAddress: String, supported: Boolean)
}
