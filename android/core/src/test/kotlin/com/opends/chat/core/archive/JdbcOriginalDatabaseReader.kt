package com.opends.chat.core.archive

import java.io.File
import java.nio.file.Files
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet

internal class JdbcOriginalDatabaseReader private constructor(
    private val connection: Connection,
) : OriginalDatabaseReader, AutoCloseable {

    override fun hasSessionList(): Boolean = connection.createStatement().use { statement ->
        statement.executeQuery(
            "SELECT name FROM sqlite_master WHERE type = 'table' AND name = 'chat_session_list'"
        ).use { result -> result.next() }
    }

    override fun readSessions(): List<RawOriginalSession> {
        val sessions = ArrayList<RawOriginalSession>()
        connection.createStatement().use { statement ->
            statement.executeQuery(OriginalSql.SESSION_LIST).use { result ->
                while (result.next()) {
                    val sessionId = result.getString(1) ?: continue
                    sessions.add(
                        RawOriginalSession(
                            id = sessionId,
                            title = result.getString(2),
                            insertedAt = result.doubleOrNull(3),
                            updatedAt = result.doubleOrNull(4),
                            currentMessageId = result.longOrNull(5),
                            pinned = (result.longOrNull(6) ?: 0L) != 0L,
                            messages = readMessages(sessionId),
                        )
                    )
                }
            }
        }
        return sessions
    }

    private fun readMessages(sessionId: String): List<RawOriginalMessage> {
        val messages = ArrayList<RawOriginalMessage>()
        connection.createStatement().use { statement ->
            statement.executeQuery(OriginalSql.messageQuery(sessionId)).use { result ->
                while (result.next()) {
                    messages.add(
                        RawOriginalMessage(
                            id = result.getLong(1),
                            parentId = result.longOrNull(2),
                            role = result.getString(3),
                            thinkingEnabled = (result.longOrNull(4) ?: 0L) != 0L,
                            status = result.getString(5),
                            insertedAt = result.doubleOrNull(6),
                            fragmentsJson = result.getString(7),
                            conversationMode = result.getString(8),
                        )
                    )
                }
            }
        }
        return messages
    }

    override fun close() {
        runCatching { connection.close() }
    }

    companion object {
        fun open(databaseFile: File): JdbcOriginalDatabaseReader {
            Class.forName("org.sqlite.JDBC")
            val workDir = Files.createTempDirectory("opends-jdbc-db").toFile()
            val target = File(workDir, databaseFile.name)
            listOf("", "-wal", "-shm").forEach { suffix ->
                val source = File("${databaseFile.absolutePath}$suffix")
                if (source.exists()) {
                    source.copyTo(File("${target.absolutePath}$suffix"), overwrite = true)
                }
            }
            val connection = DriverManager.getConnection("jdbc:sqlite:${target.absolutePath}")
            connection.createStatement().use { statement ->
                statement.executeQuery("PRAGMA journal_mode").use { result ->
                    if (result.next()) result.getString(1)
                }
            }
            return JdbcOriginalDatabaseReader(connection)
        }

        private fun ResultSet.longOrNull(index: Int): Long? {
            val value = getObject(index) ?: return null
            return when (value) {
                is Number -> value.toLong()
                is String -> value.toLongOrNull()
                else -> null
            }
        }

        private fun ResultSet.doubleOrNull(index: Int): Double? {
            val value = getObject(index) ?: return null
            return when (value) {
                is Number -> value.toDouble()
                is String -> value.toDoubleOrNull()
                else -> null
            }
        }
    }
}
