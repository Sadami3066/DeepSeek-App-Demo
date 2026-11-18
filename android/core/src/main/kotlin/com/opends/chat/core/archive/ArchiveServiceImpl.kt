package com.opends.chat.core.archive

import android.content.Context
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.opends.chat.core.ArchiveService
import com.opends.chat.core.ErrorLog
import com.opends.chat.core.ImportResult
import com.opends.chat.core.OriginalAppSource
import com.opends.chat.core.attach.AttachmentStoreImpl
import com.opends.chat.core.db.SessionStoreImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import org.apache.commons.compress.archivers.zip.Zip64Mode
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.OutputStream

internal class ArchiveServiceImpl(
    context: Context,
    private val store: SessionStoreImpl,
    private val attachments: AttachmentStoreImpl,
) : ArchiveService {

    private val cacheDir = File(context.applicationContext.cacheDir, "imports").apply { mkdirs() }

    override suspend fun importZipFile(file: File, displayName: String): ImportResult =
        withContext(Dispatchers.IO) {
            val size = runCatching { file.length() }.getOrDefault(0L)
            val header = readHeader(file)
            val source = try {
                ArchiveZipSource.open(file)
            } catch (error: Throwable) {
                recordFailure(displayName, size, header, error)
                throw error
            }
            runImport(source, displayName, size, header)
        }

    override suspend fun importZip(bytes: ByteArray): ImportResult = withContext(Dispatchers.IO) {
        val header = bytes.copyOf(minOf(4, bytes.size))
        val source = try {
            ArchiveZipSource.open(bytes, cacheDir)
        } catch (error: Throwable) {
            recordFailure("zip-bytes", bytes.size.toLong(), header, error)
            throw error
        }
        runImport(source, "zip-bytes", bytes.size.toLong(), header)
    }

    private suspend fun runImport(
        source: ArchiveZipSource,
        displayName: String,
        size: Long,
        header: ByteArray,
    ): ImportResult = source.use { zipSource ->
        try {
            val inspection = ArchiveZipImport.inspect(zipSource)
            val tempImages = File(cacheDir, "images-${System.nanoTime()}").apply { mkdirs() }
            try {
                store.replaceAllStreaming { sink ->
                    ArchiveZipImport.copy(
                        source = zipSource,
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
                if (inspection.warnings.isNotEmpty()) {
                    ErrorLog.record(
                        category = "导入",
                        summary = "导入完成，存在兼容处理：$displayName",
                        detail = inspection.warnings.joinToString("；"),
                    )
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
        } catch (error: Throwable) {
            recordFailure(displayName, size, header, error)
            if (error is ArchiveException) throw error
            throw ArchiveException("导入失败：${error.message ?: "文件内容不完整，无法导入"}")
        }
    }

    override suspend fun exportZipTo(out: OutputStream): Int = withContext(Dispatchers.IO) {
        val sessions = store.loadSessionsNow()
        var imageCount = 0
        ZipArchiveOutputStream(out).use { zip ->
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
                        imageCount += 1
                    }
                }
                indexEntries.add(ArchiveJson.indexEntry(session, stored.size))
            }
            writeEntry(zip, INDEX_JSON, indexEntries.toString().toByteArray(Charsets.UTF_8))
        }
        imageCount
    }

    override suspend fun exportZip(): ByteArray = withContext(Dispatchers.IO) {
        val output = ByteArrayOutputStream()
        exportZipTo(output)
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

    private fun readHeader(file: File): ByteArray = runCatching {
        file.inputStream().use { input ->
            val buffer = ByteArray(4)
            val read = input.read(buffer)
            if (read <= 0) ByteArray(0) else buffer.copyOf(read)
        }
    }.getOrDefault(ByteArray(0))

    private fun recordFailure(displayName: String, size: Long, header: ByteArray, error: Throwable) {
        val headerText = if (header.isEmpty()) "空" else header.joinToString(" ") { byte -> "%02X".format(byte) }
        ErrorLog.record(
            category = "导入",
            summary = "导入失败：$displayName",
            detail = "大小=$size 字节，文件头=$headerText",
            error = error,
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
        private const val FIXED_TIME = 315532800000L
    }
}
