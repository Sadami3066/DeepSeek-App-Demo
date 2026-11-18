package com.opends.chat.core

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface SessionStore {
    fun observeSessions(): Flow<List<SessionInfo>>
    suspend fun createSession(): SessionInfo
    suspend fun deleteSession(sessionId: String)
    suspend fun renameSession(sessionId: String, title: String)
    suspend fun setPinned(sessionId: String, pinned: Boolean)
    suspend fun loadMessages(sessionId: String): List<ChatMessage>
    fun observeMessages(sessionId: String): Flow<List<ChatMessage>>
    suspend fun setCurrentMessage(sessionId: String, messageId: Long)
    suspend fun deleteBranch(sessionId: String, messageId: Long)
}

interface ChatClient {
    val generation: StateFlow<GenerationState>
    suspend fun send(sessionId: String, input: SendInput): Result<Unit>
    suspend fun regenerate(sessionId: String, userMessageId: Long): Result<Unit>
    suspend fun continueGeneration(sessionId: String): Result<Unit>
    suspend fun editUserMessage(sessionId: String, messageId: Long, newText: String): Result<Unit>
    fun stop()
}

interface SettingsStore {
    val state: StateFlow<AppSettings>
    val hasApiKey: StateFlow<Boolean>
    suspend fun setApiKey(key: String)
    suspend fun update(settings: AppSettings)
}

interface ArchiveService {
    suspend fun importZip(bytes: ByteArray): ImportResult
    suspend fun exportZip(): ByteArray
    suspend fun importZipFile(file: java.io.File, displayName: String): ImportResult
    suspend fun exportZipTo(out: java.io.OutputStream): Int
    suspend fun importFromOriginalApp(readSource: OriginalAppSource): ImportResult
}

sealed interface OriginalAppSource {
    data class DatabaseBytes(val fileName: String, val bytes: ByteArray) : OriginalAppSource
}

interface VoiceService {
    val isSpeaking: StateFlow<Boolean>
    suspend fun transcribe(wav: ByteArray): String
    suspend fun speak(text: String, voiceId: String? = null)
    fun stopSpeaking()
}

interface AttachmentStore {
    suspend fun import(attachment: AttachmentRef): AttachmentRef
    fun findLocal(attachment: AttachmentRef): java.io.File?
}
