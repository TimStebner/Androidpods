// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.media

import android.content.Context
import android.media.AudioManager
import android.view.KeyEvent
import androidx.core.content.getSystemService
import dev.androidpods.core.airpods.EarDetectionState
import dev.androidpods.core.data.AirPodsState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

// PROJECT.md §16/§23: AudioManager.dispatchMediaKeyEvent() needs no permission and sends the
// same media-pause signal a physical headset button would -- Androidpods never takes media-
// session focus for itself, whichever app owns the active session handles the key press as usual.
// Auto-resume is explicitly optional per §16 and not implemented.
//
// Not unit-tested: Context/AudioManager/KeyEvent are framework types (same precedent as
// AapTransport). The pause/no-pause decision itself is AutoPauseDecider, tested separately.
fun observeAutoPause(context: Context, states: Flow<AirPodsState>, scope: CoroutineScope) {
    val audioManager = context.getSystemService<AudioManager>() ?: return
    var previous: EarDetectionState? = null
    states
        .onEach { state ->
            val current = state.earDetection
            if (current != null) {
                if (AutoPauseDecider.shouldPause(previous, current)) {
                    audioManager.dispatchPauseKeyEvent()
                }
                previous = current
            }
        }
        .launchIn(scope)
}

private fun AudioManager.dispatchPauseKeyEvent() {
    val eventTime = android.os.SystemClock.uptimeMillis()
    dispatchMediaKeyEvent(KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE, 0))
    dispatchMediaKeyEvent(KeyEvent(eventTime, eventTime, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PAUSE, 0))
}
