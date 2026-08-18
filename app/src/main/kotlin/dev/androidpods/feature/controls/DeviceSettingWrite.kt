// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.feature.controls

import kotlinx.coroutines.CancellationException

internal suspend fun writeDeviceSetting(
    writeToDevice: suspend () -> Unit,
    persist: suspend () -> Unit,
): Result<Unit> = try {
    writeToDevice()
    persist()
    Result.success(Unit)
} catch (failure: CancellationException) {
    throw failure
} catch (failure: Throwable) {
    Result.failure(failure)
}
