// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import dev.androidpods.core.bluetooth.ProtocolLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

enum class ChimeTarget {
    LEFT,
    RIGHT,
    BOTH,
}

class ChimePlayer(
    private val context: Context? = null,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _activeTarget = MutableStateFlow<ChimeTarget?>(null)
    val activeTarget: StateFlow<ChimeTarget?> = _activeTarget.asStateFlow()

    private var audioTrack: AudioTrack? = null
    private var playbackJob: Job? = null

    fun play(target: ChimeTarget) {
        stop()

        _activeTarget.value = target
        _isPlaying.value = true

        playbackJob = scope.launch(Dispatchers.IO) {
            try {
                val sampleRate = 44100
                val (leftVol, rightVol) = when (target) {
                    ChimeTarget.LEFT -> 1.0 to 0.0
                    ChimeTarget.RIGHT -> 0.0 to 1.0
                    ChimeTarget.BOTH -> 1.0 to 1.0
                }
                val pcmData = generateFindMyChirpPcm(sampleRate, leftGain = leftVol, rightGain = rightVol)

                val minBufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT,
                ).coerceAtLeast(pcmData.size * 2)

                val attributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()

                val format = AudioFormat.Builder()
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .build()

                val track = AudioTrack(
                    attributes,
                    format,
                    minBufferSize,
                    AudioTrack.MODE_STREAM,
                    AudioManager.AUDIO_SESSION_ID_GENERATE,
                )
                audioTrack = track

                // Route audio directly to connected Bluetooth headphones/AirPods
                context?.let {
                    val audioManager = it.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                    val btDevice = audioManager?.getDevices(AudioManager.GET_DEVICES_OUTPUTS)?.firstOrNull {
                        it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                        it.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO ||
                        it.type == AudioDeviceInfo.TYPE_BLE_HEADSET ||
                        it.type == AudioDeviceInfo.TYPE_HEARING_AID
                    }
                    if (btDevice != null) {
                        track.preferredDevice = btDevice
                    }
                }

                track.play()

                ProtocolLogging.rawPacket("ChimePlayer") { "Playing locating chime on target: $target (L: $leftVol, R: $rightVol)" }

                while (isActive && _isPlaying.value) {
                    track.write(pcmData, 0, pcmData.size)
                    delay(200)
                }
            } catch (e: Exception) {
                ProtocolLogging.rawPacket("ChimePlayer") { "Chime playback failed: ${e.message}" }
            } finally {
                cleanupAudioTrack()
            }
        }
    }

    fun stop() {
        _isPlaying.value = false
        _activeTarget.value = null
        playbackJob?.cancel()
        playbackJob = null
        cleanupAudioTrack()
    }

    private fun cleanupAudioTrack() {
        try {
            audioTrack?.let {
                if (it.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    it.stop()
                }
                it.release()
            }
        } catch (_: Exception) {
        } finally {
            audioTrack = null
        }
    }

    companion object {
        /**
         * Generates the authentic Apple Find My ascending chirp acoustic pulse cycle.
         * Cycle contains two 3-pulse burst clusters sweeping from 2500Hz to 5500Hz.
         */
        fun generateFindMyChirpPcm(
            sampleRate: Int = 44100,
            leftGain: Double = 1.0,
            rightGain: Double = 1.0,
        ): ShortArray {
            val cycleDurationSec = 1.6
            val totalSamples = (sampleRate * cycleDurationSec).toInt()
            val buffer = ShortArray(totalSamples * 2) // Stereo (L, R interleaved)

            fun writeSweep(startSample: Int, durationSec: Double, startFreq: Double, endFreq: Double, gain: Double) {
                val numSamples = (sampleRate * durationSec).toInt()
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    val progress = i.toDouble() / numSamples
                    val currentFreq = startFreq + (endFreq - startFreq) * progress
                    // Apply smooth Hann envelope to avoid clicks
                    val envelope = sin(PI * progress)
                    val baseWave = sin(2.0 * PI * currentFreq * t) * envelope * gain * Short.MAX_VALUE

                    val leftVal = (baseWave * leftGain).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                    val rightVal = (baseWave * rightGain).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()

                    val bufferIndex = (startSample + i) * 2
                    if (bufferIndex + 1 < buffer.size) {
                        buffer[bufferIndex] = leftVal      // Left channel
                        buffer[bufferIndex + 1] = rightVal // Right channel
                    }
                }
            }

            val pulseDuration = 0.07 // 70ms pulse
            val pulseGap = 0.035     // 35ms silence between pulses

            // Cluster 1 (3 rising pulses)
            var currentPos = (sampleRate * 0.05).toInt()
            for (p in 0..2) {
                writeSweep(
                    startSample = currentPos,
                    durationSec = pulseDuration,
                    startFreq = 2600.0 + p * 300.0,
                    endFreq = 4800.0 + p * 300.0,
                    gain = 0.75 + p * 0.1,
                )
                currentPos += ((pulseDuration + pulseGap) * sampleRate).toInt()
            }

            // Cluster 2 (3 louder rising pulses)
            currentPos += (sampleRate * 0.18).toInt()
            for (p in 0..2) {
                writeSweep(
                    startSample = currentPos,
                    durationSec = pulseDuration,
                    startFreq = 2800.0 + p * 350.0,
                    endFreq = 5400.0 + p * 350.0,
                    gain = 0.9 + p * 0.05,
                )
                currentPos += ((pulseDuration + pulseGap) * sampleRate).toInt()
            }

            return buffer
        }
    }
}
