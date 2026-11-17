package com.opends.chat.core.archive

import android.content.Context
import com.google.gson.JsonArray
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.opends.chat.core.ImportResult
import com.opends.chat.core.ArchiveService
import com.opends.chat.core.OriginalAppSource
import com.opends.chat.core.SessionInfo
import com.opends.chat.core.attach.AttachmentStoreImpl
import com.opends.chat.core.db.SessionStoreImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import org.apache.commons.compress.archivers.zip.Zip64Mode
import java.io.ByteArrayOutputStream
import java.io.File

internal class ArchiveServiceImpl(
    context: Context,
    private val store: SessionStoreImpl,
    private val attachments: AttachmentStoreImpl,
) : ArchiveService {

    private val cacheDir = File(context.applicationContext.cacheDir, "imports").apply { mkdirs() }

    override suspend fun importZip(bytes: ByteArray): ImportResult = withContext(Dispatchers.IO) {
        val importRoot = File(cacheDir, "import").apply { mkdirs() }
        ArchiveZipSource.open(bytes, cacheDir).use { source ->
            val inspection = ArchiveZipImport.inspect(source)
            val tempImages = File(importRoot, "images-${System.nanoTime()}").apply { mkdirs() }
            try {
                store.replaceAllStreaming { sink ->
                    ArchiveZipImport.copy(
                        source = source,
                        inspection = inspection,
                        onSession = { session -> sink.insert(session) },
                        onImage = { fileId, imageBytes -> File(tempImages, fileId).writeBytes(imageBytes) },
                    )
                }
                var savedImages = 0
                tempImages.listFiles().orEmpty().forEach { file ->
                    if (file.isFile && file.length() > 0L) {
                        attachments.save(file.name, file.readBytes())
                        savedImages += 1
                    }
                }
                ImportResult(
                    sessions = inspection.sessions,
                    messages = inspection.messages,
                    images = savedImages,
                    skipped = inspection.skipped,
                    warnings = inspection.warnings,
                )
            } finally {
                runCatching { tempImages.deleteRecursively() }
            }
        }
    }

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
