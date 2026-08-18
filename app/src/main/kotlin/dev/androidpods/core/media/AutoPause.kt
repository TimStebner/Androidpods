// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.media

import android.content.Context
import android.media.AudioManager
import android.util.Log
import android.view.KeyEvent
import androidx.core.content.getSystemService
import dev.androidpods.core.airpods.EarDetectionState
import dev.androidpods.core.bluetooth.AirPodsTransport
import dev.androidpods.core.data.AirPodsState
import dev.androidpods.core.data.AppSettingsRepositoryProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

private const val TAG = "AutoPause"

// PROJECT.md §16/§23: AudioManager.dispatchMediaKeyEvent() sends pause/play signals to the active
// media session owner (e.g. Spotify, YouTube Music).
fun observeAutoPause(context: Context, states: Flow<AirPodsState>, scope: CoroutineScope) {
    val audioManager = context.getSystemService<AudioManager>() ?: return
    var previous: EarDetectionState? = null
    var autoPausedByUs = false

    states
        .distinctUntilChangedBy { it.connection to it.earDetection }
        .onEach { state ->
            if (state.connection !is AirPodsTransport.ConnectionState.Connected) {
                previous = null
                autoPausedByUs = false
                return@onEach
            }
            val current = state.earDetection
            if (current != null) {
                val settings = AppSettingsRepositoryProvider.settings.value
                if (settings.autoPauseEnabled && AutoPauseDecider.shouldPause(previous, current)) {
                    Log.d(TAG, "Auto-pausing playback (previous=$previous, current=$current)")
                    audioManager.dispatchPauseKeyEvent()
                    autoPausedByUs = true
                } else if (settings.autoResumeEnabled && autoPausedByUs && AutoPauseDecider.shouldResume(previous, current)) {
                    Log.d(TAG, "Auto-resuming playback (previous=$previous, current=$current)")
                    audioManager.dispatchPlayKeyEvent()
                    autoPausedByUs = false
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

private fun AudioManager.dispatchPlayKeyEvent() {
    val eventTime = android.os.SystemClock.uptimeMillis()
    dispatchMediaKeyEvent(KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY, 0))
    dispatchMediaKeyEvent(KeyEvent(eventTime, eventTime, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY, 0))
}
