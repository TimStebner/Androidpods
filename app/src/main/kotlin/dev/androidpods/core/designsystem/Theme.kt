// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.designsystem

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// This file is the isolation boundary for experimental Material 3 Expressive APIs
// (PROJECT.md §6.1): callers only ever see AndroidpodsTheme, never MaterialExpressiveTheme
// or MotionScheme directly, so future API churn stays contained here.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AndroidpodsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    // minSdk is 31, so dynamic color (introduced in Android 12 / API 31) is always available
    // on a device that can run Androidpods at all -- no version gate needed.
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor -> if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> AndroidpodsFallbackDarkColorScheme
        else -> AndroidpodsFallbackLightColorScheme
    }

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        motionScheme = MotionScheme.expressive(),
        content = content,
    )
}

// The one sanctioned way for feature code to reach the expressive motion scheme without
// importing ExperimentalMaterial3ExpressiveApi itself (PROJECT.md §6.1 isolation boundary).
// Generic so both Dp (size/position) and Int/Float (battery percentage) animations share it.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun <T> androidpodsSpatialSpec(): FiniteAnimationSpec<T> =
    MaterialTheme.motionScheme.defaultSpatialSpec()
