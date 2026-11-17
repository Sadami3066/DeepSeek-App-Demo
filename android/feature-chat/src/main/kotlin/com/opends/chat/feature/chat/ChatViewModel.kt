package com.opends.chat.feature.chat

import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.opends.chat.core.AppSettings
import com.opends.chat.core.AttachmentRef
import com.opends.chat.core.ChatMessage
import com.opends.chat.core.GenerationState
import com.opends.chat.core.OpenDS
import com.opends.chat.core.Role
import com.opends.chat.core.SendInput
import com.opends.chat.core.SessionInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ChatViewModel(application: Application) : AndroidViewModel(application) {

    internal data class Draft(
        val text: String = "",
        val attachments: List<DraftAttachment> = emptyList(),
    )

    private data class SessionData(
        val session: SessionInfo? = null,
        val messages: List<ChatMessage> = emptyList(),
    )

    private data class BaseState(
        val sessionId: String? = null,
        val data: SessionData = SessionData(),
        val generation: GenerationState = GenerationState.Idle,
        val settings: AppSettings = AppSettings(),
        val hasApiKey: Boolean = false,
    )

    private data class EphemeralState(
        val toast: ToastState? = null,
        val reading: ReadingState = ReadingState(),
        val searchPanel: SearchPanelState? = null,
        val autoRead: Boolean = false,
    )

    private data class VoiceState(
        val recording: Boolean = false,
        val recordingSeconds: Int = 0,
        val transcribing: Boolean = false,
        val amplitudes: List<Float> = emptyList(),
    )

    private val _sessionId = MutableStateFlow<String?>(null)
    private val _loaded = MutableStateFlow(true)
    private val _drafts = MutableStateFlow<Map<String?, Draft>>(emptyMap())
    private val _toast = MutableStateFlow<ToastState?>(null)
    private val _reading = MutableStateFlow(ReadingState())
    private val _searchPanel = MutableStateFlow<SearchPanelState?>(null)
    private val _autoRead = MutableStateFlow(false)
    private val _recording = MutableStateFlow(false)
    private val _recordingSeconds = MutableStateFlow(0)
    private val _transcribing = MutableStateFlow(false)
    private val _amplitudes = MutableStateFlow<List<Float>>(emptyList())

    private var recorder: VoiceRecorder? = null
    private var recordingTimer: Job? = null
    private var amplitudeJob: Job? = null
    private var transcribeJob: Job? = null
    private var onSessionCreated: ((String) -> Unit)? = null
    private var lastAutoReadId: Long? = null

    private val sessionData: Flow<SessionData> = _sessionId.flatMapLatest { sessionId ->
        if (sessionId == null) {
            _loaded.value = true
            flowOf(SessionData())
        } else {
            combine(
                OpenDS.sessions.observeSessions(),
                OpenDS.sessions.observeMessages(sessionId).onStart { _loaded.value = false }.onEach { _loaded.value = true },
            ) { sessions, messages ->
                SessionData(sessions.firstOrNull { it.id == sessionId }, messages)
            }
        }
    }

    private val baseFlow: Flow<BaseState> = combine(
        sessionData,
        OpenDS.chat.generation,
        OpenDS.settings.state,
        OpenDS.settings.hasApiKey,
    ) { data, generation, settings, hasApiKey ->
        BaseState(_sessionId.value, data, generation, settings, hasApiKey)
    }

    private val draftFlow: Flow<Draft> = combine(_drafts, _sessionId) { drafts, sessionId ->
        drafts[sessionId] ?: Draft()
    }

    private val ephemeralFlow: Flow<EphemeralState> = combine(
        _toast,
        _reading,
        _searchPanel,
        _autoRead,
    ) { toast, reading, searchPanel, autoRead ->
        EphemeralState(toast, reading, searchPanel, autoRead)
    }

    private val voiceFlow: Flow<VoiceState> = combine(
        _recording,
        _recordingSeconds,
        _transcribing,
        _amplitudes,
    ) { recording, seconds, transcribing, amplitudes ->
        VoiceState(recording, seconds, transcribing, amplitudes)
    }

    val uiState: StateFlow<ChatUiState> = combine(
        baseFlow,
        ephemeralFlow,
        _loaded,
    ) { base, ephemeral, loaded ->
        val messages = visibleMessages(base.data.messages, base.data.session?.currentMessageId)
        ChatUiState(
            sessionId = base.sessionId,
            title = base.data.session?.title?.takeIf { it.isNotBlank() } ?: "新对话",
            messages = messages,
            allMessages = base.data.messages,
            versions = messageVersions(base.data.messages, messages),
            generation = base.generation,
            loaded = loaded || base.data.session != null,
            hasApiKey = base.hasApiKey,
            thinkingEnabled = base.settings.thinkingDefault,
            searchEnabled = base.settings.searchDefault,
            toast = ephemeral.toast,
            reading = ephemeral.reading,
            searchPanel = ephemeral.searchPanel,
            autoRead = ephemeral.autoRead,
            thinkingAutoFold = base.settings.thinkingAutoFold,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ChatUiState())

    val draftState: StateFlow<ChatDraftState> = draftFlow
        .map { draft -> ChatDraftState(draft.text, draft.attachments) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, ChatDraftState())

    val voiceState: StateFlow<ChatVoiceState> = voiceFlow
        .map { voice ->
            ChatVoiceState(
                recording = voice.recording,
                recordingSeconds = voice.recordingSeconds,
                transcribing = voice.transcribing,
                amplitudes = voice.amplitudes,
            )
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, ChatVoiceState())

    init {
        viewModelScope.launch {
            OpenDS.voice.isSpeaking.collect { speaking ->
                val current = _reading.value
                if (current.messageId != null) {
                    _reading.value = current.copy(status = if (speaking) ReadingStatus.PLAYING else ReadingStatus.LOADING)
                }
            }
        }
        viewModelScope.launch {
            OpenDS.chat.generation.collect { generation ->
                if (generation !is GenerationState.Streaming) maybeAutoRead()
            }
        }
    }

    fun bind(sessionId: String?, onSessionCreated: ((String) -> Unit)? = null) {
        this.onSessionCreated = onSessionCreated
        if (_sessionId.value != sessionId) {
            _sessionId.value = sessionId
            if (sessionId == null) _loaded.value = true
            _searchPanel.value = null
        }
    }

    fun startNewChat(onSessionCreated: (String) -> Unit) {
        this.onSessionCreated = onSessionCreated
        if (_sessionId.value == null && uiState.value.messages.isEmpty()) return
        _sessionId.value = null
        _searchPanel.value = null
        _reading.value = ReadingState()
        OpenDS.chat.stop()
    }

    fun onScreenResume() {
        if (_sessionId.value != null) _loaded.value = true
    }

    fun send() {
        val state = uiState.value
        val draft = draftState.value
        if (state.streaming) return
        val text = draft.text.trim()
        if (text.isEmpty() && draft.attachments.isEmpty()) return
        if (!state.hasApiKey) {
            showToast("请先在设置中填写 API Key")
            return
        }
        val input = SendInput(
            text = text,
            thinkingEnabled = state.thinkingEnabled,
            searchEnabled = state.searchEnabled,
            attachments = draft.attachments.filter { it.state == AttachmentState.READY }.map { it.ref },
        )
        updateDraft { Draft() }
        val sessionId = state.sessionId
        requestScope.launch {
            var target = sessionId
            if (target == null) {
                val created = runCatching { OpenDS.sessions.createSession() }.getOrNull()
                if (created == null) {
                    showToast("创建对话失败，请重试")
                    return@launch
                }
                target = created.id
                _sessionId.value = created.id
                onSessionCreated?.invoke(created.id)
            }
            val result = OpenDS.chat.send(target, input)
            result.exceptionOrNull()?.let { showToast(describeError(it)) }
        }
    }

    fun stop() {
        OpenDS.chat.stop()
    }

    fun regenerateFrom(userMessageId: Long) {
        val sessionId = uiState.value.sessionId ?: return
        if (uiState.value.streaming) return
        requestScope.launch {
            OpenDS.chat.regenerate(sessionId, userMessageId).exceptionOrNull()?.let { showToast(describeError(it)) }
        }
    }

    fun regenerate() {
        val state = uiState.value
        val sessionId = state.sessionId ?: return
        if (state.streaming) return
        val index = state.messages.indexOfLast { it.role == Role.ASSISTANT }
        if (index <= 0) return
        val question = state.messages.getOrNull(index - 1) ?: return
        if (question.role != Role.USER) return
        requestScope.launch {
            OpenDS.chat.regenerate(sessionId, question.id).exceptionOrNull()?.let { showToast(describeError(it)) }
        }
    }

    fun continueGeneration() {
        val sessionId = uiState.value.sessionId ?: return
        requestScope.launch {
            OpenDS.chat.continueGeneration(sessionId).exceptionOrNull()?.let { showToast(describeError(it)) }
        }
    }

    fun editUserMessage(messageId: Long, text: String) {
        val state = uiState.value
        val sessionId = state.sessionId ?: return
        if (state.streaming) return
        if (text.isBlank()) return
        requestScope.launch {
            OpenDS.chat.editUserMessage(sessionId, messageId, text).exceptionOrNull()?.let { showToast(describeError(it)) }
        }
    }

    fun selectVersion(messageId: Long, siblingId: Long) {
        val state = uiState.value
        val sessionId = state.sessionId ?: return
        if (state.streaming) return
        val all = state.allMessages
        if (all.none { it.id == messageId }) return
        var cursor = siblingId
        val visited = mutableSetOf<Long>()
        while (visited.add(cursor)) {
            val children = all.filter { it.parentId == cursor }
            val next = children.maxByOrNull { it.id } ?: break
            cursor = next.id
        }
        requestScope.launch { OpenDS.sessions.setCurrentMessage(sessionId, cursor) }
    }

    fun deleteBranch(messageId: Long) {
        val sessionId = uiState.value.sessionId ?: return
        requestScope.launch { OpenDS.sessions.deleteBranch(sessionId, messageId) }
    }

    fun toggleThinking() {
        val settings = OpenDS.settings.state.value
        requestScope.launch { OpenDS.settings.update(settings.copy(thinkingDefault = !settings.thinkingDefault)) }
    }

    fun toggleSearch() {
        val settings = OpenDS.settings.state.value
        requestScope.launch { OpenDS.settings.update(settings.copy(searchDefault = !settings.searchDefault)) }
    }

    fun setDraftText(text: String) {
        updateDraft { it.copy(text = text) }
    }

    fun addAttachments(uris: List<Uri>) {
        if (uris.isEmpty()) return
        val resolver = getApplication<Application>().contentResolver
        uris.forEach { uri ->
            viewModelScope.launch {
                val ref = buildAttachmentRef(resolver, uri)
                updateDraft { it.copy(attachments = it.attachments + DraftAttachment(ref, AttachmentState.UPLOADING)) }
                runCatching { OpenDS.attachments.import(ref) }
                    .onSuccess { imported ->
                        updateDraft { draft ->
                            draft.copy(
                                attachments = draft.attachments.map {
                                    if (it.ref.id == ref.id) DraftAttachment(imported, AttachmentState.READY) else it
                                },
                            )
                        }
                    }
                    .onFailure {
                        updateDraft { draft ->
                            draft.copy(
                                attachments = draft.attachments.map {
                                    if (it.ref.id == ref.id) DraftAttachment(it.ref, AttachmentState.ERROR, "上传失败") else it
                                },
                            )
                        }
                    }
            }
        }
    }

    fun removeAttachment(id: String) {
        updateDraft { it.copy(attachments = it.attachments.filterNot { attachment -> attachment.ref.id == id }) }
    }

    fun retryAttachment(id: String) {
        val attachment = draftSnapshot().attachments.firstOrNull { it.ref.id == id } ?: return
        updateDraft { draft ->
            draft.copy(attachments = draft.attachments.map { if (it.ref.id == id) it.copy(state = AttachmentState.UPLOADING, error = null) else it })
        }
        viewModelScope.launch {
            runCatching { OpenDS.attachments.import(attachment.ref) }
                .onSuccess { imported ->
                    updateDraft { draft ->
                        draft.copy(attachments = draft.attachments.map { if (it.ref.id == id) DraftAttachment(imported, AttachmentState.READY) else it })
                    }
                }
                .onFailure {
                    updateDraft { draft ->
                        draft.copy(attachments = draft.attachments.map { if (it.ref.id == id) it.copy(state = AttachmentState.ERROR, error = "上传失败") else it })
                    }
                }
        }
    }

    fun startRecording() {
        if (_recording.value) return
        val voiceRecorder = VoiceRecorder(getApplication())
        if (!voiceRecorder.start()) {
            showToast("无法使用麦克风，请检查权限设置")
            return
        }
        recorder = voiceRecorder
        _recording.value = true
        _recordingSeconds.value = 0
        recordingTimer = viewModelScope.launch {
            while (true) {
                delay(1000)
                _recordingSeconds.value += 1
                if (_recordingSeconds.value >= VoiceMaxRecordSeconds) {
                    stopRecording(cancelled = false)
                    break
                }
            }
        }
        amplitudeJob = viewModelScope.launch {
            voiceRecorder.amplitudes.collect { values -> _amplitudes.value = values.toList() }
        }
    }

    fun stopRecording(cancelled: Boolean) {
        val voiceRecorder = recorder ?: return
        recorder = null
        recordingTimer?.cancel()
        amplitudeJob?.cancel()
        _recording.value = false
        _recordingSeconds.value = 0
        _amplitudes.value = emptyList()
        val wav = if (cancelled) {
            voiceRecorder.cancel()
            null
        } else {
            voiceRecorder.stop()
        }
        if (wav == null || wav.isEmpty()) return
        _transcribing.value = true
        transcribeJob = requestScope.launch {
            val result = runCatching { OpenDS.voice.transcribe(wav) }
            if (!_transcribing.value) return@launch
            _transcribing.value = false
            result.onSuccess { text ->
                if (text.isBlank()) showToast("未识别到语音内容") else sendVoiceMessage(text.trim())
            }.onFailure {
                showToast("语音识别失败，请重试")
            }
        }
    }

    private fun sendVoiceMessage(text: String) {
        val state = uiState.value
        if (!state.hasApiKey) {
            showToast("请先在设置中填写 API Key")
            return
        }
        val input = SendInput(
            text = text,
            thinkingEnabled = state.thinkingEnabled,
            searchEnabled = state.searchEnabled,
        )
        val sessionId = state.sessionId
        requestScope.launch {
            var target = sessionId
            if (target == null) {
                val created = runCatching { OpenDS.sessions.createSession() }.getOrNull()
                if (created == null) {
                    showToast("创建对话失败，请重试")
                    return@launch
                }
                target = created.id
                _sessionId.value = created.id
                onSessionCreated?.invoke(created.id)
            }
            OpenDS.chat.send(target, input).exceptionOrNull()?.let { showToast(describeError(it)) }
        }
    }

    fun cancelTranscription() {
        transcribeJob?.cancel()
        transcribeJob = null
        _transcribing.value = false
    }

    fun toggleReadAloud(messageId: Long, text: String) {
        if (text.isBlank()) {
            showToast("这条回答暂时无法朗读")
            return
        }
        if (_reading.value.messageId == messageId) {
            OpenDS.voice.stopSpeaking()
            _reading.value = ReadingState()
            return
        }
        requestScope.launch {
            _reading.value = ReadingState(messageId, ReadingStatus.LOADING)
            runCatching { OpenDS.voice.speak(text) }
                .onFailure { showToast("朗读失败，请稍后再试") }
            if (_reading.value.messageId == messageId) _reading.value = ReadingState()
        }
    }

    fun openSearchPanel(entryKey: String, sources: List<com.opends.chat.core.SearchResult>) {
        _searchPanel.value = SearchPanelState(entryKey, sources)
    }

    fun closeSearchPanel() {
        _searchPanel.value = null
    }

    fun toggleAutoRead() {
        _autoRead.value = !_autoRead.value
    }

    fun showToast(text: String, withIcon: Boolean = true) {
        _toast.value = ToastState(System.currentTimeMillis(), text, withIcon)
    }

    fun dismissToast(id: Long) {
        if (_toast.value?.id == id) _toast.value = null
    }

    private fun maybeAutoRead() {
        if (!_autoRead.value) return
        val state = uiState.value
        val last = state.messages.lastOrNull() ?: return
        if (last.role != Role.ASSISTANT || last.id == lastAutoReadId) return
        lastAutoReadId = last.id
        val text = replyText(last)
        if (text.isBlank()) return
        requestScope.launch {
            _reading.value = ReadingState(last.id, ReadingStatus.LOADING)
            runCatching { OpenDS.voice.speak(text) }
            if (_reading.value.messageId == last.id) _reading.value = ReadingState()
        }
    }

    private fun appendDraftText(text: String) {
        updateDraft {
            val separator = if (it.text.isEmpty() || it.text.endsWith(" ")) "" else ""
            it.copy(text = it.text + separator + text)
        }
    }

    private fun draftSnapshot(): Draft = _drafts.value[_sessionId.value] ?: Draft()

    private fun updateDraft(transform: (Draft) -> Draft) {
        val key = _sessionId.value
        val current = _drafts.value
        val updated = current.toMutableMap()
        updated[key] = transform(current[key] ?: Draft())
        _drafts.value = updated
    }

    private fun buildAttachmentRef(resolver: android.content.ContentResolver, uri: Uri): AttachmentRef {
        var name = uri.lastPathSegment ?: "文件"
        var size = 0L
        runCatching {
            resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0 && !cursor.isNull(nameIndex)) name = cursor.getString(nameIndex)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) size = cursor.getLong(sizeIndex)
                }
            }
        }
        val mime = resolver.getType(uri) ?: "application/octet-stream"
        return AttachmentRef(
            id = UUID.randomUUID().toString(),
            uri = uri.toString(),
            name = name,
            mime = mime,
            size = size,
            isImage = mime.startsWith("image/"),
        )
    }

    private fun describeError(error: Throwable): String {
        val message = error.message.orEmpty()
        val known = when {
            message.contains("401") || message.contains("API Key", ignoreCase = true) || message.contains("invalid_api_key") ->
                "API Key 无效，请更换后重试"
            message.contains("402") || message.contains("余额") || message.contains("balance", ignoreCase = true) ->
                "账户余额不足，请充值后重试"
            message.contains("429") || message.contains("频繁") || message.contains("rate", ignoreCase = true) ->
                "操作过于频繁，请稍后再试"
            message.contains("超时") || message.contains("timeout", ignoreCase = true) ->
                "请求超时，请稍后再试"
            else -> null
        }
        if (known != null) return known
        if (message.isBlank()) return "网络异常，请检查你的网络状况"
        val network = message.contains("网络") || message.contains("连接", ignoreCase = true) ||
            message.contains("Unable to resolve host", ignoreCase = true) ||
            message.contains("Failed to connect", ignoreCase = true) ||
            message.contains("SSL", ignoreCase = true) ||
            message.contains("Socket", ignoreCase = true) ||
            message.contains("unreachable", ignoreCase = true)
        return if (network) "网络异常，请检查你的网络状况" else message
    }

    override fun onCleared() {
        super.onCleared()
        recorder?.cancel()
        recorder = null
        OpenDS.chat.stop()
        OpenDS.voice.stopSpeaking()
    }

    companion object {
        private const val VoiceMaxRecordSeconds = 60

        internal val requestScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

        internal fun visibleMessages(all: List<ChatMessage>, currentMessageId: Long?): List<ChatMessage> {
            if (all.isEmpty()) return emptyList()
            val byId = all.associateBy { it.id }
            var cursor = currentMessageId?.takeIf { byId.containsKey(it) }
                ?: all.maxByOrNull { it.id }?.id
                ?: return emptyList()
            val path = ArrayList<ChatMessage>()
            val visited = HashSet<Long>()
            while (visited.add(cursor)) {
                val message = byId[cursor] ?: break
                path.add(message)
                cursor = message.parentId ?: break
            }
            path.reverse()
            return path
        }

        internal fun messageVersions(all: List<ChatMessage>, visible: List<ChatMessage>): Map<Long, List<Long>> {
            if (visible.isEmpty()) return emptyMap()
            val visibleIds = visible.mapTo(HashSet()) { it.id }
            val result = HashMap<Long, List<Long>>()
            for (message in visible) {
                val siblings = all.filter { it.parentId == message.parentId && it.role == message.role }
                if (siblings.size > 1 && visibleIds.contains(message.id)) {
                    result[message.id] = siblings.sortedBy { it.id }.map { it.id }
                }
            }
            return result
        }

        internal fun replyText(message: ChatMessage): String = message.fragments
            .filterIsInstance<com.opends.chat.core.Fragment.Response>()
            .joinToString("\n\n") { it.content }
            .replace(Regex("\\[citation:\\d+\\]"), "")
    }
}
