// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.designsystem

import androidx.compose.material3.Typography

// PROJECT.md §6.1's MaterialExpressiveTheme snippet expects typography supplied explicitly.
// Roboto via MD3's default type scale is the correct baseline (not a placeholder) -- customize
// this when the design system needs a distinct type identity.
internal val AndroidpodsTypography = Typography()
