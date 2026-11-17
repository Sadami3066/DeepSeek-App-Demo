package com.opends.chat.core.chat

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.opends.chat.core.util.array
import com.opends.chat.core.util.string
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatRequestBuilderTest {

    private fun messages(): JsonArray = JsonArray()

    @Test
    fun bodyMatchesChatDemoWithoutSafetyCheckAndMaxTokens() {
        val body = ChatRequestBuilder.build(
            messages = messages(),
            thinkingEnabled = false,
            searchEnabled = false,
            titleEnabled = false,
        )
        assertEquals(
            setOf("model", "system", "messages", "thinking", "tools", "stream"),
            body.keySet(),
        )
        assertEquals("deepseek-flash", body.string("model"))
        assertEquals("该助手为DeepSeek，由深度求索公司创造。", body.string("system"))
        assertEquals(true, body.get("stream").asBoolean)
        assertEquals("disabled", body.getAsJsonObject("thinking").string("type"))
        assertNull(body.get("safety_check"))
        assertNull(body.get("max_tokens"))
        assertNull(body.get("title"))
        assertNull(body.get("output_config"))
        val tools = body.array("tools")!!
        assertEquals(1, tools.size())
        assertEquals("deepseek_read_url", tools[0].asJsonObject.string("type"))
        assertEquals("deepseek_read_url", tools[0].asJsonObject.string("name"))
    }

    @Test
    fun searchAndTitleAreConditional() {
        val body = ChatRequestBuilder.build(
            messages = messages(),
            thinkingEnabled = true,
            searchEnabled = true,
            titleEnabled = true,
        )
        assertEquals("enabled", body.getAsJsonObject("thinking").string("type"))
        val tools = body.array("tools")!!
        assertEquals(
            listOf("deepseek_read_url", "deepseek_web_search"),
            tools.map { it.asJsonObject.string("type") },
        )
        assertEquals(
            listOf("deepseek_read_url", "deepseek_web_search"),
            tools.map { it.asJsonObject.string("name") },
        )
        assertTrue(body.getAsJsonObject("title").get("enabled").asBoolean)
        assertNull(body.get("safety_check"))
        assertNull(body.get("max_tokens"))
    }

    @Test
    fun messagesArrayIsPassedThroughUnchanged() {
        val messages = JsonArray().apply {
            add(JsonObject().apply {
                addProperty("role", "latest_reminder")
                addProperty("content", "2026-11-17,星期一,广东,App,中文")
            })
            add(JsonObject().apply {
                addProperty("role", "user")
                addProperty("content", "你好")
            })
        }
        val body = ChatRequestBuilder.build(messages, true, false, false)
        assertEquals(messages, body.getAsJsonArray("messages"))
    }
}
