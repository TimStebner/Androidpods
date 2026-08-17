// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.feature.tiles

import dev.androidpods.core.airpods.BatteryChargeStatus
import dev.androidpods.core.airpods.BatteryComponentState
import dev.androidpods.core.airpods.BatteryState
import dev.androidpods.core.airpods.CapabilityResolver
import dev.androidpods.core.airpods.EarDetectionState
import dev.androidpods.core.bluetooth.AirPodsTransport
import dev.androidpods.core.data.AirPodsState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class AirPodsTileServiceTest {

    @Test
    fun `initial state is disconnected`() {
        val state = AirPodsState.INITIAL
        assertEquals(AirPodsTransport.ConnectionState.Disconnected, state.connection)
    }

    @Test
    fun `connected state formats battery components properly`() {
        val state = AirPodsState(
            connection = AirPodsTransport.ConnectionState.Connected,
            capabilities = CapabilityResolver.resolve("A3050"),
            battery = BatteryState(
                left = BatteryComponentState(95, BatteryChargeStatus.NOT_CHARGING),
                right = BatteryComponentState(90, BatteryChargeStatus.NOT_CHARGING),
                case = BatteryComponentState(100, BatteryChargeStatus.CHARGING),
            ),
            earDetection = EarDetectionState(leftInEar = true, rightInEar = true),
        )

        val battery = state.battery!!
        val left = "L ${battery.left.level}%"
        val right = "R ${battery.right.level}%"
        val case = "C ${battery.case.level}%"
        val subtitle = "$left · $right · $case"

        assertEquals("L 95% · R 90% · C 100%", subtitle)
        assertEquals("AirPods 4", state.capabilities.modelName)
    }

    @Test
    fun `connected state with missing battery shows fallback text`() {
        val state = AirPodsState(
            connection = AirPodsTransport.ConnectionState.Connected,
            capabilities = CapabilityResolver.resolve("A2698"),
            battery = null,
            earDetection = null,
        )

        assertEquals(AirPodsTransport.ConnectionState.Connected, state.connection)
        assertNotNull(state.capabilities.modelName)
    }
}
