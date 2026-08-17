// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.feature.findmy

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.androidpods.app.R
import dev.androidpods.core.audio.ChimePlayer
import dev.androidpods.core.audio.ChimeTarget
import dev.androidpods.core.bluetooth.AirPodsTransport
import dev.androidpods.core.data.AirPodsState

@Composable
fun FindMyCard(
    state: AirPodsState,
    modifier: Modifier = Modifier,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val chimePlayer = remember { ChimePlayer(context = context.applicationContext) }
    val isPlaying by chimePlayer.isPlaying.collectAsState()
    val activeTarget by chimePlayer.activeTarget.collectAsState()
    var selectedTarget by remember { mutableStateOf(ChimeTarget.BOTH) }

    val isConnected = state.connection is AirPodsTransport.ConnectionState.Connected
    val supportsCaseSpeaker = state.capabilities.supportsCaseSpeaker

    DisposableEffect(Unit) {
        onDispose {
            chimePlayer.stop()
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Text(
                        text = stringResource(R.string.findmy_title),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = stringResource(R.string.findmy_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }

                // Audio Pulsing Sonar Graphic when playing
                if (isPlaying) {
                    PulsingSonarIcon(modifier = Modifier.size(44.dp))
                } else {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.size(44.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
                }
            }

            // Target selector chips: Left, Both, Right (and Case Speaker if supported)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Select Target",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = selectedTarget == ChimeTarget.LEFT,
                        onClick = {
                            selectedTarget = ChimeTarget.LEFT
                            if (isPlaying) chimePlayer.play(ChimeTarget.LEFT)
                        },
                        label = { Text(stringResource(R.string.findmy_target_left)) },
                        modifier = Modifier.weight(1f),
                    )
                    FilterChip(
                        selected = selectedTarget == ChimeTarget.BOTH,
                        onClick = {
                            selectedTarget = ChimeTarget.BOTH
                            if (isPlaying) chimePlayer.play(ChimeTarget.BOTH)
                        },
                        label = { Text(stringResource(R.string.findmy_target_both)) },
                        modifier = Modifier.weight(1f),
                    )
                    FilterChip(
                        selected = selectedTarget == ChimeTarget.RIGHT,
                        onClick = {
                            selectedTarget = ChimeTarget.RIGHT
                            if (isPlaying) chimePlayer.play(ChimeTarget.RIGHT)
                        },
                        label = { Text(stringResource(R.string.findmy_target_right)) },
                        modifier = Modifier.weight(1f),
                    )
                }

                if (supportsCaseSpeaker) {
                    FilterChip(
                        selected = selectedTarget == ChimeTarget.CASE,
                        onClick = {
                            selectedTarget = ChimeTarget.CASE
                            if (isPlaying) chimePlayer.play(ChimeTarget.CASE)
                        },
                        label = { Text(stringResource(R.string.findmy_target_case)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            // Safety notice
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(R.string.findmy_warning),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(12.dp),
                )
            }

            // Action Button
            Button(
                onClick = {
                    if (isPlaying) {
                        chimePlayer.stop()
                    } else {
                        chimePlayer.play(selectedTarget)
                    }
                },
                enabled = isConnected,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPlaying) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                ),
                shape = CircleShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
            ) {
                Text(
                    text = stringResource(if (isPlaying) R.string.findmy_btn_stop else R.string.findmy_btn_play),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun PulsingSonarIcon(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "sonar-transition")
    val pulseRadius by transition.animateFloat(
        initialValue = 12f,
        targetValue = 38f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "sonar-radius",
    )
    val pulseAlpha by transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "sonar-alpha",
    )
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            drawCircle(
                color = primaryColor.copy(alpha = pulseAlpha),
                radius = pulseRadius,
                center = center,
                style = Stroke(width = 3.dp.toPx()),
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
        )
    }
}
