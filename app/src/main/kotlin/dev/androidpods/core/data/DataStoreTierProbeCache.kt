// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.data

import android.content.Context
import android.os.Build
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.tierProbeDataStore by preferencesDataStore(name = "tier_probe_cache")

private const val STATE_SUPPORTED = 1
private const val STATE_ONE_FAILURE = -1
private const val STATE_CONFIRMED_UNSUPPORTED = -2

// Not unit-tested: Context/DataStore are framework types (same precedent as AapTransport). The
// caching decision itself lives in AirPodsRepository and is tested there against a fake; the
// confirm-twice policy encoded below must be kept in sync with FakeTierProbeCache (see
// TierProbeCache's doc comment) -- it is verified on real hardware per the plan's DataStore
// manipulation step, not here.
class DataStoreTierProbeCache(private val context: Context) : TierProbeCache {
    override suspend fun tierBSupported(deviceAddress: String): Boolean? =
        when (context.tierProbeDataStore.data.first()[keyFor(deviceAddress)]) {
            STATE_SUPPORTED -> true
            STATE_CONFIRMED_UNSUPPORTED -> false
            else -> null
        }

    override suspend fun recordProbeResult(deviceAddress: String, supported: Boolean) {
        context.tierProbeDataStore.edit { prefs ->
            val key = keyFor(deviceAddress)
            prefs[key] = if (supported) {
                STATE_SUPPORTED
            } else if (prefs[key] == STATE_ONE_FAILURE) {
                STATE_CONFIRMED_UNSUPPORTED
            } else {
                STATE_ONE_FAILURE
            }
        }
    }

    // Bundling the build fingerprint into the key (rather than a separate stored fingerprint
    // plus manual invalidation) makes a build upgrade a cache miss for free.
    private fun keyFor(deviceAddress: String) = intPreferencesKey("$deviceAddress@${Build.FINGERPRINT}")
}
