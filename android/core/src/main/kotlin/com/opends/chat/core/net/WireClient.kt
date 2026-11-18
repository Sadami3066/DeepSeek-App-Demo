package com.opends.chat.core.net

import com.google.gson.Gson
import com.opends.chat.core.ErrorLog
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.opends.chat.core.util.array
import com.opends.chat.core.util.number
import com.opends.chat.core.util.obj
import com.opends.chat.core.util.string
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.BufferedSource
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger

internal interface StreamListener {
    fun onEvent(event: String?, data: JsonObject)
}

internal class WireClient(
    private val settings: WireSettings,
    private val client: OkHttpClient,
) {
    private val gson = Gson()

    @Volatile
    private var messageCall: Call? = null

    @Volatile
    private var speechCall: Call? = null

    fun invalidate() = Unit

    fun hasApiKey(): Boolean = settings.apiKey().isNotEmpty()

    suspend fun validateKey() {
        listModels()
    }

    suspend fun listModels(): List<String> = listModelsWith(requireKey(), baseUrl())

    suspend fun validateKeyWith(key: String, baseUrl: String) {
        listModelsWith(key.trim(), baseUrl.trim().trimEnd('/'))
    }

    suspend fun listModelsWith(key: String, baseUrl: String): List<String> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(baseUrl + "/models")
            .header("Authorization", "Bearer $key")
            .header("Accept", "application/json")
            .get()
            .build()
        execute(request, ENDPOINT_MODELS).use { response ->
            if (!response.isSuccessful) throw httpException(response, ENDPOINT_MODELS)
            val body = response.body?.string().orEmpty()
            val root = runCatching { JsonParser.parseString(body) }.getOrNull()
                ?: throw ApiException.protocol("模型列表响应无法解析")
            root.takeIf { it.isJsonObject }?.asJsonObject?.array("data")
                ?.mapNotNull { it.takeIf { item -> item.isJsonObject }?.asJsonObject?.string("id") }
                ?: emptyList()
        }
    }

    suspend fun streamMessages(
        body: JsonObject,
        listener: StreamListener,
        extraHeaders: Map<String, String> = emptyMap(),
    ) = withContext(Dispatchers.IO) {
        val key = requireKey()
        val builder = Request.Builder()
            .url(url(ENDPOINT_MESSAGES))
            .header("x-api-key", key)
            .header("Accept", "text/event-stream")
            .header("Content-Type", "application/json")
        extraHeaders.forEach { (name, value) -> builder.header(name, value) }
        val request = builder.post(gson.toJson(body).toRequestBody(JSON_MEDIA)).build()
        val call = client.newCall(request)
        messageCall = call
        try {
            val response = try {
                call.execute()
            } catch (error: IOException) {
                if (call.isCanceled()) throw ApiException.interrupted("已停止生成")
                throw ApiException.network(error, ENDPOINT_MESSAGES)
            }
            response.use { resp ->
                if (!resp.isSuccessful) throw httpException(resp, ENDPOINT_MESSAGES)
                val contentType = resp.header("Content-Type").orEmpty()
                if (!contentType.contains("text/event-stream", ignoreCase = true)) {
                    val raw = runCatching { resp.body?.string() }.getOrNull().orEmpty()
                    ErrorLog.record(
                        category = "接口",
                        summary = "$ENDPOINT_MESSAGES 返回非流式响应",
                        detail = "content-type=$contentType, body=${raw.take(400)}",
                    )
                    throw ApiException.stream(apiErrorMessage(raw) ?: "响应格式异常")
                }
                val source = resp.body?.source() ?: throw ApiException.protocol("响应内容为空")
                try {
                    readSse(source, listener)
                } catch (error: IOException) {
                    if (call.isCanceled()) throw ApiException.interrupted("已停止生成")
                    throw ApiException.network(error, ENDPOINT_MESSAGES)
                }
            }
        } finally {
            messageCall = null
        }
    }

    private fun readSse(source: BufferedSource, listener: StreamListener) {
        var eventName: String? = null
        val data = StringBuilder()
        var sawStop = false

        fun dispatch() {
            if (data.isNotEmpty()) {
                val payload = data.toString()
                data.setLength(0)
                val name = eventName
                eventName = null
                val parsed = runCatching { JsonParser.parseString(payload) }.getOrNull()
                if (parsed == null) {
                    if (name == "error") throw ApiException.stream("生成失败")
                    return
                }
                if (!parsed.isJsonObject) return
                val json = parsed.asJsonObject
                val type = name ?: json.string("type")
                if (type == "message_stop") sawStop = true
                if (type == "error" || (type == null && json.get("error") != null)) {
                    val error = json.obj("error")
                    val message = error?.string("message") ?: json.string("message") ?: "生成失败，请稍后重试"
                    throw ApiException.stream(message)
                }
                listener.onEvent(type, json)
            } else {
                eventName = null
            }
        }

        while (true) {
            val line = source.readUtf8Line() ?: break
            when {
                line.isEmpty() -> dispatch()
                line.startsWith(":") -> Unit
                line.startsWith("event:") -> eventName = line.removePrefix("event:").trim()
                line.startsWith("data:") -> {
                    if (data.isNotEmpty()) data.append('\n')
                    data.append(line.removePrefix("data:").trimStart())
                }
                line.startsWith("retry:") -> Unit
                line.startsWith("id:") -> Unit
            }
        }
        if (!sawStop) throw ApiException.interrupted("生成已中断")
    }

    suspend fun uploadFile(
        bytes: ByteArray,
        fileName: String,
        mime: String,
        purpose: String?,
    ): String = withContext(Dispatchers.IO) {
        var attempt = 0
        while (true) {
            try {
                return@withContext uploadOnce(bytes, fileName, mime, purpose)
            } catch (error: ApiException) {
                val retryable = error.retryable || error.isRetryable
                if (attempt >= 2 || !retryable) throw error
                delay(1000L shl attempt)
                attempt += 1
            }
        }
        @Suppress("UNREACHABLE_CODE")
        ""
    }

    private fun uploadOnce(bytes: ByteArray, fileName: String, mime: String, purpose: String?): String {
        val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
        if (!purpose.isNullOrEmpty()) builder.addFormDataPart("purpose", purpose)
        val media = mime.takeIf { it.isNotBlank() }?.toMediaTypeOrNull() ?: OCTET_STREAM
        builder.addFormDataPart("file", fileName, bytes.toRequestBody(media))
        val request = Request.Builder()
            .url(url(ENDPOINT_FILES))
            .header("x-api-key", requireKey())
            .header("anthropic-beta", FILES_BETA)
            .post(builder.build())
            .build()
        execute(request, ENDPOINT_FILES).use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) throw httpException(response.code, response.message, body, ENDPOINT_FILES)
            val json = runCatching { JsonParser.parseString(body) }.getOrNull()?.takeIf { it.isJsonObject }
                ?: throw ApiException.protocol("文件上传响应无法解析")
            return json.asJsonObject.string("id") ?: throw ApiException.protocol("文件上传响应缺少 id")
        }
    }

    suspend fun transcribe(wav: ByteArray): String = withContext(Dispatchers.IO) {
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", "recording.wav", wav.toRequestBody(AUDIO_WAV))
            .build()
        val request = Request.Builder()
            .url(url(ENDPOINT_ASR))
            .header("Authorization", "Bearer ${requireKey()}")
            .post(body)
            .build()
        var attempt = 0
        var text: String? = null
        while (text == null) {
            try {
                execute(request, ENDPOINT_ASR).use { response ->
                    val raw = response.body?.string().orEmpty()
                    if (!response.isSuccessful) throw httpException(response.code, response.message, raw, ENDPOINT_ASR)
                    val json = runCatching { JsonParser.parseString(raw) }.getOrNull()?.takeIf { it.isJsonObject }
                        ?: throw ApiException.protocol("语音识别响应无法解析")
                    if (json.asJsonObject.get("error") != null) {
                        throw ApiException.stream(apiErrorMessage(raw) ?: "语音识别失败")
                    }
                    text = json.asJsonObject.string("text")
                        ?: throw ApiException.protocol("语音识别响应缺少 text")
                }
            } catch (error: ApiException) {
                if (attempt >= 2 || !error.isRetryable) throw error
                delay(1000L shl attempt)
                attempt += 1
            }
        }
        text ?: ""
    }

    suspend fun synthesize(
        text: String,
        voiceId: String,
        onChunk: (ByteArray) -> Unit,
    ) = withContext(Dispatchers.IO) {
        val payload = JsonObject().apply {
            addProperty("text", text)
            addProperty("voice_id", voiceId)
            addProperty("format", "pcm")
            addProperty("filter_markdown", true)
        }
        val request = Request.Builder()
            .url(url(ENDPOINT_TTS))
            .header("Authorization", "Bearer ${requireKey()}")
            .header("Accept", "*/*")
            .post(gson.toJson(payload).toRequestBody(JSON_MEDIA))
            .build()
        val call = client.newCall(request)
        speechCall = call
        try {
            val response = try {
                call.execute()
            } catch (error: IOException) {
                if (call.isCanceled()) throw ApiException.interrupted("朗读已停止")
                throw ApiException.network(error, ENDPOINT_TTS)
            }
            response.use { resp ->
                if (!resp.isSuccessful) {
                    val raw = runCatching { resp.body?.string() }.getOrNull().orEmpty()
                    throw httpException(resp.code, resp.message, raw, ENDPOINT_TTS)
                }
                val stream = resp.body?.byteStream() ?: throw ApiException.protocol("朗读响应为空")
                val buffer = ByteArray(8192)
                try {
                    while (true) {
                        val read = stream.read(buffer)
                        if (read < 0) break
                        if (read > 0) onChunk(buffer.copyOf(read))
                    }
                } catch (error: IOException) {
                    if (call.isCanceled()) throw ApiException.interrupted("朗读已停止")
                    throw ApiException.network(error, ENDPOINT_TTS)
                }
            }
        } finally {
            speechCall = null
        }
    }

    fun cancelMessage() {
        messageCall?.cancel()
    }

    fun cancelSpeech() {
        speechCall?.cancel()
    }

    private fun execute(request: Request, endpoint: String): Response = try {
        client.newCall(request).execute()
    } catch (error: IOException) {
        throw ApiException.network(error, endpoint)
    }

    private fun requireKey(): String {
        val key = settings.apiKey().trim()
        if (key.isEmpty()) throw ApiException(ApiErrorKind.HTTP, "请先在设置中填写 API Key", 401)
        return key
    }

    private fun baseUrl(): String = settings.baseUrl().trim().trimEnd('/').ifEmpty { "https://api.deepseek.com" }

    private fun url(path: String): String = baseUrl() + path

    private fun httpException(response: Response, endpoint: String): ApiException = httpException(
        response.code,
        response.message,
        runCatching { response.body?.string() }.getOrNull().orEmpty(),
        endpoint,
    )

    private fun httpException(code: Int, message: String, body: String, endpoint: String): ApiException =
        ApiException.http(code, message, body, endpoint)

    companion object {
        const val FILES_BETA = "files-api-2025-04-14"
        private const val ENDPOINT_MODELS = "/models"
        private const val ENDPOINT_MESSAGES = "/anthropic/v1/messages"
        private const val ENDPOINT_FILES = "/anthropic/v1/files"
        private const val ENDPOINT_ASR = "/v1/tools/asr"
        private const val ENDPOINT_TTS = "/v1/tools/tts"
        private val JSON_MEDIA: MediaType = "application/json".toMediaType()
        private val AUDIO_WAV: MediaType = "audio/wav".toMediaType()
        private val OCTET_STREAM: MediaType = "application/octet-stream".toMediaType()
        private val requestSeq = AtomicInteger(0)
    }
}

private fun String.toMediaTypeOrNull(): MediaType? = runCatching { toMediaType() }.getOrNull()
