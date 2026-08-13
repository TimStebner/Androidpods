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
import dev.androidpods.core.bluetooth.hasBluetoothPermissions
import dev.androidpods.core.bluetooth.hasCompanionAssociation
import dev.androidpods.core.designsystem.AndroidpodsTheme
import dev.androidpods.feature.home.HomeScreen
import dev.androidpods.feature.onboarding.OnboardingScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidpodsTheme {
                var onboardingDone by remember {
                    mutableStateOf(hasBluetoothPermissions(this) && hasCompanionAssociation(this))
                }
                if (onboardingDone) {
                    HomeScreen()
                } else {
                    OnboardingScreen(onAssociated = { onboardingDone = true })
                }
            }
        }
    }
}
