// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.feature.notifications

import dev.androidpods.core.airpods.BatteryChargeStatus
import dev.androidpods.core.airpods.BatteryComponentState
import dev.androidpods.core.airpods.BatteryState
import dev.androidpods.core.bluetooth.AirPodsTransport
import dev.androidpods.core.data.AirPodsState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BatteryNotificationContentTest {
    @Test
    fun `disconnected state maps to null so the notification is cancelled`() {
        assertNull(AirPodsState.INITIAL.toBatteryNotificationUiState())
    }

    @Test
    fun `connected state with no battery reading yet maps to null`() {
        val state = AirPodsState.INITIAL.copy(connection = AirPodsTransport.ConnectionState.Connected, battery = null)
        assertNull(state.toBatteryNotificationUiState())
    }

    @Test
    fun `connected state with a battery reading maps to Battery with the same values`() {
        val battery = BatteryState(
            left = BatteryComponentState(95, BatteryChargeStatus.NOT_CHARGING),
            right = BatteryComponentState(96, BatteryChargeStatus.NOT_CHARGING),
            case = BatteryComponentState(60, BatteryChargeStatus.CHARGING),
        )
        val state = AirPodsState.INITIAL.copy(connection = AirPodsTransport.ConnectionState.Connected, battery = battery)
        assertEquals(
            BatteryNotificationUiState(left = battery.left, right = battery.right, case = battery.case),
            state.toBatteryNotificationUiState(),
        )
    }

    @Test
    fun `a disconnect after being connected maps back to null`() {
        val battery = BatteryState(
            left = BatteryComponentState(95, BatteryChargeStatus.NOT_CHARGING),
            right = BatteryComponentState(96, BatteryChargeStatus.NOT_CHARGING),
            case = BatteryComponentState(60, BatteryChargeStatus.CHARGING),
        )
        val state = AirPodsState.INITIAL.copy(connection = AirPodsTransport.ConnectionState.Disconnected, battery = battery)
        assertNull(state.toBatteryNotificationUiState())
    }
}
