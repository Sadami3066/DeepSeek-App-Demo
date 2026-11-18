package com.opends.chat.feature.shell

import android.content.ClipData
import android.content.ClipboardManager
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opends.chat.core.AppSettings
import com.opends.chat.core.ErrorLog
import com.opends.chat.core.ImportResult
import com.opends.chat.core.OpenDSApi
import com.opends.chat.core.OriginalAppSource
import com.opends.chat.core.ThemeMode
import com.opends.chat.design.R
import com.opends.chat.design.icon.OfficialIcons
import com.opends.chat.design.theme.OdDimens
import com.opends.chat.design.theme.OdMotion
import com.opends.chat.design.theme.OdTheme
import com.opends.chat.design.theme.OdType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class SettingsPage { HOME, DATA, FONT_SIZE, PERSONALIZATION, AGREEMENT, ERROR_LOG }
private enum class SettingsDialog { NONE, LANGUAGE, APPEARANCE }

@Composable
fun SettingsRoute(onBack: () -> Unit) {
    val settings = rememberSettingsState()
    val strings = shellStrings(settings.languageTag)
    val colors = OdTheme.colors
    val store = remember { runningSettings() }
    val archive = remember { runningArchive() }
    val version = rememberAppVersion()
    val toast = rememberShellToast()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var pageName by rememberSaveable { mutableStateOf(SettingsPage.HOME.name) }
    var dialogName by rememberSaveable { mutableStateOf(SettingsDialog.NONE.name) }
    val page = remember(pageName) { SettingsPage.valueOf(pageName) }
    val dialog = remember(dialogName) { SettingsDialog.valueOf(dialogName) }

    var voiceSheetVisible by remember { mutableStateOf(false) }
    var apiKeyDialogVisible by remember { mutableStateOf(false) }
    var keyVerifying by remember { mutableStateOf(false) }
    var keyError by remember { mutableStateOf<String?>(null) }

    var busyText by remember { mutableStateOf<String?>(null) }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
    var showImportConfirm by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    var updateChecking by remember { mutableStateOf(false) }
    var updateRelease by remember { mutableStateOf<UpdateRelease?>(null) }
    var updateDownloading by remember { mutableStateOf(false) }
    var updateProgress by remember { mutableStateOf<Int?>(null) }
    var updateJob by remember { mutableStateOf<Job?>(null) }
    var silentUpdateChecked by remember { mutableStateOf(false) }

    fun updateSettings(transform: (AppSettings) -> AppSettings) {
        val current = runningSettings() ?: return
        scope.launch {
            runCatching { current.update(transform(settings)) }
                .onFailure { toast.show(strings.operationFailed, true) }
        }
    }

    fun importMessage(result: ImportResult): String {
        val separator = if (strings.english) ", " else "，"
        val parts = mutableListOf(strings.importDetail(result.sessions, result.messages, result.images))
        if (result.skipped > 0) {
            parts += strings.importSkipped(result.skipped)
        }
        if (result.warnings.isNotEmpty()) {
            parts += strings.importWarnings()
        }
        return parts.joinToString(separator)
    }

    fun importAny(uri: Uri) {
        scope.launch {
            busyText = strings.processing
            val name = uriFileName(context, uri)
            val workDir = java.io.File(context.cacheDir, "imports").apply { mkdirs() }
            val tempFile = java.io.File(workDir, "import-${System.nanoTime()}")
            var size = 0L
            try {
                copyUriToFile(context, uri, tempFile) ?: error(strings.importFailed)
                size = tempFile.length()
                val header = readFileHeader(tempFile)
                val result = when (detectImportKind(name, header)) {
                    ImportKind.ZIP -> archive?.importZipFile(tempFile, name) ?: error(strings.importFailed)
                    ImportKind.DATABASE -> archive
                        ?.importFromOriginalApp(OriginalAppSource.DatabaseBytes(name, tempFile.readBytes()))
                        ?: error(strings.importFailed)
                    null -> null
                }
                if (result == null) {
                    ErrorLog.record(
                        "导入",
                        "无法识别导入文件：$name",
                        "size=$size header=${header.joinToString(",")}",
                    )
                    toast.show(strings.importFailed, true)
                } else {
                    toast.show(importMessage(result))
                }
            } catch (error: Throwable) {
                ErrorLog.record("导入", "导入失败：$name", "size=$size", error)
                toast.show(strings.importFailedReason(OpenDSApi.describeError(error)), true)
            } finally {
                runCatching { tempFile.delete() }
                busyText = null
            }
        }
    }

    fun copyErrorLog() {
        runCatching {
            val clipboard = context.getSystemService(ClipboardManager::class.java)
            clipboard?.setPrimaryClip(ClipData.newPlainText("DeepFish error log", ErrorLog.text()))
        }
        toast.show(strings.copied)
    }

    fun exportTo(uri: Uri) {
        scope.launch {
            busyText = strings.processing
            try {
                val service = archive ?: error(strings.exportFailed)
                withContext(Dispatchers.IO) {
                    val stream = context.contentResolver.openOutputStream(uri)
                        ?: throw IllegalStateException(strings.exportFailed)
                    stream.use { out -> service.exportZipTo(out) }
                }
                toast.show(strings.exportSuccess)
            } catch (error: Throwable) {
                ErrorLog.record("导出", "导出失败", null, error)
                toast.show(strings.exportFailedReason(OpenDSApi.describeError(error)), true)
            } finally {
                busyText = null
            }
        }
    }

    fun deleteAllChats() {
        scope.launch {
            busyText = strings.processing
            val cleared = runCatching { OpenDSApi.clearAllSessions() }.getOrDefault(false)
            busyText = null
            if (cleared) {
                toast.show(strings.deleteSuccess)
            } else {
                toast.show(strings.deleteFailed, true)
            }
        }
    }

    fun launchUpdateCheck(manual: Boolean) {
        if (updateChecking) return
        updateChecking = true
        scope.launch {
            val result = fetchLatestRelease()
            updateChecking = false
            result.onSuccess { release ->
                if (isRemoteNewer(release.version, currentVersionName(context))) {
                    updateRelease = release
                    if (!manual) {
                        markUpdatePrompted(context, release.version)
                    }
                } else if (manual) {
                    toast.show(strings.upToDate)
                }
            }.onFailure {
                if (manual) {
                    toast.show(strings.checkUpdateFailed, true)
                }
            }
        }
    }

    fun startUpdateDownload(release: UpdateRelease) {
        if (updateDownloading) return
        updateDownloading = true
        updateProgress = 0
        updateJob = scope.launch {
            try {
                val file = downloadApk(context, release.asset, release.version) { percent ->
                    updateProgress = percent
                }
                updateRelease = null
                if (installApk(context, file).isFailure) {
                    toast.show(strings.downloadFailed, true)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Throwable) {
                updateRelease = null
                toast.show(strings.downloadFailed, true)
            } finally {
                updateDownloading = false
                updateProgress = null
                updateJob = null
            }
        }
    }

    fun cancelUpdateDownload() {
        updateJob?.cancel()
        updateJob = null
        updateDownloading = false
        updateProgress = null
    }

    LaunchedEffect(Unit) {
        if (silentUpdateChecked) return@LaunchedEffect
        silentUpdateChecked = true
        val release = fetchLatestRelease().getOrNull() ?: return@LaunchedEffect
        if (isRemoteNewer(release.version, currentVersionName(context)) &&
            shouldPromptUpdate(context, release.version)
        ) {
            markUpdatePrompted(context, release.version)
            toast.show(strings.silentUpdateFound(release.version))
        }
    }

    val importPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            pendingImportUri = uri
            showImportConfirm = true
        }
    }

    val exportCreator = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        if (uri != null) {
            exportTo(uri)
        }
    }

    BackHandler(
        enabled = voiceSheetVisible ||
            apiKeyDialogVisible ||
            page != SettingsPage.HOME ||
            dialog != SettingsDialog.NONE ||
            showImportConfirm ||
            showDeleteConfirm ||
            updateRelease != null,
    ) {
        when {
            voiceSheetVisible -> {
                voiceSheetVisible = false
                runCatching { com.opends.chat.core.OpenDS.voice.stopSpeaking() }
            }
            apiKeyDialogVisible -> apiKeyDialogVisible = false
            updateRelease != null -> if (!updateDownloading) updateRelease = null
            showDeleteConfirm -> showDeleteConfirm = false
            showImportConfirm -> {
                showImportConfirm = false
                pendingImportUri = null
            }
            dialog != SettingsDialog.NONE -> dialogName = SettingsDialog.NONE.name
            else -> pageName = SettingsPage.HOME.name
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgLayer2),
    ) {
        AnimatedContent(
            targetState = page,
            transitionSpec = {
                if (targetState != SettingsPage.HOME) {
                    (slideInHorizontally(tween(OdMotion.slideMs, easing = OdMotion.decelerate)) { it } +
                        fadeIn(tween(OdMotion.slideMs))) togetherWith
                        (slideOutHorizontally(tween(OdMotion.slideMs, easing = OdMotion.accelerate)) { -it / 4 } +
                            fadeOut(tween(OdMotion.fadeMs)))
                } else {
                    (slideInHorizontally(tween(OdMotion.slideMs, easing = OdMotion.decelerate)) { -it / 4 } +
                        fadeIn(tween(OdMotion.slideMs))) togetherWith
                        (slideOutHorizontally(tween(OdMotion.slideMs, easing = OdMotion.accelerate)) { it } +
                            fadeOut(tween(OdMotion.fadeMs)))
                }
            },
            label = "settingsPage",
        ) { target ->
            when (target) {
                SettingsPage.HOME -> SettingsHome(
                    strings = strings,
                    settings = settings,
                    version = version,
                    checkingUpdate = updateChecking,
                    onBack = onBack,
                    onOpenApiKey = { apiKeyDialogVisible = true },
                    onOpenData = { pageName = SettingsPage.DATA.name },
                    onOpenLanguage = { dialogName = SettingsDialog.LANGUAGE.name },
                    onOpenAppearance = { dialogName = SettingsDialog.APPEARANCE.name },
                    onOpenFontSize = { pageName = SettingsPage.FONT_SIZE.name },
                    onOpenPersonalization = { pageName = SettingsPage.PERSONALIZATION.name },
                    onOpenVoice = { voiceSheetVisible = true },
                    onCheckUpdate = { launchUpdateCheck(manual = true) },
                    onOpenAgreement = { pageName = SettingsPage.AGREEMENT.name },
                    onOpenErrorLog = { pageName = SettingsPage.ERROR_LOG.name },
                )

                SettingsPage.DATA -> DataManagementPage(
                    strings = strings,
                    onImport = { importPicker.launch(arrayOf("*/*")) },
                    onExport = {
                        exportCreator.launch(
                            "DeepFish_data-${
                                SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
                            }.zip",
                        )
                    },
                    onDeleteAll = { showDeleteConfirm = true },
                    onBack = { pageName = SettingsPage.HOME.name },
                )

                SettingsPage.FONT_SIZE -> FontSizePage(
                    strings = strings,
                    level = settings.fontSizeLevel,
                    onLevelChange = { level -> updateSettings { it.copy(fontSizeLevel = level) } },
                    onBack = { pageName = SettingsPage.HOME.name },
                )

                SettingsPage.PERSONALIZATION -> PersonalizationPage(
                    strings = strings,
                    autoFold = settings.thinkingAutoFold,
                    onAutoFoldChange = { enabled ->
                        updateSettings { it.copy(thinkingAutoFold = enabled) }
                    },
                    onBack = { pageName = SettingsPage.HOME.name },
                )

                SettingsPage.AGREEMENT -> AgreementPage(
                    strings = strings,
                    onBack = { pageName = SettingsPage.HOME.name },
                )

                SettingsPage.ERROR_LOG -> ErrorLogPage(
                    strings = strings,
                    onCopy = { copyErrorLog() },
                    onClear = {
                        ErrorLog.clear()
                        toast.show(strings.logCleared)
                    },
                    onBack = { pageName = SettingsPage.HOME.name },
                )

            }
        }

        when (dialog) {
            SettingsDialog.LANGUAGE -> ShellRadioDialog(
                title = strings.language,
                options = listOf(strings.languageSystem, strings.langZh, strings.langEn),
                selectedIndex = if (settings.languageTag.startsWith("en")) 2 else 1,
                onDismiss = { dialogName = SettingsDialog.NONE.name },
                onConfirm = { index ->
                    dialogName = SettingsDialog.NONE.name
                    val tag = when (index) {
                        0 -> if (Locale.getDefault().language == "zh") "zh-CN" else "en-US"
                        2 -> "en-US"
                        else -> "zh-CN"
                    }
                    updateSettings { it.copy(languageTag = tag) }
                },
                iconRes = R.drawable.ic_globe_language_regular,
                rowHeight = 46.dp,
                confirmLabel = strings.confirm,
                width = 238.9.dp,
            )

            SettingsDialog.APPEARANCE -> ShellRadioDialog(
                title = strings.appearance,
                options = listOf(strings.themeSystem, strings.themeLight, strings.themeDark),
                selectedIndex = when (settings.theme) {
                    ThemeMode.SYSTEM -> 0
                    ThemeMode.LIGHT -> 1
                    ThemeMode.DARK -> 2
                },
                onDismiss = { dialogName = SettingsDialog.NONE.name },
                onConfirm = { index ->
                    dialogName = SettingsDialog.NONE.name
                    val mode = when (index) {
                        1 -> ThemeMode.LIGHT
                        2 -> ThemeMode.DARK
                        else -> ThemeMode.SYSTEM
                    }
                    updateSettings { it.copy(theme = mode) }
                },
                confirmLabel = strings.confirm,
            )

            SettingsDialog.NONE -> Unit
        }

        VoiceSheet(
            visible = voiceSheetVisible,
            strings = strings,
            currentVoiceId = settings.ttsVoice,
            onDismiss = {
                voiceSheetVisible = false
                runCatching { com.opends.chat.core.OpenDS.voice.stopSpeaking() }
            },
            onConfirm = { voiceId ->
                voiceSheetVisible = false
                runCatching { com.opends.chat.core.OpenDS.voice.stopSpeaking() }
                updateSettings { it.copy(ttsVoice = voiceId) }
                val name = ShellVoices.firstOrNull { it.voiceId == voiceId }?.nameOf(strings).orEmpty()
                toast.show(strings.voiceSwitched(name))
            },
            onAudition = { voiceId ->
                scope.launch {
                    runCatching {
                        com.opends.chat.core.OpenDS.voice.stopSpeaking()
                        com.opends.chat.core.OpenDS.voice.speak("你好，我是 DeepFish。", voiceId)
                    }
                }
            },
        )

        if (apiKeyDialogVisible) {
            ApiKeyDialog(
                strings = strings,
                hasKey = settings.apiKey.isNotEmpty(),
                verifying = keyVerifying,
                error = keyError,
                onDismiss = {
                    if (!keyVerifying) {
                        apiKeyDialogVisible = false
                        keyError = null
                    }
                },
                onSave = { key ->
                    if (!keyVerifying) {
                        scope.launch {
                            keyVerifying = true
                            keyError = null
                            val verification = OpenDSApi.verifyApiKey(key)
                            if (verification.isFailure) {
                                keyError = strings.verifyFailed(
                                    verification.exceptionOrNull()?.message.orEmpty(),
                                )
                            } else {
                                val saved = runCatching { store?.setApiKey(key) ?: error("unavailable") }
                                if (saved.isSuccess) {
                                    keyVerifying = false
                                    apiKeyDialogVisible = false
                                    toast.show(strings.apiKeySavedToast)
                                } else {
                                    keyError = strings.operationFailed
                                }
                            }
                            keyVerifying = false
                        }
                    }
                },
            )
        }

        if (showImportConfirm) {
            ShellConfirmDialog(
                title = strings.importConfirmTitle,
                message = strings.importConfirmDesc,
                confirmLabel = strings.confirm,
                cancelLabel = strings.cancel,
                onDismiss = {
                    showImportConfirm = false
                    pendingImportUri = null
                },
                onConfirm = {
                    showImportConfirm = false
                    val uri = pendingImportUri
                    pendingImportUri = null
                    if (uri != null) {
                        importAny(uri)
                    }
                },
            )
        }

        if (showDeleteConfirm) {
            ShellConfirmDialog(
                title = strings.deleteAllChats,
                message = strings.deleteAllChatsDesc,
                confirmLabel = strings.delete,
                cancelLabel = strings.cancel,
                onDismiss = { showDeleteConfirm = false },
                onConfirm = {
                    showDeleteConfirm = false
                    deleteAllChats()
                },
            )
        }

        busyText?.let { ShellBusyDialog(it) }

        updateRelease?.let { release ->
            UpdateAvailableDialog(
                strings = strings,
                release = release,
                downloading = updateDownloading,
                progress = updateProgress,
                onLater = {
                    if (!updateDownloading) {
                        updateRelease = null
                    }
                },
                onUpdate = { startUpdateDownload(release) },
                onCancelDownload = { cancelUpdateDownload() },
            )
        }

        ShellToastHost(state = toast, modifier = Modifier.align(Alignment.TopCenter))
    }
}

@Composable
private fun SettingsHome(
    strings: ShellStrings,
    settings: AppSettings,
    version: String,
    checkingUpdate: Boolean,
    onBack: () -> Unit,
    onOpenApiKey: () -> Unit,
    onOpenData: () -> Unit,
    onOpenLanguage: () -> Unit,
    onOpenAppearance: () -> Unit,
    onOpenFontSize: () -> Unit,
    onOpenPersonalization: () -> Unit,
    onOpenVoice: () -> Unit,
    onCheckUpdate: () -> Unit,
    onOpenAgreement: () -> Unit,
    onOpenErrorLog: () -> Unit,
) {
    val colors = OdTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgLayer2)
            .statusBarsPadding(),
    ) {
        ShellTopBar(title = strings.settings, onBack = onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding(),
        ) {
            SettingsSectionStart(first = true, hasTitle = true)
            SettingsSectionTitle(strings.accountSection)
            SettingsTitleGap()
            SettingsCard {
                SettingsRow(
                    title = strings.apiKeyTitle,
                    icon = { SettingsRowIcon(R.drawable.ic_lock_fill) },
                    value = if (settings.apiKey.isNotEmpty()) strings.apiKeySet else strings.apiKeyUnset,
                    onClick = onOpenApiKey,
                )
                SettingsRow(
                    title = strings.dataManagement,
                    icon = { SettingsRowIcon(R.drawable.ic_data_regular) },
                    onClick = onOpenData,
                )
            }
            SettingsSectionStart(first = false, hasTitle = true)
            SettingsSectionTitle(strings.appSection)
            SettingsTitleGap()
            SettingsCard {
                SettingsRow(
                    title = strings.language,
                    icon = { SettingsRowIcon(R.drawable.ic_globe_language_regular) },
                    value = if (settings.languageTag.startsWith("en")) strings.langEn else strings.langZh,
                    onClick = onOpenLanguage,
                )
                SettingsRow(
                    title = strings.appearance,
                    icon = {
                        SettingsRowIcon(
                            if (OdTheme.isDark) R.drawable.ic_moon_regular else R.drawable.ic_sun_regular,
                        )
                    },
                    value = when (settings.theme) {
                        ThemeMode.SYSTEM -> strings.themeSystem
                        ThemeMode.LIGHT -> strings.themeLight
                        ThemeMode.DARK -> strings.themeDark
                    },
                    onClick = onOpenAppearance,
                )
                SettingsRow(
                    title = strings.fontSize,
                    icon = {
                        Text(
                            text = "AA",
                            fontSize = 15.sp,
                            lineHeight = 18.sp,
                            color = colors.iconPrimary,
                        )
                    },
                    onClick = onOpenFontSize,
                )
                SettingsRow(
                    title = strings.personalization,
                    icon = {
                        Icon(
                            imageVector = OfficialIcons.wand,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            tint = colors.iconPrimary,
                        )
                    },
                    onClick = onOpenPersonalization,
                )
            }
            SettingsSectionStart(first = false, hasTitle = true)
            SettingsSectionTitle(strings.voiceSection)
            SettingsTitleGap()
            SettingsCard {
                SettingsRow(
                    title = strings.voiceName,
                    icon = { SettingsRowIcon(R.drawable.ic_voice_outline_26, 22.dp) },
                    onClick = onOpenVoice,
                )
            }
            SettingsSectionStart(first = false, hasTitle = true)
            SettingsSectionTitle(strings.aboutSection)
            SettingsTitleGap()
            SettingsCard {
                SettingsRow(
                    title = strings.checkUpdate,
                    icon = { SettingsRowIcon(R.drawable.ic_info_outline) },
                    value = if (checkingUpdate) strings.checkingUpdate else version,
                    onClick = onCheckUpdate,
                )
                SettingsRow(
                    title = strings.usageAgreement,
                    icon = { SettingsRowIcon(R.drawable.ic_list_pen_regular) },
                    onClick = onOpenAgreement,
                )
                SettingsRow(
                    title = strings.errorLog,
                    icon = { SettingsRowIcon(R.drawable.ic_data_regular) },
                    onClick = onOpenErrorLog,
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 80.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "${strings.appName} v$version",
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = colors.labelCaption,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = strings.footerText,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = colors.labelCaption,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = strings.thirdPartyNotice,
                    modifier = Modifier.padding(horizontal = 24.dp),
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = colors.labelTertiary,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun ShellBusyDialog(message: String) {
    val colors = OdTheme.colors
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.overlay)
            .shellClickable {},
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .width(OdDimens.dialogWidth)
                .clip(RoundedCornerShape(OdDimens.dialogRadius))
                .background(colors.menu)
                .padding(horizontal = 24.dp, vertical = 22.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = colors.brandPrimary,
                strokeWidth = 2.dp,
            )
            Text(text = message, style = OdType.rowTitle, color = colors.labelPrimary)
        }
    }
}
