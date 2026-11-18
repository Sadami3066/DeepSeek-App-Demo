package com.opends.chat.core.chat

import kotlin.math.roundToInt

internal object ComposerLimits {
    const val MAX_INPUT_CHARS = 2_621_440
    const val MAX_FILES_PER_MESSAGE = 20
    const val MAX_FILE_BYTES = 100L * 1024 * 1024
    const val MAX_FILE_LABEL = "100MB"

    fun validateText(text: String): String? {
        if (text.length <= MAX_INPUT_CHARS) return null
        val percent = ((text.length - MAX_INPUT_CHARS).toDouble() / MAX_INPUT_CHARS * 100)
            .roundToInt()
            .coerceAtLeast(1)
        return "内容超长约 $percent%，请删减后再试"
    }

    fun validateFileCount(incomingCount: Int): String? =
        if (incomingCount > MAX_FILES_PER_MESSAGE) {
            "单条消息最多上传 $MAX_FILES_PER_MESSAGE 个文件"
        } else {
            null
        }

    fun validateFileSize(size: Long): String? =
        if (size > MAX_FILE_BYTES) {
            "单个文件大小超过限制（$MAX_FILE_LABEL），请调整后重试"
        } else {
            null
        }
}
