package com.opends.chat.core.chat

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.opends.chat.core.Fragment
import com.opends.chat.core.MessageStatus
import com.opends.chat.core.Role
import com.opends.chat.core.SessionInfo
import com.opends.chat.core.archive.ArchiveJson
import com.opends.chat.core.db.FragmentCodec
import com.opends.chat.core.db.StoredMessage
import com.opends.chat.core.util.array
import com.opends.chat.core.util.string
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ToolStepChainTest {

    @Test
    fun officialToolFragmentsDecodeToModelFragments() {
        val official = listOf(
            toolOpen(status = "FINISHED", stageId = 2, url = "https://example.com/a", title = "页面A", withResult = true),
            toolOpen(status = "FINISHED", stageId = 2, url = null, title = null, withResult = false),
            toolOpen(status = "WIP", stageId = 2, url = "https://example.com/b", title = "页面B", withResult = false),
            toolFind(status = "NO_RESULT", stageId = 3, pattern = "关键词"),
            toolFind(status = "WIP", stageId = 3, pattern = "另一个"),
        )
        val fragments = FragmentCodec.toFragments(official)
        val opens = fragments.filterIsInstance<Fragment.ToolOpen>()
        assertEquals(3, opens.size)
        assertEquals("FINISHED", opens[0].status)
        assertEquals(2, opens[0].stageId)
        assertEquals("https://example.com/a", opens[0].result!!.url)
        assertEquals("页面A", opens[0].result!!.title)
        assertNotNull(opens[0].result!!.publishedAt)
        assertTrue(opens[0].result!!.publishedAt!!.startsWith("2026-"))
        assertEquals("FINISHED", opens[1].status)
        assertNull(opens[1].result)
        assertEquals("WIP", opens[2].status)

        val finds = fragments.filterIsInstance<Fragment.ToolFind>()
        assertEquals(2, finds.size)
        assertEquals("NO_RESULT", finds[0].status)
        assertEquals("关键词", finds[0].pattern)
        assertEquals(3, finds[0].stageId)
        assertEquals("WIP", finds[1].status)
    }

    @Test
    fun canonicalRoundTripPreservesToolStatusResultAndStage() {
        val official = listOf(
            toolOpen(status = "FINISHED", stageId = 2, url = "https://example.com/a", title = "页面A", withResult = true),
            toolOpen(status = "FINISHED", stageId = 2, url = null, title = null, withResult = false),
            toolOpen(status = "FAILED", stageId = 2, url = "https://example.com/c", title = "失败页", withResult = false),
            toolFind(status = "NO_RESULT", stageId = 3, pattern = "关键词"),
        )
        val blocks = WireCodec.fromOfficialFragments(official, messageId = 9)
        val canonical = WireCodec.canonical(blocks)
        val types = canonical.map { it.string("type") }
        assertEquals(
            listOf(
                FragmentCodec.TYPE_TOOL_OPEN,
                FragmentCodec.TYPE_TOOL_OPEN,
                FragmentCodec.TYPE_TOOL_OPEN,
                FragmentCodec.TYPE_TOOL_FIND,
            ),
            types,
        )
        assertEquals("FINISHED", canonical[0].string("status"))
        assertEquals(2, canonical[0].get("stage_id").asInt)
        assertTrue(canonical[0].getAsJsonObject("result").string("url")!!.startsWith("https://"))
        assertTrue(canonical[1].get("result").isJsonNull)
        assertEquals("FAILED", canonical[2].string("status"))

        val reloaded = WireCodec.blocks(FragmentCodec.parse(FragmentCodec.encode(canonical)))
        val model = WireCodec.toModelFragments(reloaded)
        val opens = model.filterIsInstance<Fragment.ToolOpen>()
        assertEquals(3, opens.size)
        assertEquals(listOf("FINISHED", "FINISHED", "FAILED"), opens.map { it.status })
        assertEquals(listOf(2, 2, 2), opens.map { it.stageId })
        assertEquals("页面A", opens[0].result!!.title)
        assertNull(opens[1].result)
        val find = model.filterIsInstance<Fragment.ToolFind>().single()
        assertEquals("NO_RESULT", find.status)
        assertEquals("关键词", find.pattern)
        assertEquals(3, find.stageId)
    }

    @Test
    fun archiveExportAndReimportKeepToolFragments() {
        val official = listOf(
            toolOpen(status = "FINISHED", stageId = 2, url = "https://example.com/a", title = "页面A", withResult = true),
            toolOpen(status = "FINISHED", stageId = 2, url = null, title = null, withResult = false),
            toolFind(status = "NO_RESULT", stageId = 3, pattern = "关键词"),
        )
        val assistant = StoredMessage(
            id = 2,
            parentId = 1,
            role = Role.ASSISTANT,
            status = MessageStatus.FINISHED,
            thinkingEnabled = true,
            searchEnabled = true,
            prefix = false,
            accumulatedTokenUsage = 10,
            insertedAt = 1794878230000L,
            fragmentsJson = FragmentCodec.encode(WireCodec.canonical(WireCodec.fromOfficialFragments(official, 2))),
        )
        val user = StoredMessage(
            id = 1,
            parentId = null,
            role = Role.USER,
            status = MessageStatus.FINISHED,
            thinkingEnabled = true,
            searchEnabled = true,
            prefix = false,
            accumulatedTokenUsage = 0,
            insertedAt = 1794878220000L,
            fragmentsJson = FragmentCodec.encode(WireCodec.userFragments("搜索", emptyList())),
        )
        val session = SessionInfo(
            id = "tool-session",
            title = "工具",
            currentMessageId = 2,
            insertedAt = 1794878220000L,
            updatedAt = 1794878246000L,
        )
        val exported = ArchiveJson.exportSession(session, listOf(user, assistant)) { _, _ -> null }
        val fragments = exported.json.getAsJsonArray("messages")[1].asJsonObject
            .getAsJsonArray("fragments").map { it.asJsonObject }
        val openFragments = fragments.filter { it.string("type") == FragmentCodec.TYPE_TOOL_OPEN }
        assertEquals(2, openFragments.size)
        assertEquals("FINISHED", openFragments[0].string("status"))
        assertEquals(2, openFragments[0].get("stage_id").asInt)
        assertEquals("https://example.com/a", openFragments[0].string("url"))
        assertEquals("页面A", openFragments[0].string("title"))
        assertTrue(openFragments[1].get("url").isJsonNull)
        assertTrue(openFragments[1].get("title").isJsonNull)
        assertTrue(openFragments[1].get("published_at").isJsonNull)
        val findFragment = fragments.first { it.string("type") == FragmentCodec.TYPE_TOOL_FIND }
        assertEquals("NO_RESULT", findFragment.string("status"))
        assertEquals("关键词", findFragment.string("pattern"))
        assertEquals(3, findFragment.get("stage_id").asInt)

        val (reimported, skipped) = ArchiveJson.importSession(exported.json, "tool-session")
        assertEquals(0, skipped)
        val message = reimported.messages[1].message
        assertEquals("DEEP_SEARCH", message.conversationMode)
        val reOpens = message.fragments.filterIsInstance<Fragment.ToolOpen>()
        assertEquals(2, reOpens.size)
        assertEquals(listOf("FINISHED", "FINISHED"), reOpens.map { it.status })
        assertEquals("https://example.com/a", reOpens[0].result!!.url)
        assertNull(reOpens[1].result)
        val reFind = message.fragments.filterIsInstance<Fragment.ToolFind>().single()
        assertEquals("NO_RESULT", reFind.status)
        assertEquals("关键词", reFind.pattern)
    }

    @Test
    fun legacyCanonicalAtomicsMigrateToToolBlocks() {
        val legacy = mutableListOf<JsonObject>()
        legacy.add(JsonObject().apply {
            addProperty("type", WireCodec.TYPE_SERVER_TOOL_USE)
            addProperty("stage_id", 2)
            add("block", JsonObject().apply {
                addProperty("type", "server_tool_use")
                addProperty("id", "srv-open-1")
                addProperty("name", "deepseek_web_search")
                addProperty("kind", "deep_search")
                add("input", JsonObject().apply {
                    addProperty("action", "open_page")
                    addProperty("url", "https://legacy.example.com/page")
                })
            })
        })
        legacy.add(JsonObject().apply {
            addProperty("type", WireCodec.TYPE_WEB_FETCH_RESULT)
            addProperty("stage_id", 2)
            add("block", JsonObject().apply {
                addProperty("type", "web_fetch_tool_result")
                addProperty("tool_use_id", "srv-open-1")
                addProperty("kind", "deep_search")
                add("content", JsonObject().apply {
                    addProperty("type", "web_fetch_result")
                    addProperty("url", "https://legacy.example.com/page")
                    add("content", JsonObject().apply {
                        addProperty("type", "document")
                        add("source", JsonObject().apply {
                            addProperty("type", "text")
                            addProperty("media_type", "text/plain")
                            addProperty("data", "")
                        })
                        addProperty("title", "旧页面")
                    })
                })
            })
        })
        val blocks = WireCodec.blocks(legacy)
        val page = blocks.filterIsInstance<WireBlock.OpenPage>().single()
        assertEquals("FINISHED", page.status)
        assertEquals(2, page.stageId)
        assertEquals("旧页面", page.result!!.string("title"))
        val model = WireCodec.toModelFragments(blocks)
        val toolOpen = model.filterIsInstance<Fragment.ToolOpen>().single()
        assertEquals("旧页面", toolOpen.result!!.title)
        assertEquals(0, model.count { it is Fragment.ToolFind })
    }

    private fun toolOpen(
        status: String,
        stageId: Int?,
        url: String?,
        title: String?,
        withResult: Boolean,
    ): JsonObject = JsonObject().apply {
        addProperty("type", FragmentCodec.TYPE_TOOL_OPEN)
        addProperty("status", status)
        stageId?.let { addProperty("stage_id", it) }
        if (withResult) {
            add("result", JsonObject().apply {
                url?.let { addProperty("url", it) }
                title?.let { addProperty("title", it) }
                addProperty("snippet", "摘要")
                addProperty("published_at", 1793635200.0)
                addProperty("site_name", "示例站点")
                addProperty("site_icon", "https://cdn.deepseek.com/site-icons/example.com")
            })
        } else if (url != null || title != null) {
            add("result", JsonObject().apply {
                url?.let { addProperty("url", it) }
                title?.let { addProperty("title", it) }
            })
        } else {
            add("result", com.google.gson.JsonNull.INSTANCE)
        }
    }

    private fun toolFind(status: String, stageId: Int?, pattern: String): JsonObject = JsonObject().apply {
        addProperty("type", FragmentCodec.TYPE_TOOL_FIND)
        addProperty("status", status)
        stageId?.let { addProperty("stage_id", it) }
        addProperty("pattern", pattern)
    }
}
