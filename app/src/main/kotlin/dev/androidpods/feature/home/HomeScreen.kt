// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.feature.home

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.androidpods.app.R
import dev.androidpods.core.designsystem.AndroidpodsTheme
import dev.androidpods.core.designsystem.androidpodsSpatialSpec

// Presentation only: no Bluetooth access, no protocol calls here (PROJECT.md §30). Once
// core.airpods/core.data exist, this screen will observe AirPodsState instead of the
// hardcoded "disconnected" placeholder below.
@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    Scaffold(modifier = modifier.fillMaxSize()) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            DisconnectedIndicator()
            Text(
                text = stringResource(R.string.home_disconnected_title),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 24.dp),
            )
            Text(
                text = stringResource(R.string.home_disconnected_body),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

// A minimal expressive-motion proof: the indicator eases its size with the theme's motion
// scheme rather than a hardcoded animation spec, so it visibly responds once AirPodsState
// drives `expanded` instead of this placeholder toggle.
@Composable
private fun DisconnectedIndicator() {
    var expanded by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { expanded = true }
    val size by animateDpAsState(
        targetValue = if (expanded) 96.dp else 88.dp,
        animationSpec = androidpodsSpatialSpec(),
        label = "disconnected-indicator-size",
    )
    Surface(
        modifier = Modifier.size(size),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // ponytail: text glyph placeholder, add material-icons-extended (Bluetooth /
            // BluetoothDisabled) when the design system needs its first real icon set.
            Text(
                text = "✕",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenDisconnectedPreview() {
    AndroidpodsTheme {
        HomeScreen()
    }
}
