// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.airpods

import dev.androidpods.core.bluetooth.AirPodsTransport
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
        transport.send(HANDSHAKE_PACKET)
        transport.send(SET_FEATURE_FLAGS_PACKET)
        transport.send(REQUEST_NOTIFICATIONS_PACKET)
    }

    companion object {
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
    }
}
