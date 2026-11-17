package com.opends.chat.core.net

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.opends.chat.core.chat.StreamAssembler
import com.opends.chat.core.util.string
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.File
import java.util.concurrent.TimeUnit

class RealApiTest {

    private val realApiEnabled: Boolean
        get() = System.getProperty("opends.realApi") == "1"

    private fun testKey(): String? {
        if (!realApiEnabled) return null
        val env = System.getenv("OPENDS_TEST_API_KEY")
        if (!env.isNullOrBlank()) return env.trim()
        val filePath = System.getProperty("opends.test.key.file")
            ?: System.getenv("OPENDS_TEST_KEY_FILE")
            ?: "/tmp/.opends_test_key"
        return runCatching { File(filePath).readText().trim() }
            .getOrNull()
            ?.takeIf { it.isNotBlank() }
    }

    private fun settings(key: String): WireSettings = object : WireSettings {
        override fun apiKey(): String = key
        override fun baseUrl(): String = "https://api.deepseek.com"
    }

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(180, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    @Test
    fun modelsEndpointListsDeepseekFlash() {
        val key = testKey()
        assumeTrue("未开启真实 API 联调（-PopendsRealApi=1 且配置测试 Key），跳过", key != null)
        val models = runBlocking { WireClient(settings(key!!), client).listModels() }
        assertTrue("模型列表应包含 deepseek-flash，实际 $models", models.contains("deepseek-flash"))
    }

    @Test
    fun minimalStreamingChatCompletesWithMessageStop() {
        val key = testKey()
        assumeTrue("未开启真实 API 联调（-PopendsRealApi=1 且配置测试 Key），跳过", key != null)
        val body = JsonObject().apply {
            addProperty("model", "deepseek-flash")
            addProperty("system", "该助手为DeepSeek，由深度求索公司创造。")
            add(
                "messages",
                JsonArray().apply {
                    add(JsonObject().apply {
                        addProperty("role", "user")
                        addProperty("content", "用一句话介绍你自己")
                    })
                },
            )
            add("thinking", JsonObject().apply { addProperty("type", "disabled") })
            add("title", JsonObject().apply { addProperty("enabled", true) })
            addProperty("max_tokens", 64)
            addProperty("stream", true)
        }
        val assembler = StreamAssembler()
        runBlocking {
            WireClient(settings(key!!), client).streamMessages(body, object : StreamListener {
                override fun onEvent(event: String?, data: JsonObject) {
                    assembler.onEvent(event, data)
                }
            })
        }
        assertTrue(assembler.endedNormally)
        assertNotNull(assembler.stopReason)
        assertFalse(assembler.blocks.isEmpty())
        val text = com.opends.chat.core.chat.WireCodec.plainText(assembler.blocks)
        assertTrue("应收到正文或标题", text.isNotBlank() || !assembler.title.isNullOrBlank())
        println(
            "[RealApiTest] stop=${assembler.stopReason} title=${assembler.title} " +
                "usage=${assembler.totalUsage()} text=${text.take(80)}"
        )
    }
}
