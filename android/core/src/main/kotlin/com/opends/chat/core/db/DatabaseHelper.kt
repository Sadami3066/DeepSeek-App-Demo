package com.opends.chat.core.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

internal class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS $SESSION_LIST (" +
                "id TEXT PRIMARY KEY, title TEXT, titleType TEXT, cache_version INTEGER, " +
                "cache_reset_at INTEGER, inserted_at REAL NOT NULL DEFAULT 0, " +
                "updated_at REAL NOT NULL DEFAULT 0, current_message_id INTEGER, " +
                "schema_version INTEGER, pinned INTEGER NOT NULL DEFAULT 0, model_type TEXT)"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS $UPLOADS (" +
                "attachment_id TEXT PRIMARY KEY, server_id TEXT, purpose TEXT, updated_at REAL NOT NULL DEFAULT 0)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 7) db.execSQL("DROP TABLE IF EXISTS session_quotas")
    }

    companion object {
        const val DATABASE_NAME = "opends_chat.db"
        const val SESSION_LIST = "chat_session_list"
        const val UPLOADS = "attachment_uploads"
        const val DATABASE_VERSION = 7
        const val SCHEMA_VERSION = 5

        fun quote(name: String): String = "\"" + name.replace("\"", "\"\"") + "\""

        fun messageTable(sessionId: String): String = "chat_session_messages_$sessionId"

        fun createMessageTable(db: SQLiteDatabase, sessionId: String) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS ${quote(messageTable(sessionId))} (" +
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
}
