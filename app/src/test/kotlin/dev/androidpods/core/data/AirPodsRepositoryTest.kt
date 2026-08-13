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
        val repository = AirPodsRepository(FakeAirPodsTransport(), backgroundScope, FakeTierProbeCache())

        val state = repository.state.value

        assertEquals(AirPodsTransport.ConnectionState.Disconnected, state.connection)
        assertEquals(AirPodsCapabilities.UNKNOWN, state.capabilities)
    }

    @Test
    fun `device info event resolves capabilities from the model number`() = runTest(UnconfinedTestDispatcher()) {
        val transport = FakeAirPodsTransport()
        val repository = AirPodsRepository(transport, backgroundScope, FakeTierProbeCache())

        transport.emit(loadFixturePacket(3)) // INFORMATION, modelNumber A3050

        val capabilities = repository.state.value.capabilities
        assertFalse(capabilities.supportsNoiseControl)
        assertTrue(capabilities.supportsEarDetection)
    }

    @Test
    fun `battery event updates battery state`() = runTest(UnconfinedTestDispatcher()) {
        val transport = FakeAirPodsTransport()
        val repository = AirPodsRepository(transport, backgroundScope, FakeTierProbeCache())

        transport.emit(loadFixturePacket(21)) // BATTERY_INFO

        val battery = repository.state.value.battery
        assertEquals(BatteryComponentState(95, BatteryChargeStatus.NOT_CHARGING), battery?.left)
        assertEquals(BatteryComponentState(96, BatteryChargeStatus.NOT_CHARGING), battery?.right)
    }

    @Test
    fun `ear detection event updates ear detection state`() = runTest(UnconfinedTestDispatcher()) {
        val transport = FakeAirPodsTransport()
        val repository = AirPodsRepository(transport, backgroundScope, FakeTierProbeCache())

        transport.emit(loadFixturePacket(27)) // EAR_DETECTION

        assertEquals(EarDetectionState(leftInEar = true, rightInEar = true), repository.state.value.earDetection)
    }

    @Test
    fun `connect drives the transport and starts the AAP handshake`() = runTest {
        val transport = FakeAirPodsTransport()
        val repository = AirPodsRepository(transport, backgroundScope, FakeTierProbeCache())

        repository.connect()

        assertEquals(AirPodsTransport.ConnectionState.Connected, transport.state.value)
        assertEquals(3, transport.sent.size)
    }

    @Test
    fun `connect leaves state Failed instead of throwing when the transport fails to connect`() = runTest {
        val failure = AirPodsTransport.ConnectionState.Failed("ACL connection failed")
        val transport = FakeAirPodsTransport(connectOutcome = failure)
        val repository = AirPodsRepository(transport, backgroundScope, FakeTierProbeCache())

        repository.connect()

        assertEquals(failure, transport.state.value)
        assertTrue(transport.sent.isEmpty())
    }

    @Test
    fun `connect skips the transport when the tier probe cache already confirms unsupported`() = runTest {
        val transport = FakeAirPodsTransport()
        val cache = FakeTierProbeCache().apply {
            recordProbeResult(transport.deviceAddress, supported = false)
            recordProbeResult(transport.deviceAddress, supported = false)
        }
        val repository = AirPodsRepository(transport, backgroundScope, cache)

        repository.connect()

        assertEquals(0, transport.connectCallCount)
        assertTrue(repository.state.value.connection is AirPodsTransport.ConnectionState.Failed)
    }

    @Test
    fun `connect records a successful probe in the cache when none exists yet`() = runTest {
        val transport = FakeAirPodsTransport()
        val cache = FakeTierProbeCache()
        val repository = AirPodsRepository(transport, backgroundScope, cache)

        repository.connect()

        assertEquals(true, cache.tierBSupported(transport.deviceAddress))
    }

    @Test
    fun `a single failed probe does not yet skip the next connect attempt`() = runTest {
        val transport = FakeAirPodsTransport(connectOutcome = AirPodsTransport.ConnectionState.Failed("ACL connection failed"))
        val cache = FakeTierProbeCache()
        val repository = AirPodsRepository(transport, backgroundScope, cache)

        repository.connect()

        assertEquals(null, cache.tierBSupported(transport.deviceAddress))
        assertEquals(1, transport.connectCallCount)
    }

    @Test
    fun `only a second consecutive failed probe confirms unsupported and starts skipping`() = runTest {
        val transport = FakeAirPodsTransport(connectOutcome = AirPodsTransport.ConnectionState.Failed("ACL connection failed"))
        val cache = FakeTierProbeCache()
        val repository = AirPodsRepository(transport, backgroundScope, cache)

        repository.connect()
        repository.connect()
        repository.connect()

        assertEquals(false, cache.tierBSupported(transport.deviceAddress))
        assertEquals(2, transport.connectCallCount)
    }

    @Test
    fun `a successful probe resets a prior single failure`() = runTest {
        val cache = FakeTierProbeCache().apply { recordProbeResult("02:00:00:00:00:01", supported = false) }

        cache.recordProbeResult("02:00:00:00:00:01", supported = true)

        assertEquals(true, cache.tierBSupported("02:00:00:00:00:01"))
    }
}
