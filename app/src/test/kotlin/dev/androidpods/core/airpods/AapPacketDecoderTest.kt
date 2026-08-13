// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.airpods

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

// Fixtures: app/src/test/resources/fixtures/aap/session-start-capture.txt -- a real capture from
// the project's own AirPods 4 (PROJECT.md §28 "capture real bytes first, then parse"). Values
// asserted below are the actual decoded values of that capture, not synthetic expectations.
private fun loadFixturePacket(index: Int): ByteArray {
    val line = object {}.javaClass.getResourceAsStream("/fixtures/aap/session-start-capture.txt")
        ?.bufferedReader()
        ?.readLines()
        ?.first { !it.startsWith("#") && it.startsWith("$index ") }
        ?: error("packet $index not found in fixture")
    val tokens = line.trim().split(Regex("\\s+"))
    val hexStart = 3 // index, byte-count, label
    return tokens.drop(hexStart).map { it.toInt(16).toByte() }.toByteArray()
}

class AapPacketDecoderTest {
    @Test
    fun `decodes battery packet from real capture`() {
        val event = AapPacketDecoder.decode(loadFixturePacket(21))

        assertTrue(event is AapEvent.Battery)
        val battery = event as AapEvent.Battery
        assertEquals(BatteryComponentState(95, BatteryChargeStatus.NOT_CHARGING), battery.state.left)
        assertEquals(BatteryComponentState(96, BatteryChargeStatus.NOT_CHARGING), battery.state.right)
        assertEquals(BatteryComponentState(0, BatteryChargeStatus.DISCONNECTED), battery.state.case)
    }

    @Test
    fun `decodes ear detection packet from real capture`() {
        val event = AapPacketDecoder.decode(loadFixturePacket(27))

        assertTrue(event is AapEvent.EarDetection)
        val earDetection = event as AapEvent.EarDetection
        assertEquals(EarDetectionState(leftInEar = true, rightInEar = true), earDetection.state)
    }

    @Test
    fun `decodes information packet from real capture`() {
        val event = AapPacketDecoder.decode(loadFixturePacket(3))

        assertTrue(event is AapEvent.DeviceInfo)
        val deviceInfo = event as AapEvent.DeviceInfo
        assertEquals("AirPods", deviceInfo.info.name)
        assertEquals("A3050", deviceInfo.info.modelNumber)
        assertEquals("Apple Inc.", deviceInfo.info.manufacturer)
        assertEquals("LKGVHWX2P5", deviceInfo.info.serialNumber)
        assertEquals("H1FHAN0C84W0000B30", deviceInfo.info.leftSerialNumber)
        assertEquals("H2KHAN0D3FP0000B32", deviceInfo.info.rightSerialNumber)
    }

    @Test
    fun `unknown opcode decodes to Unrecognized`() {
        val event = AapPacketDecoder.decode(loadFixturePacket(22))

        assertTrue(event is AapEvent.Unrecognized)
    }

    @Test
    fun `packet too short to have a header decodes to Unrecognized`() {
        val event = AapPacketDecoder.decode(byteArrayOf(0x04, 0x00))

        assertTrue(event is AapEvent.Unrecognized)
    }

    @Test
    fun `battery packet with wrong length decodes to Unrecognized`() {
        val event = AapPacketDecoder.decode(byteArrayOf(0x04, 0x00, 0x04, 0x00, 0x04, 0x00, 0x00))

        assertTrue(event is AapEvent.Unrecognized)
    }
}
