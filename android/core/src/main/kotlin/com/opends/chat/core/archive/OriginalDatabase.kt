package com.opends.chat.core.archive

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.opends.chat.core.ChatMessage
import com.opends.chat.core.MessageStatus
import com.opends.chat.core.Role
import com.opends.chat.core.chat.WireCodec
import com.opends.chat.core.db.FragmentCodec
import com.opends.chat.core.db.ImportedMessage
import com.opends.chat.core.db.ImportedSession
import com.opends.chat.core.util.string
import com.opends.chat.core.util.TimeFormats

internal object OriginalSql {
    const val SESSION_LIST =
        "SELECT id, title, inserted_at, updated_at, current_message_id, pinned FROM chat_session_list"

    const val TABLE_EXISTS = "SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = ?"

    fun messageQuery(sessionId: String): String =
        "SELECT message_id, parent_id, role, thinking_enabled, status, inserted_at, fragments, " +
            "conversation_mode FROM \"chat_session_messages_$sessionId\" ORDER BY message_id"
}

internal data class RawOriginalMessage(
    val id: Long,
    val parentId: Long?,
    val role: String?,
    val thinkingEnabled: Boolean,
    val status: String?,
    val insertedAt: Double?,
    val fragmentsJson: String?,
    val conversationMode: String?,
)

internal data class RawOriginalSession(
    val id: String,
    val title: String?,
    val insertedAt: Double?,
    val updatedAt: Double?,
    val currentMessageId: Long?,
    val pinned: Boolean,
    val messages: List<RawOriginalMessage>,
)

internal interface OriginalDatabaseReader {
    fun hasSessionList(): Boolean
    fun readSessions(): List<RawOriginalSession>
}

internal class AndroidOriginalDatabaseReader(private val db: SQLiteDatabase) : OriginalDatabaseReader {

    override fun hasSessionList(): Boolean = tableExists("chat_session_list")

    override fun readSessions(): List<RawOriginalSession> {
        val sessions = ArrayList<RawOriginalSession>()
        val cursor = db.rawQuery(OriginalSql.SESSION_LIST, null)
        cursor.use {
            while (it.moveToNext()) {
                val sessionId = it.getString(0) ?: continue
                sessions.add(
                    RawOriginalSession(
                        id = sessionId,
                        title = if (it.isNull(1)) null else it.getString(1),
                        insertedAt = if (it.isNull(2)) null else it.getDouble(2),
                        updatedAt = if (it.isNull(3)) null else it.getDouble(3),
                        currentMessageId = if (it.isNull(4)) null else it.getLong(4),
                        pinned = !it.isNull(5) && it.getInt(5) != 0,
                        messages = readMessages(sessionId),
                    )
                )
            }
        }
        return sessions
    }

    private fun readMessages(sessionId: String): List<RawOriginalMessage> {
        val table = "chat_session_messages_$sessionId"
        if (!tableExists(table)) return emptyList()
        val messages = ArrayList<RawOriginalMessage>()
        val cursor = db.rawQuery(OriginalSql.messageQuery(sessionId), null)
        cursor.use {
            while (it.moveToNext()) {
                messages.add(
                    RawOriginalMessage(
                        id = it.getLong(0),
                        parentId = if (it.isNull(1)) null else it.getLong(1),
                        role = if (it.isNull(2)) null else it.getString(2),
                        thinkingEnabled = !it.isNull(3) && it.getInt(3) != 0,
                        status = if (it.isNull(4)) null else it.getString(4),
                        insertedAt = if (it.isNull(5)) null else it.getDouble(5),
                        fragmentsJson = if (it.isNull(6)) null else it.getString(6),
                        conversationMode = if (it.isNull(7)) null else it.getString(7),
                    )
                )
            }
        }
        return messages
    }

    private fun tableExists(name: String): Boolean {
        val cursor: Cursor = db.rawQuery(OriginalSql.TABLE_EXISTS, arrayOf(name))
        return cursor.use { it.moveToFirst() }
    }
}

internal object OriginalSessionConverter {

    fun convert(reader: OriginalDatabaseReader): Pair<List<ImportedSession>, Int> {
        val sessions = ArrayList<ImportedSession>()
        var skipped = 0
        reader.readSessions().forEach { raw ->
            val converted = convertSession(raw)
            sessions.add(converted.first)
            skipped += converted.second
        }
        return sessions to skipped
    }

    fun convertSession(raw: RawOriginalSession): Pair<ImportedSession, Int> {
        var skipped = 0
        val messages = ArrayList<ImportedMessage>(raw.messages.size)
        raw.messages.forEach { message ->
            val converted = convertMessage(message)
            messages.add(converted.first)
            skipped += converted.second
        }
        return ImportedSession(
            id = raw.id,
            title = raw.title?.takeIf { it.isNotBlank() },
            pinned = raw.pinned,
            currentMessageId = raw.currentMessageId,
            insertedAt = TimeFormats.millisFromEpochSeconds(raw.insertedAt) ?: 0L,
            updatedAt = TimeFormats.millisFromEpochSeconds(raw.updatedAt) ?: 0L,
            messages = messages,
        ) to skipped
    }

    private fun convertMessage(raw: RawOriginalMessage): Pair<ImportedMessage, Int> {
        val role = runCatching { Role.valueOf(raw.role ?: "") }.getOrDefault(Role.USER)
        val status = messageStatus(raw.status)
        val official = parseFragments(raw.fragmentsJson)
        var skipped = 0
        if (official == null) skipped = 1
        val fragments = official.orEmpty()
        val searchEnabled = (!raw.conversationMode.isNullOrEmpty() && raw.conversationMode != "DEFAULT") ||
            fragments.any {
                val type = it.string("type")
                type == FragmentCodec.TYPE_TOOL_SEARCH || type == FragmentCodec.TYPE_SEARCH
            }
        val canonical = when (role) {
            Role.USER -> userFragments(fragments)
            Role.ASSISTANT -> assistantFragments(fragments, raw.id, status)
        }
        val inferredMode = if (fragments.any {
                val type = it.string("type")
                type == FragmentCodec.TYPE_TOOL_OPEN || type == FragmentCodec.TYPE_TOOL_FIND
            }
        ) "DEEP_SEARCH" else "DEFAULT"
        val conversationMode = raw.conversationMode?.takeIf { it.isNotBlank() } ?: inferredMode
        val message = ChatMessage(
            id = raw.id,
            parentId = raw.parentId,
            role = role,
            status = status,
            thinkingEnabled = raw.thinkingEnabled,
            searchEnabled = searchEnabled,
            accumulatedTokenUsage = 0,
            insertedAt = TimeFormats.millisFromEpochSeconds(raw.insertedAt) ?: 0L,
            prefix = false,
            fragments = WireCodec.modelFragments(canonical, role),
            conversationMode = conversationMode,
        )
        return ImportedMessage(message, FragmentCodec.encode(canonical)) to skipped
    }

    private fun userFragments(official: List<JsonObject>): List<JsonObject> {
        val output = ArrayList<JsonObject>(2)
        val files = JsonArray()
        val texts = StringBuilder()
        official.forEach { fragment ->
            when (fragment.string("type")) {
                FragmentCodec.TYPE_REQUEST -> texts.append(fragment.string("content") ?: "")
                FragmentCodec.TYPE_FILE -> fragment.get("files")?.takeIf { it.isJsonArray }?.asJsonArray?.forEach { element ->
                    if (!element.isJsonObject) return@forEach
                    val file = element.asJsonObject
                    val id = file.string("id")?.removePrefix("file-") ?: return@forEach
                    files.add(JsonObject().apply {
                        addProperty("id", id)
                        addProperty("file_name", file.string("file_name") ?: "")
                        addProperty("file_size", file.get("file_size")?.let { value ->
                            runCatching { value.asLong }.getOrDefault(0L)
                        } ?: 0L)
                        addProperty("is_image", file.get("is_image")?.let { value ->
                            runCatching { value.asBoolean }.getOrDefault(false)
                        } ?: false)
                    })
                }
            }
        }
        if (files.size() > 0) {
            output.add(JsonObject().apply {
                addProperty("type", FragmentCodec.TYPE_FILE)
                add("files", files)
            })
        }
        output.add(JsonObject().apply {
            addProperty("type", FragmentCodec.TYPE_REQUEST)
            addProperty("content", texts.toString())
        })
        return output
    }

    private fun assistantFragments(
        official: List<JsonObject>,
        messageId: Long,
        status: MessageStatus,
    ): List<JsonObject> {
        val items = ArrayList<JsonObject>(official.size)
        official.forEach { fragment ->
            when (fragment.string("type")) {
                FragmentCodec.TYPE_TOOL_IMAGE_SEARCH,
                FragmentCodec.TYPE_CONTENT_RISK,
                FragmentCodec.TYPE_PARTIAL_FILE_READ,
                FragmentCodec.TYPE_TIP,
                -> Unit
                else -> items.add(fragment)
            }
        }
        val blocks = WireCodec.fromOfficialFragments(items, messageId)
        val filtered = if (status == MessageStatus.CONTENT_FILTER) {
            blocks.filter {
                it is com.opends.chat.core.chat.WireBlock.Atomic ||
                    it is com.opends.chat.core.chat.WireBlock.Template
            }
        } else {
            blocks
        }
        val canonical = WireCodec.canonical(filtered).toMutableList()
        if (status == MessageStatus.CONTENT_FILTER) {
            canonical.add(JsonObject().apply {
                addProperty("type", WireCodec.TYPE_META)
                addProperty("stop_reason", "refusal")
            })
        }
        return canonical
    }

    private fun parseFragments(raw: String?): List<JsonObject>? {
        if (raw.isNullOrBlank()) return emptyList()
        val parsed = runCatching {
            com.google.gson.JsonParser.parseString(raw)
        }.getOrNull() ?: return null
        if (!parsed.isJsonArray) return null
        return parsed.asJsonArray.mapNotNull { element ->
            element.takeIf { it.isJsonObject }?.asJsonObject
        }
    }

    private fun messageStatus(raw: String?): MessageStatus = when (raw) {
        "FINISHED" -> MessageStatus.FINISHED
        "CONTENT_FILTER" -> MessageStatus.CONTENT_FILTER
        "FAILED" -> MessageStatus.FAILED
        "CONTEXT_LENGTH_EXCEEDED" -> MessageStatus.CONTEXT_LENGTH_EXCEEDED
        "TIMEOUT" -> MessageStatus.TIMEOUT
        "INCOMPLETE", "WIP" -> MessageStatus.INCOMPLETE
        else -> MessageStatus.FINISHED
    }
}
