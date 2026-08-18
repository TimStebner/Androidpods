// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.gestures

enum class HeadGesture {
    NONE,
    NOD,    // Up-down head movement (Yes / Accept)
    SHAKE,  // Left-right head movement (No / Decline)
}

data class MotionSample(
    val pitch: Float, // Up/down tilt (degrees or rad)
    val yaw: Float,   // Left/right rotation
    val timestampMs: Long = System.currentTimeMillis(),
)

class HeadGestureDetector(
    private val pitchNodThreshold: Float = 14f,
    private val yawShakeThreshold: Float = 16f,
    private val gestureWindowMs: Long = 1200L,
    private val minGestureDurationMs: Long = 200L,
    private val cooldownMs: Long = 2000L,
) {
    private val samples = ArrayDeque<MotionSample>()
    private var lastGestureTimeMs = 0L

    fun onSample(pitch: Float, yaw: Float, nowMs: Long = System.currentTimeMillis()): HeadGesture {
        if (lastGestureTimeMs > 0L && nowMs - lastGestureTimeMs < cooldownMs) {
            return HeadGesture.NONE
        }

        samples.addLast(MotionSample(pitch, yaw, nowMs))
        while (samples.isNotEmpty() && nowMs - samples.first().timestampMs > gestureWindowMs) {
            samples.removeFirst()
        }

        if (samples.size < 5) return HeadGesture.NONE
        val windowDuration = samples.last().timestampMs - samples.first().timestampMs
        if (windowDuration < minGestureDurationMs) return HeadGesture.NONE

        val baselinePitch = samples.first().pitch
        val baselineYaw = samples.first().yaw

        var maxRelPitch = 0f
        var minRelPitch = 0f
        var maxRelYaw = 0f
        var minRelYaw = 0f
        samples.forEach { sample ->
            val relativePitch = sample.pitch - baselinePitch
            val relativeYaw = sample.yaw - baselineYaw
            if (relativePitch > maxRelPitch) maxRelPitch = relativePitch
            if (relativePitch < minRelPitch) minRelPitch = relativePitch
            if (relativeYaw > maxRelYaw) maxRelYaw = relativeYaw
            if (relativeYaw < minRelYaw) minRelYaw = relativeYaw
        }
        val pitchRange = maxRelPitch - minRelPitch
        val yawRange = maxRelYaw - minRelYaw

        // Nod requires bidirectional oscillation (e.g. up then down, or down then up)
        val hasPitchReversal = (maxRelPitch >= pitchNodThreshold * 0.5f && minRelPitch <= -pitchNodThreshold * 0.35f) ||
                (minRelPitch <= -pitchNodThreshold * 0.5f && maxRelPitch >= pitchNodThreshold * 0.35f)

        if (pitchRange >= pitchNodThreshold && hasPitchReversal && pitchRange > yawRange * 1.25f) {
            lastGestureTimeMs = nowMs
            samples.clear()
            return HeadGesture.NOD
        }

        // Shake requires bidirectional oscillation (left then right, or right then left)
        val hasYawReversal = (maxRelYaw >= yawShakeThreshold * 0.5f && minRelYaw <= -yawShakeThreshold * 0.35f) ||
                (minRelYaw <= -yawShakeThreshold * 0.5f && maxRelYaw >= yawShakeThreshold * 0.35f)

        if (yawRange >= yawShakeThreshold && hasYawReversal && yawRange > pitchRange * 1.25f) {
            lastGestureTimeMs = nowMs
            samples.clear()
            return HeadGesture.SHAKE
        }

        return HeadGesture.NONE
    }

    fun reset() {
        samples.clear()
        lastGestureTimeMs = 0L
    }
}
