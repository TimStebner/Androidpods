// SPDX-License-Identifier: GPL-3.0-or-later
package dev.androidpods.core.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.androidpods.core.airpods.HoldDuration
import dev.androidpods.core.airpods.PressSpeed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val Context.settingsDataStore by preferencesDataStore(name = "app_settings")

class AppSettingsRepository(private val context: Context) {
    private val dataStore = context.applicationContext.settingsDataStore

    val settingsFlow = dataStore.data.map { prefs ->
        AppSettings(
            themeMode = prefs[KEY_THEME_MODE]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.SYSTEM,
            dynamicColor = prefs[KEY_DYNAMIC_COLOR] ?: true,
            autoPauseEnabled = prefs[KEY_AUTO_PAUSE] ?: true,
            autoResumeEnabled = prefs[KEY_AUTO_RESUME] ?: true,
            assistantTriggerEnabled = prefs[KEY_ASSISTANT_TRIGGER] ?: true,
            pressSpeed = prefs[KEY_PRESS_SPEED]?.let { runCatching { PressSpeed.valueOf(it) }.getOrNull() } ?: PressSpeed.DEFAULT,
            holdDuration = prefs[KEY_HOLD_DURATION]?.let { runCatching { HoldDuration.valueOf(it) }.getOrNull() } ?: HoldDuration.DEFAULT,
            headGesturesEnabled = prefs[KEY_HEAD_GESTURES] ?: true,
            connectionBannerEnabled = prefs[KEY_CONNECTION_BANNER] ?: true,
            batteryNotificationEnabled = prefs[KEY_BATTERY_NOTIFICATION] ?: true,
            protocolLoggingEnabled = prefs[KEY_PROTOCOL_LOGGING] ?: false,
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[KEY_THEME_MODE] = mode.name }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        dataStore.edit { it[KEY_DYNAMIC_COLOR] = enabled }
    }

    suspend fun setAutoPause(enabled: Boolean) {
        dataStore.edit { it[KEY_AUTO_PAUSE] = enabled }
    }

    suspend fun setAutoResume(enabled: Boolean) {
        dataStore.edit { it[KEY_AUTO_RESUME] = enabled }
    }

    suspend fun setAssistantTrigger(enabled: Boolean) {
        dataStore.edit { it[KEY_ASSISTANT_TRIGGER] = enabled }
    }

    suspend fun setPressSpeed(speed: PressSpeed) {
        dataStore.edit { it[KEY_PRESS_SPEED] = speed.name }
    }

    suspend fun setHoldDuration(duration: HoldDuration) {
        dataStore.edit { it[KEY_HOLD_DURATION] = duration.name }
    }

    suspend fun setHeadGestures(enabled: Boolean) {
        dataStore.edit { it[KEY_HEAD_GESTURES] = enabled }
    }

    suspend fun setConnectionBanner(enabled: Boolean) {
        dataStore.edit { it[KEY_CONNECTION_BANNER] = enabled }
    }

    suspend fun setBatteryNotification(enabled: Boolean) {
        dataStore.edit { it[KEY_BATTERY_NOTIFICATION] = enabled }
    }

    suspend fun setProtocolLogging(enabled: Boolean) {
        dataStore.edit { it[KEY_PROTOCOL_LOGGING] = enabled }
    }

    companion object {
        private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        private val KEY_DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        private val KEY_AUTO_PAUSE = booleanPreferencesKey("auto_pause")
        private val KEY_AUTO_RESUME = booleanPreferencesKey("auto_resume")
        private val KEY_ASSISTANT_TRIGGER = booleanPreferencesKey("assistant_trigger")
        private val KEY_PRESS_SPEED = stringPreferencesKey("press_speed")
        private val KEY_HOLD_DURATION = stringPreferencesKey("hold_duration")
        private val KEY_HEAD_GESTURES = booleanPreferencesKey("head_gestures")
        private val KEY_CONNECTION_BANNER = booleanPreferencesKey("connection_banner")
        private val KEY_BATTERY_NOTIFICATION = booleanPreferencesKey("battery_notification")
        private val KEY_PROTOCOL_LOGGING = booleanPreferencesKey("protocol_logging")
    }
}

object AppSettingsRepositoryProvider {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var repository: AppSettingsRepository? = null

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    fun get(context: Context): AppSettingsRepository {
        repository?.let { return it }
        val created = AppSettingsRepository(context.applicationContext)
        repository = created
        scope.launch {
            created.settingsFlow.collect { _settings.value = it }
        }
        return created
    }
}
