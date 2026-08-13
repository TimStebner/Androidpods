// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

// Manual verification tool for PROJECT.md §13.6's cache (hardware pass, 2026-08-13/14): forces
// this project's own AirPods 4 to confirmed-unsupported so AirPodsRepository.connect()'s
// cache-skip path and the honest Tier-B-unavailable UI can be checked without waiting for two
// real probe failures. resetToSupported() undoes it. Not part of CI, run manually via
// `adb shell am instrument -w -e class <FQN>#<method> ...` -- same precedent as
// L2capTierBProbeTest (kept for future re-verification, not deleted after use).
@RunWith(AndroidJUnit4::class)
class ForceTierBUnsupportedTest {
    @Test
    fun forceConfirmedUnsupported() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val cache = DataStoreTierProbeCache(context)
        cache.recordProbeResult(DEVICE_ADDRESS, supported = false)
        cache.recordProbeResult(DEVICE_ADDRESS, supported = false)
    }

    // Resets the entry forced above back to normal so the real probe result governs again.
    @Test
    fun resetToSupported() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val cache = DataStoreTierProbeCache(context)
        cache.recordProbeResult(DEVICE_ADDRESS, supported = true)
    }

    private companion object {
        const val DEVICE_ADDRESS = "TEST-DEVICE-ID"
    }
}
