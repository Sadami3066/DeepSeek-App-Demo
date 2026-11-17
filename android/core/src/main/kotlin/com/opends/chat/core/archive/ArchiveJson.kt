package com.opends.chat.core.archive

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.opends.chat.core.ChatMessage
import com.opends.chat.core.Fragment
import com.opends.chat.core.MessageStatus
import com.opends.chat.core.Role
import com.opends.chat.core.SessionInfo
import com.opends.chat.core.chat.WireBlock
import com.opends.chat.core.chat.WireCodec
import com.opends.chat.core.db.FragmentCodec
import com.opends.chat.core.util.array
import com.opends.chat.core.util.number
import com.opends.chat.core.util.string
import com.opends.chat.core.db.ImportedMessage
import com.opends.chat.core.db.ImportedSession
import com.opends.chat.core.db.StoredMessage
import com.opends.chat.core.util.TimeFormats
import java.text.Normalizer
import java.util.Locale

internal class ArchiveException(message: String) : Exception(message)

internal object ArchiveJson {
    const val FORMAT_VERSION = 3

    fun manifest(): JsonObject = JsonObject().apply { addProperty("export_format_version", FORMAT_VERSION) }

    fun checkManifest(json: JsonObject) {
        val version = json.number("export_format_version") ?: throw ArchiveException("文件版本无效，无法导入")
        if (version > FORMAT_VERSION) throw ArchiveException("文件版本过新，请升级后再导入")
        if (version != FORMAT_VERSION.toDouble()) throw ArchiveException("文件版本无效，无法导入")
    }

    fun indexEntry(session: SessionInfo, messageCount: Int): JsonObject = JsonObject().apply {
        addProperty("id", session.id)
        if (session.title.isNullOrEmpty()) add("title", JsonNull.INSTANCE) else addProperty("title", session.title)
        addProperty("pinned", false)
        addProperty("message_count", messageCount)
        addProperty("inserted_at", TimeFormats.formatMillis(session.insertedAt))
        addProperty("updated_at", TimeFormats.formatMillis(session.updatedAt))
    }

    fun exportSession(
        session: SessionInfo,
        messages: List<StoredMessage>,
        imagePath: (fileId: String, fileName: String) -> String?,
    ): ExportedSession {
        val sorted = messages.sortedBy { it.id }
        val mapping = HashMap<Long, Long>(sorted.size)
        sorted.forEachIndexed { index, message -> mapping[message.id] = index + 1L }
        val currentNewId = session.currentMessageId?.let { mapping[it] } ?: 0L
        val images = ArrayList<ExportedImage>()
        val jsonMessages = JsonArray()
        sorted.forEach { message ->
            val newId = mapping.getValue(message.id)
            val parentNewId = message.parentId?.let { mapping[it] }
            val items = FragmentCodec.parse(message.fragmentsJson)
            val fragments = when (message.role) {
                Role.USER -> userFragments(message, items, session.id, imagePath, images)
                Role.ASSISTANT -> WireCodec.officialFragments(WireCodec.blocks(items), message.id)
            }
            val stopReason = WireCodec.meta(items)?.string("stop_reason")
            jsonMessages.add(messageJson(message, newId, parentNewId, stopReason, fragments))
        }
        val json = JsonObject().apply {
            addProperty("id", session.id)
            if (session.title.isNullOrEmpty()) add("title", JsonNull.INSTANCE) else addProperty("title", session.title)
            addProperty("pinned", false)
            addProperty("current_message_id", if (messages.isEmpty()) 0 else currentNewId)
            addProperty("inserted_at", TimeFormats.formatMillis(session.insertedAt))
            addProperty("updated_at", TimeFormats.formatMillis(session.updatedAt))
            add("messages", jsonMessages)
        }
        return ExportedSession(json, images)
    }

    private fun messageJson(
        message: StoredMessage,
        newId: Long,
        parentNewId: Long?,
        stopReason: String?,
        fragments: List<JsonObject>,
    ): JsonObject = JsonObject().apply {
        addProperty("id", newId)
        if (parentNewId == null) add("parent_id", JsonNull.INSTANCE) else addProperty("parent_id", parentNewId)
        addProperty("role", message.role.name)
        addProperty("status", archiveStatus(message, stopReason))
        addProperty("thinking_enabled", message.thinkingEnabled)
        addProperty("search_enabled", message.searchEnabled)
        addProperty("accumulated_token_usage", if (message.role == Role.ASSISTANT) message.accumulatedTokenUsage else 0)
        addProperty("inserted_at", TimeFormats.formatMillis(message.insertedAt))
        add("fragments", JsonArray().apply { fragments.forEach { add(it) } })
    }

    private fun userFragments(
        message: StoredMessage,
        items: List<JsonObject>,
        sessionId: String,
        imagePath: (String, String) -> String?,
        images: MutableList<ExportedImage>,
    ): List<JsonObject> {
        val files = WireCodec.filesOf(items)
        val output = ArrayList<JsonObject>(2)
        if (files.isNotEmpty()) {
            val array = JsonArray()
            files.forEach { file ->
                val exportPath = if (file.isImage) imagePath(file.fileId, file.name ?: file.fileId) else null
                if (exportPath != null) {
                    images.add(
                        ExportedImage(
                            path = exportPath,
                            fileId = file.fileId,
                            name = file.name ?: file.fileId,
                            localPath = file.localPath,
                        )
                    )
                }
                array.add(JsonObject().apply {
                    addProperty("id", file.fileId)
                    addProperty("file_name", file.name ?: "")
                    addProperty("file_size", file.size)
                    add("token_usage", JsonNull.INSTANCE)
                    addProperty("inserted_at", TimeFormats.formatMillis(message.insertedAt))
                    if (exportPath == null) add("export_path", JsonNull.INSTANCE) else addProperty("export_path", exportPath)
                    addProperty("is_image", file.isImage)
                    add("width", JsonNull.INSTANCE)
                    add("height", JsonNull.INSTANCE)
                })
            }
            output.add(JsonObject().apply {
                addProperty("type", FragmentCodec.TYPE_FILE)
                add("files", array)
            })
        }
        output.add(JsonObject().apply {
            addProperty("type", FragmentCodec.TYPE_REQUEST)
            addProperty("content", WireCodec.requestText(items))
        })
        return output
    }

    private fun archiveStatus(message: StoredMessage, stopReason: String?): String {
        if (message.role == Role.USER) return "FINISHED"
        return when {
            stopReason == "refusal" || message.status == MessageStatus.CONTENT_FILTER -> "CONTENT_FILTER"
            message.status == MessageStatus.FINISHED && stopReason == "max_tokens" -> "INCOMPLETE"
            message.status == MessageStatus.FINISHED -> "FINISHED"
            message.status == MessageStatus.INCOMPLETE -> "INCOMPLETE"
            message.status == MessageStatus.FAILED -> "FAILED"
            message.status == MessageStatus.CONTEXT_LENGTH_EXCEEDED -> "CONTEXT_LENGTH_EXCEEDED"
            message.status == MessageStatus.TIMEOUT -> "TIMEOUT"
            message.status == MessageStatus.WIP -> "WIP"
            else -> "FINISHED"
        }
    }

    fun importSession(json: JsonObject, expectedId: String): Pair<ImportedSession, Int> {
        val id = json.string("id") ?: throw ArchiveException("文件内容不完整，无法导入")
        if (id != expectedId) throw ArchiveException("文件内容不完整，无法导入")
        val title = json.string("title")
        val currentRaw = json.number("current_message_id")?.toLong() ?: 0L
        val insertedAt = TimeFormats.parseMillis(json.string("inserted_at")) ?: 0L
        val updatedAt = TimeFormats.parseMillis(json.string("updated_at")) ?: insertedAt
        val messagesArray = json.array("messages") ?: throw ArchiveException("文件内容不完整，无法导入")
        val messages = ArrayList<ImportedMessage>(messagesArray.size())
        var skipped = 0
        val byId = LinkedHashMap<Long, ChatMessage>()
        messagesArray.forEach { element ->
            if (!element.isJsonObject) throw ArchiveException("文件内容不完整，无法导入")
            val messageJson = element.asJsonObject
            val rawId = messageJson.number("id")?.toLong() ?: throw ArchiveException("文件中的消息分支已损坏，无法导入")
            val parentRaw = messageJson.number("parent_id")?.toLong()
            val role = runCatching { Role.valueOf(messageJson.string("role") ?: "") }
                .getOrElse { throw ArchiveException("文件中的消息分支已损坏，无法导入") }
            val statusRaw = messageJson.string("status").orEmpty()
            val status = archiveStatusToModel(statusRaw)
            val parent = parentRaw?.let { byId[it] ?: throw ArchiveException("文件中的消息分支已损坏，无法导入") }
            if (parentRaw == null && role != Role.USER) throw ArchiveException("文件中的消息分支已损坏，无法导入")
            if (parent != null && parent.role == role) throw ArchiveException("文件中的消息分支已损坏，无法导入")
            val thinkingEnabled = messageJson.get("thinking_enabled")?.let { runCatching { it.asBoolean }.getOrDefault(false) } ?: false
            val searchEnabled = messageJson.get("search_enabled")?.let { runCatching { it.asBoolean }.getOrDefault(false) } ?: false
            val inserted = TimeFormats.parseMillis(messageJson.string("inserted_at")) ?: 0L
            val fragmentsArray = messageJson.array("fragments") ?: JsonArray()
            val officialTypes = fragmentsArray.mapNotNull { element ->
                if (element.isJsonObject) element.asJsonObject.get("type")?.let { runCatching { it.asString }.getOrNull() } else null
            }.toSet()
            val conversationMode = when (role) {
                Role.USER -> "DEFAULT"
                Role.ASSISTANT -> if (officialTypes.contains(FragmentCodec.TYPE_TOOL_OPEN) ||
                    officialTypes.contains(FragmentCodec.TYPE_TOOL_FIND)
                ) "DEEP_SEARCH" else "DEFAULT"
            }
            val fragments = ArrayList<JsonObject>(fragmentsArray.size())
            var stopReason: String? = null
            when (role) {
                Role.USER -> fragmentsArray.forEach { fragmentElement ->
                    if (!fragmentElement.isJsonObject) {
                        skipped += 1
                        return@forEach
                    }
                    val fragment = fragmentElement.asJsonObject
                    when (fragment.string("type")) {
                        FragmentCodec.TYPE_REQUEST, FragmentCodec.TYPE_FILE -> fragments.add(fragment)
                        else -> skipped += 1
                    }
                }
                Role.ASSISTANT -> {
                    val official = ArrayList<JsonObject>(fragmentsArray.size())
                    fragmentsArray.forEach { fragmentElement ->
                        if (fragmentElement.isJsonObject) {
                            official.add(fragmentElement.asJsonObject)
                        } else {
                            skipped += 1
                        }
                    }
                    if (statusRaw == "CONTENT_FILTER") stopReason = "refusal"
                    val blocks = WireCodec.fromOfficialFragments(official, rawId)
                    fragments.addAll(WireCodec.canonical(filterRefused(blocks, statusRaw)))
                    if (statusRaw == "CONTENT_FILTER") {
                        fragments.add(JsonObject().apply {
                            addProperty("type", WireCodec.TYPE_META)
                            addProperty("stop_reason", "refusal")
                        })
                    }
                }
            }
            val canonicalJson = FragmentCodec.encode(fragments)
            val message = ChatMessage(
                id = rawId,
                parentId = parent?.id,
                role = role,
                status = status,
                thinkingEnabled = thinkingEnabled,
                searchEnabled = searchEnabled,
                accumulatedTokenUsage = 0,
                insertedAt = inserted,
                prefix = false,
                fragments = WireCodec.modelFragments(fragments, role),
                conversationMode = conversationMode,
            )
            messages.add(ImportedMessage(message, canonicalJson))
            byId[rawId] = message
        }
        val currentId = if (messages.isEmpty()) null else {
            if (currentRaw <= 0L) messages.last().message.id else currentRaw
        }
        if (currentId != null && !byId.containsKey(currentId)) {
            throw ArchiveException("文件中的消息分支已损坏，无法导入")
        }
        return ImportedSession(
            id = id,
            title = title?.takeIf { it.isNotEmpty() },
            pinned = false,
            currentMessageId = currentId,
            insertedAt = insertedAt,
            updatedAt = updatedAt,
            messages = messages,
        ) to skipped
    }

    private fun filterRefused(blocks: List<WireBlock>, statusRaw: String): List<WireBlock> {
        if (statusRaw != "CONTENT_FILTER") return blocks
        return blocks.filter { it is WireBlock.Atomic || it is WireBlock.Template }
    }

    private fun archiveStatusToModel(status: String): MessageStatus = when (status.uppercase(Locale.US)) {
        "FINISHED" -> MessageStatus.FINISHED
        "CONTENT_FILTER" -> MessageStatus.CONTENT_FILTER
        "FAILED" -> MessageStatus.FAILED
        "CONTEXT_LENGTH_EXCEEDED" -> MessageStatus.CONTEXT_LENGTH_EXCEEDED
        "TIMEOUT" -> MessageStatus.TIMEOUT
        "INCOMPLETE", "WIP" -> MessageStatus.INCOMPLETE
        else -> MessageStatus.FINISHED
    }

    fun sessionPath(sessionId: String): String = "chat_sessions/$sessionId.json"

    fun imagePath(sessionId: String, fileId: String, fileName: String): String =
        "chat_sessions/${sessionId}_files/${safeName(fileId)}_${safeName(fileName)}"

    fun safeName(raw: String): String {
        val replaced = raw.replace('/', '_').replace('\\', '_')
        val normalized = Normalizer.normalize(replaced, Normalizer.Form.NFC)
        return when {
            normalized.isEmpty() || normalized == "." || normalized == ".." -> "unnamed"
            else -> normalized
        }
    }

    fun entryPath(raw: String): String? {
        if (raw.isEmpty()) return null
        if (raw.any { it == '\u0000' || it == '\\' }) return null
        if (raw.startsWith("/")) return null
        if (Regex("^[A-Za-z]:").containsMatchIn(raw)) return null
        val segments = raw.split('/')
        if (segments.any { it.isEmpty() || it == "." || it == ".." }) return null
        return Normalizer.normalize(raw, Normalizer.Form.NFC)
    }

    fun parseTime(element: JsonElement?): Long? {
        if (element == null || element.isJsonNull || !element.isJsonPrimitive) return null
        return TimeFormats.parseMillis(runCatching { element.asString }.getOrNull())
    }

    internal data class ExportedSession(val json: JsonObject, val images: List<ExportedImage>)

    internal data class ExportedImage(val path: String, val fileId: String, val name: String, val localPath: String?)
}
