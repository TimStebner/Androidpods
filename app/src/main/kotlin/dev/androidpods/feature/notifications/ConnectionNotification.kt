// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.feature.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.getSystemService
import dev.androidpods.app.MainActivity
import dev.androidpods.app.R

private const val CHANNEL_ID = "connection_status"
private const val NOTIFICATION_ID = 2
private const val AUTO_DISMISS_MS = 8000L

// IMPORTANCE_HIGH (unlike battery_status' IMPORTANCE_LOW): this is a one-shot, low-frequency
// event, so a heads-up banner is the "pop-up" PROJECT.md §19 asks for without an overlay
// permission -- it just posts through the normal notification surface.
fun ensureConnectionNotificationChannel(context: Context) {
    val channel = NotificationChannel(
        CHANNEL_ID,
        context.getString(R.string.notification_channel_connection),
        NotificationManager.IMPORTANCE_HIGH,
    )
    context.getSystemService<NotificationManager>()?.createNotificationChannel(channel)
}

// Presentation only (§30). Transient by design: setAutoCancel + setTimeoutAfter, unlike the
// persistent battery notification -- this is a one-time "you just connected" signal, not
// ongoing state.
fun postConnectionNotification(context: Context) {
    val granted = ActivityCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS,
    ) == PackageManager.PERMISSION_GRANTED
    if (!granted) return
    val contentIntent = PendingIntent.getActivity(
        context,
        0,
        Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_notification)
        .setContentTitle(context.getString(R.string.home_connected_title))
        .setContentIntent(contentIntent)
        .setAutoCancel(true)
        .setTimeoutAfter(AUTO_DISMISS_MS)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .build()
    NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
}
