// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.airpods

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CapabilityResolverTest {
    @Test
    fun `write capabilities stay disabled for hardware families not validated by this project`() {
        listOf("A3055", "A2083", "A2931", "A2096", "A2564", "A2031", "A1523").forEach { modelNumber ->
            val capabilities = CapabilityResolver.resolve(modelNumber)

            assertFalse("$modelNumber press-speed writes", capabilities.supportsPressSpeed)
            assertFalse("$modelNumber head-gesture writes", capabilities.supportsHeadGestures)
            assertFalse("$modelNumber chime writes", capabilities.supportsEarbudChime)
        }
    }

    @Test
    fun `validated AirPods 4 exposes only hardware verified write capabilities`() {
        val capabilities = CapabilityResolver.resolve("A3050")

        assertEquals("AirPods 4", capabilities.modelName)
        assertTrue(capabilities.supportsEarDetection)
        assertTrue(capabilities.supportsPressSpeed)
        assertTrue(capabilities.supportsHeadGestures)
        assertTrue(capabilities.supportsEarbudChime)
    }

    @Test
    fun `ANC AirPods 4 is recognized but remains read only without hardware validation`() {
        val capabilities = CapabilityResolver.resolve("A3055")

        assertEquals("AirPods 4 with ANC", capabilities.modelName)
        assertTrue(capabilities.supportsEarDetection)
        assertFalse(capabilities.supportsPressSpeed)
        assertFalse(capabilities.supportsHeadGestures)
        assertFalse(capabilities.supportsEarbudChime)
    }

    @Test
    fun `AirPods Pro 2 is recognized but remains read only without hardware validation`() {
        val capabilities = CapabilityResolver.resolve("A2931")

        assertEquals("AirPods Pro (2nd gen)", capabilities.modelName)
        assertTrue(capabilities.supportsEarDetection)
        assertFalse(capabilities.supportsPressSpeed)
        assertFalse(capabilities.supportsHeadGestures)
        assertFalse(capabilities.supportsEarbudChime)
    }

    @Test
    fun `unknown model number resolves to no supported capabilities`() {
        val capabilities = CapabilityResolver.resolve("Z9999")

        assertEquals(AirPodsCapabilities.UNKNOWN, capabilities)
        assertFalse(capabilities.supportsEarbudChime)
    }
}
