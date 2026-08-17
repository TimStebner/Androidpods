// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.media

import dev.androidpods.core.airpods.EarDetectionState

// Auto-Pause & Auto-Resume decider: triggers pause on removal, resume on re-insertion.
object AutoPauseDecider {
    fun shouldPause(previous: EarDetectionState?, current: EarDetectionState): Boolean {
        if (previous == null) return false
        val leftRemoved = previous.leftInEar && !current.leftInEar
        val rightRemoved = previous.rightInEar && !current.rightInEar
        return leftRemoved || rightRemoved
    }

    fun shouldResume(previous: EarDetectionState?, current: EarDetectionState): Boolean {
        if (previous == null) return false
        val leftInserted = !previous.leftInEar && current.leftInEar
        val rightInserted = !previous.rightInEar && current.rightInEar
        return leftInserted || rightInserted
    }
}
