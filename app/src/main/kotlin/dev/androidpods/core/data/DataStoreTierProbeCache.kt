// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.data

import android.content.Context
import android.os.Build
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.tierProbeDataStore by preferencesDataStore(name = "tier_probe_cache")

// Not unit-tested: Context/DataStore are framework types (same precedent as AapTransport). The
// caching decision itself lives in AirPodsRepository and is tested there against a fake.
class DataStoreTierProbeCache(private val context: Context) : TierProbeCache {
    override suspend fun tierBSupported(deviceAddress: String): Boolean? =
        context.tierProbeDataStore.data.first()[keyFor(deviceAddress)]

    override suspend fun recordProbeResult(deviceAddress: String, supported: Boolean) {
        context.tierProbeDataStore.edit { it[keyFor(deviceAddress)] = supported }
    }

    // Bundling the build fingerprint into the key (rather than a separate stored fingerprint
    // plus manual invalidation) makes a build upgrade a cache miss for free.
    private fun keyFor(deviceAddress: String) = booleanPreferencesKey("$deviceAddress@${Build.FINGERPRINT}")
}
