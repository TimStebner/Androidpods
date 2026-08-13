// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.feature.widgets

import dev.androidpods.core.airpods.BatteryComponentState
import dev.androidpods.core.data.AirPodsState

// UI renders capabilities, it does not infer them (PROJECT.md §9): this is the only decision the
// widget makes, and it's pure so it's testable without a Glance session (same precedent as
// AutoPauseDecider vs. the framework-bound observeAutoPause).
sealed interface BatteryWidgetUiState {
    data object NoData : BatteryWidgetUiState
    data class Battery(
        val left: BatteryComponentState,
        val right: BatteryComponentState,
        val case: BatteryComponentState,
    ) : BatteryWidgetUiState
}

fun AirPodsState.toBatteryWidgetUiState(): BatteryWidgetUiState =
    battery?.let { BatteryWidgetUiState.Battery(it.left, it.right, it.case) } ?: BatteryWidgetUiState.NoData
