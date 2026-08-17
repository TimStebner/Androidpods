// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.telecom

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.telecom.TelecomManager
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import android.view.KeyEvent
import androidx.annotation.RequiresApi
import dev.androidpods.core.airpods.AapEvent
import dev.androidpods.core.bluetooth.hasCallPermissions
import dev.androidpods.core.data.AirPodsRepositoryProvider
import dev.androidpods.core.data.AppSettingsRepositoryProvider
import dev.androidpods.core.gestures.HeadGesture
import dev.androidpods.core.gestures.HeadGestureDetector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class CallGestureManager(
    private val context: Context,
    private val scope: CoroutineScope,
) {
    private val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
    private val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
    private val detector = HeadGestureDetector()
    private var isRinging = false
    private var isRegistered = false
    private var telephonyCallback: TelephonyCallback? = null
    private var ringingStartTimeMs = 0L

    fun start() {
        if (telephonyManager == null || telecomManager == null) return

        registerIfPossible()

        scope.launch {
            AirPodsRepositoryProvider.events.collect { event ->
                if (isRinging && event is AapEvent.HeadMotion) {
                    val now = System.currentTimeMillis()
                    // Allow 600ms settling time after stream starts before processing gestures
                    if (now - ringingStartTimeMs < 600L) {
                        detector.reset()
                        return@collect
                    }
                    val settings = AppSettingsRepositoryProvider.settings.value
                    if (settings.headGesturesEnabled) {
                        val detected = detector.onSample(event.pitch, event.yaw, event.roll, now)
                        if (detected != HeadGesture.NONE) {
                            handleGesture(detected)
                        }
                    }
                }
            }
        }
    }

    fun registerIfPossible() {
        if (isRegistered || !hasCallPermissions(context)) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            registerTelephonyCallback()
        }
    }

    @SuppressLint("MissingPermission")
    private fun handleGesture(gesture: HeadGesture) {
        if (!hasCallPermissions(context)) return
        when (gesture) {
            HeadGesture.NOD -> {
                var accepted = false
                runCatching {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        telecomManager?.acceptRingingCall()
                        accepted = true
                    }
                }
                if (!accepted) {
                    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                    audioManager?.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HEADSETHOOK))
                    audioManager?.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK))
                }
                onRingingFinished()
            }
            HeadGesture.SHAKE -> {
                runCatching {
                    telecomManager?.endCall()
                }
                onRingingFinished()
            }
            HeadGesture.NONE -> Unit
        }
    }

    private fun onRingingStarted() {
        isRinging = true
        ringingStartTimeMs = System.currentTimeMillis()
        detector.reset()
        scope.launch {
            AirPodsRepositoryProvider.current?.startMotionStream()
        }
    }

    private fun onRingingFinished() {
        isRinging = false
        detector.reset()
        scope.launch {
            AirPodsRepositoryProvider.current?.stopMotionStream()
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("MissingPermission")
    private fun registerTelephonyCallback() {
        if (!hasCallPermissions(context)) return
        val callback = object : TelephonyCallback(), TelephonyCallback.CallStateListener {
            override fun onCallStateChanged(state: Int) {
                val ringing = (state == TelephonyManager.CALL_STATE_RINGING)
                if (ringing && !isRinging) {
                    onRingingStarted()
                } else if (!ringing && isRinging) {
                    onRingingFinished()
                }
            }
        }
        telephonyCallback = callback
        runCatching {
            telephonyManager?.registerTelephonyCallback(context.mainExecutor, callback)
            isRegistered = true
        }
    }
}

object CallGestureManagerProvider {
    private var instance: CallGestureManager? = null

    fun get(context: Context, scope: CoroutineScope): CallGestureManager {
        return instance ?: CallGestureManager(context.applicationContext, scope).also {
            instance = it
            it.start()
        }
    }

    fun registerIfPossible() {
        instance?.registerIfPossible()
    }
}
