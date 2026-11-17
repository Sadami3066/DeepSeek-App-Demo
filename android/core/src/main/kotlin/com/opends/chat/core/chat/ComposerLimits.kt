package com.opends.chat.core.chat

import kotlin.math.roundToInt

internal object ComposerLimits {
    const val MAX_INPUT_CHARS = 2_621_440
    const val MAX_FILES_PER_SESSION = 20
    const val MAX_FILE_BYTES = 100L * 1024 * 1024
    const val MAX_FILE_LABEL = "100MB"

    fun validateText(text: String): String? {
        if (text.length <= MAX_INPUT_CHARS) return null
        val percent = ((text.length - MAX_INPUT_CHARS).toDouble() / MAX_INPUT_CHARS * 100)
            .roundToInt()
            .coerceAtLeast(1)
        return "内容超长约 $percent%，请删减后再试"
    }

    fun validateFileCount(sessionFileCount: Int, incomingCount: Int): String? =
        if (sessionFileCount + incomingCount > MAX_FILES_PER_SESSION) {
            "上传文件数达到上限，每个对话可上传 $MAX_FILES_PER_SESSION 个文件"
        } else {
            null
        }

    fun validateFileSize(size: Long): String? =
        if (size > MAX_FILE_BYTES) {
            "单个文件大小超过限制（$MAX_FILE_LABEL），请调整后重试"
        } else {
            null
        }

    fun validateSearchWithFiles(searchEnabled: Boolean, hasIncomingFiles: Boolean): String? =
        if (searchEnabled && hasIncomingFiles) "联网搜索不支持上传文件" else null
}
