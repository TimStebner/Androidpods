// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.feature.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.androidpods.app.R
import dev.androidpods.core.designsystem.androidpodsSpatialSpec
import dev.androidpods.feature.controls.ControlsScreen
import dev.androidpods.feature.home.HomeScreen
import dev.androidpods.feature.settings.SettingsScreen
import dev.androidpods.feature.widgets.WidgetsScreen

enum class AppDestination(
    val labelRes: Int,
    val icon: ImageVector,
) {
    HOME(R.string.nav_home, Icons.Default.Home),
    CONTROLS(R.string.nav_controls, Icons.Default.Tune),
    WIDGETS(R.string.nav_widgets, Icons.Default.Widgets),
    SETTINGS(R.string.nav_settings, Icons.Default.Settings),
}

@Composable
fun AppScaffold(modifier: Modifier = Modifier) {
    var currentDestination by remember { mutableStateOf(AppDestination.HOME) }
    val haptic = LocalHapticFeedback.current
    val slideSpec = androidpodsSpatialSpec<androidx.compose.ui.unit.IntOffset>()
    val fadeSpec = androidpodsSpatialSpec<Float>()

    Box(modifier = modifier.fillMaxSize()) {
        // 1. Edge-to-Edge Animated Screen Content
        AnimatedContent(
            targetState = currentDestination,
            transitionSpec = {
                val isForward = targetState.ordinal > initialState.ordinal
                (fadeIn(animationSpec = fadeSpec) + slideInHorizontally(animationSpec = slideSpec) { width -> if (isForward) width / 6 else -width / 6 })
                    .togetherWith(fadeOut(animationSpec = fadeSpec) + slideOutHorizontally(animationSpec = slideSpec) { width -> if (isForward) -width / 6 else width / 6 })
            },
            label = "tab-screen-transition",
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds(),
        ) { destination ->
            when (destination) {
                AppDestination.HOME -> HomeScreen(modifier = Modifier.fillMaxSize())
                AppDestination.CONTROLS -> ControlsScreen(modifier = Modifier.fillMaxSize())
                AppDestination.WIDGETS -> WidgetsScreen(modifier = Modifier.fillMaxSize())
                AppDestination.SETTINGS -> SettingsScreen(modifier = Modifier.fillMaxSize())
            }
        }

        // 2. Status Bar Scrim (Seamless Edge-to-Edge Scrim)
        StatusBarScrim(modifier = Modifier.align(Alignment.TopCenter))

        // 3. Floating Navigation Pill (Material 3 Expressive)
        FloatingNavigationPill(
            currentDestination = currentDestination,
            onDestinationSelected = { dest ->
                if (dest != currentDestination) {
                    haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                    currentDestination = dest
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 16.dp),
        )
    }
}

@Composable
private fun StatusBarScrim(modifier: Modifier = Modifier) {
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(statusBarHeight + 8.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.70f),
                        Color.Transparent,
                    ),
                ),
            ),
    )
}

@Composable
private fun FloatingNavigationPill(
    currentDestination: AppDestination,
    onDestinationSelected: (AppDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.padding(horizontal = 16.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 6.dp,
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier = Modifier
                .animateContentSize(animationSpec = androidpodsSpatialSpec())
                .padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppDestination.entries.forEach { destination ->
                val selected = destination == currentDestination
                FloatingNavPillItem(
                    destination = destination,
                    selected = selected,
                    onClick = { onDestinationSelected(destination) },
                )
            }
        }
    }
}

@Composable
private fun FloatingNavPillItem(
    destination: AppDestination,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val containerColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
        animationSpec = androidpodsSpatialSpec(),
        label = "pill-container-color",
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = androidpodsSpatialSpec(),
        label = "pill-content-color",
    )

    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = containerColor,
        contentColor = contentColor,
        interactionSource = remember { MutableInteractionSource() },
    ) {
        Row(
            modifier = Modifier
                .animateContentSize(animationSpec = androidpodsSpatialSpec())
                .padding(horizontal = if (selected) 14.dp else 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = destination.icon,
                contentDescription = stringResource(destination.labelRes),
                modifier = Modifier.size(22.dp),
            )
            AnimatedVisibility(
                visible = selected,
                enter = fadeIn(animationSpec = androidpodsSpatialSpec()) +
                    expandHorizontally(animationSpec = androidpodsSpatialSpec(), expandFrom = Alignment.Start),
                exit = fadeOut(animationSpec = androidpodsSpatialSpec()) +
                    shrinkHorizontally(animationSpec = androidpodsSpatialSpec(), shrinkTowards = Alignment.Start),
            ) {
                Text(
                    text = stringResource(destination.labelRes),
                    style = MaterialTheme.typography.labelLarge,
                    color = contentColor,
                    modifier = Modifier.padding(start = 8.dp),
                    maxLines = 1,
                )
            }
        }
    }
}
