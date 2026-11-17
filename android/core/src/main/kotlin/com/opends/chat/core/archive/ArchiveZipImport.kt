package com.opends.chat.core.archive

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.opends.chat.core.db.FragmentCodec
import com.opends.chat.core.db.ImportedSession
import com.opends.chat.core.db.ImportedMessage

internal data class ArchiveInspection(
    val sessionIds: Set<String>,
    val sessions: Int,
    val messages: Int,
    val skipped: Int,
    val imagePaths: Map<String, String>,
    val warnings: List<String>,
)

internal data class ArchiveCopyStats(
    val sessions: Int,
    val messages: Int,
    val images: Int,
)

private data class ValidationEntry(
    val entry: ArchiveZipSource.Entry,
    val path: String,
)

internal object ArchiveZipImport {

    private val sessionPathPattern = Regex("^chat_sessions/([^/]+)\\.json$")
    private val imagePathMarker = "_files/"

    fun inspect(source: ArchiveZipSource): ArchiveInspection {
        val entries = source.entries().filterNot { it.isDirectory }
        if (entries.isEmpty()) throw ArchiveException("无法识别该文件，请选择 DeepSeek 导出的历史对话 ZIP 文件")
        val normalized = ArrayList<ValidationEntry>(entries.size)
        entries.forEach { entry ->
            val path = ArchiveJson.entryPath(entry.name)
                ?: throw ArchiveException("无法识别该文件，请选择 DeepSeek 导出的历史对话 ZIP 文件")
            if (!source.canRead(entry)) throw ArchiveException("文件内容不完整，无法导入")
            normalized.add(ValidationEntry(entry, path))
        }
        if (normalized.first().path != MANIFEST) {
            throw ArchiveException("无法识别该文件，请选择 DeepSeek 导出的历史对话 ZIP 文件")
        }
        val knownSessionIds = HashSet<String>()
        normalized.forEach { entry ->
            sessionPathPattern.matchEntire(entry.path)?.let { knownSessionIds.add(it.groupValues[1]) }
        }
        var manifestSeen = false
        var indexSeen = false
        var sessions = 0
        var messages = 0
        var skipped = 0
        val seenSessionIds = HashSet<String>()
        val imagePaths = LinkedHashMap<String, String>()
        var indexIds: Set<String>? = null
        normalized.forEach { entry ->
            val path = entry.path
            try {
                when {
                    !manifestSeen -> {
                        if (path != MANIFEST) {
                            throw ArchiveException("无法识别该文件，请选择 DeepSeek 导出的历史对话 ZIP 文件")
                        }
                        val manifest = parseObject(source.read(entry.entry))
                        ArchiveJson.checkManifest(manifest)
                        manifestSeen = true
                    }
                    path == USER_JSON -> {
                        if (indexSeen || sessions > 0) throw incomplete()
                    }
                    path == INDEX_JSON -> {
                        if (indexSeen) throw incomplete()
                        indexSeen = true
                        indexIds = parseIndexIds(source.read(entry.entry))
                    }
                    sessionPathPattern.matches(path) -> {
                        if (indexSeen) throw incomplete()
                        val sessionId = sessionPathPattern.matchEntire(path)!!.groupValues[1]
                        if (!seenSessionIds.add(sessionId)) throw incomplete()
                        val json = parseObject(source.read(entry.entry))
                        val parsed = ArchiveJson.importSession(json, sessionId)
                        sessions += 1
                        messages += parsed.first.messages.size
                        skipped += parsed.second
                        collectImagePaths(json, sessionId, knownSessionIds, imagePaths)
                    }
                    imagePathMarker in path -> {
                        val sessionId = path.substringAfter("chat_sessions/", "").substringBefore("_files/")
                        if (sessionId.isEmpty() || !knownSessionIds.contains(sessionId)) {
                            throw ArchiveException("文件中的附件数据已损坏，无法导入")
                        }
                    }
                    else -> throw incomplete()
                }
            } catch (error: ArchiveException) {
                throw error
            } catch (error: Exception) {
                throw incomplete()
            }
        }
        if (!manifestSeen || !indexSeen) throw incomplete()
        if (indexIds != seenSessionIds) throw incomplete()
        val availableImages = normalized.count { entry -> imagePaths.containsKey(entry.path) }
        val warnings = ArrayList<String>()
        if (imagePaths.isNotEmpty() && availableImages < imagePaths.size) {
            warnings.add("部分图片缺少原件，仅保留文字记录")
        }
        return ArchiveInspection(
            sessionIds = seenSessionIds,
            sessions = sessions,
            messages = messages,
            skipped = skipped,
            imagePaths = imagePaths,
            warnings = warnings,
        )
    }

    fun copy(
        source: ArchiveZipSource,
        inspection: ArchiveInspection,
        onSession: (ImportedSession) -> Unit,
        onImage: (fileId: String, bytes: ByteArray) -> Unit,
    ): ArchiveCopyStats {
        var sessions = 0
        var messages = 0
        var images = 0
        val deliveredImages = HashSet<String>()
        source.entries().forEach { entry ->
            if (entry.isDirectory) return@forEach
            val path = ArchiveJson.entryPath(entry.name) ?: return@forEach
            when {
                sessionPathPattern.matches(path) -> {
                    val sessionId = sessionPathPattern.matchEntire(path)!!.groupValues[1]
                    if (!inspection.sessionIds.contains(sessionId)) return@forEach
                    val json = parseObject(source.read(entry))
                    val (session, _) = ArchiveJson.importSession(json, sessionId)
                    onSession(session)
                    sessions += 1
                    messages += session.messages.size
                }
                inspection.imagePaths.containsKey(path) -> {
                    val fileId = inspection.imagePaths.getValue(path)
                    if (!deliveredImages.add(fileId)) return@forEach
                    val bytes = source.read(entry)
                    onImage(fileId, bytes)
                    images += 1
                }
            }
        }
        return ArchiveCopyStats(sessions = sessions, messages = messages, images = images)
    }

    private fun collectImagePaths(
        sessionJson: JsonObject,
        sessionId: String,
        knownSessionIds: Set<String>,
        imagePaths: MutableMap<String, String>,
    ) {
        val messages = sessionJson.getAsJsonArray("messages") ?: return
        messages.forEach { messageElement ->
            if (!messageElement.isJsonObject) return@forEach
            val fragments = messageElement.asJsonObject.getAsJsonArray("fragments") ?: return@forEach
            fragments.forEach { fragmentElement ->
                if (!fragmentElement.isJsonObject) return@forEach
                val fragment = fragmentElement.asJsonObject
                if (fragment.type() != FragmentCodec.TYPE_FILE) return@forEach
                val files = fragment.getAsJsonArray("files") ?: return@forEach
                files.forEach { fileElement ->
                    if (!fileElement.isJsonObject) return@forEach
                    val file = fileElement.asJsonObject
                    val isImage = file.get("is_image")?.let { runCatching { it.asBoolean }.getOrDefault(false) } ?: false
                    if (!isImage) return@forEach
                    val exportPath = file.get("export_path")?.takeIf { it.isJsonPrimitive }
                        ?.let { runCatching { it.asString }.getOrNull() } ?: return@forEach
                    val fileId = file.get("id")?.takeIf { it.isJsonPrimitive }
                        ?.let { runCatching { it.asString }.getOrNull() }
                        ?.removePrefix("file-") ?: return@forEach
                    if (exportPath.isEmpty() || fileId.isEmpty()) return@forEach
                    val normalizedPath = ArchiveJson.entryPath(exportPath)
                        ?: throw ArchiveException("文件中的附件数据已损坏，无法导入")
                    if (!normalizedPath.startsWith("chat_sessions/${sessionId}_files/")) {
                        throw ArchiveException("文件中的附件数据已损坏，无法导入")
                    }
                    val owner = normalizedPath.substringAfter("chat_sessions/").substringBefore("_files/")
                    if (!knownSessionIds.contains(owner)) {
                        throw ArchiveException("文件中的附件数据已损坏，无法导入")
                    }
                    val previous = imagePaths[normalizedPath]
                    if (previous != null && previous != fileId) {
                        throw ArchiveException("文件中的附件数据已损坏，无法导入")
                    }
                    imagePaths[normalizedPath] = fileId
                }
            }
        }
    }

    private fun parseIndexIds(bytes: ByteArray): Set<String> {
        val parsed = runCatching { JsonParser.parseString(String(bytes, Charsets.UTF_8)) }.getOrNull()
        if (parsed == null || !parsed.isJsonArray) throw incomplete()
        val ids = HashSet<String>()
        parsed.asJsonArray.forEach { element ->
            if (!element.isJsonObject) return@forEach
            val id = element.asJsonObject.get("id")?.takeIf { it.isJsonPrimitive }
                ?.let { runCatching { it.asString }.getOrNull() }
            if (id.isNullOrEmpty() || !ids.add(id)) throw incomplete()
        }
        return ids
    }

    private fun parseObject(bytes: ByteArray): JsonObject {
        val parsed = runCatching { JsonParser.parseString(String(bytes, Charsets.UTF_8)) }.getOrNull()
            ?: throw ArchiveException("文件不是有效的 JSON")
        if (!parsed.isJsonObject) throw incomplete()
        return parsed.asJsonObject
    }

    private fun JsonObject.type(): String? {
        val element = get("type") ?: return null
        if (element.isJsonNull || !element.isJsonPrimitive) return null
        return runCatching { element.asString }.getOrNull()
    }

    private fun incomplete() = ArchiveException("文件内容不完整，无法导入")

    private const val MANIFEST = "manifest.json"
    private const val USER_JSON = "user.json"
    private const val INDEX_JSON = "chat_session_index.json"
}
