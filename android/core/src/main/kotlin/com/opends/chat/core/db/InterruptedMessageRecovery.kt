package com.opends.chat.core.db

internal object StatusRecoverySql {
    const val LIST_MESSAGE_TABLES =
        "SELECT name FROM sqlite_master WHERE type = 'table' AND name LIKE 'chat_session_messages_%'"

    fun markWipAssistantIncomplete(tableName: String): String {
        val quoted = DatabaseHelper.quote(tableName)
        return "UPDATE $quoted SET status = 'INCOMPLETE' WHERE role = 'ASSISTANT' AND status = 'WIP'"
    }
}

internal interface StatusRecoveryTarget {
    fun listMessageTables(): List<String>
    fun markWipAssistantIncomplete(tableName: String): Int
}

internal object InterruptedMessageRecovery {
    fun run(target: StatusRecoveryTarget): Int =
        target.listMessageTables().sumOf { table -> target.markWipAssistantIncomplete(table) }
}
