// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.data

import android.bluetooth.BluetoothDevice
import android.content.Context
import dev.androidpods.core.airpods.AapEvent
import dev.androidpods.core.bluetooth.AapTransport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Process-lifetime home for the one AirPodsState (§10). A manual singleton, not Hilt: this is
// still the only injection site (AirPodsPresenceService) -- the plan defers Hilt to when a second
// one appears (plan file M0 "Verzicht"-Liste).
object AirPodsRepositoryProvider {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var repository: AirPodsRepository? = null

    // Always available, even before any device has connected (INITIAL/Disconnected) -- so UI
    // doesn't need to branch on "no repository yet" vs. "repository says disconnected".
    private val _state = MutableStateFlow(AirPodsState.INITIAL)
    val state: StateFlow<AirPodsState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<AapEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<AapEvent> = _events.asSharedFlow()

    val current: AirPodsRepository? get() = repository

    fun repositoryFor(device: BluetoothDevice, context: Context): AirPodsRepository {
        repository?.let { return it }
        val created = AirPodsRepository(
            AapTransport(device),
            scope,
            DataStoreTierProbeCache(context.applicationContext),
        )
        repository = created
        scope.launch { created.state.collect { _state.value = it } }
        scope.launch { created.events.collect { _events.emit(it) } }
        return created
    }
}
