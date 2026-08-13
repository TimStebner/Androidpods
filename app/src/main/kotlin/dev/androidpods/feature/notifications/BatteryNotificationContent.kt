// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.feature.notifications

import dev.androidpods.core.airpods.BatteryComponentState
import dev.androidpods.core.bluetooth.AirPodsTransport
import dev.androidpods.core.data.AirPodsState

// null means "no notification" (disconnected, or connected with no battery reading yet): the
// observer must cancel rather than skip, or a disconnect leaves a stale ongoing notification
// showing the last-known percentages forever.
data class BatteryNotificationUiState(
    val left: BatteryComponentState,
    val right: BatteryComponentState,
    val case: BatteryComponentState,
)

fun AirPodsState.toBatteryNotificationUiState(): BatteryNotificationUiState? {
    if (connection !is AirPodsTransport.ConnectionState.Connected) return null
    return battery?.let { BatteryNotificationUiState(it.left, it.right, it.case) }
}
