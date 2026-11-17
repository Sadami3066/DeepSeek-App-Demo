package com.opends.chat.core.chat

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.opends.chat.core.AttachmentRef
import com.opends.chat.core.ChatClient
import com.opends.chat.core.ChatMessage
import com.opends.chat.core.Fragment
import com.opends.chat.core.GenerationState
import com.opends.chat.core.MessageStatus
import com.opends.chat.core.Role
import com.opends.chat.core.SendInput
import com.opends.chat.core.SessionInfo
import com.opends.chat.core.attach.AttachmentStoreImpl
import com.opends.chat.core.db.FragmentCodec
import com.opends.chat.core.db.SessionStoreImpl
import com.opends.chat.core.db.StoredMessage
import com.opends.chat.core.net.ApiCondition
import com.opends.chat.core.net.ApiErrorKind
import com.opends.chat.core.net.ApiException
import com.opends.chat.core.net.StreamListener
import com.opends.chat.core.net.WireClient
import com.opends.chat.core.net.conditionText
import com.opends.chat.core.settings.SettingsStoreImpl
import com.opends.chat.core.util.obj
import com.opends.chat.core.util.string
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean

internal class ChatClientImpl(
    private val settings: SettingsStoreImpl,
    private val store: SessionStoreImpl,
    private val attachments: AttachmentStoreImpl,
    private val wire: WireClient,
) : ChatClient {

    private val generationState = MutableStateFlow<GenerationState>(GenerationState.Idle)
    override val generation: StateFlow<GenerationState> = generationState

    private val busy = AtomicBoolean(false)

    @Volatile
    private var activeJob: kotlinx.coroutines.Job? = null

    @Volatile
    private var stopRequested = false

    override fun stop() {
        stopRequested = true
        wire.cancelMessage()
        activeJob?.cancel()
    }

    override suspend fun send(sessionId: String, input: SendInput): Result<Unit> = guarded {
        val text = input.text.trim()
        if (text.isEmpty() && input.attachments.isEmpty()) throw userError("请输入内容")
        val session = store.getSession(sessionId) ?: throw userError("会话不存在")
        ComposerLimits.validateText(text)?.let { throw userError(it) }
        ComposerLimits.validateSearchWithFiles(input.searchEnabled, input.attachments.isNotEmpty())
            ?.let { throw userError(it) }
        val sessionFileCount = store.countSessionFiles(sessionId)
        ComposerLimits.validateFileCount(sessionFileCount, input.attachments.size)
            ?.let { throw userError(it) }
        input.attachments.firstOrNull { it.size > 0 }?.let { attachment ->
            ComposerLimits.validateFileSize(attachment.size)?.let { throw userError(it) }
        }
        val stored = store.loadStoredMessages(sessionId)
        val path = visiblePath(stored, session.currentMessageId)
        val now = System.currentTimeMillis()
        var nextId = (stored.maxOfOrNull { it.id } ?: 0L) + 1L
        val files = prepareFiles(input.attachments)
        files.firstOrNull { it.size > 0 }?.let { file ->
            ComposerLimits.validateFileSize(file.size)?.let { throw userError(it) }
        }
        val userJson = WireCodec.userFragments(text, files)
        val userMessage = ChatMessage(
            id = nextId++,
            parentId = path.lastOrNull()?.id,
            role = Role.USER,
            status = MessageStatus.FINISHED,
            thinkingEnabled = input.thinkingEnabled,
            searchEnabled = input.searchEnabled,
            insertedAt = now,
            prefix = false,
            fragments = FragmentCodec.toFragments(userJson),
            conversationMode = "DEFAULT",
        )
        val assistantId = nextId
        val assistant = ChatMessage(
            id = assistantId,
            parentId = userMessage.id,
            role = Role.ASSISTANT,
            status = MessageStatus.WIP,
            thinkingEnabled = input.thinkingEnabled,
            searchEnabled = input.searchEnabled,
            insertedAt = now,
            prefix = true,
            conversationMode = conversationModeFor(input.thinkingEnabled, input.searchEnabled),
        )
        val decoratedUser = userMessage.copy(fragments = decorateFiles(userMessage.fragments, files))
        store.insertMessage(sessionId, decoratedUser, FragmentCodec.encode(userJson))
        store.insertMessage(sessionId, assistant, "[]")
        store.setCurrentMessage(sessionId, assistantId)
        appendToCache(sessionId, decoratedUser, assistant)
        generationState.value = GenerationState.Streaming(sessionId, assistantId, thinkingActive = false)
        val uploadFailure = runCatching { ensureUploaded(files) }.exceptionOrNull()
        if (uploadFailure != null) {
            val error = uploadFailure as? ApiException
                ?: ApiException.upload(uploadFailure.message ?: "上传失败", true)
            settleSetupError(sessionId, assistantId, error.message ?: "上传失败")
            throw error
        }
        val titleEnabled = session.title.isNullOrEmpty() && path.none { it.role == Role.ASSISTANT }
        val setup = StreamSetup(
            sessionId = sessionId,
            assistantId = assistantId,
            history = path,
            currentUserItems = userJson,
            prefixBlocks = null,
            thinkingEnabled = input.thinkingEnabled,
            searchEnabled = input.searchEnabled,
            titleEnabled = titleEnabled,
            existingBlocks = emptyList(),
            canContinueInitially = true,
            attachmentFiles = files,
        )
        streamInto(setup)
    }

    override suspend fun regenerate(sessionId: String, userMessageId: Long): Result<Unit> = guarded {
        val session = store.getSession(sessionId) ?: throw userError("会话不存在")
        val stored = store.loadStoredMessages(sessionId)
        val path = visiblePath(stored, session.currentMessageId)
        if (path.none { it.id == userMessageId }) throw userError("该消息不在当前对话中")
        val userIndex = path.indexOfFirst { it.id == userMessageId }
        val user = path[userIndex]
        if (user.role != Role.USER) throw userError("只能重新生成回答")
        if (!wire.hasApiKey()) throw userError("请先在设置中填写 API Key")
        val userItems = FragmentCodec.parse(user.fragmentsJson)
        val files = WireCodec.filesOf(userItems)
        val previous = path.getOrNull(userIndex + 1)?.takeIf { it.role == Role.ASSISTANT }
        ensureUploaded(files)
        val now = System.currentTimeMillis()
        var nextId = (stored.maxOfOrNull { it.id } ?: 0L) + 1L
        val thinkingEnabled = previous?.thinkingEnabled ?: settings.state.value.thinkingDefault
        val searchEnabled = previous?.searchEnabled ?: settings.state.value.searchDefault
        val assistant = ChatMessage(
            id = nextId,
            parentId = user.id,
            role = Role.ASSISTANT,
            status = MessageStatus.WIP,
            thinkingEnabled = thinkingEnabled,
            searchEnabled = searchEnabled,
            insertedAt = now,
            prefix = true,
            conversationMode = conversationModeFor(thinkingEnabled, searchEnabled),
        )
        store.insertMessage(sessionId, assistant, "[]")
        store.setCurrentMessage(sessionId, assistant.id)
        appendToCache(sessionId, assistant)
        generationState.value = GenerationState.Streaming(sessionId, assistant.id, thinkingActive = false)
        val titleEnabled = session.title.isNullOrEmpty()
        streamInto(
            StreamSetup(
                sessionId = sessionId,
                assistantId = assistant.id,
                history = path.subList(0, userIndex),
                currentUserItems = userItems,
                prefixBlocks = null,
                thinkingEnabled = assistant.thinkingEnabled,
                searchEnabled = assistant.searchEnabled,
                titleEnabled = titleEnabled,
                existingBlocks = emptyList(),
                canContinueInitially = true,
                attachmentFiles = files,
            )
        )
    }

    override suspend fun editUserMessage(sessionId: String, messageId: Long, newText: String): Result<Unit> = guarded {
        val text = newText.trim()
        ComposerLimits.validateText(text)?.let { throw userError(it) }
        val session = store.getSession(sessionId) ?: throw userError("会话不存在")
        val stored = store.loadStoredMessages(sessionId)
        val path = visiblePath(stored, session.currentMessageId)
        val index = path.indexOfFirst { it.id == messageId }
        if (index < 0) throw userError("该消息不在当前对话中")
        val original = path[index]
        if (original.role != Role.USER) throw userError("只能编辑提问")
        val originalItems = FragmentCodec.parse(original.fragmentsJson)
        val files = WireCodec.filesOf(originalItems)
        if (text.isEmpty() && files.isEmpty()) throw userError("请输入内容")
        if (!wire.hasApiKey()) throw userError("请先在设置中填写 API Key")
        ensureUploaded(files)
        val now = System.currentTimeMillis()
        var nextId = (stored.maxOfOrNull { it.id } ?: 0L) + 1L
        val userJson = WireCodec.userFragments(text, files)
        val userMessage = ChatMessage(
            id = nextId++,
            parentId = original.parentId,
            role = Role.USER,
            status = MessageStatus.FINISHED,
            thinkingEnabled = original.thinkingEnabled,
            searchEnabled = original.searchEnabled,
            insertedAt = now,
            fragments = FragmentCodec.toFragments(userJson),
            conversationMode = "DEFAULT",
        )
        val assistant = ChatMessage(
            id = nextId,
            parentId = userMessage.id,
            role = Role.ASSISTANT,
            status = MessageStatus.WIP,
            thinkingEnabled = settings.state.value.thinkingDefault,
            searchEnabled = settings.state.value.searchDefault,
            insertedAt = now,
            prefix = true,
            conversationMode = conversationModeFor(
                settings.state.value.thinkingDefault,
                settings.state.value.searchDefault,
            ),
        )
        store.insertMessage(sessionId, userMessage, FragmentCodec.encode(userJson))
        store.insertMessage(sessionId, assistant, "[]")
        store.setCurrentMessage(sessionId, assistant.id)
        appendToCache(sessionId, userMessage, assistant)
        generationState.value = GenerationState.Streaming(sessionId, assistant.id, thinkingActive = false)
        streamInto(
            StreamSetup(
                sessionId = sessionId,
                assistantId = assistant.id,
                history = path.subList(0, index),
                currentUserItems = userJson,
                prefixBlocks = null,
                thinkingEnabled = assistant.thinkingEnabled,
                searchEnabled = assistant.searchEnabled,
                titleEnabled = session.title.isNullOrEmpty(),
                existingBlocks = emptyList(),
                canContinueInitially = true,
                attachmentFiles = files,
            )
        )
    }

    override suspend fun continueGeneration(sessionId: String): Result<Unit> = guarded {
        val session = store.getSession(sessionId) ?: throw userError("会话不存在")
        val stored = store.loadStoredMessages(sessionId)
        val path = visiblePath(stored, session.currentMessageId)
        if (path.size < 2) throw userError("没有可继续生成的回答")
        val assistant = path.last()
        val user = path[path.size - 2]
        if (assistant.role != Role.ASSISTANT || user.role != Role.USER) throw userError("没有可继续生成的回答")
        if (!assistant.prefix) throw userError("该回答不支持继续生成")
        val items = FragmentCodec.parse(assistant.fragmentsJson)
        val blocks = WireCodec.blocks(items)
        if (blocks.any { it is WireBlock.Atomic || it is WireBlock.OpenPage || it is WireBlock.FindPage }) {
            throw userError("该回答不支持继续生成")
        }
        if (blocks.any { it is WireBlock.Template }) throw userError("该回答不支持继续生成")
        val meta = WireCodec.meta(items)
        val metaReason = meta?.string("stop_reason")
        val canContinue = assistant.status == MessageStatus.INCOMPLETE ||
            (assistant.status == MessageStatus.FINISHED && metaReason == "max_tokens")
        if (!canContinue) throw userError("该回答不支持继续生成")
        if (!wire.hasApiKey()) throw userError("请先在设置中填写 API Key")
        val userItems = FragmentCodec.parse(user.fragmentsJson)
        val files = WireCodec.filesOf(userItems)
        ensureUploaded(files)
        val cached = store.cachedOrLoad(sessionId)
        val cachedAssistant = cached.firstOrNull { it.id == assistant.id }
        val updated = (cachedAssistant ?: ChatMessage(
            id = assistant.id,
            parentId = assistant.parentId,
            role = Role.ASSISTANT,
            status = MessageStatus.WIP,
            thinkingEnabled = assistant.thinkingEnabled,
            searchEnabled = assistant.searchEnabled,
            insertedAt = assistant.insertedAt,
            prefix = assistant.prefix,
        )).copy(status = MessageStatus.WIP, fragments = WireCodec.toModelFragments(blocks))
        replaceInCache(sessionId, updated)
        store.updateMessage(sessionId, updated, FragmentCodec.encode(WireCodec.canonical(blocks)))
        store.setCurrentMessage(sessionId, assistant.id)
        streamInto(
            StreamSetup(
                sessionId = sessionId,
                assistantId = assistant.id,
                history = path.subList(0, path.size - 2),
                currentUserItems = userItems,
                prefixBlocks = blocks,
                thinkingEnabled = assistant.thinkingEnabled,
                searchEnabled = assistant.searchEnabled,
                titleEnabled = false,
                existingBlocks = blocks,
                canContinueInitially = true,
                attachmentFiles = files,
            )
        )
    }

    private suspend fun guarded(block: suspend () -> Unit): Result<Unit> {
        if (!busy.compareAndSet(false, true)) {
            return Result.failure(userError("正在生成回复，请稍候"))
        }
        stopRequested = false
        activeJob = currentCoroutineContext().job
        return try {
            block()
            Result.success(Unit)
        } catch (error: CancellationException) {
            Result.success(Unit)
        } catch (error: ApiException) {
            Result.failure(error)
        } catch (error: Exception) {
            Result.failure(userError(error.message ?: "操作失败"))
        } finally {
            activeJob = null
            stopRequested = false
            generationState.value = GenerationState.Idle
            busy.set(false)
        }
    }

    private data class StreamSetup(
        val sessionId: String,
        val assistantId: Long,
        val history: List<StoredMessage>,
        val currentUserItems: List<JsonObject>,
        val prefixBlocks: List<WireBlock>?,
        val thinkingEnabled: Boolean,
        val searchEnabled: Boolean,
        val titleEnabled: Boolean,
        val existingBlocks: List<WireBlock>,
        val canContinueInitially: Boolean,
        val attachmentFiles: List<Fragment.File>,
    )

    private suspend fun streamInto(setup: StreamSetup) {
        val session = store.getSession(setup.sessionId) ?: return
        val assembler = StreamAssembler(setup.existingBlocks)
        var lastPublish = 0L
        var lastPersist = 0L

        val ui = store.cachedOrLoad(setup.sessionId).toMutableList()

        suspend fun publish(force: Boolean) {
            val now = System.currentTimeMillis()
            if (!force && now - lastPublish < 40L) return
            lastPublish = now
            val index = ui.indexOfFirst { it.id == setup.assistantId }
            if (index < 0) return
            ui[index] = ui[index].copy(
                status = MessageStatus.WIP,
                fragments = WireCodec.toModelFragments(assembler.blocks),
                accumulatedTokenUsage = assembler.totalUsage(),
            )
            store.publish(setup.sessionId, ui.toList())
            generationState.value = GenerationState.Streaming(
                sessionId = setup.sessionId,
                assistantMessageId = setup.assistantId,
                thinkingActive = assembler.thinkingActive,
            )
        }

        suspend fun persist(force: Boolean) {
            val now = System.currentTimeMillis()
            if (!force && now - lastPersist < 500L) return
            lastPersist = now
            val index = ui.indexOfFirst { it.id == setup.assistantId }
            if (index < 0) return
            val message = ui[index].copy(
                status = MessageStatus.WIP,
                fragments = WireCodec.toModelFragments(assembler.blocks),
                accumulatedTokenUsage = assembler.totalUsage(),
            )
            store.updateMessage(setup.sessionId, message, FragmentCodec.encode(WireCodec.canonical(assembler.blocks)))
        }

        val uploads = store.allUploads()
        val assemblerForRequest = RequestAssembler(uploads)
        val reminder = LatestReminder.build(settings.state.value)
        val messages = assemblerForRequest.buildMessages(
            history = setup.history,
            currentUserItems = setup.currentUserItems,
            reminder = reminder,
            prefixBlocks = setup.prefixBlocks,
            includeThinking = setup.thinkingEnabled,
        )
        val body = buildRequestBody(messages, setup.thinkingEnabled, setup.searchEnabled, setup.titleEnabled)
        val needsBeta = messages.toString().contains("\"file_id\"")
        generationState.value = GenerationState.Streaming(
            sessionId = setup.sessionId,
            assistantMessageId = setup.assistantId,
            thinkingActive = false,
        )
        publish(force = true)

        val events = Channel<Pair<String?, JsonObject>>(Channel.UNLIMITED)
        var failure: Throwable? = null
        var cancelled = false
        try {
            coroutineScope {
                launch(Dispatchers.IO) {
                    try {
                        wire.streamMessages(
                            body = body,
                            listener = object : StreamListener {
                                override fun onEvent(event: String?, data: JsonObject) {
                                    events.trySend(event to data)
                                }
                            },
                            extraHeaders = if (needsBeta) mapOf("anthropic-beta" to WireClient.FILES_BETA) else emptyMap(),
                        )
                    } catch (error: Throwable) {
                        failure = error
                    } finally {
                        events.close()
                    }
                }
                for ((eventName, data) in events) {
                    val type = eventName ?: data.string("type")
                    if (data.get("error") != null && (type == null || type == "error")) {
                        throw ApiException.stream(errorText(data))
                    }
                    assembler.onEvent(eventName, data)
                    when (type) {
                        "content_block_start", "content_block_stop", "message_delta" -> {
                            publish(force = true)
                            persist(force = true)
                        }
                        "content_block_delta" -> {
                            publish(force = false)
                            persist(force = false)
                        }
                    }
                }
            }
        } catch (error: CancellationException) {
            cancelled = true
            failure = error
            throw error
        } catch (error: Throwable) {
            failure = error
        } finally {
            withContext(NonCancellable) {
                settle(
                    session = session,
                    setup = setup,
                    ui = ui,
                    assembler = assembler,
                    failure = failure,
                    cancelled = cancelled,
                    stopRequested = stopRequested || cancelled,
                )
            }
        }
    }

    private suspend fun settle(
        session: SessionInfo,
        setup: StreamSetup,
        ui: MutableList<ChatMessage>,
        assembler: StreamAssembler,
        failure: Throwable?,
        cancelled: Boolean,
        stopRequested: Boolean,
    ) {
        val blocks = assembler.blocks
        val stopReason = assembler.stopReason
        var status = MessageStatus.FINISHED
        var template: String? = null
        var keepContinuable = assembler.canContinue
        val refusal = stopReason == "refusal"

        when {
            failure is ApiException && failure.kind == ApiErrorKind.INTERRUPTED && stopReason != null -> {
                status = MessageStatus.FINISHED
            }
            stopRequested || cancelled -> {
                status = MessageStatus.INCOMPLETE
            }
            refusal -> {
                status = MessageStatus.CONTENT_FILTER
                blocks.removeAll { it is WireBlock.Text || it is WireBlock.Thinking }
                template = REFUSAL_TEXT
                keepContinuable = false
            }
            failure != null -> {
                val mapped = mapFailure(failure, stopReason)
                status = mapped.first
                template = if (mapped.first == MessageStatus.CONTENT_FILTER) mapped.second else null
                keepContinuable = false
            }
            stopReason == "max_tokens" -> {
                status = MessageStatus.INCOMPLETE
            }
            stopReason != null || assembler.endedNormally -> {
                status = MessageStatus.FINISHED
            }
            else -> {
                status = MessageStatus.FAILED
                keepContinuable = false
            }
        }

        if (template != null) blocks.add(WireBlock.Template(template))

        val items = WireCodec.canonical(blocks)
        WireCodec.appendMeta(items, stopReason, assembler.title)
        val fragments = WireCodec.toModelFragments(blocks)
        val index = ui.indexOfFirst { it.id == setup.assistantId }
        if (index >= 0) {
            ui[index] = ui[index].copy(
                status = status,
                fragments = fragments,
                prefix = keepContinuable,
                accumulatedTokenUsage = assembler.totalUsage(),
            )
            store.publish(setup.sessionId, ui.toList())
            store.updateMessage(setup.sessionId, ui[index], FragmentCodec.encode(items))
        }
        val titleStatusAllowed = status == MessageStatus.FINISHED ||
            (status == MessageStatus.INCOMPLETE && stopReason == "max_tokens")
        if (assembler.title != null && setup.titleEnabled && titleStatusAllowed &&
            !stopRequested && (stopReason == null || stopReason == "end_turn" || stopReason == "max_tokens")
        ) {
            val trimmed = assembler.title?.trim().orEmpty()
            if (trimmed.isNotEmpty() && store.getSession(setup.sessionId)?.title.isNullOrEmpty()) {
                store.setSessionTitle(setup.sessionId, trimmed)
            }
        }
        store.touchSession(setup.sessionId)
        generationState.value = GenerationState.Idle
    }

    private fun mapFailure(failure: Throwable, stopReason: String?): Pair<MessageStatus, String> {
        if (failure is ApiException) {
            val message = failure.message ?: ""
            return when {
                failure.condition == ApiCondition.UNAUTHORIZED -> MessageStatus.FAILED to "API Key 无效或已被删除，请更换后重试"
                failure.condition == ApiCondition.INSUFFICIENT_BALANCE -> MessageStatus.FAILED to "账户余额不足，请充值后重试"
                failure.condition == ApiCondition.RATE_LIMITED -> MessageStatus.FAILED to "请求过于频繁，请稍后再试"
                failure.condition == ApiCondition.SERVER_BUSY -> MessageStatus.FAILED to "服务器繁忙，请稍后再试"
                failure.kind == ApiErrorKind.INTERRUPTED -> MessageStatus.FAILED to "生成中断，请重试"
                failure.condition == ApiCondition.NETWORK -> {
                    val timeout = message.contains("timeout", ignoreCase = true) ||
                        message.contains("timed out", ignoreCase = true)
                    if (timeout) {
                        MessageStatus.TIMEOUT to "等待响应超时，请重试"
                    } else {
                        MessageStatus.FAILED to "网络异常，请检查你的网络状况"
                    }
                }
                failure.status == 400 && message.contains("Content Exists Risk", ignoreCase = true) ->
                    MessageStatus.CONTENT_FILTER to INPUT_RISK_TEXT
                failure.status == 400 && (
                    message.contains("context length", ignoreCase = true) ||
                        message.contains("too long", ignoreCase = true) ||
                        message.contains("context_length", ignoreCase = true) ||
                        message.contains("maximum context", ignoreCase = true)
                    ) -> MessageStatus.CONTEXT_LENGTH_EXCEEDED to CONTEXT_TEXT
                stopReason == "refusal" -> MessageStatus.CONTENT_FILTER to REFUSAL_TEXT
                else -> MessageStatus.FAILED to conditionText(failure.condition).ifEmpty { message }
            }
        }
        return MessageStatus.FAILED to (failure.message ?: "生成失败，请稍后重试")
    }

    private fun buildRequestBody(
        messages: JsonArray,
        thinkingEnabled: Boolean,
        searchEnabled: Boolean,
        titleEnabled: Boolean,
    ): JsonObject = ChatRequestBuilder.build(messages, thinkingEnabled, searchEnabled, titleEnabled)

    private fun decorateFiles(fragments: List<Fragment>, files: List<Fragment.File>): List<Fragment> {
        val byId = files.associateBy { it.fileId }
        return fragments.map { fragment ->
            if (fragment is Fragment.File) {
                byId[fragment.fileId]?.let { fragment.copy(localPath = it.localPath) } ?: fragment
            } else {
                fragment
            }
        }
    }

    private suspend fun settleSetupError(sessionId: String, assistantId: Long, message: String) {
        val canonical = mutableListOf<JsonObject>()
        val ui = store.cachedOrLoad(sessionId).toMutableList()
        val index = ui.indexOfFirst { it.id == assistantId }
        if (index >= 0) {
            ui[index] = ui[index].copy(status = MessageStatus.FAILED, fragments = emptyList(), prefix = false)
            store.publish(sessionId, ui.toList())
            store.updateMessage(sessionId, ui[index], FragmentCodec.encode(canonical))
        }
        store.touchSession(sessionId)
    }

    private suspend fun prepareFiles(refs: List<AttachmentRef>): List<Fragment.File> = refs.map { ref ->
        attachments.import(ref)
        val local = attachments.localFile(ref.id)
        val mime = ref.mime.takeIf { it.isNotBlank() }
            ?: local?.let { attachments.sniffMime(it) }
            ?: ""
        Fragment.File(
            fileId = ref.id,
            name = ref.name,
            mime = mime.ifBlank { null },
            size = if (ref.size > 0) ref.size else (local?.length() ?: 0L),
            isImage = ref.isImage,
            localPath = local?.absolutePath,
        )
    }

    private suspend fun ensureUploaded(files: List<Fragment.File>): Map<String, String> {
        if (files.isEmpty()) return emptyMap()
        val existing = store.uploadIds(files.map { it.fileId })
        val pending = files.filter { existing[it.fileId].isNullOrEmpty() }
        pending.forEach { file ->
            val local = attachments.localFile(file.fileId)
                ?: throw ApiException.upload("附件原件已丢失，请重新选择", false)
            val mime = file.mime?.takeIf { it.isNotBlank() } ?: attachments.sniffMime(local)
            val purpose = purposeFor(mime, file.name, file.isImage)
            val bytes = try {
                local.readBytes()
            } catch (error: IOException) {
                throw ApiException.upload("附件读取失败", true)
            }
            val serverId = try {
                wire.uploadFile(bytes, file.name ?: file.fileId, mime, purpose)
            } catch (error: ApiException) {
                throw ApiException.upload(
                    conditionText(error.condition).ifEmpty { "上传失败" },
                    error.isRetryable,
                )
            }
            store.putUpload(file.fileId, serverId, purpose)
        }
        return store.uploadIds(files.map { it.fileId })
    }

    private fun purposeFor(mime: String, name: String?, isImage: Boolean): String? {
        if (isImage) return null
        val lowerName = (name ?: "").lowercase()
        val extension = lowerName.substringAfterLast('.', "")
        val documentTypes = setOf("pdf", "doc", "docx", "ppt", "pptx")
        if (extension in documentTypes) return "rich_layout_recognition"
        val documentMimes = listOf(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
        )
        if (documentMimes.any { mime.startsWith(it) }) return "rich_layout_recognition"
        return "user_data"
    }

    private fun appendToCache(sessionId: String, vararg messages: ChatMessage) {
        val current = store.cachedMessages(sessionId)?.toMutableList() ?: return
        messages.forEach { message ->
            val index = current.indexOfFirst { it.id == message.id }
            if (index >= 0) current[index] = message else current.add(message)
        }
        store.publish(sessionId, current)
    }

    private fun replaceInCache(sessionId: String, message: ChatMessage) {
        val current = store.cachedMessages(sessionId)?.toMutableList() ?: return
        val index = current.indexOfFirst { it.id == message.id }
        if (index >= 0) {
            current[index] = message
            store.publish(sessionId, current)
        }
    }

    private fun visiblePath(messages: List<StoredMessage>, currentMessageId: Long?): List<StoredMessage> {
        if (currentMessageId == null) return emptyList()
        val byId = messages.associateBy { it.id }
        val path = ArrayList<StoredMessage>()
        var cursor = byId[currentMessageId]
        while (cursor != null) {
            path.add(cursor)
            cursor = cursor.parentId?.let { byId[it] }
        }
        path.reverse()
        return path
    }

    private fun errorText(data: JsonObject): String {
        val error = data.obj("error")
        return error?.string("message") ?: data.string("message") ?: "生成失败，请稍后重试"
    }

    private fun conversationModeFor(thinkingEnabled: Boolean, searchEnabled: Boolean): String =
        if (thinkingEnabled && searchEnabled) "DEEP_SEARCH" else "DEFAULT"

    private fun userError(message: String): ApiException =
        ApiException(ApiErrorKind.HTTP, message)

    companion object {
        private const val REFUSAL_TEXT = "你好，这个问题我暂时无法回答，让我们换个话题再聊聊吧。"
        private const val INPUT_RISK_TEXT = "输入内容未通过内容安全审核，请调整后重试。"
        private const val CONTEXT_TEXT = "上下文长度已超出模型限制，请开启新对话或删除部分历史消息后重试。"
    }
}
