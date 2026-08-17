// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.designsystem

import android.provider.Settings
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

// This file is the isolation boundary for experimental Material 3 Expressive APIs
// (PROJECT.md §6.1): callers only ever see AndroidpodsTheme, never MaterialExpressiveTheme
// or MotionScheme directly, so future API churn stays contained here.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AndroidpodsTheme(
    themeMode: dev.androidpods.core.data.ThemeMode = dev.androidpods.core.data.ThemeMode.SYSTEM,
    darkTheme: Boolean = when (themeMode) {
        dev.androidpods.core.data.ThemeMode.SYSTEM -> isSystemInDarkTheme()
        dev.androidpods.core.data.ThemeMode.LIGHT -> false
        dev.androidpods.core.data.ThemeMode.DARK -> true
    },
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    // minSdk is 36, so dynamic color (introduced in Android 12 / API 31) is always available
    // on a device that can run Androidpods at all -- no version gate needed.
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor -> if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> AndroidpodsFallbackDarkColorScheme
        else -> AndroidpodsFallbackLightColorScheme
    }
    val reduceMotion = remember {
        isReducedMotion(
            Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f),
        )
    }

    CompositionLocalProvider(LocalReduceMotion provides reduceMotion) {
        MaterialExpressiveTheme(
            colorScheme = colorScheme,
            motionScheme = MotionScheme.expressive(),
            shapes = AndroidpodsShapes,
            typography = AndroidpodsTypography,
            content = content,
        )
    }
}

// PROJECT.md §6.3 "Respect system animation accessibility settings" / §29 accessibility: reads
// Android's "Remove animations" developer/accessibility setting once per theme, so every spring
// and infinite animation in the app can gate off it through one seam instead of each call site
// re-reading Settings.Global.
internal val LocalReduceMotion = compositionLocalOf { false }

internal fun isReducedMotion(animatorDurationScale: Float): Boolean = animatorDurationScale <= 0f

// The one sanctioned way for feature code to reach the expressive motion scheme without
// importing ExperimentalMaterial3ExpressiveApi itself (PROJECT.md §6.1 isolation boundary).
// Generic so both Dp (size/position) and Int/Float (battery percentage) animations share it.
// Snaps instantly instead of springing when the user has reduced motion enabled.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun <T> androidpodsSpatialSpec(): FiniteAnimationSpec<T> {
    val expressive = MaterialTheme.motionScheme.defaultSpatialSpec<T>()
    return if (LocalReduceMotion.current) snap() else expressive
}

// For animations that aren't expressed as a FiniteAnimationSpec (e.g. infiniteRepeatable pulses)
// -- same LocalReduceMotion seam as androidpodsSpatialSpec.
@Composable
fun androidpodsReduceMotion(): Boolean = LocalReduceMotion.current
