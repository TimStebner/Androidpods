// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.designsystem

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Fallback palette used when Dynamic Color is unavailable or disabled (PROJECT.md §6.2).
// Distinct from Material's default baseline so Androidpods keeps its own identity rather
// than looking like an unbranded Material sample app.
//
// ponytail: hand-picked tones approximating a teal/green HCT palette, not generated from a
// seed -- material3 doesn't expose a public seed-to-scheme API (dynamicLightColorScheme's
// generator is Kotlin-internal, see [[material3-expressive-needs-alpha]]). `lightColorScheme()`
// silently defaults every unset slot (background, containers, outline, ...) to Material's
// baseline purple, so *every* role actually rendered on screen must be set explicitly here or
// the "fallback" is indistinguishable from dynamic color. Upgrade path: regenerate from a real
// seed with the Material Theme Builder once the design system needs a second seed color.
internal val AndroidpodsFallbackLightColorScheme = lightColorScheme(
    primary = Color(0xFF2F6B5E),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFA8F2DE),
    onPrimaryContainer = Color(0xFF00201A),
    secondary = Color(0xFF4C6359),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCEE9DA),
    onSecondaryContainer = Color(0xFF092016),
    tertiary = Color(0xFF3E6374),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFC2E8FB),
    onTertiaryContainer = Color(0xFF001F2A),
    background = Color(0xFFF6FBF7),
    onBackground = Color(0xFF171D1A),
    surface = Color(0xFFF6FBF7),
    onSurface = Color(0xFF171D1A),
    outline = Color(0xFF6F7975),
)

internal val AndroidpodsFallbackDarkColorScheme = darkColorScheme(
    primary = Color(0xFF95D5C2),
    onPrimary = Color(0xFF00382C),
    primaryContainer = Color(0xFF155042),
    onPrimaryContainer = Color(0xFFA8F2DE),
    secondary = Color(0xFFB2CCC1),
    onSecondary = Color(0xFF1E352B),
    secondaryContainer = Color(0xFF344B41),
    onSecondaryContainer = Color(0xFFCEE9DA),
    tertiary = Color(0xFFA6CCE0),
    onTertiary = Color(0xFF063544),
    tertiaryContainer = Color(0xFF244C5B),
    onTertiaryContainer = Color(0xFFC2E8FB),
    background = Color(0xFF0E1512),
    onBackground = Color(0xFFDEE4E0),
    surface = Color(0xFF0E1512),
    onSurface = Color(0xFFDEE4E0),
    outline = Color(0xFF889390),
)
