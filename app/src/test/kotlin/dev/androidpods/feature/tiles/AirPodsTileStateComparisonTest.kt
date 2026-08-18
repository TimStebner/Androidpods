// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.feature.tiles

import dev.androidpods.core.airpods.BatteryChargeStatus
import dev.androidpods.core.airpods.BatteryComponentState
import dev.androidpods.core.airpods.BatteryState
import dev.androidpods.core.data.AirPodsState
import dev.androidpods.core.data.HeadOrientation
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AirPodsTileStateComparisonTest {
    @Test
    fun `motion-only updates do not trigger tile IPC`() {
        assertTrue(
            AirPodsState.INITIAL.hasSameTileContentAs(
                AirPodsState.INITIAL.copy(headOrientation = HeadOrientation(1f, 2f, 3f)),
            ),
        )
    }

    @Test
    fun `battery changes trigger tile IPC`() {
        val component = BatteryComponentState(50, BatteryChargeStatus.NOT_CHARGING)
        val battery = BatteryState(component, component, component)

        assertFalse(AirPodsState.INITIAL.hasSameTileContentAs(AirPodsState.INITIAL.copy(battery = battery)))
    }
}
