// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods

import org.junit.Assert.assertEquals
import org.junit.Test

// Wires up src/test/kotlin as an actual JVM test source set before M2 needs it for
// decoder-fixture TDD (PROJECT.md §28) -- if this doesn't run, nothing else will either.
class SmokeTest {
    @Test
    fun `test source set is wired`() {
        assertEquals(4, 2 + 2)
    }
}
