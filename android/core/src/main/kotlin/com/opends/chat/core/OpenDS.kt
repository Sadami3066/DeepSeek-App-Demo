package com.opends.chat.core

import android.content.Context

object OpenDS {
    private var initialized = false

    lateinit var sessions: SessionStore
    lateinit var chat: ChatClient
    lateinit var settings: SettingsStore
    lateinit var archive: ArchiveService
    lateinit var voice: VoiceService
    lateinit var attachments: AttachmentStore

    fun init(context: Context) {
        if (initialized) return
        val app = context.applicationContext
        val graph = CoreGraph(app)
        sessions = graph.sessions
        chat = graph.chat
        settings = graph.settings
        archive = graph.archive
        voice = graph.voice
        attachments = graph.attachments
        initialized = true
    }
}
