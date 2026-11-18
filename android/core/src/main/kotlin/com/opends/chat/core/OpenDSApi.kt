package com.opends.chat.core

import com.opends.chat.core.chat.ChatClientImpl
import com.opends.chat.core.db.SessionStoreImpl
import com.opends.chat.core.net.WireClient
import com.opends.chat.core.net.networkDetail
import com.opends.chat.core.net.userText
import com.opends.chat.core.settings.SettingsStoreImpl
import com.opends.chat.core.voice.VoiceServiceImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal object ApiBridge {
    @Volatile
    var wire: WireClient? = null

    @Volatile
    var sessions: SessionStoreImpl? = null

    @Volatile
    var settings: SettingsStoreImpl? = null

    @Volatile
    var chat: ChatClientImpl? = null

    @Volatile
    var voice: VoiceServiceImpl? = null

    @Volatile
    var baseUrlProvider: () -> String = { "https://api.deepseek.com" }
}

object OpenDSApi {
    internal fun attach(
        client: WireClient,
        store: SessionStoreImpl,
        settings: SettingsStoreImpl,
        baseUrl: () -> String,
    ) {
        ApiBridge.wire = client
        ApiBridge.sessions = store
        ApiBridge.settings = settings
        ApiBridge.baseUrlProvider = baseUrl
    }

    internal fun attachChat(client: ChatClientImpl) {
        ApiBridge.chat = client
    }

    internal fun attachVoice(service: VoiceServiceImpl) {
        ApiBridge.voice = service
    }

    private val fallbackFontSizeLevel = MutableStateFlow(0)
    private val fallbackInputMode = MutableStateFlow("Text")
    private val fallbackVoiceInputConfig = MutableStateFlow(VoiceInputConfig())

    val fontSizeLevel: StateFlow<Int>
        get() = ApiBridge.settings?.fontSizeLevel ?: fallbackFontSizeLevel

    val inputMode: StateFlow<String>
        get() = ApiBridge.settings?.inputMode ?: fallbackInputMode

    suspend fun setFontSizeLevel(level: Int) {
        ApiBridge.settings?.setFontSizeLevel(level)
    }

    suspend fun setInputMode(mode: String) {
        ApiBridge.settings?.setInputMode(mode)
    }

    val voiceInputConfig: StateFlow<VoiceInputConfig>
        get() = ApiBridge.settings?.voiceInputConfig ?: fallbackVoiceInputConfig

    suspend fun setVoiceInputConfig(config: VoiceInputConfig) {
        ApiBridge.settings?.setVoiceInputConfig(config)
    }

    suspend fun transcribeAndSend(
        sessionId: String,
        wav: ByteArray,
        thinkingEnabled: Boolean,
        searchEnabled: Boolean,
    ): Result<Unit> {
        val voice = ApiBridge.voice
            ?: return Result.failure(IllegalStateException("语音服务未初始化"))
        val chat = ApiBridge.chat
            ?: return Result.failure(IllegalStateException("对话服务未初始化"))
        val text = try {
            voice.transcribe(wav)
        } catch (error: Exception) {
            return Result.failure(error)
        }.trim()
        if (text.isEmpty()) {
            return Result.failure(IllegalStateException("未识别到语音内容"))
        }
        return chat.send(
            sessionId,
            SendInput(
                text = text,
                thinkingEnabled = thinkingEnabled,
                searchEnabled = searchEnabled,
            ),
        )
    }

    fun fontSizeScale(level: Int): Float = when (level.coerceIn(0, 7)) {
        0 -> 1.0f
        1 -> 0.810f
        2 -> 0.857f
        3 -> 1.0f
        4 -> 1.048f
        5 -> 1.143f
        6 -> 1.371f
        else -> 1.543f
    }

    suspend fun validateApiKey(): Boolean = runCatching {
        ApiBridge.wire?.validateKey()
        ApiBridge.wire != null
    }.getOrDefault(false)

    suspend fun listModels(): List<String> = runCatching {
        ApiBridge.wire?.listModels().orEmpty()
    }.getOrDefault(emptyList())

    suspend fun verifyApiKey(apiKey: String, baseUrl: String? = null): Result<Unit> {
        val client = ApiBridge.wire ?: return Result.failure(IllegalStateException("OpenDS 未初始化"))
        val target = baseUrl?.trim()?.takeIf { it.isNotEmpty() } ?: ApiBridge.baseUrlProvider()
        return try {
            client.validateKeyWith(apiKey, target)
            Result.success(Unit)
        } catch (error: com.opends.chat.core.net.ApiException) {
            Result.failure(IllegalStateException(error.userText))
        } catch (error: Exception) {
            ErrorLog.record("网络", "API Key 校验失败", null, error)
            Result.failure(IllegalStateException(networkDetail(error.cause ?: error)))
        }
    }

    fun describeError(error: Throwable): String = runCatching {
        error.userText()
    }.getOrNull().orEmpty().ifBlank { error.message?.takeIf { it.isNotBlank() } ?: "操作失败，请重试" }

    suspend fun clearAllSessions(): Boolean = runCatching {
        val store = ApiBridge.sessions ?: return false
        store.clearAll()
        true
    }.getOrDefault(false)
}
