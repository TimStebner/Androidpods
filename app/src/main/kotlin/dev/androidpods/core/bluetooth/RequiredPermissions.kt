// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.bluetooth

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

// The runtime grant set for Tier A/B access (PROJECT.md §23). POST_NOTIFICATIONS is declared in
// the manifest but requested separately once a notification actually exists (M5) -- requesting
// it here would violate §23's "minimum permissions for the currently used feature" rule.
val REQUIRED_BLUETOOTH_PERMISSIONS = arrayOf(
    Manifest.permission.BLUETOOTH_SCAN,
    Manifest.permission.BLUETOOTH_CONNECT,
)

fun hasBluetoothPermissions(context: Context): Boolean =
    REQUIRED_BLUETOOTH_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
