package com.opends.chat.core.voice

internal data class VoiceProfile(
    val id: String,
    val apiVoiceId: String,
    val pitch: Float,
    val speed: Float,
)

internal object VoiceProfiles {
    val haixing = VoiceProfile("stella", "stella", 1.00f, 1.00f)
    val anchao = VoiceProfile("tide", "tide", 1.00f, 1.00f)
    val beike = VoiceProfile("mira", "stella", 1.06f, 0.97f)
    val bailang = VoiceProfile("echo", "tide", 1.10f, 1.02f)

    val all: List<VoiceProfile> = listOf(haixing, anchao, beike, bailang)

    fun byId(id: String?): VoiceProfile = when (id?.trim()?.lowercase()) {
        "mira", "beike" -> beike
        "echo", "bailang" -> bailang
        "tide", "anchao" -> anchao
        else -> haixing
    }

    fun safePitch(pitch: Float): Float = pitch.coerceIn(0.90f, 1.10f)

    fun safeSpeed(speed: Float): Float = speed.coerceIn(0.95f, 1.05f)

    fun needsProcessing(profile: VoiceProfile): Boolean =
        profile.pitch != 1.0f || profile.speed != 1.0f
}
