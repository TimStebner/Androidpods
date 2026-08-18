// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.feature.popup

import dev.androidpods.core.airpods.BatteryChargeStatus
import dev.androidpods.core.airpods.BatteryComponentState
import dev.androidpods.core.airpods.BatteryState
import dev.androidpods.core.bluetooth.AirPodsTransport
import dev.androidpods.core.data.AirPodsState
import dev.androidpods.core.data.AppSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AirPodsBatteryPopupTest {

    @Test
    fun `default app settings has battery popup enabled`() {
        val settings = AppSettings()
        assertTrue(settings.batteryPopupEnabled)
        assertTrue(settings.connectionBannerEnabled)
        assertTrue(settings.batteryNotificationEnabled)
    }

    @Test
    fun `airpods state battery values are correctly exposed for popup`() {
        val batteryState = BatteryState(
            left = BatteryComponentState(level = 95, status = BatteryChargeStatus.NOT_CHARGING),
            right = BatteryComponentState(level = 80, status = BatteryChargeStatus.CHARGING),
            case = BatteryComponentState(level = 100, status = BatteryChargeStatus.CHARGING),
        )
        val state = AirPodsState.INITIAL.copy(
            connection = AirPodsTransport.ConnectionState.Connected,
            battery = batteryState,
        )

        assertEquals(95, state.battery?.left?.level)
        assertEquals(80, state.battery?.right?.level)
        assertEquals(100, state.battery?.case?.level)
        assertEquals(BatteryChargeStatus.CHARGING, state.battery?.right?.status)
        assertEquals(BatteryChargeStatus.CHARGING, state.battery?.case?.status)
        assertFalse(state.battery?.left?.status == BatteryChargeStatus.CHARGING)
    }

    @Test
    fun `main activity isForeground default is false and tracks correctly`() {
        dev.androidpods.app.MainActivity.isForeground = false
        assertFalse(dev.androidpods.app.MainActivity.isForeground)
        dev.androidpods.app.MainActivity.isForeground = true
        assertTrue(dev.androidpods.app.MainActivity.isForeground)
        dev.androidpods.app.MainActivity.isForeground = false
        assertFalse(dev.androidpods.app.MainActivity.isForeground)
    }
}
