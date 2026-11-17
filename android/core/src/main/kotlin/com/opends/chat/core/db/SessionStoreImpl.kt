package com.opends.chat.core.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.opends.chat.core.ChatMessage
import com.opends.chat.core.Fragment
import com.opends.chat.core.MessageStatus
import com.opends.chat.core.Role
import com.opends.chat.core.SessionInfo
import com.opends.chat.core.SessionStore
import com.opends.chat.core.util.TimeFormats
import com.opends.chat.core.util.string
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Collections
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

internal data class StoredMessage(
    val id: Long,
    val parentId: Long?,
    val role: Role,
    val status: MessageStatus,
    val thinkingEnabled: Boolean,
    val searchEnabled: Boolean,
    val prefix: Boolean,
    val accumulatedTokenUsage: Int,
    val insertedAt: Long,
    val fragmentsJson: String,
)

internal data class ImportedMessage(
    val message: ChatMessage,
    val fragmentsJson: String,
)

internal data class ImportedSession(
    val id: String,
    val title: String?,
    val pinned: Boolean,
    val currentMessageId: Long?,
    val insertedAt: Long,
    val updatedAt: Long,
    val messages: List<ImportedMessage>,
)

internal class SessionStoreImpl(
    context: Context,
    private val scope: CoroutineScope,
) : SessionStore {

    private val helper = DatabaseHelper(context.applicationContext)
    private val attachmentsDir = File(context.applicationContext.filesDir, "attachments")
    private val cache = ConcurrentHashMap<String, MutableStateFlow<List<ChatMessage>>>()
    private val loadedSessions = Collections.newSetFromMap(ConcurrentHashMap<String, Boolean>())
    private val loadMutex = Mutex()
    private val sessionsRevision = MutableStateFlow(0L)

    override fun observeSessions(): Flow<List<SessionInfo>> = sessionsRevision
        .map { loadSessions() }
        .flowOn(Dispatchers.IO)
        .distinctUntilChanged()

    override suspend fun createSession(): SessionInfo {
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        withDatabase { db ->
            db.beginTransaction()
            try {
                db.execSQL(
                    "INSERT INTO ${DatabaseHelper.SESSION_LIST} " +
                        "(id, title, titleType, cache_version, cache_reset_at, inserted_at, updated_at, " +
                        "current_message_id, schema_version, pinned, model_type) " +
                        "VALUES (?, NULL, NULL, 0, 0, ?, ?, NULL, ?, 0, 'default')",
                    arrayOf<Any>(id, TimeFormats.epochSeconds(now), TimeFormats.epochSeconds(now), DatabaseHelper.SCHEMA_VERSION),
                )
                DatabaseHelper.createMessageTable(db, id)
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
        cache[id] = MutableStateFlow(emptyList())
        loadedSessions.add(id)
        sessionsRevision.update { it + 1 }
        return SessionInfo(
            id = id,
            title = null,
            pinned = false,
            currentMessageId = null,
            insertedAt = now,
            updatedAt = now,
        )
    }

    override suspend fun deleteSession(sessionId: String) {
        withDatabase { db ->
            db.beginTransaction()
            try {
                db.execSQL("DROP TABLE IF EXISTS ${DatabaseHelper.quote(DatabaseHelper.messageTable(sessionId))}")
                db.delete(DatabaseHelper.SESSION_LIST, "id = ?", arrayOf(sessionId))
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
        cache.remove(sessionId)
        loadedSessions.remove(sessionId)
        sessionsRevision.update { it + 1 }
    }

    override suspend fun renameSession(sessionId: String, title: String) {
        val value = title.trim().ifEmpty { null }
        withDatabase { db -> updateSessionColumns(db, sessionId, title = value, setTitle = true, touch = true) }
        sessionsRevision.update { it + 1 }
    }

    override suspend fun setPinned(sessionId: String, pinned: Boolean) {
        withDatabase { db ->
            val values = ContentValues()
            values.put("pinned", if (pinned) 1 else 0)
            db.update(DatabaseHelper.SESSION_LIST, values, "id = ?", arrayOf(sessionId))
        }
        sessionsRevision.update { it + 1 }
    }

    override suspend fun loadMessages(sessionId: String): List<ChatMessage> {
        val messages = readMessages(sessionId)
        publish(sessionId, messages)
        loadedSessions.add(sessionId)
        return messages
    }

    override fun observeMessages(sessionId: String): Flow<List<ChatMessage>> = flow {
        ensureLoaded(sessionId)
        emitAll(cacheFor(sessionId))
    }.flowOn(Dispatchers.IO)

    override suspend fun setCurrentMessage(sessionId: String, messageId: Long) {
        withDatabase { db -> updateSessionColumns(db, sessionId, currentMessageId = messageId, setCurrent = true, touch = true) }
        sessionsRevision.update { it + 1 }
    }

    override suspend fun deleteBranch(sessionId: String, messageId: Long) {
        withDatabase { db ->
            val parent = messageParent(db, sessionId, messageId)
            val doomed = descendantsOf(db, sessionId, messageId)
            db.beginTransaction()
            try {
                doomed.chunked(400).forEach { chunk ->
                    db.delete(
                        DatabaseHelper.quote(DatabaseHelper.messageTable(sessionId)),
                        "message_id IN (${chunk.joinToString(",") { "?" }})",
                        chunk.map { it.toString() }.toTypedArray(),
                    )
                }
                val cursor = db.rawQuery(
                    "SELECT current_message_id FROM ${DatabaseHelper.SESSION_LIST} WHERE id = ?",
                    arrayOf(sessionId),
                )
                val current = if (cursor.moveToFirst() && !cursor.isNull(0)) cursor.getLong(0) else null
                cursor.close()
                if (current != null && doomed.contains(current)) {
                    val values = ContentValues()
                    if (parent == null) values.putNull("current_message_id") else values.put("current_message_id", parent)
                    values.put("updated_at", TimeFormats.nowEpochSeconds())
                    db.update(DatabaseHelper.SESSION_LIST, values, "id = ?", arrayOf(sessionId))
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
        publish(sessionId, readMessages(sessionId))
        sessionsRevision.update { it + 1 }
    }

    suspend fun getSession(sessionId: String): SessionInfo? = withDatabase { db ->
        sessionById(db, sessionId)
    }

    suspend fun loadSessionsNow(): List<SessionInfo> = withContext(Dispatchers.IO) { loadSessions() }

    suspend fun countSessionFiles(sessionId: String): Int = withDatabase { db ->
        val tableName = DatabaseHelper.messageTable(sessionId)
        val exists = db.rawQuery(
            "SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = ?",
            arrayOf(tableName),
        ).use { it.moveToFirst() }
        if (!exists) return@withDatabase 0
        val fileIds = HashSet<String>()
        db.rawQuery("SELECT fragments FROM ${DatabaseHelper.quote(tableName)}", null).use { cursor ->
            while (cursor.moveToNext()) {
                FragmentCodec.parse(cursor.getString(0)).forEach { item ->
                    if (item.string("type") != FragmentCodec.TYPE_FILE) return@forEach
                    val files = item.get("files")?.takeIf { it.isJsonArray }?.asJsonArray ?: return@forEach
                    files.forEach { entry ->
                        if (entry.isJsonObject) {
                            FragmentCodec.fileOf(entry.asJsonObject)?.let { fileIds.add(it.fileId) }
                        }
                    }
                }
            }
        }
        fileIds.size
    }

    suspend fun recoverInterruptedMessages(): Int {
        val recovered = withDatabase { db ->
            InterruptedMessageRecovery.run(AndroidStatusRecoveryTarget(db))
        }
        if (recovered > 0) {
            cache.clear()
            loadedSessions.clear()
            sessionsRevision.update { it + 1 }
        }
        return recovered
    }

    private class AndroidStatusRecoveryTarget(
        private val db: SQLiteDatabase,
    ) : StatusRecoveryTarget {
        override fun listMessageTables(): List<String> {
            val tables = ArrayList<String>()
            val cursor = db.rawQuery(StatusRecoverySql.LIST_MESSAGE_TABLES, null)
            cursor.use {
                while (it.moveToNext()) {
                    val name = it.getString(0)
                    if (!name.isNullOrEmpty()) tables.add(name)
                }
            }
            return tables
        }

        override fun markWipAssistantIncomplete(tableName: String): Int {
            val values = ContentValues()
            values.put("status", MessageStatus.INCOMPLETE.name)
            return db.update(
                DatabaseHelper.quote(tableName),
                values,
                "role = 'ASSISTANT' AND status = 'WIP'",
                null,
            )
        }
    }

    suspend fun nextMessageId(sessionId: String): Long = withDatabase { db ->
        val cursor = db.rawQuery(
            "SELECT COALESCE(MAX(message_id), 0) + 1 FROM ${DatabaseHelper.quote(DatabaseHelper.messageTable(sessionId))}",
            null,
        )
        cursor.use { if (it.moveToFirst()) it.getLong(0) else 1L }
    }

    suspend fun loadStoredMessages(sessionId: String): List<StoredMessage> = withDatabase { db ->
        queryStoredMessages(db, sessionId)
    }

    suspend fun insertMessage(sessionId: String, message: ChatMessage, fragmentsJson: String) {
        withDatabase { db -> writeMessage(db, sessionId, message, fragmentsJson) }
    }

    suspend fun updateMessage(sessionId: String, message: ChatMessage, fragmentsJson: String) {
        withDatabase { db -> writeMessage(db, sessionId, message, fragmentsJson) }
    }

    suspend fun touchSession(sessionId: String) {
        withDatabase { db -> updateSessionColumns(db, sessionId, touch = true) }
        sessionsRevision.update { it + 1 }
    }

    suspend fun setSessionTitle(sessionId: String, title: String?) {
        withDatabase { db -> updateSessionColumns(db, sessionId, title = title, setTitle = true, touch = true) }
        sessionsRevision.update { it + 1 }
    }

    suspend fun sessionExists(sessionId: String): Boolean = getSession(sessionId) != null

    fun publish(sessionId: String, messages: List<ChatMessage>) {
        cacheFor(sessionId).value = messages
        loadedSessions.add(sessionId)
    }

    fun cachedMessages(sessionId: String): List<ChatMessage>? = cache[sessionId]?.value

    suspend fun cachedOrLoad(sessionId: String): List<ChatMessage> {
        val cached = cache[sessionId]
        if (loadedSessions.contains(sessionId) && cached != null) return cached.value
        return loadMessages(sessionId)
    }

    suspend fun putUpload(attachmentId: String, serverId: String, purpose: String?) {
        withDatabase { db ->
            val values = ContentValues()
            values.put("attachment_id", attachmentId)
            values.put("server_id", serverId)
            values.put("purpose", purpose)
            values.put("updated_at", TimeFormats.nowEpochSeconds())
            db.insertWithOnConflict(
                DatabaseHelper.UPLOADS, null, values, SQLiteDatabase.CONFLICT_REPLACE,
            )
        }
    }

    suspend fun uploadIds(attachmentIds: Collection<String>): Map<String, String> {
        if (attachmentIds.isEmpty()) return emptyMap()
        return withDatabase { db ->
            val output = HashMap<String, String>(attachmentIds.size)
            attachmentIds.chunked(400).forEach { chunk ->
                val cursor = db.rawQuery(
                    "SELECT attachment_id, server_id FROM ${DatabaseHelper.UPLOADS} " +
                        "WHERE attachment_id IN (${chunk.joinToString(",") { "?" }})",
                    chunk.toTypedArray(),
                )
                cursor.use {
                    while (it.moveToNext()) {
                        val server = it.getString(1)
                        if (!server.isNullOrEmpty()) output[it.getString(0)] = server
                    }
                }
            }
            output
        }
    }

    suspend fun allUploads(): Map<String, String> = withDatabase { db ->
        val output = HashMap<String, String>()
        val cursor = db.rawQuery(
            "SELECT attachment_id, server_id FROM ${DatabaseHelper.UPLOADS} WHERE server_id IS NOT NULL AND server_id != ''",
            null,
        )
        cursor.use {
            while (it.moveToNext()) output[it.getString(0)] = it.getString(1)
        }
        output
    }

    suspend fun replaceAll(sessions: List<ImportedSession>) {
        withDatabase { db ->
            db.beginTransaction()
            try {
                val cursor = db.rawQuery(
                    "SELECT name FROM sqlite_master WHERE type = 'table' AND name LIKE 'chat_session_messages_%'",
                    null,
                )
                val tables = ArrayList<String>()
                cursor.use {
                    while (it.moveToNext()) tables.add(it.getString(0))
                }
                tables.forEach { name ->
                    db.execSQL("DROP TABLE IF EXISTS ${DatabaseHelper.quote(name)}")
                }
                db.delete(DatabaseHelper.SESSION_LIST, null, null)
                db.delete(DatabaseHelper.UPLOADS, null, null)
                sessions.forEach { session ->
                    val values = ContentValues()
                    values.put("id", session.id)
                    if (session.title == null) values.putNull("title") else values.put("title", session.title)
                    values.put("titleType", "SYSTEM")
                    values.put("cache_version", 0)
                    values.put("cache_reset_at", 0)
                    values.put("inserted_at", TimeFormats.epochSeconds(session.insertedAt))
                    values.put("updated_at", TimeFormats.epochSeconds(session.updatedAt))
                    if (session.currentMessageId == null) {
                        values.putNull("current_message_id")
                    } else {
                        values.put("current_message_id", session.currentMessageId)
                    }
                    values.put("schema_version", DatabaseHelper.SCHEMA_VERSION)
                    values.put("pinned", if (session.pinned) 1 else 0)
                    values.put("model_type", "default")
                    db.insertWithOnConflict(
                        DatabaseHelper.SESSION_LIST, null, values, SQLiteDatabase.CONFLICT_REPLACE,
                    )
                    DatabaseHelper.createMessageTable(db, session.id)
                    session.messages.forEach { imported ->
                        writeMessage(db, session.id, imported.message, imported.fragmentsJson)
                    }
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
        cache.clear()
        loadedSessions.clear()
        sessionsRevision.update { it + 1 }
    }

    suspend fun clearAll() = replaceAll(emptyList())

    fun attachmentsDirectory(): File = attachmentsDir

    private suspend fun ensureLoaded(sessionId: String) {
        if (loadedSessions.contains(sessionId)) return
        loadMutex.withLock {
            if (loadedSessions.contains(sessionId)) return
            cacheFor(sessionId).value = readMessages(sessionId)
            loadedSessions.add(sessionId)
        }
    }

    private fun cacheFor(sessionId: String): MutableStateFlow<List<ChatMessage>> =
        cache.getOrPut(sessionId) { MutableStateFlow(emptyList()) }

    private suspend fun readMessages(sessionId: String): List<ChatMessage> = withDatabase { db ->
        queryMessages(db, sessionId)
    }

    private suspend fun <T> withDatabase(block: (SQLiteDatabase) -> T): T =
        withContext(Dispatchers.IO) { block(helper.writableDatabase) }

    private fun loadSessions(): List<SessionInfo> {
        val db = helper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT id, title, pinned, current_message_id, inserted_at, updated_at " +
                "FROM ${DatabaseHelper.SESSION_LIST} ORDER BY pinned DESC, updated_at DESC",
            null,
        )
        val sessions = cursor.use {
            val output = ArrayList<SessionInfo>()
            while (it.moveToNext()) {
                output.add(
                    SessionInfo(
                        id = it.getString(0),
                        title = if (it.isNull(1)) null else it.getString(1),
                        pinned = it.getInt(2) != 0,
                        currentMessageId = if (it.isNull(3)) null else it.getLong(3),
                        insertedAt = TimeFormats.millisFromEpochSeconds(it.getDouble(4)) ?: 0L,
                        updatedAt = TimeFormats.millisFromEpochSeconds(it.getDouble(5)) ?: 0L,
                    )
                )
            }
            output
        }
        return sessions.filter { session -> hasMessages(db, session.id) }
    }

    private fun hasMessages(db: SQLiteDatabase, sessionId: String): Boolean {
        val table = DatabaseHelper.messageTable(sessionId)
        val exists = db.rawQuery(
            "SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = ?",
            arrayOf(table),
        ).use { it.moveToFirst() }
        if (!exists) return false
        return db.rawQuery(
            "SELECT 1 FROM ${DatabaseHelper.quote(table)} LIMIT 1",
            null,
        ).use { it.moveToFirst() }
    }

    suspend fun pruneEmptySessions(): Int {
        val removed = withDatabase { db ->
            val cursor = db.rawQuery("SELECT id FROM ${DatabaseHelper.SESSION_LIST}", null)
            val empty = ArrayList<String>()
            cursor.use {
                while (it.moveToNext()) {
                    val id = it.getString(0)
                    if (!id.isNullOrEmpty() && !hasMessages(db, id)) empty.add(id)
                }
            }
            if (empty.isEmpty()) return@withDatabase 0
            db.beginTransaction()
            try {
                empty.forEach { id ->
                    db.execSQL("DROP TABLE IF EXISTS ${DatabaseHelper.quote(DatabaseHelper.messageTable(id))}")
                    db.delete(DatabaseHelper.SESSION_LIST, "id = ?", arrayOf(id))
                    }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
            empty.size
        }
        if (removed > 0) {
            sessionsRevision.update { it + 1 }
        }
        return removed
    }

    private fun sessionById(db: SQLiteDatabase, sessionId: String): SessionInfo? {
        val cursor = db.rawQuery(
            "SELECT id, title, pinned, current_message_id, inserted_at, updated_at " +
                "FROM ${DatabaseHelper.SESSION_LIST} WHERE id = ?",
            arrayOf(sessionId),
        )
        return cursor.use {
            if (!it.moveToFirst()) return null
            SessionInfo(
                id = it.getString(0),
                title = if (it.isNull(1)) null else it.getString(1),
                pinned = it.getInt(2) != 0,
                currentMessageId = if (it.isNull(3)) null else it.getLong(3),
                insertedAt = TimeFormats.millisFromEpochSeconds(it.getDouble(4)) ?: 0L,
                updatedAt = TimeFormats.millisFromEpochSeconds(it.getDouble(5)) ?: 0L,
            )
        }
    }

    private fun updateSessionColumns(
        db: SQLiteDatabase,
        sessionId: String,
        title: String? = null,
        setTitle: Boolean = false,
        currentMessageId: Long? = null,
        setCurrent: Boolean = false,
        touch: Boolean = false,
    ) {
        val values = ContentValues()
        if (setTitle) {
            if (title == null) values.putNull("title") else values.put("title", title)
        }
        if (setCurrent) {
            if (currentMessageId == null) values.putNull("current_message_id") else values.put("current_message_id", currentMessageId)
        }
        if (touch) values.put("updated_at", TimeFormats.nowEpochSeconds())
        if (values.size() > 0) {
            db.update(DatabaseHelper.SESSION_LIST, values, "id = ?", arrayOf(sessionId))
        }
    }

    private fun writeMessage(db: SQLiteDatabase, sessionId: String, message: ChatMessage, fragmentsJson: String) {
        val values = ContentValues()
        values.put("message_id", message.id)
        if (message.parentId == null) values.putNull("parent_id") else values.put("parent_id", message.parentId)
        values.put("role", message.role.name)
        values.put("thinking_enabled", if (message.thinkingEnabled) 1 else 0)
        values.put("status", message.status.name)
        values.put("inserted_at", TimeFormats.epochSeconds(message.insertedAt))
        values.put("accumulated_token_usage", message.accumulatedTokenUsage)
        values.put("fragments", fragmentsJson)
        values.put("conversation_mode", message.conversationMode ?: "DEFAULT")
        values.put("prefix", if (message.prefix) 1 else 0)
        values.put("search_enabled", if (message.searchEnabled) 1 else 0)
        db.insertWithOnConflict(
            DatabaseHelper.quote(DatabaseHelper.messageTable(sessionId)),
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE,
        )
    }

    private fun queryMessages(db: SQLiteDatabase, sessionId: String): List<ChatMessage> {
        val cursor = db.rawQuery(
            "SELECT message_id, parent_id, role, thinking_enabled, status, inserted_at, " +
                "accumulated_token_usage, fragments, prefix, search_enabled, conversation_mode " +
                "FROM ${DatabaseHelper.quote(DatabaseHelper.messageTable(sessionId))} ORDER BY message_id",
            null,
        )
        return cursor.use {
            val output = ArrayList<ChatMessage>()
            while (it.moveToNext()) {
                val role = roleOf(it.getString(2))
                output.add(
                    ChatMessage(
                        id = it.getLong(0),
                        parentId = if (it.isNull(1)) null else it.getLong(1),
                        role = role,
                        status = statusOf(it.getString(4)),
                        thinkingEnabled = it.getInt(3) != 0,
                        searchEnabled = it.getInt(9) != 0,
                        accumulatedTokenUsage = it.getInt(6),
                        insertedAt = TimeFormats.millisFromEpochSeconds(it.getDouble(5)) ?: 0L,
                        prefix = it.getInt(8) != 0,
                        fragments = decorate(com.opends.chat.core.chat.WireCodec.modelFragments(FragmentCodec.parse(it.getString(7)), role)),
                        conversationMode = if (it.isNull(10)) null else it.getString(10),
                    )
                )
            }
            output
        }
    }

    private fun queryStoredMessages(db: SQLiteDatabase, sessionId: String): List<StoredMessage> {
        val cursor = db.rawQuery(
            "SELECT message_id, parent_id, role, thinking_enabled, status, inserted_at, " +
                "accumulated_token_usage, fragments, prefix, search_enabled " +
                "FROM ${DatabaseHelper.quote(DatabaseHelper.messageTable(sessionId))} ORDER BY message_id",
            null,
        )
        return cursor.use {
            val output = ArrayList<StoredMessage>()
            while (it.moveToNext()) {
                output.add(
                    StoredMessage(
                        id = it.getLong(0),
                        parentId = if (it.isNull(1)) null else it.getLong(1),
                        role = roleOf(it.getString(2)),
                        status = statusOf(it.getString(4)),
                        thinkingEnabled = it.getInt(3) != 0,
                        searchEnabled = it.getInt(9) != 0,
                        prefix = it.getInt(8) != 0,
                        accumulatedTokenUsage = it.getInt(6),
                        insertedAt = TimeFormats.millisFromEpochSeconds(it.getDouble(5)) ?: 0L,
                        fragmentsJson = it.getString(7) ?: "[]",
                    )
                )
            }
            output
        }
    }

    private fun messageParent(db: SQLiteDatabase, sessionId: String, messageId: Long): Long? {
        val cursor = db.rawQuery(
            "SELECT parent_id FROM ${DatabaseHelper.quote(DatabaseHelper.messageTable(sessionId))} WHERE message_id = ?",
            arrayOf(messageId.toString()),
        )
        return cursor.use {
            if (!it.moveToFirst() || it.isNull(0)) null else it.getLong(0)
        }
    }

    private fun descendantsOf(db: SQLiteDatabase, sessionId: String, messageId: Long): Set<Long> {
        val table = DatabaseHelper.quote(DatabaseHelper.messageTable(sessionId))
        val children = HashMap<Long?, MutableList<Long>>()
        val cursor = db.rawQuery("SELECT message_id, parent_id FROM $table", null)
        cursor.use {
            while (it.moveToNext()) {
                val id = it.getLong(0)
                val parent = if (it.isNull(1)) null else it.getLong(1)
                children.getOrPut(parent) { ArrayList() }.add(id)
            }
        }
        val doomed = LinkedHashSet<Long>()
        val stack = ArrayDeque<Long>()
        stack.add(messageId)
        while (stack.isNotEmpty()) {
            val current = stack.removeLast()
            if (!doomed.add(current)) continue
            children[current]?.forEach { stack.add(it) }
        }
        return doomed
    }

    private fun roleOf(value: String?): Role = runCatching { Role.valueOf(value ?: "") }.getOrDefault(Role.USER)

    private fun statusOf(value: String?): MessageStatus =
        runCatching { MessageStatus.valueOf(value ?: "") }.getOrDefault(MessageStatus.FINISHED)

    private fun decorate(fragments: List<Fragment>): List<Fragment> = fragments.map { fragment ->
        when (fragment) {
            is Fragment.File -> {
                val local = File(attachmentsDir, fragment.fileId)
                fragment.copy(localPath = if (local.exists()) local.absolutePath else null)
            }
            else -> fragment
        }
    }
}
