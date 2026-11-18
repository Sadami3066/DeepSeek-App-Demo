package com.opends.chat.core.archive

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.opends.chat.core.db.FragmentCodec
import com.opends.chat.core.db.ImportedSession
import com.opends.chat.core.util.array
import com.opends.chat.core.util.number
import com.opends.chat.core.util.string

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

private data class SessionEntry(
    val sessionId: String,
    val entry: ArchiveZipSource.Entry,
    val path: String,
)

internal object ArchiveZipImport {

    private const val MANIFEST = "manifest.json"
    private const val USER_JSON = "user.json"
    private const val INDEX_JSON = "chat_session_index.json"
    private val sessionPathPattern = Regex("^chat_sessions/([^/]+)\\.json$")
    private val imagePathPattern = Regex("^chat_sessions/([^/]+)_files/.+$")

    fun inspect(source: ArchiveZipSource): ArchiveInspection {
        val entries = source.entries().filterNot { it.isDirectory }
        if (entries.isEmpty()) throw unrecognized()
        val manifestEntries = ArrayList<ValidationEntry>()
        val indexEntries = ArrayList<ValidationEntry>()
        val sessionEntries = ArrayList<SessionEntry>()
        val imageEntries = ArrayList<ValidationEntry>()
        var ignoredEntries = 0
        entries.forEach { entry ->
            val path = ArchiveJson.entryPath(entry.name)
            if (path == null) {
                ignoredEntries += 1
                return@forEach
            }
            if (!source.canRead(entry)) throw incomplete()
            when {
                path == MANIFEST -> manifestEntries.add(ValidationEntry(entry, path))
                path == USER_JSON -> Unit
                path == INDEX_JSON -> indexEntries.add(ValidationEntry(entry, path))
                sessionPathPattern.matches(path) -> {
                    val sessionId = sessionPathPattern.matchEntire(path)!!.groupValues[1]
                    sessionEntries.add(SessionEntry(sessionId, entry, path))
                }
                imagePathPattern.matches(path) -> imageEntries.add(ValidationEntry(entry, path))
                else -> ignoredEntries += 1
            }
        }
        val warnings = ArrayList<String>()
        if (ignoredEntries > 0) warnings.add("已忽略 $ignoredEntries 个未识别文件")
        if (manifestEntries.isEmpty()) throw ArchiveException("文件版本无效，无法导入")
        if (manifestEntries.size > 1) warnings.add("存在 ${manifestEntries.size - 1} 个重复的清单文件，已忽略")
        val manifest = parseObject(source.read(manifestEntries.first().entry))
        checkManifest(manifest)
        if (sessionEntries.isEmpty()) throw unrecognized()
        val uniqueSessions = LinkedHashMap<String, ValidationEntry>()
        sessionEntries.forEach { session ->
            if (uniqueSessions.put(session.sessionId, ValidationEntry(session.entry, session.path)) != null) {
                warnings.add("存在重复的会话文件，已忽略：${session.sessionId}")
            }
        }
        val knownSessionIds = uniqueSessions.keys
        var sessions = 0
        var messages = 0
        var skipped = 0
        val imagePaths = LinkedHashMap<String, String>()
        val sessionWarnings = LinkedHashSet<String>()
        val parsedSessionIds = LinkedHashSet<String>()
        uniqueSessions.forEach { (sessionId, entry) ->
            val json = parseObject(source.read(entry.entry))
            val result = ArchiveJson.importSessionDetailed(json, sessionId)
            sessions += 1
            messages += result.session.messages.size
            skipped += result.skipped
            sessionWarnings.addAll(result.warnings)
            parsedSessionIds.add(sessionId)
            collectImagePaths(json, knownSessionIds, imagePaths, warnings)
        }
        if (indexEntries.isNotEmpty()) {
            if (indexEntries.size > 1) warnings.add("存在多个会话索引文件，已使用第一个")
            val indexIds = runCatching { parseIndexIds(source.read(indexEntries.first().entry)) }.getOrNull()
            if (indexIds == null) {
                warnings.add("会话索引无法解析，已忽略")
            } else if (indexIds != parsedSessionIds) {
                warnings.add("会话索引与文件内容不一致，已按实际会话文件导入")
            }
        }
        val availableImages = imageEntries.count { entry -> imagePaths.containsKey(entry.path) }
        if (imagePaths.isNotEmpty() && availableImages < imagePaths.size) {
            warnings.add("部分图片缺少原件，仅保留文字记录")
        }
        warnings.addAll(sessionWarnings)
        return ArchiveInspection(
            sessionIds = parsedSessionIds,
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
        val handledSessions = HashSet<String>()
        source.entries().forEach { entry ->
            if (entry.isDirectory) return@forEach
            val path = ArchiveJson.entryPath(entry.name) ?: return@forEach
            when {
                sessionPathPattern.matches(path) -> {
                    val sessionId = sessionPathPattern.matchEntire(path)!!.groupValues[1]
                    if (!inspection.sessionIds.contains(sessionId)) return@forEach
                    if (!handledSessions.add(sessionId)) return@forEach
                    val json = parseObject(source.read(entry))
                    val (session, _) = ArchiveJson.importSession(json, sessionId)
                    onSession(session)
                    sessions += 1
                    messages += session.messages.size
                }
                inspection.imagePaths.containsKey(path) -> {
                    val fileId = inspection.imagePaths.getValue(path)
                    if (!deliveredImages.add(fileId)) return@forEach
                    onImage(fileId, source.read(entry))
                    images += 1
                }
            }
        }
        return ArchiveCopyStats(sessions = sessions, messages = messages, images = images)
    }

    private fun collectImagePaths(
        sessionJson: JsonObject,
        knownSessionIds: Set<String>,
        imagePaths: MutableMap<String, String>,
        warnings: MutableList<String>,
    ) {
        var orphanImages = 0
        var invalidImages = 0
        val messages = sessionJson.array("messages") ?: return
        messages.forEach { messageElement ->
            if (!messageElement.isJsonObject) return@forEach
            val fragments = messageElement.asJsonObject.array("fragments") ?: return@forEach
            fragments.forEach { fragmentElement ->
                if (!fragmentElement.isJsonObject) return@forEach
                val fragment = fragmentElement.asJsonObject
                if (fragment.string("type") != FragmentCodec.TYPE_FILE) return@forEach
                val files = fragment.array("files") ?: return@forEach
                files.forEach { fileElement ->
                    if (!fileElement.isJsonObject) return@forEach
                    val file = fileElement.asJsonObject
                    val isImage = file.get("is_image")?.let { runCatching { it.asBoolean }.getOrDefault(false) } ?: false
                    if (!isImage) return@forEach
                    val exportPath = file.string("export_path") ?: return@forEach
                    if (exportPath.isEmpty()) return@forEach
                    val fileId = file.string("id")?.removePrefix("file-") ?: return@forEach
                    if (fileId.isEmpty()) return@forEach
                    val normalizedPath = ArchiveJson.entryPath(exportPath)
                    if (normalizedPath == null) {
                        invalidImages += 1
                        return@forEach
                    }
                    val owner = imagePathPattern.matchEntire(normalizedPath)?.groupValues?.getOrNull(1)
                    if (owner == null || !knownSessionIds.contains(owner)) {
                        orphanImages += 1
                        return@forEach
                    }
                    val previous = imagePaths[normalizedPath]
                    if (previous != null && previous != fileId) {
                        warnings.add("存在图片映射冲突，已保留第一份：$owner")
                        return@forEach
                    }
                    imagePaths[normalizedPath] = fileId
                }
            }
        }
        if (orphanImages > 0) warnings.add("已忽略 $orphanImages 张归属不明的图片")
        if (invalidImages > 0) warnings.add("已忽略 $invalidImages 张路径非法的图片")
    }

    private fun checkManifest(manifest: JsonObject) {
        val version = manifest.number("export_format_version")
            ?: throw ArchiveException("文件版本无效，无法导入")
        if (version > 3) throw ArchiveException("文件版本过新，请升级后再导入")
        if (version != 1.0 && version != 2.0 && version != 3.0) {
            throw ArchiveException("文件版本无效，无法导入")
        }
    }

    private fun parseIndexIds(bytes: ByteArray): Set<String>? {
        val parsed = runCatching { JsonParser.parseString(String(bytes, Charsets.UTF_8)) }.getOrNull()
        if (parsed == null || !parsed.isJsonArray) return null
        val ids = LinkedHashSet<String>()
        parsed.asJsonArray.forEach { element ->
            if (!element.isJsonObject) return@forEach
            val id = element.asJsonObject.string("id")
            if (!id.isNullOrEmpty()) ids.add(id)
        }
        return ids
    }

    private fun parseObject(bytes: ByteArray): JsonObject {
        val parsed = runCatching { JsonParser.parseString(String(bytes, Charsets.UTF_8)) }.getOrNull()
            ?: throw ArchiveException("文件不是有效的 JSON")
        if (!parsed.isJsonObject) throw incomplete()
        return parsed.asJsonObject
    }

    private fun unrecognized(): ArchiveException =
        ArchiveException("无法识别该文件，请选择 DeepSeek 导出的历史对话 ZIP 文件")

    private fun incomplete(): ArchiveException = ArchiveException("文件内容不完整，无法导入")
}
