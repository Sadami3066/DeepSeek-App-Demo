package com.opends.chat.core.archive

import android.content.Context
import com.google.gson.JsonArray
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.opends.chat.core.ImportResult
import com.opends.chat.core.ArchiveService
import com.opends.chat.core.OriginalAppSource
import com.opends.chat.core.SessionInfo
import com.opends.chat.core.attach.AttachmentStoreImpl
import com.opends.chat.core.db.ImportedSession
import com.opends.chat.core.db.SessionStoreImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import org.apache.commons.compress.archivers.zip.Zip64Mode
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File

internal class ArchiveServiceImpl(
    context: Context,
    private val store: SessionStoreImpl,
    private val attachments: AttachmentStoreImpl,
) : ArchiveService {

    private val cacheDir = File(context.applicationContext.cacheDir, "imports").apply { mkdirs() }

    override suspend fun importZip(bytes: ByteArray): ImportResult = withContext(Dispatchers.IO) {
        val entries = readEntries(bytes)
        if (entries.isEmpty()) throw ArchiveException("无法识别该文件，请选择 DeepSeek 导出的历史对话 ZIP 文件")
        val paths = entries.keys.toList()
        if (paths.first() != MANIFEST) {
            throw ArchiveException("无法识别该文件，请选择 DeepSeek 导出的历史对话 ZIP 文件")
        }
        val manifest = parseJson(entries.getValue(MANIFEST))
        ArchiveJson.checkManifest(manifest)

        val sessions = ArrayList<ImportedSession>()
        val images = HashMap<String, String>()
        var skipped = 0
        var sawIndex = false
        val warnings = ArrayList<String>()
        paths.drop(1).forEach { path ->
            when {
                path == USER_JSON -> {
                    if (sawIndex || sessions.isNotEmpty()) throw ArchiveException("文件内容不完整，无法导入")
                }
                path == INDEX_JSON -> {
                    if (sawIndex) throw ArchiveException("文件内容不完整，无法导入")
                    sawIndex = true
                }
                path.startsWith("chat_sessions/") && path.endsWith(".json") && path.count { it == '/' } == 1 -> {
                    if (sawIndex) throw ArchiveException("文件内容不完整，无法导入")
                    val sessionId = path.removePrefix("chat_sessions/").removeSuffix(".json")
                    val json = parseJson(entries.getValue(path))
                    val (imported, skippedFragments) = ArchiveJson.importSession(json, sessionId)
                    skipped += skippedFragments
                    sessions.add(imported)
                    collectImages(json, sessionId, images)
                }
                path.startsWith("chat_sessions/") && path.contains("_files/") -> Unit
                else -> throw ArchiveException("文件内容不完整，无法导入")
            }
        }
        if (!sawIndex) throw ArchiveException("文件内容不完整，无法导入")
        val index = JsonParser.parseString(String(entries.getValue(INDEX_JSON), Charsets.UTF_8))
        if (!index.isJsonArray) throw ArchiveException("文件内容不完整，无法导入")
        val indexIds = index.asJsonArray.mapNotNull { element ->
            if (element.isJsonObject) element.asJsonObject.get("id")?.let { runCatching { it.asString }.getOrNull() } else null
        }.toSet()
        if (indexIds != sessions.map { it.id }.toSet()) throw ArchiveException("文件内容不完整，无法导入")

        var imageCount = 0
        val savedImageIds = HashSet<String>()
        sessions.forEach { session ->
            val prefix = "chat_sessions/${session.id}_files/"
            entries.forEach { (path, data) ->
                if (!path.startsWith(prefix)) return@forEach
                val fileId = images[path] ?: return@forEach
                if (savedImageIds.contains(fileId)) return@forEach
                attachments.save(fileId, data)
                savedImageIds.add(fileId)
                imageCount += 1
            }
        }
        val messageCount = sessions.sumOf { it.messages.size }
        store.replaceAll(sessions)
        if (imageCount == 0 && sessions.sumOf { session ->
                session.messages.sumOf { message -> message.message.fragments.count { it is com.opends.chat.core.Fragment.File && it.isImage } }
            } > 0
        ) {
            warnings.add("部分图片缺少原件，仅保留文字记录")
        }
        ImportResult(
            sessions = sessions.size,
            messages = messageCount,
            images = imageCount,
            skipped = skipped,
            warnings = warnings,
        )
    }

    private fun collectImages(sessionJson: JsonObject, sessionId: String, images: MutableMap<String, String>) {
        val messages = sessionJson.getAsJsonArray("messages") ?: return
        messages.forEach { element ->
            if (!element.isJsonObject) return@forEach
            val fragments = element.asJsonObject.getAsJsonArray("fragments") ?: return@forEach
            fragments.forEach { fragmentElement ->
                if (!fragmentElement.isJsonObject) return@forEach
                val fragment = fragmentElement.asJsonObject
                val type = fragment.get("type")?.let { runCatching { it.asString }.getOrNull() }
                if (type != "FILE") return@forEach
                val files = fragment.getAsJsonArray("files") ?: return@forEach
                files.forEach { fileElement ->
                    if (!fileElement.isJsonObject) return@forEach
                    val file = fileElement.asJsonObject
                    val isImage = file.get("is_image")?.let { runCatching { it.asBoolean }.getOrDefault(false) } ?: false
                    if (!isImage) return@forEach
                    val exportPath = file.get("export_path")?.let { elementPath(it) } ?: return@forEach
                    val fileId = file.get("id")?.let { runCatching { it.asString }.getOrNull() } ?: return@forEach
                    if (exportPath.isEmpty() || fileId.isEmpty()) return@forEach
                    val normalized = ArchiveJson.entryPath(exportPath) ?: throw ArchiveException("文件中的附件数据已损坏，无法导入")
                    if (!normalized.startsWith("chat_sessions/${sessionId}_files/")) {
                        throw ArchiveException("文件中的附件数据已损坏，无法导入")
                    }
                    images[normalized] = fileId.removePrefix("file-")
                }
            }
        }
    }

    private fun elementPath(element: com.google.gson.JsonElement): String? =
        if (element.isJsonPrimitive) runCatching { element.asString }.getOrNull() else null

    override suspend fun exportZip(): ByteArray = withContext(Dispatchers.IO) {
        val sessions = store.loadSessionsNow()
        val output = ByteArrayOutputStream()
        ZipArchiveOutputStream(output).use { zip ->
            zip.setEncoding("UTF-8")
            zip.setUseLanguageEncodingFlag(true)
            zip.setUseZip64(Zip64Mode.Never)
            writeEntry(zip, MANIFEST, ArchiveJson.manifest().toString().toByteArray(Charsets.UTF_8))
            val indexEntries = JsonArray()
            sessions.forEach { session ->
                val stored = store.loadStoredMessages(session.id)
                val exported = ArchiveJson.exportSession(session, stored) { fileId, fileName ->
                    ArchiveJson.imagePath(session.id, fileId, fileName)
                }
                writeEntry(
                    zip,
                    ArchiveJson.sessionPath(session.id),
                    exported.json.toString().toByteArray(Charsets.UTF_8),
                )
                exported.images.forEach { image ->
                    val local = image.localPath?.let { File(it) } ?: attachments.localFile(image.fileId)
                    if (local != null && local.exists() && local.isFile) {
                        writeEntry(zip, image.path, local.readBytes())
                    }
                }
                indexEntries.add(ArchiveJson.indexEntry(session, stored.size))
            }
            writeEntry(zip, INDEX_JSON, indexEntries.toString().toByteArray(Charsets.UTF_8))
        }
        output.toByteArray()
    }

    override suspend fun importFromOriginalApp(readSource: OriginalAppSource): ImportResult = withContext(Dispatchers.IO) {
        val source = readSource as? OriginalAppSource.DatabaseBytes
            ?: throw ArchiveException("请手动选择原版 App 的 .db 数据库文件进行导入")
        val importer = OriginalAppImporter(cacheDir)
        val parsed = importer.parse(source)
        if (parsed.first.isEmpty()) {
            throw ArchiveException("未找到可导入的原版会话数据")
        }
        val messageCount = parsed.first.sumOf { it.messages.size }
        store.replaceAll(parsed.first)
        ImportResult(
            sessions = parsed.first.size,
            messages = messageCount,
            images = 0,
            skipped = parsed.second,
            warnings = parsed.third,
        )
    }

    private fun readEntries(bytes: ByteArray): LinkedHashMap<String, ByteArray> {
        val map = LinkedHashMap<String, ByteArray>()
        val input = ZipArchiveInputStream(ByteArrayInputStream(bytes))
        input.use { zip ->
            while (true) {
                val entry = zip.nextEntry as? ZipArchiveEntry ?: break
                val rawName = entry.name
                if (entry.isDirectory) {
                    if (entry.size != 0L) throw ArchiveException("文件内容不完整，无法导入")
                    continue
                }
                val path = ArchiveJson.entryPath(rawName)
                    ?: throw ArchiveException("无法识别该文件，请选择 DeepSeek 导出的历史对话 ZIP 文件")
                if (map.containsKey(path)) throw ArchiveException("文件内容不完整，无法导入")
                val buffer = ByteArrayOutputStream()
                val chunk = ByteArray(8192)
                while (true) {
                    val read = zip.read(chunk)
                    if (read < 0) break
                    if (read > 0) buffer.write(chunk, 0, read)
                }
                map[path] = buffer.toByteArray()
            }
        }
        return map
    }

    private fun parseJson(bytes: ByteArray): JsonObject {
        val parsed = runCatching { JsonParser.parseString(String(bytes, Charsets.UTF_8)) }.getOrNull()
            ?: throw ArchiveException("文件不是有效的 JSON")
        if (!parsed.isJsonObject) throw ArchiveException("文件内容不完整，无法导入")
        return parsed.asJsonObject
    }

    private fun writeEntry(zip: ZipArchiveOutputStream, path: String, bytes: ByteArray) {
        val entry = ZipArchiveEntry(path)
        entry.time = FIXED_TIME
        zip.putArchiveEntry(entry)
        zip.write(bytes)
        zip.closeArchiveEntry()
    }

    companion object {
        private const val MANIFEST = "manifest.json"
        private const val INDEX_JSON = "chat_session_index.json"
        private const val USER_JSON = "user.json"
        private const val FIXED_TIME = 315532800000L
    }
}
