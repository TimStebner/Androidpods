// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.feature.controls

import dev.androidpods.core.airpods.PressSpeed
import dev.androidpods.core.data.AirPodsState
import dev.androidpods.core.data.HeadOrientation
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ControlsStateComparisonTest {
    @Test
    fun `motion-only updates do not invalidate the controls screen`() {
        val previous = AirPodsState.INITIAL
        val motionUpdate = previous.copy(
            headOrientation = HeadOrientation(pitch = 1f, yaw = 2f, roll = 3f),
            motionStreamActive = true,
        )

        assertTrue(previous.hasSameControlsContentAs(motionUpdate))
    }

    @Test
    fun `device setting updates still invalidate the controls screen`() {
        val previous = AirPodsState.INITIAL

        assertFalse(previous.hasSameControlsContentAs(previous.copy(pressSpeed = PressSpeed.SLOW)))
    }
}
