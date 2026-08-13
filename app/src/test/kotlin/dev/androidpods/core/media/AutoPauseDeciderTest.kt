// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.media

import dev.androidpods.core.airpods.EarDetectionState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AutoPauseDeciderTest {
    @Test
    fun `pauses when both AirPods go from in-ear to out-of-ear`() {
        val previous = EarDetectionState(leftInEar = true, rightInEar = true)
        val current = EarDetectionState(leftInEar = false, rightInEar = false)

        assertTrue(AutoPauseDecider.shouldPause(previous, current))
    }

    @Test
    fun `does not pause while one AirPod is still in ear`() {
        val previous = EarDetectionState(leftInEar = true, rightInEar = true)
        val current = EarDetectionState(leftInEar = true, rightInEar = false)

        assertFalse(AutoPauseDecider.shouldPause(previous, current))
    }

    @Test
    fun `does not pause when both were already out of ear`() {
        val previous = EarDetectionState(leftInEar = false, rightInEar = false)
        val current = EarDetectionState(leftInEar = false, rightInEar = false)

        assertFalse(AutoPauseDecider.shouldPause(previous, current))
    }

    @Test
    fun `does not pause on the first ear detection event with no prior state`() {
        val current = EarDetectionState(leftInEar = false, rightInEar = false)

        assertFalse(AutoPauseDecider.shouldPause(previous = null, current = current))
    }
}
