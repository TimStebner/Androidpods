// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.airpods

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CapabilityResolverTest {
    @Test
    fun `A3050 -- this project's own AirPods 4 -- has no noise control`() {
        val capabilities = CapabilityResolver.resolve("A3050")

        assertFalse(capabilities.supportsNoiseControl)
        assertEquals(emptySet<NoiseControlMode>(), capabilities.supportedNoiseControlModes)
        assertTrue(capabilities.supportsEarDetection)
    }

    @Test
    fun `ANC AirPods 4 model number supports all noise control modes`() {
        val capabilities = CapabilityResolver.resolve("A3055")

        assertTrue(capabilities.supportsNoiseControl)
        assertEquals(
            setOf(
                NoiseControlMode.OFF,
                NoiseControlMode.TRANSPARENCY,
                NoiseControlMode.ADAPTIVE,
                NoiseControlMode.NOISE_CANCELLATION,
            ),
            capabilities.supportedNoiseControlModes,
        )
    }

    @Test
    fun `unknown model number resolves to no supported capabilities`() {
        val capabilities = CapabilityResolver.resolve("Z9999")

        assertEquals(AirPodsCapabilities.UNKNOWN, capabilities)
    }
}
