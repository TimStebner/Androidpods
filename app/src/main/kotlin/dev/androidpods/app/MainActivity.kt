// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.androidpods.core.bluetooth.hasBluetoothPermissions
import dev.androidpods.core.bluetooth.hasCompanionAssociation
import dev.androidpods.core.data.AppSettingsRepositoryProvider
import dev.androidpods.core.designsystem.AndroidpodsTheme
import dev.androidpods.feature.navigation.AppScaffold
import dev.androidpods.feature.onboarding.OnboardingScreen

import dev.androidpods.core.bluetooth.AirPodsTransport
import dev.androidpods.core.bluetooth.resumeObservingAssociatedDevices
import dev.androidpods.core.data.AirPodsRepositoryProvider

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by AppSettingsRepositoryProvider.settings.collectAsStateWithLifecycle()
            AndroidpodsTheme(
                themeMode = settings.themeMode,
                dynamicColor = settings.dynamicColor,
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface,
                ) {
                    var onboardingDone by remember {
                        mutableStateOf(hasBluetoothPermissions(this) && hasCompanionAssociation(this))
                    }
                    if (onboardingDone) {
                        AppScaffold()
                    } else {
                        OnboardingScreen(onAssociated = { onboardingDone = true })
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        dev.androidpods.core.telecom.CallGestureManagerProvider.registerIfPossible()
        val connection = AirPodsRepositoryProvider.state.value.connection
        if (connection != AirPodsTransport.ConnectionState.Connected && connection != AirPodsTransport.ConnectionState.Connecting) {
            resumeObservingAssociatedDevices(this)
        }
    }
}
