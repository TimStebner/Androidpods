// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.feature.onboarding

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.IntentSender
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material.icons.outlined.BluetoothDisabled
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.androidpods.app.R
import dev.androidpods.core.bluetooth.AirPodsAssociationManager
import dev.androidpods.core.bluetooth.BluetoothAvailability
import dev.androidpods.core.bluetooth.REQUIRED_BLUETOOTH_PERMISSIONS
import dev.androidpods.core.bluetooth.bluetoothAvailability
import dev.androidpods.core.bluetooth.hasBluetoothPermissions
import dev.androidpods.core.designsystem.AirPodsIllustration
import dev.androidpods.core.designsystem.AndroidpodsTheme
import dev.androidpods.core.designsystem.MorphingShapeHero
import dev.androidpods.core.designsystem.StatusBarScrim
import dev.androidpods.core.designsystem.androidpodsSpatialSpec
import dev.androidpods.core.designsystem.rememberAppHaptics

/**
 * Material 3 Expressive Onboarding Screen (PROJECT.md §6, §11, §30).
 *
 * Drives the CDM/permission pairing flow with bold typography, continuous shape morphing,
 * vibrant tonal containers, and crisp tactile haptic feedback.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(onAssociated: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val haptics = rememberAppHaptics()
    val scrollState = rememberScrollState()

    var permissionsGranted by remember { mutableStateOf(hasBluetoothPermissions(context)) }
    var isPairing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val bluetoothAvailability by remember { bluetoothAvailability(context) }
        .collectAsStateWithLifecycle(initialValue = BluetoothAvailability.Disabled)
    val cancelledMessage = stringResource(R.string.onboarding_pairing_cancelled)

    val associationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        isPairing = false
        if (result.resultCode != Activity.RESULT_OK) {
            haptics.reject()
            errorMessage = cancelledMessage
        } else {
            haptics.confirm()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        val allGranted = grants.values.all { it }
        permissionsGranted = allGranted
        if (allGranted) {
            haptics.confirm()
        } else {
            haptics.reject()
        }
    }

    val bluetoothEnableLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        // State updates reactively via bluetoothAvailability StateFlow
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                // 1. Centerpiece: Expressive Shape Morphing Hero with AirPods Illustration
                MorphingShapeHero(
                    sizeDp = 220.dp,
                    haptics = haptics,
                ) {
                    AirPodsIllustration(
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        pulsing = isPairing,
                        modifier = Modifier.size(136.dp),
                    )
                }

                Text(
                    text = stringResource(R.string.onboarding_tap_morph_hint),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 8.dp),
                )

                Spacer(modifier = Modifier.height(24.dp))

            // 2. Bold Headline & Subtitle
            Text(
                text = stringResource(R.string.onboarding_headline),
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.onboarding_tagline),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp),
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Expressive Feature Showcase Highlights
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                maxItemsInEachRow = 2,
            ) {
                FeaturePill(
                    icon = Icons.Outlined.GraphicEq,
                    title = stringResource(R.string.onboarding_feature_spatial),
                    subtitle = stringResource(R.string.onboarding_feature_spatial_desc),
                    modifier = Modifier.weight(1f),
                )
                FeaturePill(
                    icon = Icons.Outlined.BatteryChargingFull,
                    title = stringResource(R.string.onboarding_feature_battery),
                    subtitle = stringResource(R.string.onboarding_feature_battery_desc),
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                maxItemsInEachRow = 2,
            ) {
                FeaturePill(
                    icon = Icons.Outlined.Call,
                    title = stringResource(R.string.onboarding_feature_gestures),
                    subtitle = stringResource(R.string.onboarding_feature_gestures_desc),
                    modifier = Modifier.weight(1f),
                )
                FeaturePill(
                    icon = Icons.AutoMirrored.Outlined.VolumeUp,
                    title = stringResource(R.string.onboarding_feature_findmy),
                    subtitle = stringResource(R.string.onboarding_feature_findmy_desc),
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 4. Action Dock & State Handling
            when {
                bluetoothAvailability is BluetoothAvailability.Unsupported -> {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.BluetoothDisabled,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(28.dp),
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = stringResource(R.string.onboarding_bluetooth_unsupported),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                            )
                        }
                    }
                }

                bluetoothAvailability is BluetoothAvailability.Disabled -> {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = stringResource(R.string.onboarding_bluetooth_disabled),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            FilledTonalButton(
                                onClick = {
                                    haptics.click()
                                    try {
                                        bluetoothEnableLauncher.launch(
                                            Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                                        )
                                    } catch (_: Exception) {
                                        bluetoothEnableLauncher.launch(
                                            Intent(Settings.ACTION_BLUETOOTH_SETTINGS),
                                        )
                                    }
                                },
                                shape = CircleShape,
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Bluetooth,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.onboarding_enable_bluetooth))
                            }
                        }
                    }
                }

                !permissionsGranted -> {
                    Button(
                        onClick = {
                            haptics.click()
                            permissionLauncher.launch(REQUIRED_BLUETOOTH_PERMISSIONS)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Bluetooth,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.onboarding_grant_permissions),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        )
                    }
                }

                else -> {
                    Button(
                        onClick = {
                            haptics.confirm()
                            errorMessage = null
                            isPairing = true
                            AirPodsAssociationManager(context).associate(
                                onPendingConfirmation = { intentSender: IntentSender ->
                                    associationLauncher.launch(
                                        IntentSenderRequest.Builder(intentSender).build(),
                                    )
                                },
                                onAssociated = {
                                    isPairing = false
                                    haptics.confirm()
                                    onAssociated()
                                },
                                onFailure = { reason ->
                                    isPairing = false
                                    haptics.reject()
                                    errorMessage = reason.toString()
                                },
                            )
                        },
                        enabled = !isPairing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    ) {
                        if (isPairing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.5.dp,
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = stringResource(R.string.home_connecting_title),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.Bluetooth,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = stringResource(R.string.onboarding_pair_airpods),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = errorMessage != null,
                enter = fadeIn() + scaleIn(animationSpec = androidpodsSpatialSpec()),
                exit = fadeOut() + scaleOut(animationSpec = androidpodsSpatialSpec()),
            ) {
                errorMessage?.let { msg ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                    ) {
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        StatusBarScrim(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
        )
    }
}
}

/**
 * An Expressive tonal card pill presenting an individual AirPods capability.
 */
@Composable
private fun FeaturePill(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(20.dp),
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = MaterialTheme.typography.bodySmall.fontSize * 1.25f,
            )
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Composable
private fun OnboardingScreenPreview() {
    AndroidpodsTheme(darkTheme = false, dynamicColor = false) {
        OnboardingScreen(onAssociated = {})
    }
}

@Preview(name = "Dark Mode", showBackground = true)
@Composable
private fun OnboardingScreenDarkPreview() {
    AndroidpodsTheme(darkTheme = true, dynamicColor = false) {
        OnboardingScreen(onAssociated = {})
    }
}
