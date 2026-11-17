package com.opends.chat.core.chat

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.opends.chat.core.MessageStatus
import com.opends.chat.core.Role
import com.opends.chat.core.SessionInfo
import com.opends.chat.core.TestPaths
import com.opends.chat.core.archive.ArchiveJson
import com.opends.chat.core.db.FragmentCodec
import com.opends.chat.core.db.StoredMessage
import com.opends.chat.core.net.StreamListener
import com.opends.chat.core.net.WireClient
import com.opends.chat.core.net.WireSettings
import com.opends.chat.core.util.array
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

class CitationTest {

    private val server: HttpServer = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0).apply { start() }

    @After
    fun tearDown() {
        server.stop(0)
    }

    private fun replayRealSample(): StreamAssembler {
        val sample = TestPaths.repoFile("refs/dumps/sse-real-sample.txt")
        assumeTrue("缺少真实 SSE 样例：$sample", sample.exists())
        val bytes = sample.readBytes()
        server.createContext("/anthropic/v1/messages") { exchange ->
            exchange.responseHeaders.add("Content-Type", "text/event-stream; charset=utf-8")
            exchange.sendResponseHeaders(200, bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
        }
        val assembler = StreamAssembler()
        runBlocking {
            WireClient(
                object : WireSettings {
                    override fun apiKey(): String = "sk-citation"
                    override fun baseUrl(): String = "http://127.0.0.1:${server.address.port}"
                },
                OkHttpClient(),
            ).streamMessages(JsonObject(), object : StreamListener {
                override fun onEvent(event: String?, data: JsonObject) {
                    assembler.onEvent(event, data)
                }
            })
        }
        return assembler
    }

    @Test
    fun realSampleCitationsAreParsedAndInsertedInline() {
        val assembler = replayRealSample()
        assertTrue(assembler.endedNormally)
        assertEquals(3, assembler.citationCount)

        val textBlocks = assembler.blocks.filterIsInstance<WireBlock.Text>()
        val citations = textBlocks.flatMap { it.citations }
        assertEquals(3, citations.size)
        assertEquals(listOf(1, 2, 3), citations.map { it.number })
        assertEquals(
            listOf(
                "https://developer.aliyun.com/article/1783452#1",
                "https://api-docs.deepseek.com/news/news261113/#1",
                "https://api-docs.deepseek.com/news/news261113.md#1",
            ),
            citations.map { it.url },
        )
        citations.forEach { citation ->
            assertNotNull(citation.title)
            assertNotNull(citation.citedText)
            val block = textBlocks.first { it.citations.contains(citation) }
            val text = block.text.toString()
            val marker = "[citation:${citation.number}]"
            assertTrue("正文应包含 $marker", text.contains(marker))
            if (!citation.citedText.isNullOrEmpty()) {
                assertTrue(
                    "标记应紧跟 cited_text 之后：$marker",
                    text.contains(citation.citedText + marker),
                )
            }
        }
        val numbered = textBlocks.flatMap { block -> block.citations.map { "[citation:${it.number}]" } }
        assertEquals(listOf("[citation:1]", "[citation:2]", "[citation:3]"), numbered)
    }

    @Test
    fun canonicalRoundTripKeepsCitationsAndMarkers() {
        val assembler = replayRealSample()
        val canonical = WireCodec.canonical(assembler.blocks)
        val encoded = FragmentCodec.encode(canonical)
        val parsed = FragmentCodec.parse(encoded)
        assertEquals(canonical.size, parsed.size)

        val loadedBlocks = WireCodec.blocks(parsed)
        val modelFragments = WireCodec.toModelFragments(loadedBlocks)
        val responses = modelFragments.filterIsInstance<com.opends.chat.core.Fragment.Response>()
        val citations = responses.flatMap { it.citations }
        assertEquals(3, citations.size)
        assertEquals(
            listOf(
                "https://developer.aliyun.com/article/1783452#1",
                "https://api-docs.deepseek.com/news/news261113/#1",
                "https://api-docs.deepseek.com/news/news261113.md#1",
            ),
            citations.map { it.url },
        )
        assertNotNull(citations[0].citedText)
        val body = responses.joinToString("") { it.content }
        assertTrue(body.contains("[citation:1]"))
        assertTrue(body.contains("[citation:2]"))
        assertTrue(body.contains("[citation:3]"))
    }

    @Test
    fun archiveExportStripsCitationMarkers() {
        val assembler = replayRealSample()
        val assistant = StoredMessage(
            id = 2,
            parentId = 1,
            role = Role.ASSISTANT,
            status = MessageStatus.FINISHED,
            thinkingEnabled = true,
            searchEnabled = true,
            prefix = false,
            accumulatedTokenUsage = 100,
            insertedAt = 1794878230000L,
            fragmentsJson = FragmentCodec.encode(WireCodec.canonical(assembler.blocks)),
        )
        val user = StoredMessage(
            id = 1,
            parentId = null,
            role = Role.USER,
            status = MessageStatus.FINISHED,
            thinkingEnabled = false,
            searchEnabled = true,
            prefix = false,
            accumulatedTokenUsage = 0,
            insertedAt = 1794878220000L,
            fragmentsJson = FragmentCodec.encode(
                WireCodec.userFragments("DeepSeek 昨天发布了什么？", emptyList())
            ),
        )
        val session = SessionInfo(
            id = "citation-session",
            title = "引用",
            currentMessageId = 2,
            insertedAt = 1794878220000L,
            updatedAt = 1794878246000L,
        )
        val exported = ArchiveJson.exportSession(session, listOf(user, assistant)) { _, _ -> null }
        val fragments = exported.json.getAsJsonArray("messages")[1].asJsonObject
            .getAsJsonArray("fragments").map { it.asJsonObject }
        val content = fragments.filter { it.string("type") == FragmentCodec.TYPE_RESPONSE }
            .joinToString("") { it.string("content") ?: "" }
        assertTrue(content.isNotBlank())
        assertFalse("归档导出正文不应含引用标记", content.contains("[citation:"))
        assertFalse("归档导出正文不应含 reference 标记", content.contains("[reference:"))
        val (reimported, _) = ArchiveJson.importSession(exported.json, "citation-session")
        val reimportedBody = reimported.messages[1].message.fragments
            .filterIsInstance<com.opends.chat.core.Fragment.Response>()
            .joinToString("") { it.content }
        assertFalse(reimportedBody.contains("[citation:"))
    }

    @Test
    fun contextReplayStripsMarkersForContinuation() {
        val assembler = replayRealSample()
        val content = WireCodec.assistantContextContent(assembler.blocks, includeThinking = false)
        assertNotNull(content)
        val text = content!!.filter { it.isJsonObject && it.asJsonObject.string("type") == "text" }
            .joinToString("") { it.asJsonObject.string("text") ?: "" }
        assertTrue(text.isNotBlank())
        assertFalse("续写回传正文不应含 [citation:N]", text.contains("[citation:"))
        assertEquals(0, text.count { it == '[' && false })
    }

    @Test
    fun markerFallbackAndOrdering() {
        val assembler = StreamAssembler()
        assembler.onEvent("content_block_start", JsonObject().apply {
            addProperty("type", "content_block_start")
            addProperty("index", 0)
            add("content_block", JsonObject().apply {
                addProperty("type", "text")
                addProperty("text", "")
            })
        })
        assembler.onEvent("content_block_delta", JsonObject().apply {
            addProperty("type", "content_block_delta")
            addProperty("index", 0)
            add("delta", JsonObject().apply {
                addProperty("type", "text_delta")
                addProperty("text", "hello world")
            })
        })
        assembler.onEvent("content_block_delta", citationDelta(0, "https://a.example/1", "world"))
        assembler.onEvent("content_block_delta", citationDelta(0, "https://a.example/2", "hello"))
        assembler.onEvent("content_block_delta", citationDelta(0, "https://a.example/3", "not-present"))
        val text = assembler.blocks.filterIsInstance<WireBlock.Text>().single().text.toString()
        assertEquals("hello[citation:2] world[citation:1][citation:3]", text)
        assertEquals(3, assembler.citationCount)
        assertEquals(
            listOf("https://a.example/1", "https://a.example/2", "https://a.example/3"),
            assembler.blocks.filterIsInstance<WireBlock.Text>().flatMap { block ->
                block.citations.map { it.url }
            },
        )
    }

    private fun citationDelta(index: Int, url: String, citedText: String?): JsonObject = JsonObject().apply {
        addProperty("type", "content_block_delta")
        addProperty("index", index)
        add("delta", JsonObject().apply {
            addProperty("type", "citations_delta")
            add("citation", JsonObject().apply {
                addProperty("type", "web_search_result_location")
                addProperty("url", url)
                addProperty("title", "t-$url")
                addProperty("encrypted_index", "")
                if (citedText != null) addProperty("cited_text", citedText)
            })
        })
    }

    @Test
    fun modelResponseCarriesCitationMetadata() {
        val assembler = StreamAssembler()
        assembler.onEvent("content_block_start", JsonObject().apply {
            addProperty("type", "content_block_start")
            addProperty("index", 0)
            add("content_block", JsonObject().apply {
                addProperty("type", "text")
                addProperty("text", "")
            })
        })
        assembler.onEvent("content_block_delta", JsonObject().apply {
            addProperty("type", "content_block_delta")
            addProperty("index", 0)
            add("delta", JsonObject().apply {
                addProperty("type", "text_delta")
                addProperty("text", "引用来源说明")
            })
        })
        assembler.onEvent(
            "content_block_delta",
            citationDelta(0, "https://b.example/x", "引用来源说明"),
        )
        val responses = WireCodec.toModelFragments(assembler.blocks)
            .filterIsInstance<com.opends.chat.core.Fragment.Response>()
        assertEquals(1, responses.size)
        assertEquals(1, responses[0].citations.size)
        assertEquals("https://b.example/x", responses[0].citations[0].url)
        assertEquals("引用来源说明[citation:1]", responses[0].content)
    }
}
