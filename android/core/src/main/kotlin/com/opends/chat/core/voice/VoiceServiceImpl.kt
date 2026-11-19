package com.opends.chat.core.voice

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import com.opends.chat.core.VoiceService
import com.opends.chat.core.net.ApiErrorKind
import com.opends.chat.core.net.ApiCondition
import com.opends.chat.core.net.ApiException
import com.opends.chat.core.net.WireClient
import com.opends.chat.core.settings.SettingsStoreImpl
import com.opends.chat.core.util.TextMarkers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

internal class VoiceServiceImpl(
    private val settings: SettingsStoreImpl,
    private val wire: WireClient,
) : VoiceService {

    private val speakingState = MutableStateFlow(false)
    override val isSpeaking: StateFlow<Boolean> = speakingState

    private val player = PcmPlayer()
    private val playerLock = Any()

    @Volatile
    private var speaking: Boolean = false

    override suspend fun transcribe(wav: ByteArray): String {
        if (wav.isEmpty()) throw ApiException.protocol("没有录到声音")
        if (wav.size > MAX_ASR_BYTES) throw ApiException.upload("录音超过 8 MiB 限制", false)
        return try {
            wire.transcribe(wav)
        } catch (error: ApiException) {
            throw ApiException(ApiErrorKind.HTTP, asrMessage(error), error.status)
        }
    }

    override suspend fun speak(text: String, voiceId: String?) = withContext(Dispatchers.IO) {
        val plain = TextMarkers.stripAll(text).trim()
        if (plain.isEmpty()) throw ApiException.protocol("没有可朗读的内容")
        stopSpeaking()
        synchronized(playerLock) {
            speaking = true
        }
        speakingState.value = true
        // 只有两个原生音色，播放链路不做任何变调/变速处理。
        val profile = VoiceProfiles.byId(voiceId ?: settings.state.value.ttsVoice)
        try {
            player.start()
            splitText(plain).forEach { chunk ->
                synchronized(playerLock) {
                    if (!speaking) return@withContext
                }
                wire.synthesize(chunk, profile.apiVoiceId) { bytes ->
                    if (speaking) player.write(bytes)
                }
            }
            synchronized(playerLock) {
                speaking = false
            }
            player.drain()
        } catch (error: ApiException) {
            if (error.kind == ApiErrorKind.INTERRUPTED) return@withContext
            throw ApiException(ApiErrorKind.HTTP, ttsMessage(error), error.status)
        } catch (error: kotlinx.coroutines.CancellationException) {
            throw error
        } catch (error: Exception) {
            throw ApiException(ApiErrorKind.HTTP, "朗读失败，请稍后再试")
        } finally {
            player.release()
            speakingState.value = false
        }
    }

    override fun stopSpeaking() {
        synchronized(playerLock) {
            speaking = false
        }
        wire.cancelSpeech()
        player.release()
        speakingState.value = false
    }

    private fun splitText(text: String): List<String> {
        val chunks = ArrayList<String>()
        val limit = MAX_TTS_CHARS
        var remaining = text
        while (remaining.length > limit) {
            val window = remaining.substring(0, limit)
            var cut = window.lastIndexOfAny(charArrayOf('。', '！', '？', '\n', '.', '!', '?'))
            if (cut < limit / 2) cut = limit
            chunks.add(remaining.substring(0, cut + 1))
            remaining = remaining.substring(cut + 1)
        }
        if (remaining.isNotBlank()) chunks.add(remaining)
        return chunks
    }

    private class PcmPlayer {
        private var track: AudioTrack? = null
        private var remainder: ByteArray = ByteArray(0)
        private var writtenBytes: Long = 0L
        private val released = AtomicBoolean(false)

        fun start() {
            released.set(false)
            remainder = ByteArray(0)
            writtenBytes = 0L
            val minBuffer = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
            ).coerceAtLeast(FRAME_BYTES * 4)
            val created = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .build()
                )
                .setTransferMode(AudioTrack.MODE_STREAM)
                .setBufferSizeInBytes(maxOf(minBuffer, FRAME_BYTES * 4))
                .build()
            if (created.state != AudioTrack.STATE_INITIALIZED) {
                created.release()
                throw ApiException.protocol("朗读播放器初始化失败")
            }
            created.play()
            track = created
        }

        fun write(bytes: ByteArray) {
            val target = track ?: return
            if (released.get()) return
            val merged = if (remainder.isEmpty()) bytes else remainder + bytes
            val playable = merged.size - (merged.size % 2)
            if (playable <= 0) {
                remainder = merged
                return
            }
            remainder = merged.copyOfRange(playable, merged.size)
            var offset = 0
            while (offset < playable) {
                val written = target.write(merged, offset, playable - offset, AudioTrack.WRITE_BLOCKING)
                if (written < 0) throw ApiException.protocol("朗读播放失败")
                if (written == 0) continue
                offset += written
                writtenBytes += written
            }
        }

        fun drain() {
            val target = track ?: return
            val estimatedMillis = (writtenBytes / 48.0).toLong()
            val deadline = System.currentTimeMillis() + estimatedMillis + 120_000L
            while (writtenBytes > 0 && System.currentTimeMillis() < deadline) {
                val played = runCatching { target.playbackHeadPosition.toLong() * 2 }.getOrDefault(0L)
                if (played >= writtenBytes) break
                Thread.sleep(20)
            }
            Thread.sleep(TAIL_MARGIN_MS)
        }

        fun release() {
            val target = track ?: return
            track = null
            if (!released.compareAndSet(false, true)) return
            runCatching { target.stop() }
            runCatching { target.flush() }
            runCatching { target.release() }
        }
    }

    private fun asrMessage(error: ApiException): String = when (error.condition) {
        ApiCondition.UNAUTHORIZED -> "API Key 无效，请更换后重试"
        ApiCondition.INSUFFICIENT_BALANCE -> "账户余额不足"
        ApiCondition.RATE_LIMITED -> "操作过于频繁，请稍后再试"
        ApiCondition.TOO_LARGE -> "录音过长，请缩短后重试"
        ApiCondition.SERVER_BUSY -> "服务器繁忙，请稍后再试"
        else -> "语音识别失败，请稍后再试"
    }

    private fun ttsMessage(error: ApiException): String = when {
        error.status == 400 -> "这条回答暂时无法朗读"
        error.condition == ApiCondition.UNAUTHORIZED -> "API Key 无效，请更换后重试"
        error.condition == ApiCondition.INSUFFICIENT_BALANCE -> "账户余额不足"
        error.condition == ApiCondition.RATE_LIMITED -> "操作过于频繁，请稍后再试"
        else -> "朗读失败，请稍后再试"
    }

    companion object {
        private const val SAMPLE_RATE = 24000
        private const val FRAME_BYTES = 960
        private const val TAIL_MARGIN_MS = 200L
        private const val MAX_ASR_BYTES = 8 * 1024 * 1024
        private const val MAX_TTS_CHARS = 4000
    }
}
