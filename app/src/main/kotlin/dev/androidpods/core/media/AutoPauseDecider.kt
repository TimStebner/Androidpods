// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.media

import dev.androidpods.core.airpods.EarDetectionState

// PROJECT.md §16: pause only on the transition into "both AirPods out of ear" -- taking one bud
// out while the other stays in must not pause (matches Apple's own behavior, and avoids pausing
// playback for someone who just readjusts one earbud).
object AutoPauseDecider {
    fun shouldPause(previous: EarDetectionState?, current: EarDetectionState): Boolean {
        val wasInEar = previous != null && (previous.leftInEar || previous.rightInEar)
        val isNowOutOfEar = !current.leftInEar && !current.rightInEar
        return wasInEar && isNowOutOfEar
    }
}
