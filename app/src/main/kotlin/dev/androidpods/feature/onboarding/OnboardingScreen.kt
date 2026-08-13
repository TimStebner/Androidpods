// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.feature.onboarding

import android.app.Activity
import android.content.IntentSender
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.androidpods.app.R
import dev.androidpods.core.bluetooth.AirPodsAssociationManager
import dev.androidpods.core.bluetooth.BluetoothAvailability
import dev.androidpods.core.bluetooth.REQUIRED_BLUETOOTH_PERMISSIONS
import dev.androidpods.core.bluetooth.bluetoothAvailability
import dev.androidpods.core.bluetooth.hasBluetoothPermissions

// Presentation only (§30): drives the CDM/permission flow through core.bluetooth's public entry
// points, never touches BluetoothAdapter/CompanionDeviceManager directly.
@Composable
fun OnboardingScreen(onAssociated: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var permissionsGranted by remember { mutableStateOf(hasBluetoothPermissions(context)) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val bluetoothAvailability by remember { bluetoothAvailability(context) }
        .collectAsState(initial = BluetoothAvailability.Disabled)
    val cancelledMessage = stringResource(R.string.onboarding_pairing_cancelled)

    val associationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) {
            errorMessage = cancelledMessage
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants -> permissionsGranted = grants.values.all { it } }

    Scaffold(modifier = modifier.fillMaxSize()) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.onboarding_title),
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = stringResource(R.string.onboarding_rationale),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
            )
            when {
                bluetoothAvailability is BluetoothAvailability.Unsupported -> {
                    Text(
                        text = stringResource(R.string.onboarding_bluetooth_unsupported),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                bluetoothAvailability is BluetoothAvailability.Disabled -> {
                    Text(
                        text = stringResource(R.string.onboarding_bluetooth_disabled),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                !permissionsGranted -> {
                    Button(onClick = { permissionLauncher.launch(REQUIRED_BLUETOOTH_PERMISSIONS) }) {
                        Text(stringResource(R.string.onboarding_grant_permissions))
                    }
                }

                else -> {
                    Button(onClick = {
                        errorMessage = null
                        AirPodsAssociationManager(context).associate(
                            onPendingConfirmation = { intentSender: IntentSender ->
                                associationLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
                            },
                            onAssociated = { onAssociated() },
                            onFailure = { reason -> errorMessage = reason.toString() },
                        )
                    }) {
                        Text(stringResource(R.string.onboarding_pair_airpods))
                    }
                }
            }
            errorMessage?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }
        }
    }
}
