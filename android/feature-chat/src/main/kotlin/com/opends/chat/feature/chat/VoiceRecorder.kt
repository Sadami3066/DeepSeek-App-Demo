package com.opends.chat.feature.chat

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.sin
import kotlin.math.sqrt

class VoiceRecorder(private val context: Context) {

    private val sampleRate = 16000
    private val fftSize = 512
    private val barCount = 60
    private val minHz = 100.0
    private val maxHz = 4000.0
    private val minDb = -80.0
    private val maxDb = -25.0

    private var record: AudioRecord? = null
    private var scope: CoroutineScope? = null
    private var loopJob: Job? = null
    private val pcm = ArrayList<Short>(sampleRate * 8)
    private val window = DoubleArray(fftSize) { 0.5 - 0.5 * cos(2.0 * PI * it / (fftSize - 1)) }
    private val re = DoubleArray(fftSize)
    private val im = DoubleArray(fftSize)
    private val smoothed = FloatArray(barCount)
    private val ring = ShortArray(fftSize)
    private var ringPos = 0
    private var hasPermission = false

    private val _amplitudes = MutableStateFlow(FloatArray(barCount))
    val amplitudes: StateFlow<FloatArray> = _amplitudes.asStateFlow()

    fun start(): Boolean {
        hasPermission = context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        if (!hasPermission) return false
        val minBuffer = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
        ).coerceAtLeast(4096)
        val recorder = try {
            AudioRecord(
                MediaRecorder.AudioSource.VOICE_RECOGNITION,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minBuffer * 2,
            )
        } catch (throwable: Throwable) {
            return false
        }
        if (recorder.state != AudioRecord.STATE_INITIALIZED) {
            recorder.release()
            return false
        }
        return try {
            recorder.startRecording()
            record = recorder
            pcm.clear()
            java.util.Arrays.fill(smoothed, 0f)
            ringPos = 0
            java.util.Arrays.fill(ring, 0)
            val s = CoroutineScope(SupervisorJob() + Dispatchers.Default)
            scope = s
            loopJob = s.launch { readLoop(recorder) }
            true
        } catch (throwable: Throwable) {
            recorder.release()
            record = null
            false
        }
    }

    private suspend fun readLoop(recorder: AudioRecord) {
        val chunk = ShortArray(256)
        while (true) {
            val coroutineScope = scope ?: break
            if (!coroutineScope.isActive) break
            val read = try {
                recorder.read(chunk, 0, chunk.size)
            } catch (throwable: Throwable) {
                break
            }
            if (read <= 0) continue
            synchronized(pcm) {
                for (i in 0 until read) pcm.add(chunk[i])
            }
            for (i in 0 until read) {
                ring[ringPos] = chunk[i]
                ringPos = (ringPos + 1) % fftSize
            }
            emitSpectrum()
        }
    }

    private fun emitSpectrum() {
        var index = ringPos
        for (i in 0 until fftSize) {
            re[i] = ring[index] / 32768.0 * window[i]
            im[i] = 0.0
            index = if (index + 1 >= fftSize) 0 else index + 1
        }
        fft(re, im)
        val binHz = sampleRate.toDouble() / fftSize
        val out = FloatArray(barCount)
        for (bar in 0 until barCount) {
            val low = minHz * Math.pow(maxHz / minHz, bar.toDouble() / barCount)
            val high = minHz * Math.pow(maxHz / minHz, (bar + 1).toDouble() / barCount)
            val lowBin = (low / binHz).toInt().coerceIn(1, fftSize / 2 - 1)
            val highBin = (high / binHz).toInt().coerceIn(lowBin + 1, fftSize / 2)
            var peak = 0.0
            for (bin in lowBin until highBin) {
                val magnitude = sqrt(re[bin] * re[bin] + im[bin] * im[bin]) / fftSize
                if (magnitude > peak) peak = magnitude
            }
            var db = 20.0 * kotlin.math.log10(peak + 1e-9)
            val octave = ln((low + high) / 2 / 100.0) / ln(2.0)
            db += octave * 4.0
            val level = ((db - minDb) / (maxDb - minDb)).coerceIn(0.0, 1.0).toFloat()
            val previous = smoothed[bar]
            smoothed[bar] = if (level > previous) {
                previous + (level - previous) * 0.8f
            } else {
                previous + (level - previous) * 0.35f
            }
            out[bar] = smoothed[bar]
        }
        _amplitudes.value = out
    }

    private fun fft(real: DoubleArray, imag: DoubleArray) {
        val n = real.size
        var j = 0
        for (i in 1 until n) {
            var bit = n shr 1
            while (j and bit != 0) {
                j = j xor bit
                bit = bit shr 1
            }
            j = j or bit
            if (i < j) {
                val tr = real[i]
                real[i] = real[j]
                real[j] = tr
                val ti = imag[i]
                imag[i] = imag[j]
                imag[j] = ti
            }
        }
        var length = 2
        while (length <= n) {
            val angle = -2.0 * PI / length
            val wRe = cos(angle)
            val wIm = sin(angle)
            var i = 0
            while (i < n) {
                var curRe = 1.0
                var curIm = 0.0
                for (k in 0 until length / 2) {
                    val nextRe = real[i + k + length / 2] * curRe - imag[i + k + length / 2] * curIm
                    val nextIm = real[i + k + length / 2] * curIm + imag[i + k + length / 2] * curRe
                    real[i + k + length / 2] = real[i + k] - nextRe
                    imag[i + k + length / 2] = imag[i + k] - nextIm
                    real[i + k] += nextRe
                    imag[i + k] += nextIm
                    val temp = curRe * wRe - curIm * wIm
                    curIm = curRe * wIm + curIm * wRe
                    curRe = temp
                }
                i += length
            }
            length = length shl 1
        }
    }

    fun stop(): ByteArray? {
        loopJob?.cancel()
        loopJob = null
        scope?.cancel()
        scope = null
        val recorder = record
        record = null
        try {
            recorder?.stop()
        } catch (throwable: Throwable) {
        }
        try {
            recorder?.release()
        } catch (throwable: Throwable) {
        }
        val samples = synchronized(pcm) { pcm.toShortArray() }
        if (samples.isEmpty()) return null
        return encodeWav(samples)
    }

    fun cancel() {
        loopJob?.cancel()
        loopJob = null
        scope?.cancel()
        scope = null
        val recorder = record
        record = null
        try {
            recorder?.stop()
        } catch (throwable: Throwable) {
        }
        try {
            recorder?.release()
        } catch (throwable: Throwable) {
        }
        synchronized(pcm) { pcm.clear() }
        _amplitudes.value = FloatArray(barCount)
    }

    private fun encodeWav(samples: ShortArray): ByteArray {
        val dataSize = samples.size * 2
        val header = ByteArray(44)
        fun writeInt(offset: Int, value: Int) {
            header[offset] = (value and 0xFF).toByte()
            header[offset + 1] = ((value shr 8) and 0xFF).toByte()
            header[offset + 2] = ((value shr 16) and 0xFF).toByte()
            header[offset + 3] = ((value shr 24) and 0xFF).toByte()
        }
        fun writeShort(offset: Int, value: Int) {
            header[offset] = (value and 0xFF).toByte()
            header[offset + 1] = ((value shr 8) and 0xFF).toByte()
        }
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        writeInt(4, 36 + dataSize)
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        writeInt(16, 16)
        writeShort(20, 1)
        writeShort(22, 1)
        writeInt(24, sampleRate)
        writeInt(28, sampleRate * 2)
        writeShort(32, 2)
        writeShort(34, 16)
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        writeInt(40, dataSize)
        val out = ByteArray(44 + dataSize)
        System.arraycopy(header, 0, out, 0, 44)
        var offset = 44
        for (sample in samples) {
            out[offset] = (sample.toInt() and 0xFF).toByte()
            out[offset + 1] = ((sample.toInt() shr 8) and 0xFF).toByte()
            offset += 2
        }
        return out
    }
}
