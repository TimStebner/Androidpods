// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.airpods

import dev.androidpods.core.bluetooth.AirPodsTransport
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Session-start handshake bytes and send order verified against real AirPods 4 hardware
// (L2capTierBProbeTest.captureAapFixture, plan file M2 section). Adapted from LibrePods
// (GPL-3.0-or-later), AACPManager.kt, commit 790e3963451002a3aabf8dcd71d40c635724176a:
// https://github.com/kavishdevar/librepods/blob/790e3963451002a3aabf8dcd71d40c635724176a/android/app/src/main/java/me/kavishdevar/librepods/bluetooth/AACPManager.kt
class AapSession(private val transport: AirPodsTransport) {
    // Transport-agnostic decoding lives here, not in AirPodsTransport (§11).
    val events: Flow<AapEvent> = transport.packets.map(AapPacketDecoder::decode)

    suspend fun start() {
        // Reset any lingering motion stream from previous session before initial handshake
        runCatching { stopMotionStream() }
        delay(50)
        transport.send(HANDSHAKE_PACKET)
        delay(200)
        transport.send(SET_FEATURE_FLAGS_PACKET)
        delay(200)
        transport.send(REQUEST_NOTIFICATIONS_PACKET)
    }

    suspend fun setPressSpeed(speed: PressSpeed) {
        transport.send(createPressSpeedPacket(speed))
    }

    suspend fun setHoldDuration(duration: HoldDuration) {
        transport.send(createHoldDurationPacket(duration))
    }

    suspend fun setHeadGesturesEnabled(enabled: Boolean) {
        transport.send(createHeadGesturesPacket(enabled))
    }

    suspend fun startMotionStream() {
        transport.send(createMotionStreamPacket(serviceId = 0x10.toByte(), enabled = true))
        delay(50)
        transport.send(createMotionStreamPacket(serviceId = 0x12.toByte(), enabled = true))
    }

    suspend fun stopMotionStream() {
        transport.send(createMotionStreamPacket(serviceId = 0x10.toByte(), enabled = false))
        delay(50)
        transport.send(createMotionStreamPacket(serviceId = 0x12.toByte(), enabled = false))
    }

    companion object {
        fun createMotionStreamPacket(serviceId: Byte, enabled: Boolean): ByteArray {
            val rateByte = if (enabled) 0x40.toByte() else 0x00.toByte()
            val rateByte2 = if (enabled) 0x9C.toByte() else 0x00.toByte()
            return byteArrayOf(
                0x04, 0x00, 0x04, 0x00, 0x17, 0x00, 0x00, 0x00,
                0x10, 0x00, 0x10, 0x00, 0x08, 0xA1.toByte(), 0x02, 0x42,
                0x0B, 0x08, serviceId, 0x10, 0x02, 0x1A, 0x05, 0x01,
                rateByte, rateByte2, 0x00, 0x00,
            )
        }

        // Own 4-byte pseudo-header (00 00 04 00), unlike the two packets below.
        val HANDSHAKE_PACKET = byteArrayOf(
            0x00, 0x00, 0x04, 0x00, 0x01, 0x00, 0x02, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        )
        val SET_FEATURE_FLAGS_PACKET = byteArrayOf(
            0x04, 0x00, 0x04, 0x00, 0x4D, 0x00,
            0xD7.toByte(), 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        )
        val REQUEST_NOTIFICATIONS_PACKET = byteArrayOf(
            0x04, 0x00, 0x04, 0x00, 0x0F, 0x00,
            0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(),
        )

        fun createPressSpeedPacket(speed: PressSpeed): ByteArray = byteArrayOf(
            0x04, 0x00, 0x04, 0x00, // Header
            0x09, 0x00,             // Opcode 0x09
            0x25, speed.rawValue,   // Config 0x25
            0x00, 0x00, 0x00,
        )

        fun createHoldDurationPacket(duration: HoldDuration): ByteArray = byteArrayOf(
            0x04, 0x00, 0x04, 0x00, // Header
            0x09, 0x00,             // Opcode 0x09
            0x26, duration.rawValue,// Config 0x26
            0x00, 0x00, 0x00,
        )

        fun createHeadGesturesPacket(enabled: Boolean): ByteArray = byteArrayOf(
            0x04, 0x00, 0x04, 0x00, // Header
            0x09, 0x00,             // Opcode 0x09
            0x3E.toByte(), if (enabled) HeadGesturesState.ENABLED.rawValue else HeadGesturesState.DISABLED.rawValue,
            0x00, 0x00, 0x00,
        )
    }
}
