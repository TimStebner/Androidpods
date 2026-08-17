// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.bluetooth

import android.bluetooth.BluetoothManager
import android.companion.AssociationInfo
import android.companion.AssociationRequest
import android.companion.BluetoothDeviceFilter
import android.companion.CompanionDeviceManager
import android.companion.ObservingDevicePresenceRequest
import android.content.Context
import android.content.IntentSender
import android.util.Log
import androidx.core.content.getSystemService
import dev.androidpods.core.data.AirPodsRepositoryProvider
import dev.androidpods.core.data.DataStoreTierProbeCache
import java.util.regex.Pattern
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "AirPodsAssociation"

private val AIRPODS_NAME_PATTERN: Pattern = Pattern.compile("(?i)^AirPods.*")

// A registration failure must not crash association (this fires from a Callback) or app startup
// (resumeObservingAssociatedDevices runs in Application.onCreate) -- worst case is falling back
// to no presence-gated wakeup for that device, not a dead app (§2.6).
private fun CompanionDeviceManager.observePresenceQuietly(associationId: Int) {
    try {
        startObservingDevicePresence(
            ObservingDevicePresenceRequest.Builder()
                .setAssociationId(associationId)
                .build(),
        )
    } catch (e: Exception) {
        Log.w(TAG, "Failed to start observing device presence for association $associationId", e)
    }
}

// Onboarding discovery goes through CompanionDeviceManager instead of a raw BLE scan (PROJECT.md
// §13.3): the system owns the picker UI and the runtime-permission story, Androidpods only
// supplies the AirPods name filter.
class AirPodsAssociationManager(private val context: Context) {
    private val manager = context.getSystemService<CompanionDeviceManager>()
        ?: error("CompanionDeviceManager unavailable -- requires FEATURE_COMPANION_DEVICE_SETUP")

    fun associate(
        onPendingConfirmation: (IntentSender) -> Unit,
        onAssociated: (AssociationInfo) -> Unit,
        onFailure: (CharSequence) -> Unit,
    ) {
        val request = AssociationRequest.Builder()
            .addDeviceFilter(
                BluetoothDeviceFilter.Builder()
                    .setNamePattern(AIRPODS_NAME_PATTERN)
                    .build(),
            )
            .setSingleDevice(true)
            .build()

        manager.associate(
            request,
            context.mainExecutor,
            object : CompanionDeviceManager.Callback() {
                override fun onAssociationPending(intentSender: IntentSender) {
                    onPendingConfirmation(intentSender)
                }

                override fun onAssociationCreated(associationInfo: AssociationInfo) {
                    manager.observePresenceQuietly(associationInfo.id)
                    onAssociated(associationInfo)
                }

                override fun onFailure(error: CharSequence?) {
                    onFailure(error ?: "AirPods pairing failed")
                }
            },
        )
    }
}

fun hasCompanionAssociation(context: Context): Boolean =
    context.getSystemService<CompanionDeviceManager>()?.myAssociations?.isNotEmpty() == true

// Presence observation is registered once, right after association -- call this at process start
// too so a registration that was somehow lost (OS data reset, association predating this code)
// self-heals instead of silently leaving AirPodsPresenceService unbound forever.
//
// In addition to CDM presence observation (which only fires on state transitions), we also
// trigger a proactive connection attempt for all associated devices on startup -- if the AirPods
// are already connected to Android Bluetooth when the app opens, this immediately connects the
// L2CAP session without waiting for a re-pairing event.
fun resumeObservingAssociatedDevices(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)) {
    val manager = context.getSystemService<CompanionDeviceManager>() ?: return
    val bluetoothManager = context.getSystemService<BluetoothManager>()
    val adapter = bluetoothManager?.adapter

    manager.myAssociations.forEach { association ->
        manager.observePresenceQuietly(association.id)
        val mac = association.deviceMacAddress ?: return@forEach
        val device = runCatching {
            adapter?.getRemoteDevice(mac.toString().uppercase())
        }.getOrNull() ?: return@forEach

        scope.launch {
            DataStoreTierProbeCache(context).clear()
            AirPodsRepositoryProvider.repositoryFor(device, context).connect()
        }
    }
}
