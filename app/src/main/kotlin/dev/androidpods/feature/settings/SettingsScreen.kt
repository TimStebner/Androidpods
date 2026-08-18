// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.feature.settings

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.net.toUri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.androidpods.app.R
import dev.androidpods.core.data.AppSettings
import dev.androidpods.core.data.AppSettingsRepositoryProvider
import dev.androidpods.core.data.DataStoreTierProbeCache
import dev.androidpods.core.data.ThemeMode
import dev.androidpods.core.designsystem.AndroidpodsTheme
import dev.androidpods.core.designsystem.ExpressiveScreenHeader
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val settings by AppSettingsRepositoryProvider.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val settingsRepo = AppSettingsRepositoryProvider.get(context)
    val resetDoneMessage = stringResource(R.string.settings_probe_cache_reset_done)

    SettingsScreenContent(
        settings = settings,
        onThemeModeSelected = { mode ->
            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
            scope.launch { settingsRepo.setThemeMode(mode) }
        },
        onDynamicColorChanged = { enabled ->
            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
            scope.launch { settingsRepo.setDynamicColor(enabled) }
        },
        onConnectionBannerChanged = { enabled ->
            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
            scope.launch { settingsRepo.setConnectionBanner(enabled) }
        },
        onBatteryNotificationChanged = { enabled ->
            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
            scope.launch { settingsRepo.setBatteryNotification(enabled) }
        },
        onBatteryPopupChanged = { enabled ->
            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
            scope.launch { settingsRepo.setBatteryPopupEnabled(enabled) }
        },
        onPreviewPopup = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            dev.androidpods.feature.popup.BatteryPopupActivity.start(context)
        },
        onProtocolLoggingChanged = { enabled ->
            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
            scope.launch { settingsRepo.setProtocolLogging(enabled) }
        },
        onResetProbeCache = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            scope.launch {
                DataStoreTierProbeCache(context).clear()
                Toast.makeText(context, resetDoneMessage, Toast.LENGTH_SHORT).show()
            }
        },
        onPrivacyPolicyClick = {
            runCatching {
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        "https://github.com/TimStebner/Androidpods/blob/main/PRIVACY.md".toUri(),
                    ),
                )
            }
        },
        modifier = modifier,
    )
}

@Composable
internal fun SettingsScreenContent(
    settings: AppSettings,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onDynamicColorChanged: (Boolean) -> Unit,
    onBatteryPopupChanged: (Boolean) -> Unit,
    onPreviewPopup: () -> Unit,
    onConnectionBannerChanged: (Boolean) -> Unit,
    onBatteryNotificationChanged: (Boolean) -> Unit,
    onProtocolLoggingChanged: (Boolean) -> Unit,
    onResetProbeCache: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .padding(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ExpressiveScreenHeader(
            title = stringResource(R.string.settings_title),
            subtitle = stringResource(R.string.settings_subtitle),
            icon = Icons.Default.Settings,
            iconBadgeColor = MaterialTheme.colorScheme.primaryContainer,
            iconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )

        // Section 1: Appearance
        Text(
            text = stringResource(R.string.settings_appearance_header),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
            color = MaterialTheme.colorScheme.primary,
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Dynamic Color
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                        Text(
                            text = stringResource(R.string.settings_dynamic_color_title),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = stringResource(R.string.settings_dynamic_color_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                    Switch(
                        checked = settings.dynamicColor,
                        onCheckedChange = onDynamicColorChanged,
                    )
                }

                // Theme Mode
                Text(
                    text = stringResource(R.string.settings_theme_title),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 4.dp),
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    ThemeOptionRow(
                        label = stringResource(R.string.settings_theme_system),
                        selected = settings.themeMode == ThemeMode.SYSTEM,
                        onClick = { onThemeModeSelected(ThemeMode.SYSTEM) },
                    )
                    ThemeOptionRow(
                        label = stringResource(R.string.settings_theme_light),
                        selected = settings.themeMode == ThemeMode.LIGHT,
                        onClick = { onThemeModeSelected(ThemeMode.LIGHT) },
                    )
                    ThemeOptionRow(
                        label = stringResource(R.string.settings_theme_dark),
                        selected = settings.themeMode == ThemeMode.DARK,
                        onClick = { onThemeModeSelected(ThemeMode.DARK) },
                    )
                }
            }
        }

        // Section 2: Notifications
        Text(
            text = stringResource(R.string.settings_notifications_header),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SettingSwitchRow(
                    title = stringResource(R.string.settings_notif_popup_title),
                    subtitle = stringResource(R.string.settings_notif_popup_desc),
                    checked = settings.batteryPopupEnabled,
                    onCheckedChange = onBatteryPopupChanged,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    OutlinedButton(
                        onClick = onPreviewPopup,
                    ) {
                        Text(text = stringResource(R.string.settings_notif_popup_preview))
                    }
                }
                SettingSwitchRow(
                    title = stringResource(R.string.settings_notif_banner_title),
                    subtitle = stringResource(R.string.settings_notif_banner_desc),
                    checked = settings.connectionBannerEnabled,
                    onCheckedChange = onConnectionBannerChanged,
                )
                SettingSwitchRow(
                    title = stringResource(R.string.settings_notif_battery_title),
                    subtitle = stringResource(R.string.settings_notif_battery_desc),
                    checked = settings.batteryNotificationEnabled,
                    onCheckedChange = onBatteryNotificationChanged,
                )
            }
        }

        // Section 3: Diagnostics
        Text(
            text = stringResource(R.string.settings_diagnostics_header),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                        Text(
                            text = stringResource(R.string.settings_probe_cache_title),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        )
                        Text(
                            text = stringResource(R.string.settings_probe_cache_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                    OutlinedButton(onClick = onResetProbeCache) {
                        Text(text = stringResource(R.string.settings_probe_cache_reset))
                    }
                }

                if (dev.androidpods.app.BuildConfig.DEBUG) {
                    SettingSwitchRow(
                        title = stringResource(R.string.settings_logging_title),
                        subtitle = stringResource(R.string.settings_logging_desc),
                        checked = settings.protocolLoggingEnabled,
                        onCheckedChange = onProtocolLoggingChanged,
                    )
                }
            }
        }

        // Section 4: About & Legal
        Text(
            text = stringResource(R.string.settings_about_header),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(text = stringResource(R.string.settings_version_title), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = stringResource(R.string.settings_version_value), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.onSurface)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(text = stringResource(R.string.settings_license_title), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = stringResource(R.string.settings_license_value), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.onSurface)
                }
                OutlinedButton(
                    onClick = onPrivacyPolicyClick,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = stringResource(R.string.settings_privacy_policy))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ThemeOptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 12.dp),
        )
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    AndroidpodsTheme {
        SettingsScreenContent(
            settings = AppSettings(),
            onThemeModeSelected = {},
            onDynamicColorChanged = {},
            onBatteryPopupChanged = {},
            onPreviewPopup = {},
            onConnectionBannerChanged = {},
            onBatteryNotificationChanged = {},
            onProtocolLoggingChanged = {},
            onResetProbeCache = {},
            onPrivacyPolicyClick = {},
        )
    }
}
