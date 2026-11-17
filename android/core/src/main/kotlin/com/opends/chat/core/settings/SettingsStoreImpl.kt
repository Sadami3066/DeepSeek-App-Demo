package com.opends.chat.core.settings

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.opends.chat.core.AppSettings
import com.opends.chat.core.SettingsStore
import com.opends.chat.core.ThemeMode
import com.opends.chat.core.VoiceInputConfig
import com.opends.chat.core.net.WireSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.runBlocking
import java.io.File

internal class SettingsStoreImpl(
    context: Context,
    private val scope: CoroutineScope,
) : SettingsStore, WireSettings {

    private val dataStore = PreferenceDataStoreFactory.create(
        scope = scope + Dispatchers.IO,
        produceFile = { File(context.filesDir, "settings/opends.preferences_pb") },
    )

    private val cached = MutableStateFlow(loadBlocking())

    override val state: StateFlow<AppSettings> = cached

    override val hasApiKey: StateFlow<Boolean> = cached
        .map { it.apiKey.isNotEmpty() }
        .stateIn(scope, SharingStarted.Eagerly, cached.value.apiKey.isNotEmpty())

    val fontSizeLevel: StateFlow<Int> = cached
        .map { it.fontSizeLevel }
        .stateIn(scope, SharingStarted.Eagerly, cached.value.fontSizeLevel)

    val inputMode: StateFlow<String> = cached
        .map { it.inputMode }
        .stateIn(scope, SharingStarted.Eagerly, cached.value.inputMode)

    val voiceInputConfig: StateFlow<VoiceInputConfig> = dataStore.data
        .map { decodeVoiceConfig(it) }
        .stateIn(scope, SharingStarted.Eagerly, VoiceInputConfig())

    init {
        scope.launch {
            dataStore.data.collect { preferences -> cached.value = decode(preferences) }
        }
    }

    private fun loadBlocking(): AppSettings {
        val preferences = runCatching { runBlocking { dataStore.data.first() } }.getOrNull()
        return if (preferences == null) AppSettings() else decode(preferences)
    }

    private fun decode(preferences: Preferences): AppSettings {
        val defaults = AppSettings()
        return AppSettings(
            apiKey = SecretBox.decrypt(preferences[KEY_API_KEY] ?: ""),
            apiBaseUrl = preferences[KEY_BASE_URL] ?: defaults.apiBaseUrl,
            theme = runCatching { ThemeMode.valueOf(preferences[KEY_THEME] ?: "") }.getOrDefault(defaults.theme),
            languageTag = preferences[KEY_LANGUAGE] ?: defaults.languageTag,
            ttsVoice = preferences[KEY_TTS_VOICE] ?: defaults.ttsVoice,
            locationEnabled = preferences[KEY_LOCATION_ENABLED] ?: defaults.locationEnabled,
            location = preferences[KEY_LOCATION],
            thinkingDefault = preferences[KEY_THINKING] ?: defaults.thinkingDefault,
            searchDefault = preferences[KEY_SEARCH] ?: defaults.searchDefault,
            fontSizeLevel = (preferences[KEY_FONT_SIZE_LEVEL] ?: defaults.fontSizeLevel).coerceIn(0, 7),
            inputMode = when (preferences[KEY_INPUT_MODE] ?: defaults.inputMode) {
                "Voice" -> "Voice"
                "voice" -> "Voice"
                else -> "Text"
            },
            thinkingAutoFold = preferences[KEY_THINKING_AUTO_FOLD] ?: defaults.thinkingAutoFold,
        )
    }

    private suspend fun write(settings: AppSettings) {
        dataStore.edit { preferences ->
            preferences[KEY_API_KEY] = SecretBox.encrypt(settings.apiKey)
            preferences[KEY_BASE_URL] = settings.apiBaseUrl
            preferences[KEY_THEME] = settings.theme.name
            preferences[KEY_LANGUAGE] = settings.languageTag
            preferences[KEY_TTS_VOICE] = settings.ttsVoice
            preferences[KEY_LOCATION_ENABLED] = settings.locationEnabled
            if (settings.location == null) {
                preferences.remove(KEY_LOCATION)
            } else {
                preferences[KEY_LOCATION] = settings.location
            }
            preferences[KEY_THINKING] = settings.thinkingDefault
            preferences[KEY_SEARCH] = settings.searchDefault
            preferences[KEY_FONT_SIZE_LEVEL] = settings.fontSizeLevel.coerceIn(0, 7)
            preferences[KEY_INPUT_MODE] = if (settings.inputMode == "Voice") "Voice" else "Text"
            preferences[KEY_THINKING_AUTO_FOLD] = settings.thinkingAutoFold
        }
    }

    override suspend fun setApiKey(key: String) {
        val trimmed = key.trim()
        cached.value = cached.value.copy(apiKey = trimmed)
        dataStore.edit { preferences -> preferences[KEY_API_KEY] = SecretBox.encrypt(trimmed) }
    }

    suspend fun setVoiceInputConfig(config: VoiceInputConfig) {
        dataStore.edit { preferences ->
            preferences[KEY_VOICE_GESTURE_MS] = config.gestureThresholdMs
            preferences[KEY_VOICE_MAX_MS] = config.maxDurationMs
            preferences[KEY_VOICE_EMPTY_MS] = config.emptyDetectTimeMs
            preferences[KEY_VOICE_STOP_MS] = config.stopDelayMs
            preferences[KEY_VOICE_OPUS_BITRATE] = config.opusBitrate
            preferences[KEY_VOICE_ENABLED] = config.voiceInputEnabled
            preferences[KEY_VOICE_DEFAULT_MODE] = config.inputDefaultVoice
            preferences[KEY_VOICE_INPUT_LIMIT] = config.inputCharacterLimit
        }
    }

    private fun decodeVoiceConfig(preferences: Preferences): VoiceInputConfig {
        val defaults = VoiceInputConfig()
        return VoiceInputConfig(
            gestureThresholdMs = preferences[KEY_VOICE_GESTURE_MS] ?: defaults.gestureThresholdMs,
            maxDurationMs = (preferences[KEY_VOICE_MAX_MS] ?: defaults.maxDurationMs).coerceAtMost(120_000),
            emptyDetectTimeMs = preferences[KEY_VOICE_EMPTY_MS] ?: defaults.emptyDetectTimeMs,
            stopDelayMs = preferences[KEY_VOICE_STOP_MS] ?: defaults.stopDelayMs,
            opusBitrate = preferences[KEY_VOICE_OPUS_BITRATE] ?: defaults.opusBitrate,
            voiceInputEnabled = preferences[KEY_VOICE_ENABLED] ?: defaults.voiceInputEnabled,
            inputDefaultVoice = preferences[KEY_VOICE_DEFAULT_MODE] ?: defaults.inputDefaultVoice,
            inputCharacterLimit = preferences[KEY_VOICE_INPUT_LIMIT] ?: defaults.inputCharacterLimit,
        )
    }

    suspend fun setFontSizeLevel(level: Int) {
        update(cached.value.copy(fontSizeLevel = level.coerceIn(0, 7)))
    }

    suspend fun setInputMode(mode: String) {
        update(cached.value.copy(inputMode = if (mode == "Voice" || mode == "voice") "Voice" else "Text"))
    }

    override suspend fun update(settings: AppSettings) {
        val normalized = settings.copy(apiKey = settings.apiKey.trim())
        cached.value = normalized
        write(normalized)
    }

    override fun apiKey(): String = cached.value.apiKey

    override fun baseUrl(): String {
        val configured = cached.value.apiBaseUrl.trim().trimEnd('/')
        return configured.ifEmpty { AppSettings().apiBaseUrl }
    }

    private companion object {
        val KEY_API_KEY = stringPreferencesKey("api_key")
        val KEY_BASE_URL = stringPreferencesKey("api_base_url")
        val KEY_THEME = stringPreferencesKey("theme")
        val KEY_LANGUAGE = stringPreferencesKey("language")
        val KEY_TTS_VOICE = stringPreferencesKey("tts_voice")
        val KEY_LOCATION_ENABLED = booleanPreferencesKey("location_enabled")
        val KEY_LOCATION = stringPreferencesKey("location")
        val KEY_THINKING = booleanPreferencesKey("thinking_default")
        val KEY_SEARCH = booleanPreferencesKey("search_default")
        val KEY_FONT_SIZE_LEVEL = intPreferencesKey("font_size_level")
        val KEY_INPUT_MODE = stringPreferencesKey("key_input_mode")
        val KEY_THINKING_AUTO_FOLD = booleanPreferencesKey("thinking_auto_fold")
        val KEY_VOICE_GESTURE_MS = intPreferencesKey("input_view_voice_gesture_duration_ms")
        val KEY_VOICE_MAX_MS = intPreferencesKey("max_duration_ms")
        val KEY_VOICE_EMPTY_MS = intPreferencesKey("record_empty_detect_time_ms")
        val KEY_VOICE_STOP_MS = intPreferencesKey("record_stop_delay_ms")
        val KEY_VOICE_OPUS_BITRATE = intPreferencesKey("opus_bitrate")
        val KEY_VOICE_ENABLED = booleanPreferencesKey("voice_input_enabled")
        val KEY_VOICE_DEFAULT_MODE = booleanPreferencesKey("input_default_voice")
        val KEY_VOICE_INPUT_LIMIT = intPreferencesKey("input_character_limit")
    }
}
