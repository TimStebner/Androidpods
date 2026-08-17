// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
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
            val settings by AppSettingsRepositoryProvider.settings.collectAsState()
            AndroidpodsTheme(
                themeMode = settings.themeMode,
                dynamicColor = settings.dynamicColor,
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

    override fun onResume() {
        super.onResume()
        dev.androidpods.core.telecom.CallGestureManagerProvider.registerIfPossible()
        if (AirPodsRepositoryProvider.state.value.connection != AirPodsTransport.ConnectionState.Connected) {
            resumeObservingAssociatedDevices(this)
        }
    }
}
