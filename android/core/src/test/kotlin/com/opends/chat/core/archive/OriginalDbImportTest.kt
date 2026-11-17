package com.opends.chat.core.archive

import com.google.gson.JsonParser
import com.opends.chat.core.Fragment
import com.opends.chat.core.Role
import com.opends.chat.core.TestPaths
import com.opends.chat.core.chat.WireCodec
import com.opends.chat.core.db.FragmentCodec
import com.opends.chat.core.db.StoredMessage
import com.opends.chat.core.util.array
import com.opends.chat.core.util.string
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.File

class OriginalDbImportTest {

    private val database: File = TestPaths.repoFile(
        "refs/dumps/device-db/dsdump/deepseek_chat_1321287d-9ef0-4d11-b442-27200e269e28.db"
    )
    private val legacyDump: File = TestPaths.repoFile("refs/dumps/app-sessions.json")

    @Test
    fun convertsRealDeviceDatabase() {
        assumeTrue("缺少真实原版数据库快照：$database", database.exists())
        val result = JdbcOriginalDatabaseReader.open(database).use { reader ->
            assertTrue(reader.hasSessionList())
            OriginalSessionConverter.convert(reader)
        }
        val sessions = result.first
        assertEquals(0, result.second)
        assertEquals(1, sessions.size)

        val session = sessions[0]
        assertEquals("8e0e749a-c9b5-4e5f-8ad1-d09c649e1d1f", session.id)
        assertEquals("小红书误封申诉指南", session.title)
        assertEquals(4L, session.currentMessageId)
        assertEquals(1794878220196L, session.insertedAt)
        assertEquals(1794879153627L, session.updatedAt)
        assertEquals(listOf(1L, 2L, 3L, 4L), session.messages.map { it.message.id })
        assertEquals(listOf<Long?>(null, 1L, 2L, 3L), session.messages.map { it.message.parentId })
        assertEquals(
            listOf(Role.USER, Role.ASSISTANT, Role.USER, Role.ASSISTANT),
            session.messages.map { it.message.role },
        )
        session.messages.forEach { message ->
            assertEquals(com.opends.chat.core.MessageStatus.FINISHED, message.message.status)
            assertTrue(message.message.thinkingEnabled)
            assertFalse(message.message.prefix)
        }

        val userOne = FragmentCodec.parse(session.messages[0].fragmentsJson)
        assertEquals("你好你好，小红书被误封了怎么办", WireCodec.requestText(userOne))

        val assistantTwo = FragmentCodec.parse(session.messages[1].fragmentsJson)
        val expectedTwo = ArrayList<String>()
        expectedTwo.add(FragmentCodec.TYPE_THINK)
        expectedTwo.add(WireCodec.TYPE_SERVER_TOOL_USE)
        expectedTwo.add(WireCodec.TYPE_WEB_SEARCH_RESULT)
        expectedTwo.add(FragmentCodec.TYPE_THINK)
        repeat(8) {
            expectedTwo.add(FragmentCodec.TYPE_TOOL_OPEN)
        }
        expectedTwo.add(FragmentCodec.TYPE_THINK)
        expectedTwo.add(FragmentCodec.TYPE_RESPONSE)
        assertEquals(expectedTwo, assistantTwo.map { it.string("type") })

        val searchBlock = assistantTwo.first { it.string("type") == WireCodec.TYPE_WEB_SEARCH_RESULT }
            .getAsJsonObject("block")
        assertEquals(16, searchBlock.getAsJsonArray("content").size())
        val searchUse = assistantTwo.first { it.string("type") == WireCodec.TYPE_SERVER_TOOL_USE }
            .getAsJsonObject("block")
        assertEquals("search", searchUse.getAsJsonObject("input").string("action"))
        assertEquals(4, searchUse.getAsJsonObject("input").getAsJsonArray("queries").size())

        val openFragments = assistantTwo.filter { it.string("type") == FragmentCodec.TYPE_TOOL_OPEN }
        assertEquals(8, openFragments.size)
        openFragments.forEach { open ->
            assertEquals("FINISHED", open.string("status"))
            assertEquals(2, open.get("stage_id").asInt)
        }
        val openWithResult = openFragments.filter { it.get("result")?.isJsonObject == true }
        assertEquals(4, openWithResult.size)
        openWithResult.forEach { open ->
            val result = open.getAsJsonObject("result")
            assertTrue(result.string("url")!!.startsWith("https://"))
            assertNotNull(result.string("title"))
            assertNotNull(result.get("published_at"))
        }
        assertEquals(4, openFragments.count { it.get("result")?.isJsonNull == true })

        val responses = assistantTwo.filter { it.string("type") == FragmentCodec.TYPE_RESPONSE }
        assertEquals(1, responses.size)
        val body = responses[0].string("content")!!.orEmpty()
        assertTrue(body.length > 1000)
        assertFalse(body.contains("[reference:"))

        val thinks = assistantTwo.filter { it.string("type") == FragmentCodec.TYPE_THINK }
        assertEquals(3, thinks.size)
        thinks.forEach { assertNotNull(it.get("elapsed_secs")) }

        val modelFragments = session.messages[1].message.fragments
        assertEquals(3, modelFragments.count { it is Fragment.Think })
        assertEquals(1, modelFragments.count { it is Fragment.Response })
        assertEquals(0, modelFragments.count { it is Fragment.ReadLink })
        val searchFragment = modelFragments.filterIsInstance<Fragment.Search>().single()
        assertEquals(16, searchFragment.results.size)
        assertEquals(4, searchFragment.queries.size)
        assertEquals("FINISHED", searchFragment.status)
        val modelOpens = modelFragments.filterIsInstance<Fragment.ToolOpen>()
        assertEquals(8, modelOpens.size)
        assertEquals(4, modelOpens.count { it.result != null })
        modelOpens.forEach { open ->
            assertEquals("FINISHED", open.status)
            assertEquals(2, open.stageId)
        }
        assertEquals("DEEP_SEARCH", session.messages[1].message.conversationMode)
        assertEquals("DEFAULT", session.messages[0].message.conversationMode)
        assertEquals("DEEP_SEARCH", session.messages[3].message.conversationMode)

        val thinkStageIds = assistantTwo.filter { it.string("type") == FragmentCodec.TYPE_THINK }
            .map { it.get("stage_id").asInt }
        assertEquals(listOf(1, 2, 3), thinkStageIds)
        val searchStage = assistantTwo.first { it.string("type") == WireCodec.TYPE_SERVER_TOOL_USE }
        assertEquals(1, searchStage.get("stage_id").asInt)
        assertEquals(1, searchFragment.stageId)
        val openStages = openFragments.map { it.get("stage_id").asInt }
        assertEquals(List(8) { 2 }, openStages)

        val assistantFour = FragmentCodec.parse(session.messages[3].fragmentsJson)
        assertEquals(11, assistantFour.size)
        assertEquals(5, assistantFour.count { it.string("type") == FragmentCodec.TYPE_TOOL_OPEN })
        assertEquals(
            "1:1复刻Deepseek官方APP犯法吗",
            WireCodec.requestText(FragmentCodec.parse(session.messages[2].fragmentsJson)),
        )
    }

    @Test
    fun importedSessionExportsBackToArchive() {
        assumeTrue("缺少真实原版数据库快照：$database", database.exists())
        val session = JdbcOriginalDatabaseReader.open(database).use { reader ->
            OriginalSessionConverter.convert(reader).first.single()
        }
        val stored = session.messages.map { imported ->
            StoredMessage(
                id = imported.message.id,
                parentId = imported.message.parentId,
                role = imported.message.role,
                status = imported.message.status,
                thinkingEnabled = imported.message.thinkingEnabled,
                searchEnabled = imported.message.searchEnabled,
                prefix = imported.message.prefix,
                accumulatedTokenUsage = imported.message.accumulatedTokenUsage,
                insertedAt = imported.message.insertedAt,
                fragmentsJson = imported.fragmentsJson,
            )
        }
        val exported = ArchiveJson.exportSession(
            com.opends.chat.core.SessionInfo(
                id = session.id,
                title = session.title,
                pinned = session.pinned,
                currentMessageId = session.currentMessageId,
                insertedAt = session.insertedAt,
                updatedAt = session.updatedAt,
            ),
            stored,
        ) { _, _ -> null }
        val fragmentsTwo = exported.json.getAsJsonArray("messages")[1].asJsonObject
            .getAsJsonArray("fragments").map { it.asJsonObject }
        val search = fragmentsTwo.singleOrNull { it.string("type") == "TOOL_SEARCH" }
        assertNotNull("原版导入的搜索步骤应在导出时保留", search)
        assertEquals("FINISHED", search!!.string("status"))
        assertEquals(16, search.getAsJsonArray("results").size())
        assertEquals(8, fragmentsTwo.count { it.string("type") == "TOOL_OPEN" })
        val (reimported, skipped) = ArchiveJson.importSession(exported.json, session.id)
        assertEquals(0, skipped)
        assertEquals(4, reimported.messages.size)
        assertEquals(4L, reimported.currentMessageId)
        assertEquals(listOf<Long?>(null, 1L, 2L, 3L), reimported.messages.map { it.message.parentId })
        assertEquals("DEEP_SEARCH", reimported.messages[1].message.conversationMode)
        assertEquals(
            8,
            reimported.messages[1].message.fragments.count { it is Fragment.ToolOpen },
        )
    }

    @Test
    fun matchesLegacyDumpStructure() {
        assumeTrue("缺少真实原版数据库快照：$database", database.exists())
        assumeTrue("缺少交叉校验数据：$legacyDump", legacyDump.exists())
        val raw = JdbcOriginalDatabaseReader.open(database).use { it.readSessions() }.single()
        val legacy = JsonParser.parseString(legacyDump.readText()).asJsonObject
        val legacySession = legacy.getAsJsonArray("chat_sessions")[0].asJsonObject
        assertEquals(raw.id, legacySession.string("id"))
        assertEquals(raw.title, legacySession.string("title"))
        assertEquals(raw.currentMessageId, legacySession.get("current_message_id")?.asLong)
        val legacyMessages = legacySession.getAsJsonArray("messages")
        assertEquals(raw.messages.size, legacyMessages.size())
        raw.messages.forEachIndexed { index, message ->
            val legacyMessage = legacyMessages[index].asJsonObject
            assertEquals(message.id, legacyMessage.get("id").asLong)
            assertEquals(message.parentId, legacyMessage.get("parent_id")?.takeIf { !it.isJsonNull }?.asLong)
            assertEquals(message.role, legacyMessage.string("role"))
            assertEquals(message.status, legacyMessage.string("status"))
            val deviceTypes = FragmentCodec.parse(message.fragmentsJson)
                .map { it.string("type") }
                .filterNot { it == FragmentCodec.TYPE_TOOL_OPEN }
            val legacyTypes = legacyMessage.getAsJsonArray("fragments")
                .mapNotNull { it.takeIf { element -> element.isJsonObject }?.asJsonObject?.string("type") }
                .filterNot { it == "TOOL_OPEN" }
            assertEquals("消息 ${message.id} 片段类型应一致", legacyTypes, deviceTypes)
        }
    }
}
