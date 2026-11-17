package com.opends.chat.core.db

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import java.nio.file.Files
import java.sql.Connection
import java.sql.DriverManager

class InterruptedMessageRecoveryTest {

    private val connection: Connection = createDatabase()

    @After
    fun tearDown() {
        runCatching { connection.close() }
    }

    @Test
    fun recoversWipAssistantMessagesOnly() {
        createMessageTable("chat_session_messages_s1")
        createMessageTable("chat_session_messages_s2")
        createMessageTable("app_meta")

        insertMessage("chat_session_messages_s1", 1, "ASSISTANT", "WIP", "partial thinking")
        insertMessage("chat_session_messages_s1", 2, "ASSISTANT", "FINISHED", "done answer")
        insertMessage("chat_session_messages_s1", 3, "USER", "WIP", "unexpected user wip")
        insertMessage("chat_session_messages_s2", 1, "ASSISTANT", "WIP", "second session partial")
        insertMessage("app_meta", 1, "ASSISTANT", "WIP", "unrelated table")

        val recovered = InterruptedMessageRecovery.run(target())

        assertEquals(2, recovered)
        assertEquals("INCOMPLETE", statusOf("chat_session_messages_s1", 1))
        assertEquals("FINISHED", statusOf("chat_session_messages_s1", 2))
        assertEquals("WIP", statusOf("chat_session_messages_s1", 3))
        assertEquals("INCOMPLETE", statusOf("chat_session_messages_s2", 1))
        assertEquals("WIP", statusOf("app_meta", 1))
        assertEquals("partial thinking", fragmentsOf("chat_session_messages_s1", 1))
        assertEquals("second session partial", fragmentsOf("chat_session_messages_s2", 1))
    }

    @Test
    fun recoveryIsIdempotent() {
        createMessageTable("chat_session_messages_only")
        insertMessage("chat_session_messages_only", 7, "ASSISTANT", "WIP", "half")

        assertEquals(1, InterruptedMessageRecovery.run(target()))
        assertEquals(0, InterruptedMessageRecovery.run(target()))
        assertEquals("INCOMPLETE", statusOf("chat_session_messages_only", 7))
    }

    private fun target(): StatusRecoveryTarget = JdbcStatusRecoveryTarget(connection)

    private fun createDatabase(): Connection {
        Class.forName("org.sqlite.JDBC")
        val file = Files.createTempFile("opends-recovery", ".db").toFile()
        file.deleteOnExit()
        return DriverManager.getConnection("jdbc:sqlite:${file.absolutePath}")
    }

    private fun createMessageTable(table: String) {
        connection.createStatement().use { statement ->
            statement.executeUpdate(
                "CREATE TABLE \"$table\" (" +
                    "message_id INTEGER PRIMARY KEY, parent_id INTEGER, role TEXT, " +
                    "thinking_enabled INTEGER NOT NULL DEFAULT 0, status TEXT, " +
                    "inserted_at REAL NOT NULL DEFAULT 0, feedback_type TEXT, " +
                    "accumulated_token_usage INTEGER NOT NULL DEFAULT 0, " +
                    "ban_edit INTEGER NOT NULL DEFAULT 0, ban_regenerate INTEGER NOT NULL DEFAULT 0, " +
                    "tips TEXT, fragments TEXT, conversation_mode TEXT, extra_search_providers TEXT, " +
                    "prefix INTEGER NOT NULL DEFAULT 0, search_enabled INTEGER NOT NULL DEFAULT 0)"
            )
        }
    }

    private fun insertMessage(table: String, id: Long, role: String, status: String, fragments: String) {
        connection.prepareStatement(
            "INSERT INTO \"$table\" (message_id, parent_id, role, status, inserted_at, fragments) " +
                "VALUES (?, NULL, ?, ?, 0, ?)"
        ).use { statement ->
            statement.setLong(1, id)
            statement.setString(2, role)
            statement.setString(3, status)
            statement.setString(4, fragments)
            statement.executeUpdate()
        }
    }

    private fun statusOf(table: String, id: Long): String {
        connection.prepareStatement("SELECT status FROM \"$table\" WHERE message_id = ?").use { statement ->
            statement.setLong(1, id)
            statement.executeQuery().use { result ->
                result.next()
                return result.getString(1)
            }
        }
    }

    private fun fragmentsOf(table: String, id: Long): String {
        connection.prepareStatement("SELECT fragments FROM \"$table\" WHERE message_id = ?").use { statement ->
            statement.setLong(1, id)
            statement.executeQuery().use { result ->
                result.next()
                return result.getString(1)
            }
        }
    }

    internal class JdbcStatusRecoveryTarget(
        private val connection: Connection,
    ) : StatusRecoveryTarget {
        override fun listMessageTables(): List<String> {
            val tables = ArrayList<String>()
            connection.createStatement().use { statement ->
                statement.executeQuery(StatusRecoverySql.LIST_MESSAGE_TABLES).use { result ->
                    while (result.next()) {
                        val name = result.getString(1)
                        if (!name.isNullOrEmpty()) tables.add(name)
                    }
                }
            }
            return tables
        }

        override fun markWipAssistantIncomplete(tableName: String): Int {
            connection.createStatement().use { statement ->
                return statement.executeUpdate(StatusRecoverySql.markWipAssistantIncomplete(tableName))
            }
        }
    }
}
