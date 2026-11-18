package com.opends.chat.core

import android.content.Context
import com.opends.chat.core.archive.ArchiveServiceImpl
import com.opends.chat.core.attach.AttachmentStoreImpl
import com.opends.chat.core.chat.ChatClientImpl
import com.opends.chat.core.db.SessionStoreImpl
import com.opends.chat.core.net.WireClient
import com.opends.chat.core.settings.SettingsStoreImpl
import com.opends.chat.core.voice.VoiceServiceImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

internal class CoreGraph(context: Context) {

    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(600, TimeUnit.SECONDS)
        .writeTimeout(600, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val settingsStore = SettingsStoreImpl(appContext, scope)
    private val sessionStore = SessionStoreImpl(appContext, scope)
    private val attachmentStore = AttachmentStoreImpl(appContext)
    private val wireClient = WireClient(settingsStore, httpClient)
    private val chatClient = ChatClientImpl(settingsStore, sessionStore, attachmentStore, wireClient)
    private val archiveService = ArchiveServiceImpl(appContext, sessionStore, attachmentStore)
    private val voiceService = VoiceServiceImpl(settingsStore, wireClient)

    init {
        ErrorLog.install(appContext)
        runBlocking {
            sessionStore.recoverInterruptedMessages()
            sessionStore.pruneEmptySessions()
        }
        OpenDSApi.attach(wireClient, sessionStore, settingsStore) { settingsStore.baseUrl() }
        OpenDSApi.attachChat(chatClient)
        OpenDSApi.attachVoice(voiceService)
    }

    val sessions: SessionStore get() = sessionStore
    val chat: ChatClient get() = chatClient
    val settings: SettingsStore get() = settingsStore
    val archive: ArchiveService get() = archiveService
    val voice: VoiceService get() = voiceService
    val attachments: AttachmentStore get() = attachmentStore
}
