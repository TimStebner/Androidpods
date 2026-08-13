// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.data

import dev.androidpods.core.airpods.AirPodsCapabilities
import dev.androidpods.core.airpods.BatteryChargeStatus
import dev.androidpods.core.airpods.BatteryComponentState
import dev.androidpods.core.airpods.EarDetectionState
import dev.androidpods.core.bluetooth.AirPodsTransport
import dev.androidpods.core.bluetooth.FakeAirPodsTransport
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// Fixture packets: app/src/test/resources/fixtures/aap/session-start-capture.txt -- same real
// AirPods 4 capture AapPacketDecoderTest verifies against (PROJECT.md §28).
private fun loadFixturePacket(index: Int): ByteArray {
    val line = object {}.javaClass.getResourceAsStream("/fixtures/aap/session-start-capture.txt")
        ?.bufferedReader()
        ?.readLines()
        ?.first { !it.startsWith("#") && it.startsWith("$index ") }
        ?: error("packet $index not found in fixture")
    val tokens = line.trim().split(Regex("\\s+"))
    return tokens.drop(3).map { it.toInt(16).toByte() }.toByteArray()
}

@OptIn(ExperimentalCoroutinesApi::class)
class AirPodsRepositoryTest {
    @Test
    fun `initial state is disconnected with unknown capabilities`() = runTest {
        val repository = AirPodsRepository(FakeAirPodsTransport(), backgroundScope)

        val state = repository.state.value

        assertEquals(AirPodsTransport.ConnectionState.Disconnected, state.connection)
        assertEquals(AirPodsCapabilities.UNKNOWN, state.capabilities)
    }

    @Test
    fun `device info event resolves capabilities from the model number`() = runTest(UnconfinedTestDispatcher()) {
        val transport = FakeAirPodsTransport()
        val repository = AirPodsRepository(transport, backgroundScope)

        transport.emit(loadFixturePacket(3)) // INFORMATION, modelNumber A3050

        val capabilities = repository.state.value.capabilities
        assertFalse(capabilities.supportsNoiseControl)
        assertTrue(capabilities.supportsEarDetection)
    }

    @Test
    fun `battery event updates battery state`() = runTest(UnconfinedTestDispatcher()) {
        val transport = FakeAirPodsTransport()
        val repository = AirPodsRepository(transport, backgroundScope)

        transport.emit(loadFixturePacket(21)) // BATTERY_INFO

        val battery = repository.state.value.battery
        assertEquals(BatteryComponentState(95, BatteryChargeStatus.NOT_CHARGING), battery?.left)
        assertEquals(BatteryComponentState(96, BatteryChargeStatus.NOT_CHARGING), battery?.right)
    }

    @Test
    fun `ear detection event updates ear detection state`() = runTest(UnconfinedTestDispatcher()) {
        val transport = FakeAirPodsTransport()
        val repository = AirPodsRepository(transport, backgroundScope)

        transport.emit(loadFixturePacket(27)) // EAR_DETECTION

        assertEquals(EarDetectionState(leftInEar = true, rightInEar = true), repository.state.value.earDetection)
    }

    @Test
    fun `connect drives the transport and starts the AAP handshake`() = runTest {
        val transport = FakeAirPodsTransport()
        val repository = AirPodsRepository(transport, backgroundScope)

        repository.connect()

        assertEquals(AirPodsTransport.ConnectionState.Connected, transport.state.value)
        assertEquals(3, transport.sent.size)
    }
}
