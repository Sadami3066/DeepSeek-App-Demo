package com.opends.chat.core

data class VoiceInputConfig(
    val gestureThresholdMs: Int = 250,
    val maxDurationMs: Int = 60000,
    val emptyDetectTimeMs: Int = 3000,
    val stopDelayMs: Int = 0,
    val opusBitrate: Int = 24000,
    val voiceInputEnabled: Boolean = true,
    val inputDefaultVoice: Boolean = false,
    val inputCharacterLimit: Int = 2621440,
)
