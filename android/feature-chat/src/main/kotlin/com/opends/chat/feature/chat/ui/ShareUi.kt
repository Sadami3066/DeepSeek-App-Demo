package com.opends.chat.feature.chat.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.opends.chat.core.ChatMessage
import com.opends.chat.core.Fragment
import com.opends.chat.core.MessageStatus
import com.opends.chat.core.Role
import com.opends.chat.design.R
import com.opends.chat.design.theme.OdDimens
import com.opends.chat.design.theme.OdTheme
import com.opends.chat.render.ShareImageContent
import com.opends.chat.render.ShareImageGroup
import java.io.File

internal data class ShareMessageGroup(
    val key: Long,
    val userMessage: ChatMessage?,
    val assistantMessage: ChatMessage?,
) {
    val enabled: Boolean
        get() {
            val assistant = assistantMessage ?: return false
            if (assistant.status == MessageStatus.WIP) return false
            if (assistant.status == MessageStatus.CONTENT_FILTER) return false
            val steps = buildReplySteps(assistant, streaming = false)
            return steps.answer.isNotBlank() || steps.steps.isNotEmpty()
        }
}

internal fun buildShareGroups(messages: List<ChatMessage>): List<ShareMessageGroup> {
    val groups = ArrayList<ShareMessageGroup>()
    var index = 0
    while (index < messages.size) {
        val current = messages[index]
        if (current.role == Role.USER) {
            val next = messages.getOrNull(index + 1)?.takeIf { it.role == Role.ASSISTANT }
            groups.add(ShareMessageGroup(current.id, current, next))
            index += if (next == null) 1 else 2
        } else {
            groups.add(ShareMessageGroup(current.id, null, current))
            index += 1
        }
    }
    return groups
}

internal fun groupOfMessage(messages: List<ChatMessage>, messageId: Long): ShareMessageGroup? =
    buildShareGroups(messages).firstOrNull { group ->
        group.userMessage?.id == messageId || group.assistantMessage?.id == messageId
    }

internal fun buildShareImageContent(
    sessionId: String?,
    groups: List<ShareMessageGroup>,
    dark: Boolean,
): ShareImageContent {
    val items = groups.mapNotNull { group ->
        val assistant = group.assistantMessage
        val userText = group.userMessage?.fragments
            ?.filterIsInstance<Fragment.Request>()
            ?.joinToString("\n") { it.content }
            ?.takeIf { it.isNotBlank() }
        val steps = assistant?.let { buildReplySteps(it, streaming = false) }
        val thinkingSeconds = steps?.steps
            ?.filterIsInstance<ReplyStep.Thinking>()
            ?.mapNotNull { it.elapsedSeconds?.toInt() }
            ?.takeIf { it.isNotEmpty() }
            ?.sum()
        val stepLabels = steps?.steps?.mapNotNull { step ->
            when (step) {
                is ReplyStep.Search -> step.searchLabel(streaming = false, queryIndex = 0).takeIf {
                    step.status == ToolStatus.FINISHED
                }
                is ReplyStep.Open -> step.openLabel(streaming = false).takeIf {
                    step.status == ToolStatus.FINISHED
                }
                is ReplyStep.Find -> step.findLabel(streaming = false)
                is ReplyStep.ReadingLink -> null
                is ReplyStep.Thinking -> null
            }
        }.orEmpty()
        val markdown = steps?.answer?.takeIf { it.isNotBlank() }
        if (userText == null && markdown == null && thinkingSeconds == null && stepLabels.isEmpty()) {
            null
        } else {
            ShareImageGroup(
                userText = userText,
                assistantMarkdown = markdown,
                thinkingSeconds = thinkingSeconds,
                steps = stepLabels,
            )
        }
    }
    return ShareImageContent(
        groups = items,
        dark = dark,
        qrPayload = sessionId?.let { "opends://session/$it" }.orEmpty(),
    )
}

internal fun shareSessionLink(sessionId: String): String = "opends://session/$sessionId"

internal fun copyShareLink(context: Context, sessionId: String) {
    val clipboard = context.getSystemService(ClipboardManager::class.java) ?: return
    clipboard.setPrimaryClip(ClipData.newPlainText("link", shareSessionLink(sessionId)))
}

internal fun saveShareBitmap(context: Context, bitmap: Bitmap): Boolean = runCatching {
    val resolver = context.contentResolver
    val name = "deepseek_share_${System.currentTimeMillis()}.png"
    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, name)
        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/DeepSeek")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
    }
    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return false
    resolver.openOutputStream(uri)?.use { output ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
    } ?: return false
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        values.clear()
        values.put(MediaStore.Images.Media.IS_PENDING, 0)
        resolver.update(uri, values, null, null)
    }
    true
}.getOrDefault(false)

internal fun shareShareBitmap(context: Context, bitmap: Bitmap): Boolean = runCatching {
    val directory = File(context.cacheDir, "share").apply { mkdirs() }
    val file = File(directory, "deepseek_share_${System.currentTimeMillis()}.png")
    file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "分享图片"))
    true
}.getOrDefault(false)

internal fun shareTextContent(context: Context, text: String): Boolean = runCatching {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "分享"))
    true
}.getOrDefault(false)

@Composable
internal fun ShareSelectionScreen(
    groups: List<ShareMessageGroup>,
    selectedKeys: Set<Long>,
    initialKey: Long? = null,
    onToggleGroup: (Long) -> Unit,
    onToggleAll: () -> Unit,
    onGenerateImage: () -> Unit,
    onCopyLink: () -> Unit,
    onMoreShare: () -> Unit,
    onClose: () -> Unit,
    generating: Boolean = false,
    modifier: Modifier = Modifier,
    sheetModifier: Modifier = Modifier,
) {
    val palette = OdTheme.colors
    val enabledGroups = groups.filter { it.enabled }
    val allSelected = enabledGroups.isNotEmpty() && enabledGroups.all { it.key in selectedKeys }
    val initialIndex = remember(initialKey) {
        val index = groups.indexOfFirst { it.key == initialKey }
        if (index > 0) index - 1 else 0
    }
    val listState = androidx.compose.foundation.lazy.rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(palette.bgBase)
                .statusBarsPadding(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(start = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SelectionCheckbox(
                    selected = allSelected,
                    enabled = enabledGroups.isNotEmpty(),
                    onClick = onToggleAll,
                )
                Spacer(Modifier.width(10.3.dp))
                Text(
                    text = "全选",
                    color = palette.labelPrimary,
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                )
            }
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(top = 4.dp, bottom = 172.dp),
            ) {
                items(groups, key = { it.key }) { group ->
                    ShareGroupRow(
                        group = group,
                        selected = group.key in selectedKeys,
                        onToggle = { onToggleGroup(group.key) },
                    )
                }
            }
        }
        ShareSelectionSheet(
            selectedCount = selectedKeys.size,
            canShare = selectedKeys.isNotEmpty() && !generating,
            generating = generating,
            onGenerateImage = onGenerateImage,
            onCopyLink = onCopyLink,
            onMoreShare = onMoreShare,
            onClose = onClose,
            modifier = sheetModifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun ShareGroupRow(
    group: ShareMessageGroup,
    selected: Boolean,
    onToggle: () -> Unit,
) {
    val palette = OdTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 22.15.dp, top = 14.dp),
    ) {
        SelectionCheckbox(
            selected = selected,
            enabled = group.enabled,
            onClick = onToggle,
            modifier = Modifier.padding(top = 2.dp),
        )
        Spacer(Modifier.width(19.8.dp))
        Column(Modifier.weight(1f)) {
            group.userMessage?.let { user ->
                val text = user.fragments.filterIsInstance<Fragment.Request>().joinToString("\n") { it.content }
                if (text.isNotBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(palette.bubble)
                                .padding(horizontal = 14.46.dp, vertical = 9.23.dp),
                        ) {
                            Text(text = text, color = palette.bubbleText, fontSize = 16.sp, lineHeight = 24.sp)
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                }
            }
            val assistant = group.assistantMessage ?: return@Column
            val steps = buildReplySteps(assistant, streaming = false)
            steps.steps.forEach { step ->
                val label = when (step) {
                    is ReplyStep.Thinking -> step.elapsedSeconds?.let { "已思考（用时 ${it.toInt()} 秒）" } ?: "深度思考"
                    is ReplyStep.Search -> step.searchLabel(streaming = false, queryIndex = 0)
                    is ReplyStep.Open -> step.openLabel(streaming = false)
                    is ReplyStep.Find -> step.findLabel(streaming = false)
                    is ReplyStep.ReadingLink -> null
                }
                if (label != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(text = label, color = palette.labelSecondary, fontSize = 14.sp, lineHeight = 22.sp)
                    }
                }
            }
            if (steps.answer.isNotBlank()) {
                AssistantMarkdown(text = steps.answer, isStreaming = false)
            }
            if (steps.answer.isNotBlank()) {
                Spacer(Modifier.height(13.dp))
                Text(
                    text = "本回答由 AI 生成，内容仅供参考，请仔细甄别",
                    color = palette.labelSecondary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                )
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun SelectionCheckbox(
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = OdTheme.colors
    val tint = when {
        !enabled -> palette.labelDimmed.copy(alpha = 0.5f)
        selected -> palette.brandLogo
        else -> palette.labelDimmed
    }
    Box(
        modifier = modifier
            .size(19.dp)
            .clip(CircleShape)
            .background(if (selected && enabled) palette.brandLogo else Color.Transparent)
            .border(1.5.dp, tint, CircleShape)
            .clickable(
                interactionSource = remember2(),
                indication = null,
                enabled = enabled,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (selected && enabled) {
            Icon(
                imageVector = AppIcons.check,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = Color.White,
            )
        }
    }
}

@Composable
private fun remember2(): MutableInteractionSource =
    androidx.compose.runtime.remember { MutableInteractionSource() }

@Composable
private fun ShareSelectionSheet(
    selectedCount: Int,
    canShare: Boolean,
    generating: Boolean,
    onGenerateImage: () -> Unit,
    onCopyLink: () -> Unit,
    onMoreShare: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = OdTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(palette.menu)
            .navigationBarsPadding()
            .padding(top = 14.dp, bottom = 15.6.dp),
    ) {
        Box(Modifier.fillMaxWidth().height(26.dp)) {
            Text(
                text = "已选 $selectedCount 组",
                modifier = Modifier.align(Alignment.Center),
                color = palette.labelPrimary,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Medium,
            )
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 10.dp)
                    .size(24.dp)
                    .clickable(
                        interactionSource = remember2(),
                        indication = null,
                        onClick = onClose,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                OdIcon(R.drawable.ic_close_outline_24, 20.dp, palette.labelPrimary)
            }
        }
        Spacer(Modifier.height(10.6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
        ) {
            ShareActionItem(
                label = if (generating) "生成中..." else "生成图片",
                circleColor = Color(0xFF52A0FF),
                icon = AppIcons.photo,
                enabled = canShare,
                loading = generating,
                onClick = onGenerateImage,
                modifier = Modifier.weight(1f),
            )
            ShareActionItem(
                label = "复制链接",
                circleColor = Color(0xFF4E77FF),
                icon = AppIcons.link,
                enabled = canShare,
                onClick = onCopyLink,
                modifier = Modifier.weight(1f),
            )
            ShareActionItem(
                label = "更多分享",
                circleColor = Color(0xFFE0EDFF),
                icon = AppIcons.more,
                iconTint = palette.brandPrimary,
                enabled = canShare,
                onClick = onMoreShare,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ShareActionItem(
    label: String,
    circleColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconTint: Color = Color.White,
    loading: Boolean = false,
) {
    val palette = OdTheme.colors
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (enabled) circleColor else circleColor.copy(alpha = 0.45f))
                .clickable(
                    interactionSource = remember2(),
                    indication = null,
                    enabled = enabled,
                    onClick = onClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (loading) {
                LoadingSpinner(size = 20.dp, color = iconTint)
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = iconTint,
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            color = palette.labelPrimary,
            fontSize = 12.sp,
            lineHeight = 18.sp,
        )
    }
}

@Composable
internal fun ShareImagePreviewSheet(
    bitmap: Bitmap,
    onClose: () -> Unit,
    onSave: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = OdTheme.colors
    Box(modifier = modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxSize()
                .background(palette.scrimSheet)
                .clickable(
                    interactionSource = remember2(),
                    indication = null,
                    onClick = onClose,
                ),
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxSize()
                .padding(top = 73.dp)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(palette.bgLayer2),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(15.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    Modifier
                        .width(32.dp)
                        .height(4.2.dp)
                        .clip(CircleShape)
                        .background(palette.iconWeak),
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                ) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 29.6.dp)
                            .padding(top = 11.dp, bottom = 4.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(palette.bgBase),
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 29.6.dp, end = 19.dp, top = 12.dp, bottom = 15.dp)
                    .navigationBarsPadding()
                    .height(44.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier
                        .width(93.6.dp)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(palette.bgBase)
                        .border(OdDimens.inputBorder, palette.borderL1, CircleShape)
                        .clickable(
                            interactionSource = remember2(),
                            indication = null,
                            onClick = onShare,
                        ),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = AppIcons.share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = palette.labelPrimary,
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(text = "分享", color = palette.labelPrimary, fontSize = 15.sp, lineHeight = 20.sp)
                }
                Spacer(Modifier.width(4.2.dp))
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(palette.brandPrimary)
                        .clickable(
                            interactionSource = remember2(),
                            indication = null,
                            onClick = onSave,
                        ),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_download_outline_20),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color.White,
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(text = "保存图片", color = Color.White, fontSize = 15.sp, lineHeight = 20.sp)
                }
            }
        }
    }
}

@Composable
private fun shareHeaderText(title: String?, modifier: Modifier = Modifier) {
    if (title.isNullOrBlank()) return
    Text(
        text = title,
        modifier = modifier,
        color = OdTheme.colors.labelSecondary,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}
