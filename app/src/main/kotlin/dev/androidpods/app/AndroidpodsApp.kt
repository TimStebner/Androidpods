// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.app

import android.app.Application
import dev.androidpods.core.bluetooth.resumeObservingAssociatedDevices

class AndroidpodsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        resumeObservingAssociatedDevices(this)
    }
}
