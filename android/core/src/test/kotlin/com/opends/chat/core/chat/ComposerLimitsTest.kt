package com.opends.chat.core.chat

import com.opends.chat.core.VoiceInputConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ComposerLimitsTest {

    @Test
    fun textLimitBoundaryAndPercentMessage() {
        assertNull(ComposerLimits.validateText("a".repeat(ComposerLimits.MAX_INPUT_CHARS)))
        assertEquals(
            "内容超长约 1%，请删减后再试",
            ComposerLimits.validateText("a".repeat(ComposerLimits.MAX_INPUT_CHARS + 1)),
        )
        val quarterOver = ComposerLimits.MAX_INPUT_CHARS + ComposerLimits.MAX_INPUT_CHARS / 4
        assertEquals(
            "内容超长约 25%，请删减后再试",
            ComposerLimits.validateText("a".repeat(quarterOver)),
        )
    }

    @Test
    fun matchesVoiceInputConfigDefaultLimit() {
        assertEquals(VoiceInputConfig().inputCharacterLimit, ComposerLimits.MAX_INPUT_CHARS)
    }

    @Test
    fun fileCountLimitIsPerMessage() {
        assertNull(ComposerLimits.validateFileCount(19))
        assertNull(ComposerLimits.validateFileCount(20))
        assertEquals(
            "单条消息最多上传 20 个文件",
            ComposerLimits.validateFileCount(21),
        )
    }

    @Test
    fun singleFileSizeLimit() {
        assertNull(ComposerLimits.validateFileSize(ComposerLimits.MAX_FILE_BYTES))
        assertEquals(
            "单个文件大小超过限制（100MB），请调整后重试",
            ComposerLimits.validateFileSize(ComposerLimits.MAX_FILE_BYTES + 1),
        )
    }

    @Test
    fun searchAndFilesAreMutuallyExclusive() {
        assertNull(ComposerLimits.validateSearchWithFiles(true, false))
        assertNull(ComposerLimits.validateSearchWithFiles(false, true))
        assertEquals(
            "联网搜索不支持上传文件",
            ComposerLimits.validateSearchWithFiles(true, true),
        )
    }
}
