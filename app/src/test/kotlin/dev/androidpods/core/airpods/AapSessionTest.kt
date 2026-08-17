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

        val expected = listOf(
            AapSession.createMotionStreamPacket(0x10, false).toList(),
            AapSession.createMotionStreamPacket(0x12, false).toList(),
            AapSession.HANDSHAKE_PACKET.toList(),
            AapSession.SET_FEATURE_FLAGS_PACKET.toList(),
            AapSession.REQUEST_NOTIFICATIONS_PACKET.toList(),
        )
        assertEquals(expected, transport.sent.map { it.toList() })
    }

    @Test
    fun `inbound transport packets are decoded into AAP events`() = runTest(UnconfinedTestDispatcher()) {
        val transport = FakeAirPodsTransport()
        val session = AapSession(transport)
        val earDetectionPacket = byteArrayOf(0x04, 0x00, 0x04, 0x00, 0x06, 0x00, 0x00, 0x00)

        val received = mutableListOf<AapEvent>()
        val job = launch { session.events.collect { received.add(it) } }
        transport.emit(earDetectionPacket)
        job.cancel()

        assertEquals(
            listOf(AapEvent.EarDetection(EarDetectionState(leftInEar = true, rightInEar = true))),
            received,
        )
    }

    @Test
    fun `setStemAction sends correctly formatted configuration packet`() = runTest {
        val transport = FakeAirPodsTransport()
        val session = AapSession(transport)

        session.setStemAction(isLeft = true, StemPressAndHoldAction.VOICE_ASSISTANT)

        val expected = byteArrayOf(0x04, 0x00, 0x04, 0x00, 0x09, 0x00, 0x18, 0x00, 0x00, 0x00, 0x00)
        assertEquals(listOf(expected.toList()), transport.sent.map { it.toList() })
    }

    @Test
    fun `setAssistantTriggerEnabled dispatches configuration for both earbuds`() = runTest {
        val transport = FakeAirPodsTransport()
        val session = AapSession(transport)

        session.setAssistantTriggerEnabled(false)

        val expectedLeft = byteArrayOf(0x04, 0x00, 0x04, 0x00, 0x09, 0x00, 0x18, 0x00, 0x02, 0x00, 0x00)
        val expectedRight = byteArrayOf(0x04, 0x00, 0x04, 0x00, 0x09, 0x00, 0x17, 0x00, 0x02, 0x00, 0x00)
        assertEquals(
            listOf(expectedLeft.toList(), expectedRight.toList()),
            transport.sent.map { it.toList() },
        )
    }

    @Test
    fun `setPressSpeed sends config 0x25 packet`() = runTest {
        val transport = FakeAirPodsTransport()
        val session = AapSession(transport)

        session.setPressSpeed(PressSpeed.SLOW)

        val expected = byteArrayOf(0x04, 0x00, 0x04, 0x00, 0x09, 0x00, 0x25, 0x02, 0x00, 0x00, 0x00)
        assertEquals(listOf(expected.toList()), transport.sent.map { it.toList() })
    }

    @Test
    fun `setHoldDuration sends config 0x26 packet`() = runTest {
        val transport = FakeAirPodsTransport()
        val session = AapSession(transport)

        session.setHoldDuration(HoldDuration.LONG)

        val expected = byteArrayOf(0x04, 0x00, 0x04, 0x00, 0x09, 0x00, 0x26, 0x03, 0x00, 0x00, 0x00)
        assertEquals(listOf(expected.toList()), transport.sent.map { it.toList() })
    }

    @Test
    fun `setHeadGesturesEnabled sends config 0x3E packet`() = runTest {
        val transport = FakeAirPodsTransport()
        val session = AapSession(transport)

        session.setHeadGesturesEnabled(true)

        val expected = byteArrayOf(0x04, 0x00, 0x04, 0x00, 0x09, 0x00, 0x3E.toByte(), 0x02, 0x00, 0x00, 0x00)
        assertEquals(listOf(expected.toList()), transport.sent.map { it.toList() })
    }

    @Test
    fun `startMotionStream and stopMotionStream send opcode 0x17 packets`() = runTest {
        val transport = FakeAirPodsTransport()
        val session = AapSession(transport)

        session.startMotionStream()
        session.stopMotionStream()

        assertEquals(
            listOf(
                AapSession.createMotionStreamPacket(0x10, true).toList(),
                AapSession.createMotionStreamPacket(0x12, true).toList(),
                AapSession.createMotionStreamPacket(0x10, false).toList(),
                AapSession.createMotionStreamPacket(0x12, false).toList(),
            ),
            transport.sent.map { it.toList() },
        )
    }
}
