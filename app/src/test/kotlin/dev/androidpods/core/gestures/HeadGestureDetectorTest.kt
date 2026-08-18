// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.gestures

import org.junit.Assert.assertEquals
import org.junit.Test

class HeadGestureDetectorTest {
    @Test
    fun `detects nodding gesture from pitch oscillation`() {
        val detector = HeadGestureDetector(pitchNodThreshold = 15f, yawShakeThreshold = 20f)
        var result = HeadGesture.NONE

        // Simulate upward then downward nodding motion over 500ms
        val baseTime = 1000L
        val pitchSamples = listOf(0f, 10f, 22f, 12f, -15f, 0f)
        for (i in pitchSamples.indices) {
            val gesture = detector.onSample(
                pitch = pitchSamples[i],
                yaw = 1f,
                nowMs = baseTime + (i * 100L),
            )
            if (gesture != HeadGesture.NONE) {
                result = gesture
            }
        }

        assertEquals(HeadGesture.NOD, result)
    }

    @Test
    fun `detects shaking gesture from yaw oscillation`() {
        val detector = HeadGestureDetector(pitchNodThreshold = 15f, yawShakeThreshold = 20f)
        var result = HeadGesture.NONE

        // Simulate left then right head shaking motion over 500ms
        val baseTime = 1000L
        val yawSamples = listOf(0f, 15f, 25f, 5f, -22f, 0f)
        for (i in yawSamples.indices) {
            val gesture = detector.onSample(
                pitch = 1f,
                yaw = yawSamples[i],
                nowMs = baseTime + (i * 100L),
            )
            if (gesture != HeadGesture.NONE) {
                result = gesture
            }
        }

        assertEquals(HeadGesture.SHAKE, result)
    }

    @Test
    fun `returns NONE on small random drift`() {
        val detector = HeadGestureDetector(pitchNodThreshold = 15f, yawShakeThreshold = 20f)
        var result = HeadGesture.NONE

        val baseTime = 1000L
        val samples = listOf(1f, 2f, 3f, 2f, 1f, 0f)
        for (i in samples.indices) {
            val gesture = detector.onSample(
                pitch = samples[i],
                yaw = samples[i],
                nowMs = baseTime + (i * 100L),
            )
            if (gesture != HeadGesture.NONE) {
                result = gesture
            }
        }

        assertEquals(HeadGesture.NONE, result)
    }

    @Test
    fun `returns NONE on static head tilt without oscillation`() {
        val detector = HeadGestureDetector()
        var result = HeadGesture.NONE

        val baseTime = 1000L
        // User holds head tilted down at ~20 degrees
        val samples = listOf(18f, 19f, 21f, 20f, 20f, 19f)
        for (i in samples.indices) {
            val gesture = detector.onSample(
                pitch = samples[i],
                yaw = 0f,
                nowMs = baseTime + (i * 100L),
            )
            if (gesture != HeadGesture.NONE) {
                result = gesture
            }
        }

        assertEquals(HeadGesture.NONE, result)
    }
}
