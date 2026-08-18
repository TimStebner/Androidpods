// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.feature.popup

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.graphics.drawable.toDrawable
import androidx.compose.runtime.getValue
import dev.androidpods.app.MainActivity
import dev.androidpods.core.data.AirPodsRepositoryProvider
import dev.androidpods.core.data.AppSettingsRepositoryProvider
import dev.androidpods.core.designsystem.AndroidpodsTheme

/**
 * Translucent dialog Activity for presenting the Material 3 Expressive
 * AirPods Battery Pop-up across the system when AirPods connect.
 */
class BatteryPopupActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, 0, 0)
        overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, 0, 0)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        window.setBackgroundDrawable(android.graphics.Color.TRANSPARENT.toDrawable())
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

        setContent {
            val settings by AppSettingsRepositoryProvider.settings.collectAsStateWithLifecycle()
            val state by AirPodsRepositoryProvider.state.collectAsStateWithLifecycle()

            AndroidpodsTheme(
                themeMode = settings.themeMode,
                dynamicColor = settings.dynamicColor,
            ) {
                AirPodsBatteryPopupContent(
                    state = state,
                    onDismiss = {
                        finish()
                    },
                    onOpenApp = {
                        val intent = Intent(this@BatteryPopupActivity, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        }
                        startActivity(intent)
                        finish()
                    },
                )
            }
        }
    }

    override fun finish() {
        super.finish()
        overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, 0, 0)
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, BatteryPopupActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            context.startActivity(intent)
        }
    }
}
