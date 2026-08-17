// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.airpods

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CapabilityResolverTest {
    @Test
    fun `A3050 -- this project's own AirPods 4 -- has no noise control and no stem configuration`() {
        val capabilities = CapabilityResolver.resolve("A3050")

        assertEquals("AirPods 4", capabilities.modelName)
        assertFalse(capabilities.supportsNoiseControl)
        assertFalse(capabilities.supportsStemConfiguration)
        assertEquals(emptySet<NoiseControlMode>(), capabilities.supportedNoiseControlModes)
        assertTrue(capabilities.supportsEarDetection)
        assertTrue(capabilities.supportsEarbudChime)
        assertFalse(capabilities.supportsCaseSpeaker)
    }

    @Test
    fun `ANC AirPods 4 model number supports all noise control modes and stem configuration`() {
        val capabilities = CapabilityResolver.resolve("A3055")

        assertEquals("AirPods 4 with ANC", capabilities.modelName)
        assertTrue(capabilities.supportsNoiseControl)
        assertTrue(capabilities.supportsStemConfiguration)
        assertTrue(capabilities.supportsEarbudChime)
        assertTrue(capabilities.supportsCaseSpeaker)
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
    fun `AirPods Pro 2 supports stem configuration and noise control`() {
        val capabilities = CapabilityResolver.resolve("A2931")

        assertEquals("AirPods Pro (2nd gen)", capabilities.modelName)
        assertTrue(capabilities.supportsNoiseControl)
        assertTrue(capabilities.supportsStemConfiguration)
        assertTrue(capabilities.supportsEarbudChime)
        assertTrue(capabilities.supportsCaseSpeaker)
    }

    @Test
    fun `unknown model number resolves to no supported capabilities`() {
        val capabilities = CapabilityResolver.resolve("Z9999")

        assertEquals(AirPodsCapabilities.UNKNOWN, capabilities)
        assertFalse(capabilities.supportsEarbudChime)
        assertFalse(capabilities.supportsCaseSpeaker)
    }
}
