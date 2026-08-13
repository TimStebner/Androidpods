// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.bluetooth

import android.companion.CompanionDeviceService
import android.companion.DevicePresenceEvent

// The wake trigger for Tier B session work (M2, PROJECT.md §13.4 point 1): the system binds this
// service only while the associated device is nearby or profile-connected, so nothing here polls
// or scans on its own. `onDevicePresenceEvent` already distinguishes BLE proximity (Tier A
// signal) from an ACL/profile connection (Tier B signal) instead of the deprecated single
// onDeviceAppeared/onDeviceDisappeared callbacks.
//
// No state is kept here (§10: one state machine, owned by the repository layer). M2's
// AirPodsRepository will own the nearby/connected state and react to these events directly --
// a process-global singleton here would be a second, unsynchronized copy of that state, and one
// that a dead app process silently resets to false.
class AirPodsPresenceService : CompanionDeviceService() {
    override fun onDevicePresenceEvent(event: DevicePresenceEvent) {
        // M2 wires this into AirPodsRepository. Until then there is nothing to update.
    }
}
