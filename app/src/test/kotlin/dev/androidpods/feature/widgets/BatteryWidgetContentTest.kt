// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.feature.widgets

import dev.androidpods.core.airpods.BatteryChargeStatus
import dev.androidpods.core.airpods.BatteryComponentState
import dev.androidpods.core.airpods.BatteryState
import dev.androidpods.core.data.AirPodsState
import org.junit.Assert.assertEquals
import org.junit.Test

class BatteryWidgetContentTest {
    @Test
    fun `INITIAL state maps to NoData`() {
        assertEquals(BatteryWidgetUiState.NoData, AirPodsState.INITIAL.toBatteryWidgetUiState())
    }

    @Test
    fun `connected state with no battery reading yet still maps to NoData`() {
        val state = AirPodsState.INITIAL.copy(battery = null)

        assertEquals(BatteryWidgetUiState.NoData, state.toBatteryWidgetUiState())
    }

    @Test
    fun `state with a battery reading maps to Battery with the same values`() {
        val battery = BatteryState(
            left = BatteryComponentState(95, BatteryChargeStatus.NOT_CHARGING),
            right = BatteryComponentState(96, BatteryChargeStatus.NOT_CHARGING),
            case = BatteryComponentState(60, BatteryChargeStatus.CHARGING),
        )
        val state = AirPodsState.INITIAL.copy(battery = battery)

        assertEquals(
            BatteryWidgetUiState.Battery(left = battery.left, right = battery.right, case = battery.case),
            state.toBatteryWidgetUiState(),
        )
    }
}
