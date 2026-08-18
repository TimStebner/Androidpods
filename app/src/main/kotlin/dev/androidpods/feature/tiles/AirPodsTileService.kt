// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.feature.tiles

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import dev.androidpods.app.MainActivity
import dev.androidpods.app.R
import dev.androidpods.core.bluetooth.AirPodsTransport
import dev.androidpods.core.data.AirPodsRepositoryProvider
import dev.androidpods.core.data.AirPodsState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

internal fun AirPodsState.hasSameTileContentAs(other: AirPodsState): Boolean =
    (connection is AirPodsTransport.ConnectionState.Connected) ==
        (other.connection is AirPodsTransport.ConnectionState.Connected) &&
        battery == other.battery &&
        capabilities.modelName == other.capabilities.modelName

class AirPodsTileService : TileService() {
    private var scope: CoroutineScope? = null

    override fun onStartListening() {
        super.onStartListening()
        scope?.cancel()
        val newScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        scope = newScope

        newScope.launch {
            AirPodsRepositoryProvider.state
                .distinctUntilChanged { previous, next -> previous.hasSameTileContentAs(next) }
                .collectLatest { state ->
                    updateTileState(state)
                }
        }
    }

    override fun onStopListening() {
        super.onStopListening()
        scope?.cancel()
        scope = null
    }

    override fun onClick() {
        super.onClick()
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        startActivityAndCollapse(pendingIntent)
    }

    private fun updateTileState(state: AirPodsState) {
        val tile = qsTile ?: return
        val isConnected = state.connection is AirPodsTransport.ConnectionState.Connected

        if (isConnected) {
            tile.state = Tile.STATE_ACTIVE
            tile.label = state.capabilities.modelName.ifEmpty { getString(R.string.app_name) }

            val battery = state.battery
            if (battery != null) {
                val left = "L ${battery.left.level}%"
                val right = "R ${battery.right.level}%"
                val case = "C ${battery.case.level}%"
                tile.subtitle = "$left · $right · $case"
            } else {
                tile.subtitle = getString(R.string.tile_connected_no_battery)
            }
            tile.icon = Icon.createWithResource(this, R.drawable.ic_airpods_case)
        } else {
            tile.state = Tile.STATE_INACTIVE
            tile.label = getString(R.string.app_name)
            tile.subtitle = getString(R.string.tile_disconnected)
            tile.icon = Icon.createWithResource(this, R.drawable.ic_airpods_case)
        }

        tile.updateTile()
    }

    companion object {
        fun requestUpdate(context: Context) {
            runCatching {
                requestListeningState(
                    context.applicationContext,
                    ComponentName(context.applicationContext, AirPodsTileService::class.java),
                )
            }
        }
    }
}

fun observeTileUpdates(
    context: Context,
    stateFlow: StateFlow<AirPodsState>,
    scope: CoroutineScope,
) {
    scope.launch {
        stateFlow
            .distinctUntilChanged { previous, next -> previous.hasSameTileContentAs(next) }
            .collectLatest {
                AirPodsTileService.requestUpdate(context)
            }
    }
}
