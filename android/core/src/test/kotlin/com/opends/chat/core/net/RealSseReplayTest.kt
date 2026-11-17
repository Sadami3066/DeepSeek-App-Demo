package com.opends.chat.core.net

import com.google.gson.JsonObject
import com.opends.chat.core.TestPaths
import com.opends.chat.core.db.FragmentCodec
import com.opends.chat.core.chat.StreamAssembler
import com.opends.chat.core.chat.WireBlock
import com.opends.chat.core.chat.WireCodec
import com.opends.chat.core.util.string
import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.net.InetSocketAddress

class RealSseReplayTest {

    private val server: HttpServer = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0).apply { start() }

    @After
    fun tearDown() {
        server.stop(0)
    }

    private fun client(): WireClient = WireClient(
        object : WireSettings {
            override fun apiKey(): String = "sk-replay"
            override fun baseUrl(): String = "http://127.0.0.1:${server.address.port}"
        },
        OkHttpClient(),
    )

    @Test
    fun replaysRealSampleIntoBlocks() {
        val sample = TestPaths.repoFile("refs/dumps/sse-real-sample.txt")
        assumeTrue("缺少真实 SSE 样例：$sample", sample.exists())
        val bytes = sample.readBytes()
        server.createContext("/anthropic/v1/messages") { exchange ->
            exchange.responseHeaders.add("Content-Type", "text/event-stream; charset=utf-8")
            exchange.sendResponseHeaders(200, bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
        }
        val events = ArrayList<Pair<String?, JsonObject>>()
        runBlocking {
            client().streamMessages(JsonObject(), object : StreamListener {
                override fun onEvent(event: String?, data: JsonObject) {
                    events.add(event to data)
                }
            })
        }
        assertEquals(558, events.size)
        assertEquals(1, events.count { it.first == "message_start" })
        assertEquals(17, events.count { it.first == "content_block_start" })
        assertEquals(520, events.count { it.first == "content_block_delta" })
        assertEquals(17, events.count { it.first == "content_block_stop" })
        assertEquals(1, events.count { it.first == "ping" })
        assertEquals(1, events.count { it.first == "message_delta" })
        assertEquals(1, events.count { it.first == "message_stop" })

        val assembler = StreamAssembler()
        events.forEach { (eventName, data) -> assembler.onEvent(eventName, data) }
        assertTrue(assembler.endedNormally)
        assertEquals("end_turn", assembler.stopReason)
        assertEquals("DeepSeek开源App API", assembler.title)
        assertEquals(9533, assembler.inputTokens)
        assertEquals(5504, assembler.cacheReadTokens)
        assertEquals(749, assembler.outputTokens)
        assertEquals(15786, assembler.totalUsage())
        assertFalse(assembler.thinkingActive)
        assertEquals(14, assembler.blocks.size)

        val kinds = assembler.blocks.map { block -> category(block) }
        assertEquals(
            listOf(
                "thinking", "tool", "tool", "thinking",
                "tool", "tool", "tool", "tool",
                "tool", "thinking", "text", "text", "text", "text",
            ),
            kinds,
        )

        val toolBlocks = assembler.blocks.filterIsInstance<WireBlock.Atomic>().map { it.json }
        val searchCalls = toolBlocks.filter {
            it.string("type") == "server_tool_use" && it.getAsJsonObject("input")?.string("action") == "search"
        }
        assertEquals(2, searchCalls.size)
        searchCalls.forEach {
            assertEquals("deepseek_web_search", it.string("name"))
            assertEquals("deep_search", it.string("kind"))
            assertNotNull(it.getAsJsonObject("input")?.getAsJsonArray("queries"))
        }
        val openPages = assembler.blocks.filterIsInstance<WireBlock.OpenPage>()
        assertEquals(3, openPages.size)
        openPages.forEach { page ->
            assertEquals("FINISHED", page.status)
            assertEquals(2, page.stageId)
            assertNotNull(page.result)
            assertTrue(page.result!!.string("url")!!.startsWith("http"))
            assertNotNull(page.result!!.string("title"))
        }

        val searchResults = toolBlocks.filter { it.string("type") == "web_search_tool_result" }
        assertEquals(2, searchResults.size)
        assertEquals(23, searchResults[0].getAsJsonArray("content").size())
        assertEquals(15, searchResults[1].getAsJsonArray("content").size())
        val firstResult = searchResults[0].getAsJsonArray("content")[0].asJsonObject
        assertEquals("web_search_result", firstResult.string("type"))
        assertNotNull(firstResult.string("encrypted_content"))
        assertNotNull(firstResult.string("page_age"))
        assertNotNull(firstResult.string("site_name"))
        assertNotNull(firstResult.string("site_icon"))
        assertTrue(firstResult.string("url")!!.startsWith("http"))

        assertEquals(0, toolBlocks.count { it.string("type") == "web_fetch_tool_result" })

        val modelFragments = WireCodec.toModelFragments(assembler.blocks)
        assertEquals(3, modelFragments.count { it is com.opends.chat.core.Fragment.Think })
        assertEquals(4, modelFragments.count { it is com.opends.chat.core.Fragment.Response })
        assertEquals(0, modelFragments.count { it is com.opends.chat.core.Fragment.ReadLink })
        val searches = modelFragments.filterIsInstance<com.opends.chat.core.Fragment.Search>()
        assertEquals(2, searches.size)
        assertEquals(23, searches[0].results.size)
        assertEquals(15, searches[1].results.size)
        assertEquals(3, searches[0].queries.size)
        assertEquals(2, searches[1].queries.size)
        assertTrue(searches[0].results.any { it.url.contains("api-docs.deepseek.com") })
        val toolOpens = modelFragments.filterIsInstance<com.opends.chat.core.Fragment.ToolOpen>()
        assertEquals(3, toolOpens.size)
        toolOpens.forEach { open ->
            assertEquals("FINISHED", open.status)
            assertEquals(2, open.stageId)
            assertTrue(open.result!!.url!!.startsWith("http"))
        }
        assertEquals(0, modelFragments.count { it is com.opends.chat.core.Fragment.ToolFind })

        val canonical = WireCodec.canonical(assembler.blocks)
        assertEquals(14, canonical.size)
        val openJson = canonical.filter { it.string("type") == com.opends.chat.core.db.FragmentCodec.TYPE_TOOL_OPEN }
        assertEquals(3, openJson.size)
        openJson.forEach { open ->
            assertEquals("FINISHED", open.string("status"))
            assertEquals(2, open.get("stage_id").asInt)
            assertTrue(open.getAsJsonObject("result").string("url")!!.startsWith("http"))
        }
        val thinks = canonical.filter { it.string("type") == WireCodec.TYPE_THINK }
        assertEquals(3, thinks.size)
        thinks.forEach { assertNotNull(it.get("elapsed_secs")) }
        val responses = canonical.filter { it.string("type") == WireCodec.TYPE_RESPONSE }
        assertTrue(responses.joinToString("") { it.string("content") ?: "" }.isNotBlank())

        val thinkStageIds = canonical.filter { it.string("type") == WireCodec.TYPE_THINK }
            .map { it.get("stage_id").asInt }
        assertEquals(listOf(1, 2, 3), thinkStageIds)
        val searchStageIds = canonical.filter {
            it.string("type") == WireCodec.TYPE_SERVER_TOOL_USE &&
                it.getAsJsonObject("block").getAsJsonObject("input").string("action") == "search"
        }.map { it.get("stage_id").asInt }
        assertEquals(listOf(1, 2), searchStageIds)
        val openStageIds = openJson.map { it.get("stage_id").asInt }
        assertEquals(listOf(2, 2, 2), openStageIds)
        assertEquals(listOf(1, 2), searches.stream().map { it.stageId }.toList())

        val restored = WireCodec.blocks(FragmentCodec.parse(FragmentCodec.encode(canonical)))
        val restoredSearches = WireCodec.toModelFragments(restored)
            .filterIsInstance<com.opends.chat.core.Fragment.Search>()
        assertEquals(listOf(1, 2), restoredSearches.map { it.stageId })
    }

    private fun category(block: WireBlock): String = when (block) {
        is WireBlock.Thinking -> "thinking"
        is WireBlock.Text -> "text"
        is WireBlock.Template -> "template"
        is WireBlock.Atomic -> "tool"
        is WireBlock.OpenPage -> "tool"
        is WireBlock.FindPage -> "tool"
    }
}
