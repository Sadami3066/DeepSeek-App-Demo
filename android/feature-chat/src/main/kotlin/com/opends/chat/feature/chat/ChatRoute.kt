@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.opends.chat.feature.chat

import android.Manifest
import android.graphics.Bitmap
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.opends.chat.core.ChatMessage
import com.opends.chat.core.OpenDSApi
import com.opends.chat.core.Fragment
import com.opends.chat.core.Role
import com.opends.chat.core.SearchResult
import com.opends.chat.design.theme.OdTheme
import com.opends.chat.render.renderShareImageAsync
import com.opends.chat.feature.chat.ui.AppIcons
import com.opends.chat.feature.chat.ui.ChatInputDock
import com.opends.chat.feature.chat.ui.ChatTopBar
import com.opends.chat.feature.chat.ui.HomeGreetings
import com.opends.chat.feature.chat.ui.HomeGreeting
import com.opends.chat.feature.chat.ui.MessageContextMenu
import com.opends.chat.feature.chat.ui.MessageList
import com.opends.chat.feature.chat.ui.MessageMenuItem
import com.opends.chat.feature.chat.ui.SourcesSheet
import com.opends.chat.feature.chat.ui.MicPermissionDialog
import com.opends.chat.feature.chat.ui.ShareImagePreviewSheet
import com.opends.chat.feature.chat.ui.ShareSelectionScreen
import com.opends.chat.feature.chat.ui.buildShareGroups
import com.opends.chat.feature.chat.ui.buildShareImageContent
import com.opends.chat.feature.chat.ui.copyShareLink
import com.opends.chat.feature.chat.ui.groupOfMessage
import com.opends.chat.feature.chat.ui.saveShareBitmap
import com.opends.chat.feature.chat.ui.shareShareBitmap
import com.opends.chat.feature.chat.ui.shareTextContent
import com.opends.chat.feature.chat.ui.ToastHost
import com.opends.chat.feature.chat.ui.clickableNoIndication
import com.opends.chat.feature.chat.ui.VoiceInputOverlay
import com.opends.chat.feature.chat.ui.VoiceOverlayPhase
import com.opends.chat.feature.chat.ui.rememberRecentPhotos
import java.io.File
import java.util.Random
import kotlinx.coroutines.launch

@Composable
fun ChatRoute(
    sessionId: String?,
    onSessionCreated: (String) -> Unit,
    onOpenSider: () -> Unit,
    onOpenSettings: () -> Unit,
    initialMessageId: Long? = null,
    onInitialMessageHandled: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val viewModel: ChatViewModel = viewModel()
    LaunchedEffect(sessionId) { viewModel.bind(sessionId?.takeIf { it.isNotBlank() }, onSessionCreated) }
    LaunchedEffect(sessionId, initialMessageId) {
        val targetSession = sessionId?.takeIf { it.isNotBlank() } ?: return@LaunchedEffect
        val targetMessage = initialMessageId ?: return@LaunchedEffect
        viewModel.revealMessage(targetSession, targetMessage)
        kotlinx.coroutines.delay(3000)
        onInitialMessageHandled()
    }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val draft by viewModel.draftState.collectAsStateWithLifecycle()
    val voice by viewModel.voiceState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current

    var contextMenuTarget by remember { mutableStateOf<ContextMenuTarget?>(null) }
    var editRequestId by remember { mutableStateOf<Long?>(null) }
    var selectionMessageId by remember { mutableStateOf<Long?>(null) }
    var attachOpen by rememberSaveable { mutableStateOf(false) }
    var sourcesSheet by remember { mutableStateOf<List<SearchResult>?>(null) }
    var mediaPermissionRequested by rememberSaveable { mutableStateOf(false) }
    var mediaPermissionGranted by remember {
        mutableStateOf(
            context.checkSelfPermission(requiredMediaPermission()) == PackageManager.PERMISSION_GRANTED,
        )
    }
    val greetingIndex = rememberSaveable { Random().nextInt(HomeGreetings.size) }
    val imeVisible = WindowInsets.isImeVisible
    val scope = rememberCoroutineScope()
    val inputMode by OpenDSApi.inputMode.collectAsStateWithLifecycle(initialValue = "Text")
    val voiceMode = inputMode == "Voice"
    val fontSizeLevel by OpenDSApi.fontSizeLevel.collectAsStateWithLifecycle(initialValue = 0)
    var micDialogVisible by remember { mutableStateOf(false) }
    var voiceCancelZone by remember { mutableStateOf(false) }
    var voiceStartedAt by remember { mutableStateOf(0L) }
    val voiceOverlayVisible = voice.recording || voice.transcribing
    var shareAnchorId by remember { mutableStateOf<Long?>(null) }
    var shareSelected by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var sharePreview by remember { mutableStateOf<Bitmap?>(null) }
    var shareGenerating by remember { mutableStateOf(false) }
    val shareGroups = remember(state.messages) { buildShareGroups(state.messages) }
    val darkTheme = OdTheme.isDark

    fun enterShareMode(message: ChatMessage) {
        val group = groupOfMessage(state.messages, message.id)
        if (group == null || !group.enabled) {
            viewModel.showToast("该消息暂不支持分享", withIcon = false)
            return
        }
        shareAnchorId = message.id
        shareSelected = setOf(group.key)
    }

    fun generateShareImage() {
        val selectedGroups = shareGroups.filter { it.key in shareSelected }
        if (selectedGroups.isEmpty()) {
            viewModel.showToast("暂无可选对话", withIcon = false)
            return
        }
        if (shareGenerating) return
        shareGenerating = true
        val content = buildShareImageContent(state.sessionId, selectedGroups, darkTheme)
        renderShareImageAsync(context, content) { bitmap ->
            shareGenerating = false
            if (bitmap == null) {
                viewModel.showToast("选择的消息过长，图片生成失败", withIcon = false)
            } else {
                sharePreview = bitmap
            }
        }
    }

    val recordPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (!granted) micDialogVisible = true
    }

    val requestRecord = {
        val granted = context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
        if (granted) {
            viewModel.startRecording()
        } else {
            recordPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    val mediaPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> mediaPermissionGranted = granted }

    val recentPhotos = rememberRecentPhotos(
        enabled = attachOpen && mediaPermissionGranted,
        refreshKey = draft.attachments.size,
    )

    val addUris: (List<Uri>) -> Unit = { uris ->
        if (uris.isNotEmpty()) {
            val existing = draft.attachments.map { it.ref.uri }.toSet()
            val incoming = uris.filter { !existing.contains(it.toString()) }
                .distinctBy { it.toString() }
            if (incoming.isNotEmpty()) {
                val room = MaxAttachmentCount - existing.size
                if (room <= 0) {
                    viewModel.showToast("单条消息最多上传 $MaxAttachmentCount 个文件", withIcon = false)
                } else {
                    val accepted = ArrayList<Uri>()
                    var reachedLimit = false
                    for (uri in incoming) {
                        if (accepted.size >= room) {
                            reachedLimit = true
                            break
                        }
                        if (queryAttachmentSize(context, uri) > MaxAttachmentBytes) {
                            viewModel.showToast("单个文件大小超过限制（100MB），请调整后重试", withIcon = false)
                            continue
                        }
                        accepted.add(uri)
                    }
                    if (reachedLimit) {
                        viewModel.showToast("单条消息最多上传 $MaxAttachmentCount 个文件", withIcon = false)
                    }
                    if (accepted.isNotEmpty()) {
                        viewModel.addAttachments(accepted)
                    }
                }
            }
        }
    }

    val photoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(9),
    ) { uris ->
        addUris(uris)
    }
    val documentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments(),
    ) { uris ->
        addUris(uris)
    }
    val cameraUri = remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture(),
    ) { success ->
        val uri = cameraUri.value
        cameraUri.value = null
        if (success && uri != null) {
            addUris(listOf(uri))
        }
    }
    val cameraStarter = remember { CameraStarter() }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            cameraStarter.start?.invoke()
        } else {
            viewModel.showToast("需要相机权限才能拍照", withIcon = false)
        }
    }
    cameraStarter.start = {
        runCatching {
            val directory = File(context.cacheDir, "camera").apply { mkdirs() }
            val file = File(directory, "photo_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            cameraUri.value = uri
            cameraLauncher.launch(uri)
        }.onFailure {
            viewModel.showToast("无法打开相机，请稍后重试", withIcon = false)
        }
    }
    val requestCameraCapture = {
        attachOpen = false
        val granted = context.checkSelfPermission(Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        if (granted) {
            cameraStarter.start?.invoke()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    val selectedPhotoUris = draft.attachments.map { it.ref.uri }.toSet()
    val onSelectRecentPhoto: (Uri) -> Unit = { uri ->
        val key = uri.toString()
        val existing = draft.attachments.firstOrNull { it.ref.uri == key }
        if (existing != null) {
            viewModel.removeAttachment(existing.ref.id)
        } else {
            addUris(listOf(uri))
        }
    }

    val copyText: (String) -> Unit = { text ->
        if (text.isNotEmpty()) {
            val clipboard = context.getSystemService(ClipboardManager::class.java)
            clipboard?.setPrimaryClip(ClipData.newPlainText("text", text))
            viewModel.showToast("已复制", withIcon = false)
        }
    }

    val shareText: (String) -> Unit = { text ->
        if (text.isNotBlank()) {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            runCatching { context.startActivity(Intent.createChooser(intent, "分享回答")) }
        }
    }

    val palette = OdTheme.colors
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.bgBase)
            .imePadding(),
    ) {
        if (shareAnchorId == null) Column(modifier = Modifier.fillMaxSize()) {
            ChatTopBar(
                title = if (state.hasMessages) state.title else null,
                autoRead = state.autoRead,
                onOpenSider = onOpenSider,
                onToggleAutoRead = {
                    viewModel.toggleAutoRead()
                    viewModel.showToast(
                        if (state.autoRead) "已关闭自动朗读" else "已开启自动朗读",
                        withIcon = false,
                    )
                },
                onNewChat = {
                    attachOpen = false
                    viewModel.startNewChat(onSessionCreated)
                    onSessionCreated("")
                },
            )
            if (state.messages.isEmpty()) {
                HomeGreeting(
                    greeting = HomeGreetings[greetingIndex % HomeGreetings.size],
                    modifier = Modifier.weight(1f),
                )
            } else {
                val density = LocalDensity.current
                val fontScale = (density.fontScale * OpenDSApi.fontSizeScale(fontSizeLevel))
                    .coerceIn(0.85f, 2.0f)
                CompositionLocalProvider(
                    LocalDensity provides Density(density = density.density, fontScale = fontScale),
                ) {
                MessageList(
                    state = state,
                    editRequestId = editRequestId,
                    selectionMessageId = selectionMessageId,
                    onEditMessage = { id, text ->
                        editRequestId = null
                        viewModel.editUserMessage(id, text)
                    },
                    onCopyText = copyText,
                    onRegenerate = viewModel::regenerate,
                    onContinue = viewModel::continueGeneration,
                    onReadAloud = viewModel::toggleReadAloud,
                    onSelectVersion = viewModel::selectVersion,
                    onShare = { message -> enterShareMode(message) },
                    onOpenSources = { sources -> sourcesSheet = sources },
                    onOpenCitation = { source -> sourcesSheet = listOf(source) },
                    onLongPressMessage = { message, x, y ->
                        if (attachOpen) attachOpen = false
                        contextMenuTarget = ContextMenuTarget(message, x, y)
                    },
                    scrollToMessageId = initialMessageId,
                    onScrollToMessageHandled = onInitialMessageHandled,
                    modifier = Modifier.weight(1f),
                )
                }
            }
            ChatInputDock(
                text = draft.text,
                onTextChange = viewModel::setDraftText,
                thinkOn = state.thinkingEnabled,
                searchOn = state.searchEnabled,
                onToggleThink = viewModel::toggleThinking,
                onToggleSearch = viewModel::toggleSearch,
                onSend = {
                    attachOpen = false
                    when {
                        draft.attachments.any { it.state == AttachmentState.UPLOADING } ->
                            viewModel.showToast("附件上传中，请稍候", withIcon = false)
                        draft.attachments.any { it.state == AttachmentState.ERROR } ->
                            viewModel.showToast("有附件上传失败，请删除后重试", withIcon = false)
                        else -> viewModel.send()
                    }
                },
                onStop = viewModel::stop,
                streaming = state.streaming,
                voiceMode = voiceMode,
                onToggleVoiceMode = {
                    focusManager.clearFocus()
                    keyboard?.hide()
                    scope.launch { OpenDSApi.setInputMode(if (voiceMode) "Text" else "Voice") }
                },
                attachOpen = attachOpen,
                onToggleAttach = {
                    attachOpen = !attachOpen
                    if (attachOpen) {
                        focusManager.clearFocus()
                        keyboard?.hide()
                    }
                    if (attachOpen && !mediaPermissionGranted && !mediaPermissionRequested) {
                        mediaPermissionRequested = true
                        mediaPermissionLauncher.launch(requiredMediaPermission())
                    }
                },
                attachments = draft.attachments,
                selectedPhotos = selectedPhotoUris,
                onSelectPhoto = onSelectRecentPhoto,
                onRemoveAttachment = viewModel::removeAttachment,
                onRetryAttachment = viewModel::retryAttachment,
                recentPhotos = recentPhotos,
                onCamera = { requestCameraCapture() },
                onPhoto = {
                    attachOpen = false
                    photoLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                    )
                },
                onFile = {
                    attachOpen = false
                    documentLauncher.launch(arrayOf("*/*"))
                },
                onVoiceGestureStart = {
                    focusManager.clearFocus()
                    keyboard?.hide()
                    voiceCancelZone = false
                    voiceStartedAt = android.os.SystemClock.elapsedRealtime() - 250L
                    requestRecord()
                },
                onVoiceCancelChange = { cancelled -> voiceCancelZone = cancelled },
                onVoiceGestureEnd = { cancelled ->
                    val elapsed = android.os.SystemClock.elapsedRealtime() - voiceStartedAt
                    viewModel.stopRecording(cancelled || elapsed < 400L)
                    voiceCancelZone = false
                },
                showFixedHint = state.hasMessages && !imeVisible,
                imeVisible = imeVisible,
                onFocusedChange = { focused -> if (focused && attachOpen) attachOpen = false },
                modifier = (if (imeVisible) Modifier else Modifier.navigationBarsPadding())
                    .then(if (voiceOverlayVisible) Modifier.alpha(0f) else Modifier),
            )
        } else {
            val shareDensity = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(
                    density = shareDensity.density,
                    fontScale = (shareDensity.fontScale * OpenDSApi.fontSizeScale(fontSizeLevel))
                        .coerceIn(0.85f, 2.0f),
                ),
            ) {
            ShareSelectionScreen(
                groups = shareGroups,
                selectedKeys = shareSelected,
                initialKey = shareAnchorId,
                onToggleGroup = { key ->
                    shareSelected = if (key in shareSelected) shareSelected - key else shareSelected + key
                },
                onToggleAll = {
                    val enabledKeys = shareGroups.filter { it.enabled }.map { it.key }.toSet()
                    shareSelected = if (enabledKeys.isNotEmpty() && shareSelected.containsAll(enabledKeys)) {
                        emptySet()
                    } else {
                        enabledKeys
                    }
                },
                onGenerateImage = { generateShareImage() },
                generating = shareGenerating,
                onCopyLink = {
                    val target = state.sessionId
                    if (target == null) {
                        viewModel.showToast("暂无可分享的链接", withIcon = false)
                    } else {
                        copyShareLink(context, target)
                        viewModel.showToast("分享链接已复制", withIcon = false)
                    }
                },
                onMoreShare = {
                    val selectedGroups = shareGroups.filter { it.key in shareSelected }
                    val text = selectedGroups.joinToString("\n\n") { group ->
                        val request = group.userMessage?.fragments
                            ?.filterIsInstance<Fragment.Request>()
                            ?.joinToString("\n") { it.content }
                            .orEmpty()
                        val answer = group.assistantMessage?.let { ChatViewModel.replyText(it) }.orEmpty()
                        listOf(request, answer).filter { it.isNotBlank() }.joinToString("\n")
                    }
                    if (text.isBlank()) {
                        viewModel.showToast("暂无可分享的内容", withIcon = false)
                    } else if (!shareTextContent(context, text)) {
                        viewModel.showToast("分享失败", withIcon = false)
                    }
                },
                onClose = {
                    shareAnchorId = null
                    shareSelected = emptySet()
                },
                modifier = Modifier.fillMaxSize(),
            )
            }
        }
        sharePreview?.let { bitmap ->
            ShareImagePreviewSheet(
                bitmap = bitmap,
                onClose = { sharePreview = null },
                onSave = {
                    if (saveShareBitmap(context, bitmap)) {
                        viewModel.showToast("已保存到相册", withIcon = false)
                    } else {
                        viewModel.showToast("保存失败", withIcon = false)
                    }
                },
                onShare = {
                    if (!shareShareBitmap(context, bitmap)) {
                        viewModel.showToast("分享失败", withIcon = false)
                    }
                },
            )
        }
        if (shareAnchorId != null) {
            BackHandler {
                if (sharePreview != null) {
                    sharePreview = null
                } else {
                    shareAnchorId = null
                    shareSelected = emptySet()
                }
            }
        }
        if (voiceOverlayVisible) {
            val phase = when {
                voice.transcribing -> VoiceOverlayPhase.RECOGNIZING
                voiceCancelZone -> VoiceOverlayPhase.CANCEL
                else -> VoiceOverlayPhase.RECORDING
            }
            Box(modifier = Modifier.fillMaxSize()) {
                VoiceInputOverlay(
                    phase = phase,
                    remainingSeconds = (60 - voice.recordingSeconds).coerceAtLeast(0),
                    amplitudes = voice.amplitudes,
                )
                if (voice.transcribing) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickableNoIndication { viewModel.cancelTranscription() },
                    )
                } else {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                awaitPointerEventScope {
                                    while (true) {
                                        awaitPointerEvent().changes.forEach { it.consume() }
                                    }
                                }
                            },
                    )
                }
            }
        }
        if (micDialogVisible) {
            MicPermissionDialog(
                onDismiss = { micDialogVisible = false },
                onOpenSettings = {
                    micDialogVisible = false
                    runCatching {
                        context.startActivity(
                            Intent(
                                android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", context.packageName, null),
                            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                        )
                    }
                },
            )
        }
        ToastHost(
            toast = state.toast,
            onDismiss = viewModel::dismissToast,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding(),
        )
        SourcesSheet(
            sources = sourcesSheet,
            onClose = { sourcesSheet = null },
        )
        if (attachOpen) {
            BackHandler { attachOpen = false }
        }
        if (selectionMessageId != null) {
            BackHandler { selectionMessageId = null }
        }
        contextMenuTarget?.let { target ->
            val isUser = target.message.role == Role.USER
            val messageText = if (isUser) {
                requestMessageText(target.message)
            } else {
                ChatViewModel.replyText(target.message)
            }
            val regenerateTarget = if (isUser) target.message.id else target.message.parentId
            val items = buildList {
                add(MessageMenuItem(AppIcons.copy, "复制") { copyText(messageText) })
                if (isUser) {
                    add(MessageMenuItem(AppIcons.edit, "编辑提问") { editRequestId = target.message.id })
                } else {
                    add(MessageMenuItem(AppIcons.selectText, "选择文本") {
                        selectionMessageId = target.message.id
                    })
                }
                if (regenerateTarget != null) {
                    add(MessageMenuItem(AppIcons.refresh, "重新生成") {
                        viewModel.regenerateFrom(regenerateTarget)
                    })
                }
                add(MessageMenuItem(AppIcons.like, "喜欢") { viewModel.showToast("感谢反馈", withIcon = false) })
                add(MessageMenuItem(AppIcons.dislike, "不喜欢") { viewModel.showToast("感谢反馈", withIcon = false) })
                if (!isUser && messageText.isNotBlank()) {
                    add(MessageMenuItem(AppIcons.play, "朗读") {
                        viewModel.toggleReadAloud(target.message.id, messageText)
                    })
                }
                if (!isUser && messageText.isNotBlank()) {
                    add(MessageMenuItem(AppIcons.share, "分享") { enterShareMode(target.message) })
                }
                add(MessageMenuItem(AppIcons.report, "举报") { viewModel.showToast("已收到反馈", withIcon = false) })
            }
            MessageContextMenu(
                x = target.x,
                y = target.y,
                items = items,
                onDismiss = { contextMenuTarget = null },
            )
        }
    }
}

private fun requiredMediaPermission(): String =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

private fun requestMessageText(message: ChatMessage): String = message.fragments
    .filterIsInstance<Fragment.Request>()
    .joinToString("\n") { it.content }

private data class ContextMenuTarget(val message: ChatMessage, val x: Int, val y: Int)

private const val MaxAttachmentCount = 20
private const val MaxAttachmentBytes = 100L * 1024L * 1024L

private fun queryAttachmentSize(context: android.content.Context, uri: Uri): Long = runCatching {
    context.contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) cursor.getLong(0) else 0L
    } ?: 0L
}.getOrDefault(0L)

private class CameraStarter {
    var start: (() -> Unit)? = null
}
