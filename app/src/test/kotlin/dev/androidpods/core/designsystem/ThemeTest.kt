// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.designsystem

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeTest {
    @Test
    fun `animator duration scale of zero is reduced motion`() {
        assertTrue(isReducedMotion(0f))
    }

    @Test
    fun `normal animator duration scale is not reduced motion`() {
        assertFalse(isReducedMotion(1f))
        assertFalse(isReducedMotion(0.5f))
    }
}
