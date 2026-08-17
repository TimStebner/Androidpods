// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.airpods

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

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
        assertEquals(BatteryComponentState(96, BatteryChargeStatus.NOT_CHARGING), battery.state.left)
        assertEquals(BatteryComponentState(95, BatteryChargeStatus.NOT_CHARGING), battery.state.right)
        assertEquals(BatteryComponentState(0, BatteryChargeStatus.DISCONNECTED), battery.state.case)
    }

    @Test
    fun `decodes ear detection packet from real capture`() {
        val event = AapPacketDecoder.decode(loadFixturePacket(27))

        assertTrue(event is AapEvent.EarDetection)
        val earDetection = event as AapEvent.EarDetection
        assertEquals(EarDetectionState(leftInEar = false, rightInEar = false), earDetection.state)
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

    @Test
    fun `decodes stem config packet from real capture`() {
        val event = AapPacketDecoder.decode(loadFixturePacket(4))

        assertTrue(event is AapEvent.StemConfig)
        val stemConfig = event as AapEvent.StemConfig
        assertTrue(stemConfig.isLeft)
        assertEquals(StemPressAndHoldAction.VOICE_ASSISTANT, stemConfig.action)
    }

    @Test
    fun `decodes press speed packet from real capture`() {
        val event = AapPacketDecoder.decode(loadFixturePacket(6))

        assertTrue(event is AapEvent.PressSpeedConfig)
        assertEquals(PressSpeed.DEFAULT, (event as AapEvent.PressSpeedConfig).speed)
    }

    @Test
    fun `decodes hold duration packet from real capture`() {
        val event = AapPacketDecoder.decode(loadFixturePacket(10))

        assertTrue(event is AapEvent.HoldDurationConfig)
        assertEquals(HoldDuration.DEFAULT, (event as AapEvent.HoldDurationConfig).duration)
    }

    @Test
    fun `decodes head gestures packet from real capture`() {
        val event = AapPacketDecoder.decode(loadFixturePacket(13))

        assertTrue(event is AapEvent.HeadGesturesConfig)
        assertEquals(HeadGesturesState.ENABLED, (event as AapEvent.HeadGesturesConfig).state)
    }

    @Test
    fun `decodes head tracking packet from sensor report`() {
        // Construct a synthetic 52-byte sensor report with o1, o2, o3 values
        val packet = ByteArray(52) { 0 }
        packet[0] = 0x04
        packet[1] = 0x00
        packet[2] = 0x04
        packet[3] = 0x00
        packet[4] = 0x17 // Opcode 0x17
        packet[5] = 0x00

        // Put o1 = 0, o2 = 3200 (10 deg), o3 = 3200 (10 deg)
        // pitch = ((3200 + 3200)/2 / 32000) * 180 = (3200/32000)*180 = 18.0 deg
        val o2 = 3200.toShort()
        packet[45] = (o2.toInt() and 0xFF).toByte()
        packet[46] = (o2.toInt() shr 8).toByte()
        val o3 = 3200.toShort()
        packet[47] = (o3.toInt() and 0xFF).toByte()
        packet[48] = (o3.toInt() shr 8).toByte()

        val event = AapPacketDecoder.decode(packet)
        assertTrue(event is AapEvent.HeadMotion)
        val motion = event as AapEvent.HeadMotion
        assertEquals(18f, motion.pitch, 0.1f)
        assertEquals(0f, motion.yaw, 0.1f)
    }
}
