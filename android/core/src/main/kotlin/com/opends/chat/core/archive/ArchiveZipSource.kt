package com.opends.chat.core.archive

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipFile
import java.io.File
import java.util.UUID

internal class ArchiveZipSource private constructor(
    private val zipFile: ZipFile,
    private val tempFile: File?,
) : AutoCloseable {

    internal class Entry(
        val name: String,
        val isDirectory: Boolean,
        val size: Long,
        internal val archiveEntry: ZipArchiveEntry,
    )

    private val orderedEntries: List<Entry> = zipFile.entries.toList()
        .sortedBy { (it as ZipArchiveEntry).localHeaderOffset }
        .map { entry ->
            Entry(
                name = entry.name,
                isDirectory = entry.isDirectory,
                size = entry.size,
                archiveEntry = entry as ZipArchiveEntry,
            )
        }

    fun entries(): List<Entry> = orderedEntries

    fun read(entry: Entry): ByteArray =
        zipFile.getInputStream(entry.archiveEntry).use { input -> input.readBytes() }

    fun canRead(entry: Entry): Boolean = zipFile.canReadEntryData(entry.archiveEntry)

    override fun close() {
        runCatching { zipFile.close() }
        tempFile?.let { runCatching { it.delete() } }
    }

    companion object {
        fun open(file: File): ArchiveZipSource {
            val zipFile = try {
                ZipFile(file)
            } catch (error: Exception) {
                throw ArchiveException("无法识别该文件，请选择 DeepSeek 导出的历史对话 ZIP 文件")
            }
            return ArchiveZipSource(zipFile, null)
        }

        fun open(bytes: ByteArray, cacheDir: File): ArchiveZipSource {
            if (!isZipHeader(bytes)) {
                throw ArchiveException("无法识别该文件，请选择 DeepSeek 导出的历史对话 ZIP 文件")
            }
            val workDir = File(cacheDir, "zip").apply { mkdirs() }
            val tempFile = File(workDir, "${UUID.randomUUID()}.zip")
            try {
                tempFile.writeBytes(bytes)
                val zipFile = try {
                    ZipFile(tempFile)
                } catch (error: Exception) {
                    throw ArchiveException("无法识别该文件，请选择 DeepSeek 导出的历史对话 ZIP 文件")
                }
                return ArchiveZipSource(zipFile, tempFile)
            } catch (error: ArchiveException) {
                runCatching { tempFile.delete() }
                throw error
            } catch (error: Exception) {
                runCatching { tempFile.delete() }
                throw ArchiveException("无法识别该文件，请选择 DeepSeek 导出的历史对话 ZIP 文件")
            }
        }

        private fun isZipHeader(bytes: ByteArray): Boolean {
            if (bytes.size < 4) return false
            return bytes[0] == 'P'.code.toByte() && bytes[1] == 'K'.code.toByte() &&
                bytes[2] == 0x03.toByte() && bytes[3] == 0x04.toByte()
        }
    }
}
