// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.designsystem

import androidx.compose.material3.Shapes

// PROJECT.md §6.1's MaterialExpressiveTheme snippet expects shapes supplied explicitly, not left
// to the implicit default. MD3's default corner scale is correct as-is -- §6.4's shape morphing
// work customizes individual components (noise-control selector, connection cards, ...), not this
// global scale, so there is nothing to override yet.
internal val AndroidpodsShapes = Shapes()
