// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.bluetooth

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProtocolLoggingTest {
    @Test
    fun `raw packet logging requires both a debug build and explicit opt in`() {
        assertFalse(ProtocolLogging.isRawPacketLoggingAllowed(isDebugBuild = false, isOptedIn = false))
        assertFalse(ProtocolLogging.isRawPacketLoggingAllowed(isDebugBuild = false, isOptedIn = true))
        assertFalse(ProtocolLogging.isRawPacketLoggingAllowed(isDebugBuild = true, isOptedIn = false))
        assertTrue(ProtocolLogging.isRawPacketLoggingAllowed(isDebugBuild = true, isOptedIn = true))
    }
}
