// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.designsystem

import androidx.graphics.shapes.Morph
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MorphingShapeTest {
    @Test
    fun `onboarding morph polygon list contains multiple shapes`() {
        assertTrue(OnboardingMorphPolygons.size >= 2)
    }

    @Test
    fun `morph converts to valid cubics at various progress points`() {
        val shapeA = OnboardingMorphPolygons[0]
        val shapeB = OnboardingMorphPolygons[1]
        val morph = Morph(shapeA, shapeB)

        val cubics0 = morph.asCubics(0f)
        val cubicsHalf = morph.asCubics(0.5f)
        val cubics1 = morph.asCubics(1f)

        assertFalse("Cubics at progress 0 should not be empty", cubics0.isEmpty())
        assertFalse("Cubics at progress 0.5 should not be empty", cubicsHalf.isEmpty())
        assertFalse("Cubics at progress 1.0 should not be empty", cubics1.isEmpty())
    }

    @Test
    fun `morph bounds calculate non-zero dimensions`() {
        val shapeA = OnboardingMorphPolygons[0]
        val shapeB = OnboardingMorphPolygons[1]
        val morph = Morph(shapeA, shapeB)

        val bounds = morph.calculateBoundsRect()
        assertNotNull(bounds)
        assertTrue("Bounds width must be positive", bounds.width > 0f)
        assertTrue("Bounds height must be positive", bounds.height > 0f)
    }
}
