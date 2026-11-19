package com.opends.chat.core.voice

/**
 * 朗读音色。
 *
 * 官方 TTS 接口只有两个原生音色（`stella` 女声、`tide` 男声），
 * 早期版本用 AudioTrack 的 `PlaybackParams` 变调/变速伪造出「贝壳」「白浪」，
 * 但 `AUDIO_FALLBACK_MODE_FAIL` 在不少设备上会直接导致整轨无声，
 * 默认音色恰好就是伪造音色，于是表现为「点了朗读没反应」。
 * 现只保留两个原生音色，播放链路不再做任何变调处理。
 */
internal data class VoiceProfile(
    val id: String,
    val apiVoiceId: String,
)

internal object VoiceProfiles {
    /** 海星：API 内置女声 stella */
    val haixing = VoiceProfile(id = "stella", apiVoiceId = "stella")

    /** 暗潮：API 内置男声 tide */
    val anchao = VoiceProfile(id = "tide", apiVoiceId = "tide")

    val all: List<VoiceProfile> = listOf(haixing, anchao)

    /**
     * 旧版本存档里可能仍存着 mira/beike（贝壳）或 echo/bailang（白浪）。
     * 这两种已下线，读到旧值时映射到它们当初的基底音色，避免用户设置失效。
     */
    fun byId(id: String?): VoiceProfile = when (id?.trim()?.lowercase()) {
        "tide", "anchao" -> anchao
        "echo", "bailang" -> anchao
        else -> haixing
    }
}
