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
    fun `pauses when right AirPod is removed from ear`() {
        val previous = EarDetectionState(leftInEar = true, rightInEar = true)
        val current = EarDetectionState(leftInEar = true, rightInEar = false)

        assertTrue(AutoPauseDecider.shouldPause(previous, current))
    }

    @Test
    fun `pauses when left AirPod is removed from ear`() {
        val previous = EarDetectionState(leftInEar = true, rightInEar = true)
        val current = EarDetectionState(leftInEar = false, rightInEar = true)

        assertTrue(AutoPauseDecider.shouldPause(previous, current))
    }

    @Test
    fun `does not pause when inserting an AirPod into ear`() {
        val previous = EarDetectionState(leftInEar = false, rightInEar = false)
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

    @Test
    fun `resumes when right AirPod is inserted back into ear`() {
        val previous = EarDetectionState(leftInEar = true, rightInEar = false)
        val current = EarDetectionState(leftInEar = true, rightInEar = true)

        assertTrue(AutoPauseDecider.shouldResume(previous, current))
    }

    @Test
    fun `resumes when left AirPod is inserted back into ear`() {
        val previous = EarDetectionState(leftInEar = false, rightInEar = true)
        val current = EarDetectionState(leftInEar = true, rightInEar = true)

        assertTrue(AutoPauseDecider.shouldResume(previous, current))
    }

    @Test
    fun `resumes when both AirPods are inserted into ear`() {
        val previous = EarDetectionState(leftInEar = false, rightInEar = false)
        val current = EarDetectionState(leftInEar = true, rightInEar = true)

        assertTrue(AutoPauseDecider.shouldResume(previous, current))
    }

    @Test
    fun `does not resume when removing an AirPod from ear`() {
        val previous = EarDetectionState(leftInEar = true, rightInEar = true)
        val current = EarDetectionState(leftInEar = true, rightInEar = false)

        assertFalse(AutoPauseDecider.shouldResume(previous, current))
    }

    @Test
    fun `does not resume on first ear detection event with no prior state`() {
        val current = EarDetectionState(leftInEar = true, rightInEar = true)

        assertFalse(AutoPauseDecider.shouldResume(previous = null, current = current))
    }
}
