// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.feature.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.getSystemService
import dev.androidpods.app.R

private const val CHANNEL_ID = "battery_status"
private const val NOTIFICATION_ID = 1

// IMPORTANCE_LOW: battery state changes fire often (§13.4 event-driven), IMPORTANCE_DEFAULT
// would buzz the phone on every one.
fun ensureBatteryNotificationChannel(context: Context) {
    val channel = NotificationChannel(
        CHANNEL_ID,
        context.getString(R.string.notification_channel_battery),
        NotificationManager.IMPORTANCE_LOW,
    )
    context.getSystemService<NotificationManager>()?.createNotificationChannel(channel)
}

// Presentation only (§30): renders BatteryNotificationUiState, never touches Bluetooth or the
// repository beyond the state it's handed. `state == null` (disconnected, or POST_NOTIFICATIONS
// not granted) must cancel, not skip -- otherwise a disconnect leaves a stale notification
// showing the last-known percentages forever. No setOngoing(true): since Android 14, that's only
// honored for a foreground-service or media/call notification, and this app deliberately runs
// neither (§14) -- a user-dismissible notification is the correct shape here, not a shortcut.
fun updateBatteryNotification(context: Context, state: BatteryNotificationUiState?) {
    val manager = NotificationManagerCompat.from(context)
    val granted = ActivityCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS,
    ) == PackageManager.PERMISSION_GRANTED
    if (state == null || !granted) {
        manager.cancel(NOTIFICATION_ID)
        return
    }
    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_notification)
        .setContentTitle(context.getString(R.string.notification_battery_title))
        .setContentText(
            context.getString(
                R.string.notification_battery_text,
                state.left.level,
                state.right.level,
                state.case.level,
            ),
        )
        .setOnlyAlertOnce(true)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .build()
    manager.notify(NOTIFICATION_ID, notification)
}
