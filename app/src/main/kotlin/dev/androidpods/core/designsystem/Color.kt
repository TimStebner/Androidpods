// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.designsystem

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Signature Material 3 Expressive Color Palette for Androidpods (PROJECT.md §6.2).
 *
 * Designed with deep obsidian surfaces, rich container elevation layers, high-contrast
 * readable typography (WCAG AAA), and vibrant electric teal & cyan accents.
 */
internal val AndroidpodsLightColorScheme = lightColorScheme(
    primary = Color(0xFF0284C7), // Vibrant electric sky blue
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = Color(0xFF4F46E5), // Indigo
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFEEF2FF),
    onSecondaryContainer = Color(0xFF3730A3),
    tertiary = Color(0xFF0D9488), // Teal
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFCCFBF1),
    onTertiaryContainer = Color(0xFF115E59),
    background = Color(0xFFF1F5F9), // Crisp light slate canvas (distinct from white cards)
    onBackground = Color(0xFF0F172A), // Deep high-contrast slate
    surface = Color(0xFFF1F5F9),
    onSurface = Color(0xFF0F172A),
    surfaceDim = Color(0xFFE2E8F0),
    surfaceBright = Color(0xFFFFFFFF),
    surfaceContainerLowest = Color(0xFFFFFFFF), // Pure white
    surfaceContainerLow = Color(0xFFFFFFFF), // Pure white
    surfaceContainer = Color(0xFFFFFFFF), // Crisp pure white cards (pops out against slate canvas)
    surfaceContainerHigh = Color(0xFFE2E8F0), // Distinct inner pillars and tonal sections
    surfaceContainerHighest = Color(0xFFCBD5E1),
    onSurfaceVariant = Color(0xFF475569), // Slate-600 readable subtitle
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0),
    error = Color(0xFFDC2626),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF991B1B),
)

internal val AndroidpodsDarkColorScheme = darkColorScheme(
    primary = Color(0xFF38BDF8), // Electric Cyan - crisp, bright & high contrast
    onPrimary = Color(0xFF002238),
    primaryContainer = Color(0xFF075985),
    onPrimaryContainer = Color(0xFFE0F2FE),
    secondary = Color(0xFF818CF8), // Indigo accent
    onSecondary = Color(0xFF1E1B4B),
    secondaryContainer = Color(0xFF312E81),
    onSecondaryContainer = Color(0xFFE0E7FF),
    tertiary = Color(0xFF2DD4BF), // Emerald / Mint accent
    onTertiary = Color(0xFF00382E),
    tertiaryContainer = Color(0xFF134E4A),
    onTertiaryContainer = Color(0xFFCCFBF1),
    background = Color(0xFF0A0D14), // Deep Obsidian / OLED Dark (clean, no murky brown)
    onBackground = Color(0xFFF8FAFC), // Pure Crisp White Text (100% readable)
    surface = Color(0xFF0A0D14),
    onSurface = Color(0xFFF8FAFC), // Pure Crisp White Text
    surfaceDim = Color(0xFF06080D),
    surfaceBright = Color(0xFF1E2433),
    surfaceContainerLowest = Color(0xFF06080D),
    surfaceContainerLow = Color(0xFF10141F),
    surfaceContainer = Color(0xFF161B28), // Distinct elevated card container
    surfaceContainerHigh = Color(0xFF1E2536), // Pillar and button containers
    surfaceContainerHighest = Color(0xFF283247),
    onSurfaceVariant = Color(0xFF94A3B8), // Readable Slate-400 subtitle text
    outline = Color(0xFF475569),
    outlineVariant = Color(0xFF334155),
    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFECACA),
)
