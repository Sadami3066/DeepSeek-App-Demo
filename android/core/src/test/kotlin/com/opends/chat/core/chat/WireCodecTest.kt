package com.opends.chat.core.chat

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.opends.chat.core.AppSettings
import com.opends.chat.core.ChatMessage
import com.opends.chat.core.Fragment
import com.opends.chat.core.MessageStatus
import com.opends.chat.core.Role
import com.opends.chat.core.SessionInfo
import com.opends.chat.core.archive.ArchiveJson
import com.opends.chat.core.db.FragmentCodec
import com.opends.chat.core.db.StoredMessage
import com.opends.chat.core.util.array
import com.opends.chat.core.util.obj
import com.opends.chat.core.util.string
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WireCodecTest {

    private fun responseBlock(text: String): JsonObject = JsonObject().apply {
        addProperty("type", "text")
        addProperty("text", text)
    }

    private fun readLinkBlocks(): List<WireBlock> {
        val toolUse = JsonParser.parseString(
            """{"type":"server_tool_use","id":"srv-rl-1","name":"deepseek_read_url",""" +
                """"input":{"action":"read_link","url":"https://example.com/a"},""" +
                """"caller":{"type":"direct"},"kind":"read_link"}"""
        ).asJsonObject
        val fetchResult = JsonParser.parseString(
            """{"type":"web_fetch_tool_result","tool_use_id":"srv-rl-1","kind":"read_link",""" +
                """"signature":"SIG-ABC","content":{"type":"web_fetch_result","url":"https://example.com/a",""" +
                """"content":{"type":"document","source":{"type":"text","media_type":"text/plain","data":""},""" +
                """"title":"Example"}}}"""
        ).asJsonObject
        return listOf(
            WireBlock.Text().also { it.text.append("页面要点：") },
            WireBlock.Atomic(toolUse),
            WireBlock.Atomic(fetchResult),
            WireBlock.Text().also { it.text.append("如上。") },
        )
    }

    @Test
    fun readLinkPairIsReassembledWithSignature() {
        val content = WireCodec.assistantContextContent(readLinkBlocks(), includeThinking = false)
        assertNotNull(content)
        assertEquals(4, content!!.size())
        assertEquals("text", content[0].asJsonObject.string("type"))
        val toolUse = content[1].asJsonObject
        assertEquals("read_link", toolUse.string("kind"))
        val fetch = content[2].asJsonObject
        assertEquals("web_fetch_tool_result", fetch.string("type"))
        assertEquals("SIG-ABC", fetch.string("signature"))
        assertEquals("如上。", content[3].asJsonObject.string("text"))
    }

    @Test
    fun fetchResultWithoutSignatureIsDropped() {
        val blocks = readLinkBlocks().map { block ->
            if (block is WireBlock.Atomic && block.json.string("type") == "web_fetch_tool_result") {
                WireBlock.Atomic(block.json.apply { remove("signature") })
            } else {
                block
            }
        }
        val content = WireCodec.assistantContextContent(blocks, includeThinking = false)
        assertNotNull(content)
        assertEquals(1, content!!.size())
        assertEquals("页面要点：\n\n如上。", content[0].asJsonObject.string("text"))
    }

    @Test
    fun historyThinkingIsOmittedAndKeepsParagraphGap() {
        val blocks = listOf(
            WireBlock.Text().also { it.text.append("第一段") },
            WireBlock.Thinking().also { it.text.append("内部思考") },
            WireBlock.Text().also { it.text.append("第二段") },
        )
        val content = WireCodec.assistantContextContent(blocks, includeThinking = false)
        assertNotNull(content)
        assertEquals(1, content!!.size())
        assertEquals("第一段\n\n第二段", content[0].asJsonObject.string("text"))
    }

    @Test
    fun currentThinkingIsKeptForPrefix() {
        val blocks = listOf(
            WireBlock.Text().also { it.text.append("开头") },
            WireBlock.Thinking().also {
                it.text.append("想法")
                it.signature.append("SIG-T")
            },
            WireBlock.Text().also { it.text.append("继续") },
        )
        val content = WireCodec.assistantContextContent(blocks, includeThinking = true)
        assertNotNull(content)
        assertEquals(3, content!!.size())
        val thinking = content[1].asJsonObject
        assertEquals("thinking", thinking.string("type"))
        assertEquals("SIG-T", thinking.string("signature"))
    }

    @Test
    fun searchBlocksAreNotReturnedToContext() {
        val searchUse = JsonParser.parseString(
            """{"type":"server_tool_use","id":"srv-s-1","name":"deepseek_web_search",""" +
                """"input":{"action":"search","queries":["a"]},"kind":"search"}"""
        ).asJsonObject
        val result = JsonParser.parseString(
            """{"type":"web_search_tool_result","tool_use_id":"srv-s-1","kind":"search","content":[]}"""
        ).asJsonObject
        val blocks = listOf(
            WireBlock.Atomic(searchUse),
            WireBlock.Atomic(result),
            WireBlock.Text().also { it.text.append("答案") },
        )
        val content = WireCodec.assistantContextContent(blocks, includeThinking = false)
        assertNotNull(content)
        assertEquals(1, content!!.size())
        assertEquals("答案", content[0].asJsonObject.string("text"))
    }

    @Test
    fun modelFragmentsPairToolUseAndResult() {
        val fragments = WireCodec.toModelFragments(readLinkBlocks())
        val readLink = fragments.filterIsInstance<Fragment.ReadLink>().firstOrNull()
        assertNotNull(readLink)
        assertEquals("https://example.com/a", readLink!!.url)
        assertEquals("FINISHED", readLink.status)
        assertEquals("Example", readLink.title)
    }

    @Test
    fun latestReminderUsesAppPlatform() {
        val reminder = LatestReminder.build(AppSettings(languageTag = "zh-CN", location = "广东"), 1794900000000L)
        val parts = reminder.split(",")
        assertEquals(5, parts.size)
        assertEquals("App", parts[3])
        assertEquals("中文", parts[4])
        assertTrue(parts[0].matches(Regex("""\d{4}-\d{2}-\d{2}""")))
    }

    @Test
    fun requestAssemblerSkipsUnusableRounds() {
        val user1 = stored(1, null, Role.USER, MessageStatus.FINISHED, userFragments("第一个问题"))
        val reply1 = stored(2, 1, Role.ASSISTANT, MessageStatus.FAILED, "[]")
        val user2 = stored(3, 2, Role.USER, MessageStatus.FINISHED, userFragments("第二个问题"))
        val reply2 = stored(
            4, 3, Role.ASSISTANT, MessageStatus.FINISHED,
            textFragments("第二个回答"),
        )
        val assembler = RequestAssembler(emptyMap())
        val messages = assembler.buildMessages(
            history = listOf(user1, reply1, user2, reply2),
            currentUserItems = userFragments("新问题"),
            reminder = "2026-11-17,星期一,广东,App,中文",
            prefixBlocks = null,
            includeThinking = true,
        )
        assertEquals(4, messages.size())
        assertEquals("user", messages[0].asJsonObject.string("role"))
        assertEquals("第二个问题", messages[0].asJsonObject.string("content"))
        assertEquals("assistant", messages[1].asJsonObject.string("role"))
        assertEquals("latest_reminder", messages[2].asJsonObject.string("role"))
        assertEquals("新问题", messages[3].asJsonObject.string("content"))
    }

    @Test
    fun assistantContextKeepsOnlyReadLinkToolBlocks() {
        val blocks = readLinkBlocks().map { block ->
            if (block is WireBlock.Text) WireBlock.Text().also { it.text.append(block.text) } else block
        }
        val canonical = WireCodec.canonical(blocks)
        val stored = StoredMessage(
            id = 2,
            parentId = 1,
            role = Role.ASSISTANT,
            status = MessageStatus.FINISHED,
            thinkingEnabled = false,
            searchEnabled = false,
            prefix = true,
            accumulatedTokenUsage = 0,
            insertedAt = 0L,
            fragmentsJson = FragmentCodec.encode(canonical),
        )
        val assembler = RequestAssembler(emptyMap())
        val messages = assembler.buildMessages(
            history = listOf(
                stored(1, null, Role.USER, MessageStatus.FINISHED, userFragments("读一下 https://example.com/a")),
                stored,
            ),
            currentUserItems = userFragments("继续"),
            reminder = null,
            prefixBlocks = null,
            includeThinking = true,
        )
        assertEquals(3, messages.size())
        val assistant = messages[1].asJsonObject
        val content = assistant.getAsJsonArray("content")
        assertEquals(4, content.size())
        assertEquals("server_tool_use", content[1].asJsonObject.string("type"))
        assertEquals("web_fetch_tool_result", content[2].asJsonObject.string("type"))
        assertEquals("SIG-ABC", content[2].asJsonObject.string("signature"))
    }

    @Test
    fun archiveRoundTripKeepsTreeAndFragments() {
        val session = SessionInfo(
            id = "8e0e749a-c9b5-4e5f-8ad1-d09c649e1d1f",
            title = "测试会话",
            pinned = false,
            currentMessageId = 2,
            insertedAt = 1794878220000L,
            updatedAt = 1794878246000L,
        )
        val stored = listOf(
            StoredMessage(
                id = 1, parentId = null, role = Role.USER, status = MessageStatus.FINISHED,
                thinkingEnabled = true, searchEnabled = false, prefix = false, accumulatedTokenUsage = 0,
                insertedAt = 1794878220000L,
                fragmentsJson = FragmentCodec.encode(userFragments("你好")),
            ),
            StoredMessage(
                id = 2, parentId = 1, role = Role.ASSISTANT, status = MessageStatus.FINISHED,
                thinkingEnabled = true, searchEnabled = false, prefix = true, accumulatedTokenUsage = 42,
                insertedAt = 1794878230000L,
                fragmentsJson = FragmentCodec.encode(
                    WireCodec.canonical(
                        listOf(
                            WireBlock.Thinking().also {
                                it.text.append("思考")
                                it.startedAt = 1000L
                                it.endedAt = 2000L
                            },
                            WireBlock.Text().also { it.text.append("回答") },
                        )
                    )
                ),
            ),
        )
        val exported = ArchiveJson.exportSession(session, stored) { _, _ -> null }
        val (imported, skipped) = ArchiveJson.importSession(exported.json, session.id)
        assertEquals(0, skipped)
        assertEquals(2, imported.messages.size)
        assertEquals(2L, imported.currentMessageId)
        assertEquals(1794878220000L, imported.insertedAt)
        val assistant = imported.messages[1].message
        assertEquals(Role.ASSISTANT, assistant.role)
        assertEquals(listOf(Fragment.Think("思考", 1.0), Fragment.Response("回答")), assistant.fragments)
        assertFalse(assistant.prefix)
    }


    @Test
    fun archiveExportKeepsToolFragments() {
        val searchUse = JsonParser.parseString(
            """{"type":"server_tool_use","id":"srv-s-1","name":"deepseek_web_search",""" +
                """"input":{"action":"search","queries":["q1"]},"caller":{"type":"direct"},"kind":"search"}"""
        ).asJsonObject
        val searchResult = JsonParser.parseString(
            """{"type":"web_search_tool_result","tool_use_id":"srv-s-1","kind":"search","content":[""" +
                """{"type":"web_search_result","url":"https://a.example/1","title":"标题","snippet":"摘要",""" +
                """"page_age":"2026-11-16T08:00:00+00:00","site_name":"站点","site_icon":"https://a.example/icon.png"}]}"""
        ).asJsonObject
        val blocks = listOf(
            WireBlock.Atomic(searchUse),
            WireBlock.Atomic(searchResult),
        ) + readLinkBlocks() + listOf(WireBlock.Text().also { it.text.append("正文") })
        val stored = listOf(
            StoredMessage(
                id = 1, parentId = null, role = Role.USER, status = MessageStatus.FINISHED,
                thinkingEnabled = false, searchEnabled = true, prefix = false, accumulatedTokenUsage = 0,
                insertedAt = 1794878220000L,
                fragmentsJson = FragmentCodec.encode(userFragments("搜索并读一下")),
            ),
            StoredMessage(
                id = 2, parentId = 1, role = Role.ASSISTANT, status = MessageStatus.FINISHED,
                thinkingEnabled = false, searchEnabled = true, prefix = false, accumulatedTokenUsage = 10,
                insertedAt = 1794878230000L,
                fragmentsJson = FragmentCodec.encode(WireCodec.canonical(blocks)),
            ),
        )
        val session = SessionInfo(id = "s1", title = "t", currentMessageId = 2, insertedAt = 1L, updatedAt = 2L)
        val exported = ArchiveJson.exportSession(session, stored) { _, _ -> null }
        val fragments = exported.json.getAsJsonArray("messages")[1].asJsonObject
            .getAsJsonArray("fragments").map { it.asJsonObject }
        val search = fragments.firstOrNull { it.string("type") == "TOOL_SEARCH" }
        assertNotNull(search)
        assertEquals("FINISHED", search!!.string("status"))
        assertEquals(1, search.getAsJsonArray("results").size())
        assertEquals("https://a.example/1", search.getAsJsonArray("results")[0].asJsonObject.string("url"))
        val readLink = fragments.firstOrNull { it.string("type") == "READ_LINK" }
        assertNotNull(readLink)
        assertEquals(
            "SIG-ABC",
            readLink!!.getAsJsonArray("results")[0].asJsonObject.string("signature"),
        )
        val (imported, _) = ArchiveJson.importSession(exported.json, "s1")
        val canonical = FragmentCodec.parse(imported.messages[1].fragmentsJson)
        val fetch = canonical.firstOrNull {
            it.string("type") == WireCodec.TYPE_WEB_FETCH_RESULT
        }
        assertNotNull(fetch)
        assertEquals("SIG-ABC", fetch!!.getAsJsonObject("block").string("signature"))
        val searchFragment = canonical.firstOrNull {
            it.string("type") == WireCodec.TYPE_WEB_SEARCH_RESULT
        }
        assertNotNull(searchFragment)
        assertEquals(1, searchFragment!!.getAsJsonObject("block").getAsJsonArray("content").size())
    }

    private fun userFragments(text: String): List<JsonObject> = WireCodec.userFragments(text, emptyList())

    private fun textFragments(text: String): String = FragmentCodec.encode(
        WireCodec.canonical(listOf(WireBlock.Text().also { it.text.append(text) }))
    )

    private fun stored(
        id: Long,
        parentId: Long?,
        role: Role,
        status: MessageStatus,
        fragmentsJson: Any,
    ): StoredMessage = StoredMessage(
        id = id,
        parentId = parentId,
        role = role,
        status = status,
        thinkingEnabled = false,
        searchEnabled = false,
        prefix = role == Role.ASSISTANT,
        accumulatedTokenUsage = 0,
        insertedAt = 0L,
        fragmentsJson = when (fragmentsJson) {
            is String -> fragmentsJson
            is List<*> -> FragmentCodec.encode(fragmentsJson.filterIsInstance<JsonObject>())
            else -> "[]"
        },
    )
}
