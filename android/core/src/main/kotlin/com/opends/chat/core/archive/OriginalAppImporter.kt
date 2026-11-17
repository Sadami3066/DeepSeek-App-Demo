package com.opends.chat.core.archive

import android.database.sqlite.SQLiteDatabase
import com.opends.chat.core.OriginalAppSource
import com.opends.chat.core.db.ImportedSession
import java.io.File
import java.util.UUID

internal class OriginalAppImporter(private val cacheDir: File) {

    fun parse(readSource: OriginalAppSource.DatabaseBytes): Triple<List<ImportedSession>, Int, List<String>> {
        val warnings = ArrayList<String>()
        val databaseFiles = extractManualDatabase(readSource, warnings)
        val sessions = ArrayList<ImportedSession>()
        var skipped = 0
        databaseFiles.forEach { database ->
            runCatching { parseDatabase(database) }
                .onSuccess { parsed ->
                    sessions.addAll(parsed.first)
                    skipped += parsed.second
                }
                .onFailure { error ->
                    warnings.add("${database.name} 读取失败：${error.message ?: "未知错误"}")
                }
        }
        return Triple(sessions, skipped, warnings)
    }

    fun parse(reader: OriginalDatabaseReader): Pair<List<ImportedSession>, Int> =
        OriginalSessionConverter.convert(reader)

    private fun extractManualDatabase(
        source: OriginalAppSource.DatabaseBytes,
        warnings: MutableList<String>,
    ): List<File> {
        val workDir = File(cacheDir, "manual").apply { mkdirs() }
        val name = ArchiveJson.safeName(source.fileName.ifBlank { "original.db" })
        val target = File(workDir, "${UUID.randomUUID()}_$name")
        target.writeBytes(source.bytes)
        if (!name.endsWith(".db")) warnings.add("所选文件可能不是数据库文件，仍尝试读取")
        return listOf(target)
    }

    private fun parseDatabase(database: File): Pair<List<ImportedSession>, Int> {
        val workDir = File(cacheDir, "work").apply { mkdirs() }
        val copy = File(workDir, "${UUID.randomUUID()}.db")
        database.copyTo(copy, overwrite = true)
        File("${database.absolutePath}-wal").takeIf { it.exists() }?.let {
            it.copyTo(File("${copy.absolutePath}-wal"), overwrite = true)
        }
        File("${database.absolutePath}-shm").takeIf { it.exists() }?.let {
            it.copyTo(File("${copy.absolutePath}-shm"), overwrite = true)
        }
        var db: SQLiteDatabase? = null
        try {
            db = SQLiteDatabase.openDatabase(copy.absolutePath, null, SQLiteDatabase.OPEN_READWRITE)
            val reader = AndroidOriginalDatabaseReader(db)
            if (!reader.hasSessionList()) {
                throw ArchiveException("数据库中未找到会话列表")
            }
            return OriginalSessionConverter.convert(reader)
        } finally {
            runCatching { db?.close() }
            runCatching { copy.delete() }
            runCatching { File("${copy.absolutePath}-wal").delete() }
            runCatching { File("${copy.absolutePath}-shm").delete() }
        }
    }
}
