// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.telecom

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CallGestureMotionGateTest {
    @Test
    fun `ringing starts motion only when setting capability and connection all allow it`() {
        assertTrue(shouldStartCallGestureMotion(settingEnabled = true, capabilitySupported = true, connected = true))
        assertFalse(shouldStartCallGestureMotion(settingEnabled = false, capabilitySupported = true, connected = true))
        assertFalse(shouldStartCallGestureMotion(settingEnabled = true, capabilitySupported = false, connected = true))
        assertFalse(shouldStartCallGestureMotion(settingEnabled = true, capabilitySupported = true, connected = false))
    }
}
