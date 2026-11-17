package com.opends.chat.core.archive

import com.google.gson.JsonObject
import com.opends.chat.core.Fragment
import com.opends.chat.core.MessageStatus
import com.opends.chat.core.Role
import com.opends.chat.core.SessionInfo
import com.opends.chat.core.chat.WireCodec
import com.opends.chat.core.db.FragmentCodec
import com.opends.chat.core.db.StoredMessage
import com.opends.chat.core.util.string
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ArchiveBoundaryTest {

    @Test
    fun emptySessionRoundTrip() {
        val session = SessionInfo(
            id = "empty-session",
            title = null,
            currentMessageId = null,
            insertedAt = 1794878220000L,
            updatedAt = 1794878220000L,
        )
        val exported = ArchiveJson.exportSession(session, emptyList()) { _, _ -> null }
        assertEquals(0L, exported.json.get("current_message_id").asLong)
        assertEquals(0, exported.json.getAsJsonArray("messages").size())
        val (imported, skipped) = ArchiveJson.importSession(exported.json, "empty-session")
        assertEquals(0, skipped)
        assertTrue(imported.messages.isEmpty())
        assertNull(imported.currentMessageId)
        assertNull(imported.title)
    }

    @Test
    fun imageOnlyUserMessageRoundTrip() {
        val files = listOf(
            Fragment.File(
                fileId = "0f0e0d0c",
                name = "photo.png",
                mime = "image/png",
                size = 1234L,
                isImage = true,
                localPath = null,
            )
        )
        val userJson = WireCodec.userFragments("", files)
        val user = stored(1, null, Role.USER, MessageStatus.FINISHED, userJson)
        val session = SessionInfo(
            id = "image-session",
            title = "图片",
            currentMessageId = 1,
            insertedAt = 1L,
            updatedAt = 2L,
        )
        val exported = ArchiveJson.exportSession(session, listOf(user)) { fileId, _ ->
            "chat_sessions/image-session_files/${fileId}_photo.png"
        }
        assertEquals(1, exported.images.size)
        val fragments = exported.json.getAsJsonArray("messages")[0].asJsonObject
            .getAsJsonArray("fragments").map { it.asJsonObject }
        assertEquals("FILE", fragments[0].string("type"))
        val fileEntry = fragments[0].getAsJsonArray("files")[0].asJsonObject
        assertEquals("0f0e0d0c", fileEntry.string("id"))
        assertEquals(true, fileEntry.get("is_image")?.asBoolean)
        assertEquals(
            "chat_sessions/image-session_files/0f0e0d0c_photo.png",
            fileEntry.string("export_path"),
        )
        assertEquals("REQUEST", fragments[1].string("type"))
        assertEquals("", fragments[1].string("content"))
        val (imported, skipped) = ArchiveJson.importSession(exported.json, "image-session")
        assertEquals(0, skipped)
        val message = imported.messages.single().message
        val fileFragment = message.fragments.filterIsInstance<Fragment.File>().single()
        assertEquals("0f0e0d0c", fileFragment.fileId)
        assertEquals("photo.png", fileFragment.name)
        assertTrue(fileFragment.isImage)
        assertEquals("", WireCodec.requestText(FragmentCodec.parse(imported.messages.single().fragmentsJson)))
    }

    @Test
    fun branchTreeRoundTripKeepsCurrentPath() {
        val messages = listOf(
            stored(10, null, Role.USER, MessageStatus.FINISHED, WireCodec.userFragments("第一问", emptyList())),
            stored(20, 10, Role.ASSISTANT, MessageStatus.FINISHED, textFragments("第一答")),
            stored(30, 20, Role.USER, MessageStatus.FINISHED, WireCodec.userFragments("第二问", emptyList())),
            stored(40, 30, Role.ASSISTANT, MessageStatus.FINISHED, textFragments("第二答")),
            stored(50, 10, Role.ASSISTANT, MessageStatus.FINISHED, textFragments("另一版本回答")),
        )
        val session = SessionInfo(
            id = "branch-session",
            title = "分支",
            currentMessageId = 40,
            insertedAt = 1L,
            updatedAt = 2L,
        )
        val exported = ArchiveJson.exportSession(session, messages) { _, _ -> null }
        val exportedMessages = exported.json.getAsJsonArray("messages").map { it.asJsonObject }
        assertEquals(listOf(1L, 2L, 3L, 4L, 5L), exportedMessages.map { it.get("id").asLong })
        assertEquals(listOf<Long?>(null, 1L, 2L, 3L, 1L), exportedMessages.map {
            it.get("parent_id")?.takeIf { element -> !element.isJsonNull }?.asLong
        })
        assertEquals(4L, exported.json.get("current_message_id").asLong)
        val (imported, skipped) = ArchiveJson.importSession(exported.json, "branch-session")
        assertEquals(0, skipped)
        assertEquals(4L, imported.currentMessageId)
        val childrenOfRoot = imported.messages.filter { it.message.parentId == 1L }.map { it.message.id }
        assertEquals(listOf(2L, 5L), childrenOfRoot)
        assertEquals(listOf(3L), imported.messages.filter { it.message.parentId == 2L }.map { it.message.id })
    }

    @Test
    fun statusMappingRoundTrips() {
        val refusal = stored(
            2, 1, Role.ASSISTANT, MessageStatus.CONTENT_FILTER,
            mutableListOf(JsonObject().apply {
                addProperty("type", WireCodec.TYPE_META)
                addProperty("stop_reason", "refusal")
            }),
        )
        val stopped = stored(3, 2, Role.USER, MessageStatus.FINISHED, WireCodec.userFragments("继续", emptyList()))
        val incomplete = stored(4, 3, Role.ASSISTANT, MessageStatus.INCOMPLETE, textFragments("半截"))
        val failed = stored(5, 4, Role.USER, MessageStatus.FINISHED, WireCodec.userFragments("再试", emptyList()))
        val failedReply = stored(6, 5, Role.ASSISTANT, MessageStatus.FAILED, textFragments(""))
        val session = SessionInfo(
            id = "status-session",
            title = "状态",
            currentMessageId = 6,
            insertedAt = 1L,
            updatedAt = 2L,
        )
        val storedMessages = listOf(
            stored(1, null, Role.USER, MessageStatus.FINISHED, WireCodec.userFragments("开始", emptyList())),
            refusal,
            stopped,
            incomplete,
            failed,
            failedReply,
        )
        val exported = ArchiveJson.exportSession(session, storedMessages) { _, _ -> null }
        val exportedStatuses = exported.json.getAsJsonArray("messages").map {
            it.asJsonObject.string("status")
        }
        assertEquals(listOf("FINISHED", "CONTENT_FILTER", "FINISHED", "INCOMPLETE", "FINISHED", "FAILED"), exportedStatuses)
        val (imported, _) = ArchiveJson.importSession(exported.json, "status-session")
        assertEquals(
            listOf(
                MessageStatus.FINISHED, MessageStatus.CONTENT_FILTER, MessageStatus.FINISHED,
                MessageStatus.INCOMPLETE, MessageStatus.FINISHED, MessageStatus.FAILED,
            ),
            imported.messages.map { it.message.status },
        )
        val refusalJson = FragmentCodec.parse(imported.messages[1].fragmentsJson)
        val meta = refusalJson.last { it.string("type") == WireCodec.TYPE_META }
        assertEquals("refusal", meta.string("stop_reason"))
    }

    private fun textFragments(text: String): List<JsonObject> =
        WireCodec.canonical(listOf(com.opends.chat.core.chat.WireBlock.Text().also { it.text.append(text) }))

    private fun stored(
        id: Long,
        parentId: Long?,
        role: Role,
        status: MessageStatus,
        fragments: Any,
    ): StoredMessage = StoredMessage(
        id = id,
        parentId = parentId,
        role = role,
        status = status,
        thinkingEnabled = false,
        searchEnabled = false,
        prefix = false,
        accumulatedTokenUsage = 0,
        insertedAt = 1794878220000L + id,
        fragmentsJson = when (fragments) {
            is String -> fragments
            is List<*> -> FragmentCodec.encode(fragments.filterIsInstance<JsonObject>())
            else -> "[]"
        },
    )
}
