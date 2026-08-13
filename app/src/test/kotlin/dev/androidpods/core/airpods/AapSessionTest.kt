// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.airpods

import dev.androidpods.core.bluetooth.FakeAirPodsTransport
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AapSessionTest {
    @Test
    fun `start sends handshake, then feature flags, then notification request, in order`() = runTest {
        val transport = FakeAirPodsTransport()
        val session = AapSession(transport)

        session.start()

        assertEquals(
            listOf(
                AapSession.HANDSHAKE_PACKET.toList(),
                AapSession.SET_FEATURE_FLAGS_PACKET.toList(),
                AapSession.REQUEST_NOTIFICATIONS_PACKET.toList(),
            ),
            transport.sent.map { it.toList() },
        )
    }

    @Test
    fun `inbound transport packets are decoded into AAP events`() = runTest(UnconfinedTestDispatcher()) {
        val transport = FakeAirPodsTransport()
        val session = AapSession(transport)
        val earDetectionPacket = byteArrayOf(0x04, 0x00, 0x04, 0x00, 0x06, 0x00, 0x01, 0x01)

        val received = mutableListOf<AapEvent>()
        val job = launch { session.events.collect { received.add(it) } }
        transport.emit(earDetectionPacket)
        job.cancel()

        assertEquals(
            listOf(AapEvent.EarDetection(EarDetectionState(leftInEar = true, rightInEar = true))),
            received,
        )
    }
}
