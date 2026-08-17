// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.data

import dev.androidpods.core.airpods.HoldDuration
import dev.androidpods.core.airpods.PressSpeed

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val autoPauseEnabled: Boolean = true,
    val autoResumeEnabled: Boolean = true,
    val assistantTriggerEnabled: Boolean = true,
    val pressSpeed: PressSpeed = PressSpeed.DEFAULT,
    val holdDuration: HoldDuration = HoldDuration.DEFAULT,
    val headGesturesEnabled: Boolean = true,
    val connectionBannerEnabled: Boolean = true,
    val batteryNotificationEnabled: Boolean = true,
    val batteryPopupEnabled: Boolean = true,
    val protocolLoggingEnabled: Boolean = false,
)
