// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.bluetooth

import android.util.Log
import dev.androidpods.app.BuildConfig

// Raw protocol packet logging is opt-in and debug-build-only (§27): M2's AAP decoder will log
// through this instead of calling Log.v directly, so release builds can never emit packet bytes
// no matter how the flag is set.
object ProtocolLogging {
    var rawPacketLoggingEnabled: Boolean = false

    fun rawPacket(tag: String, message: () -> String) {
        if (BuildConfig.DEBUG && rawPacketLoggingEnabled) {
            Log.v(tag, message())
        }
    }
}
