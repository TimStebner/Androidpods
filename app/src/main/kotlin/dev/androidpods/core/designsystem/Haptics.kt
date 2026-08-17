// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.designsystem

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

/**
 * Tactile haptic feedback interface for high-polish interactions (PROJECT.md §6.3).
 */
interface AppHaptics {
    fun click()
    fun tick()
    fun confirm()
    fun reject()
    fun impact()
}

/**
 * Remembers an [AppHaptics] instance backed by the current Compose [View].
 * Native minSdk 36 allows direct usage of modern [HapticFeedbackConstants].
 */
@Composable
fun rememberAppHaptics(): AppHaptics {
    val view = LocalView.current
    return remember(view) {
        object : AppHaptics {
            override fun click() {
                view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
            }

            override fun tick() {
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            }

            override fun confirm() {
                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            }

            override fun reject() {
                view.performHapticFeedback(HapticFeedbackConstants.REJECT)
            }

            override fun impact() {
                view.performHapticFeedback(HapticFeedbackConstants.GESTURE_START)
            }
        }
    }
}
