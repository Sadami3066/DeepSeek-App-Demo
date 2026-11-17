package com.opends.chat.core.net

import com.google.gson.JsonObject
import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.net.InetSocketAddress

class WireClientTest {

    private val server: HttpServer = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0).apply { start() }
    private val client = OkHttpClient()

    @After
    fun tearDown() {
        server.stop(0)
    }

    private fun client(): WireClient = WireClient(
        object : WireSettings {
            override fun apiKey(): String = "sk-test"
            override fun baseUrl(): String = "http://127.0.0.1:${server.address.port}"
        },
        client,
    )

    private fun route(
        path: String,
        handler: (com.sun.net.httpserver.HttpExchange) -> Unit,
    ) {
        server.createContext(path) { exchange ->
            exchange.use {
                handler(it)
                it.close()
            }
        }
    }

    private fun sse(exchange: com.sun.net.httpserver.HttpExchange, frames: List<String>) {
        exchange.responseHeaders.add("Content-Type", "text/event-stream; charset=utf-8")
        exchange.sendResponseHeaders(200, 0)
        val out = exchange.responseBody
        frames.forEach { frame ->
            out.write(frame.toByteArray(Charsets.UTF_8))
            out.flush()
            Thread.sleep(5)
        }
        out.close()
    }

    @Test
    fun parsesNamedSseUntilMessageStop() {
        route("/anthropic/v1/messages") { exchange ->
            sse(
                exchange,
                listOf(
                    ": keep-alive\n\n",
                    "event: message_start\ndata: {\"type\":\"message_start\",\"message\":{\"id\":\"m1\",\"usage\":{\"input_tokens\":7}}}\n\n",
                    "event: content_block_start\ndata: {\"type\":\"content_block_start\",\"index\":0,\"content_block\":{\"type\":\"text\",\"text\":\"\"}}\n\n",
                    "event: content_block_delta\ndata: {\"type\":\"content_block_delta\",\"index\":0,\"delta\":{\"type\":\"text_delta\",\"text\":\"你好\"}}\n\n",
                    "event: ping\ndata: {\"type\":\"ping\"}\n\n",
                    "event: content_block_stop\ndata: {\"type\":\"content_block_stop\",\"index\":0}\n\n",
                    "event: message_delta\ndata: {\"type\":\"message_delta\",\"delta\":{\"stop_reason\":\"end_turn\",\"title\":\"标题\"},\"usage\":{\"output_tokens\":3}}\n\n",
                    "event: message_stop\ndata: {\"type\":\"message_stop\"}\n\n",
                ),
            )
        }
        val events = ArrayList<Pair<String?, JsonObject>>()
        runBlocking {
            client().streamMessages(JsonObject(), object : StreamListener {
                override fun onEvent(event: String?, data: JsonObject) {
                    events.add(event to data)
                }
            })
        }
        assertTrue(events.any { it.first == "message_start" })
        assertTrue(events.any { it.first == "message_stop" })
        assertEquals("你好", events.first { it.first == "content_block_delta" }.second.obj("delta")?.string("text"))
        assertEquals("end_turn", events.first { it.first == "message_delta" }.second.obj("delta")?.string("stop_reason"))
    }

    @Test
    fun interruptedStreamThrowsInterrupted() {
        route("/anthropic/v1/messages") { exchange ->
            sse(
                exchange,
                listOf(
                    "event: message_start\ndata: {\"type\":\"message_start\",\"message\":{\"id\":\"m1\"}}\n\n",
                ),
            )
        }
        try {
            runBlocking {
                client().streamMessages(JsonObject(), object : StreamListener {
                    override fun onEvent(event: String?, data: JsonObject) = Unit
                })
            }
            fail("expected ApiException")
        } catch (error: ApiException) {
            assertEquals(ApiErrorKind.INTERRUPTED, error.kind)
        }
    }

    @Test
    fun streamErrorEventThrowsStreamKind() {
        route("/anthropic/v1/messages") { exchange ->
            sse(
                exchange,
                listOf(
                    "event: error\ndata: {\"type\":\"error\",\"error\":{\"type\":\"api_error\",\"message\":\"模型失败\"}}\n\n",
                ),
            )
        }
        try {
            runBlocking {
                client().streamMessages(JsonObject(), object : StreamListener {
                    override fun onEvent(event: String?, data: JsonObject) = Unit
                })
            }
            fail("expected ApiException")
        } catch (error: ApiException) {
            assertEquals(ApiErrorKind.STREAM, error.kind)
            assertEquals("模型失败", error.message)
        }
    }

    @Test
    fun httpErrorIsClassified() {
        route("/anthropic/v1/messages") { exchange ->
            val body = "{\"type\":\"error\",\"error\":{\"type\":\"authentication_error\",\"message\":\"invalid key\"}}"
                .toByteArray(Charsets.UTF_8)
            exchange.responseHeaders.add("Content-Type", "application/json")
            exchange.sendResponseHeaders(401, body.size.toLong())
            exchange.responseBody.write(body)
        }
        try {
            runBlocking {
                client().streamMessages(JsonObject(), object : StreamListener {
                    override fun onEvent(event: String?, data: JsonObject) = Unit
                })
            }
            fail("expected ApiException")
        } catch (error: ApiException) {
            assertEquals(401, error.status)
            assertEquals(ApiCondition.UNAUTHORIZED, error.condition)
            assertEquals("invalid key", error.message)
        }
    }

    @Test
    fun fileUploadPutsPurposeBeforeFileAndSendsBetaHeader() {
        var headerRecording: String? = null
        var bodyRecording = ""
        route("/anthropic/v1/files") { exchange ->
            headerRecording = exchange.requestHeaders.getFirst("anthropic-beta")
            bodyRecording = exchange.requestBody.readBytes().toString(Charsets.UTF_8)
            val body = "{\"id\":\"file-api-123\",\"type\":\"file\"}".toByteArray(Charsets.UTF_8)
            exchange.responseHeaders.add("Content-Type", "application/json")
            exchange.sendResponseHeaders(200, body.size.toLong())
            exchange.responseBody.write(body)
        }
        val fileId = runBlocking {
            client().uploadFile("hello".toByteArray(), "report.pdf", "application/pdf", "rich_layout_recognition")
        }
        assertEquals("file-api-123", fileId)
        assertEquals("files-api-2025-04-14", headerRecording)
        val purposeIndex = bodyRecording.indexOf("name=\"purpose\"")
        val fileIndex = bodyRecording.indexOf("name=\"file\"")
        assertTrue(purposeIndex >= 0)
        assertTrue(fileIndex > purposeIndex)
    }

    @Test
    fun asrReturnsText() {
        var authorization: String? = null
        route("/v1/tools/asr") { exchange ->
            authorization = exchange.requestHeaders.getFirst("Authorization")
            val body = "{\"request_id\":\"r1\",\"text\":\"你好\",\"duration_ms\":3200}".toByteArray(Charsets.UTF_8)
            exchange.responseHeaders.add("Content-Type", "application/json")
            exchange.sendResponseHeaders(200, body.size.toLong())
            exchange.responseBody.write(body)
        }
        val text = runBlocking { client().transcribe(ByteArray(44)) }
        assertEquals("你好", text)
        assertEquals("Bearer sk-test", authorization)
    }

    @Test
    fun modelsListIsParsed() {
        route("/models") { exchange ->
            val body = "{\"object\":\"list\",\"data\":[{\"id\":\"deepseek-flash\"}]}".toByteArray(Charsets.UTF_8)
            exchange.responseHeaders.add("Content-Type", "application/json")
            exchange.sendResponseHeaders(200, body.size.toLong())
            exchange.responseBody.write(body)
        }
        val models = runBlocking { client().listModels() }
        assertEquals(listOf("deepseek-flash"), models)
    }

    @Test
    fun ttsStreamsRawPcm() {
        route("/v1/tools/tts") { exchange ->
            exchange.responseHeaders.add("Content-Type", "audio/pcm")
            exchange.sendResponseHeaders(200, 0)
            exchange.responseBody.write(byteArrayOf(1, 2, 3, 4))
            exchange.responseBody.close()
        }
        val collected = ArrayList<Byte>()
        runBlocking {
            client().synthesize("你好", "stella") { chunk -> chunk.forEach { collected.add(it) } }
        }
        assertEquals(4, collected.size)
        assertNull(null)
    }

    private fun JsonObject.string(key: String): String? {
        val element = get(key) ?: return null
        if (!element.isJsonPrimitive) return null
        return element.asString
    }

    private fun JsonObject.obj(key: String): JsonObject? {
        val element = get(key) ?: return null
        return element.takeIf { it.isJsonObject }?.asJsonObject
    }
}
