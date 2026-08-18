// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.feature.controls

import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeviceSettingWriteTest {
    @Test
    fun `failed device write is reported without persisting the requested value`() = runTest {
        var persistCalls = 0

        val result = writeDeviceSetting(
            writeToDevice = { throw IOException("write failed") },
            persist = { persistCalls++ },
        )

        assertTrue(result.exceptionOrNull() is IOException)
        assertEquals(0, persistCalls)
    }

    @Test
    fun `successful device write is persisted afterwards`() = runTest {
        val order = mutableListOf<String>()

        val result = writeDeviceSetting(
            writeToDevice = { order += "device" },
            persist = { order += "settings" },
        )

        assertTrue(result.isSuccess)
        assertEquals(listOf("device", "settings"), order)
    }

    @Test
    fun `cancellation is propagated without persisting`() = runTest {
        var persistCalls = 0
        var cancellation: CancellationException? = null

        try {
            writeDeviceSetting(
                writeToDevice = { throw CancellationException("screen left") },
                persist = { persistCalls++ },
            )
        } catch (failure: CancellationException) {
            cancellation = failure
        }

        assertTrue(cancellation != null)
        assertEquals(0, persistCalls)
    }
}
