// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.feature.widgets

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class BatteryWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = BatteryWidget()
}
