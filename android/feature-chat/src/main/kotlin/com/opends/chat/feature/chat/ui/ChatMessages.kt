package com.opends.chat.feature.chat.ui

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opends.chat.core.ChatMessage
import com.opends.chat.core.Fragment
import com.opends.chat.core.MessageStatus
import com.opends.chat.core.Role
import com.opends.chat.core.SearchResult
import com.opends.chat.design.R
import com.opends.chat.design.motion.OdLottieAssets
import com.opends.chat.design.theme.OdDimens
import com.opends.chat.design.theme.OdTheme
import com.opends.chat.design.theme.OdType
import com.opends.chat.feature.chat.ChatViewModel
import com.opends.chat.feature.chat.ReadingState
import com.opends.chat.feature.chat.ReadingStatus

internal val ChatContentStart = 22.15.dp
internal val ChatContentEnd = 22.15.dp

private val BubbleShape = RoundedCornerShape(20.dp)
private val ActionBoxSize = 32.dp
private val ActionIconSize = 18.dp
private val ActionGap = 4.92.dp
private val ActionGapAfterRead = 2.55.dp
private val ReadBoxWidth = 36.9.dp
private val ReadGap = 2.46.dp
private val ActionRowExtra = 11.7.dp
private val ActionRowOffset = 0.8.dp

internal fun requestText(message: ChatMessage): String = message.fragments
    .filterIsInstance<Fragment.Request>()
    .joinToString("\n") { it.content }

internal fun thinkFragment(message: ChatMessage): Fragment.Think? =
    message.fragments.filterIsInstance<Fragment.Think>().firstOrNull { it.content.isNotBlank() }

internal fun replyText(message: ChatMessage): String = ChatViewModel.replyText(message)

internal fun templateText(message: ChatMessage): String = message.fragments
    .filterIsInstance<Fragment.TemplateResponse>()
    .joinToString("\n\n") { it.content }

internal fun searchSources(message: ChatMessage): List<SearchResult> = message.fragments
    .filterIsInstance<Fragment.Search>()
    .flatMap { it.results }
    .distinctBy { it.url }

internal fun statusNotice(message: ChatMessage): String? = when (message.status) {
    MessageStatus.FAILED -> "网络异常，请检查你的网络状况"
    MessageStatus.TIMEOUT -> "请求超时，请稍后再试"
    MessageStatus.CONTEXT_LENGTH_EXCEEDED -> "对话长度已达上限，请开启新对话继续"
    else -> null
}

@Composable
fun UserMessageItem(
    message: ChatMessage,
    editRequested: Boolean,
    busy: Boolean,
    versions: List<Long>?,
    versionDisabled: Boolean,
    onSelectVersion: (Long, Float) -> Unit,
    onSwitcherPosition: (Float) -> Unit,
    onLongPress: (Int, Int) -> Unit,
    onCommitEdit: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = OdTheme.colors
    val text = requestText(message)
    val files = message.fragments.filterIsInstance<Fragment.File>()
    var editing by rememberSaveable(message.id) { mutableStateOf(false) }
    var draft by rememberSaveable(message.id) { mutableStateOf("") }
    var origin by remember { mutableStateOf(Offset.Zero) }
    androidx.compose.runtime.LaunchedEffect(editRequested) {
        if (editRequested) {
            draft = text
            editing = true
        }
    }
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.End,
    ) {
        if (files.isNotEmpty()) {
            MessageFiles(files = files, modifier = Modifier.padding(bottom = 8.dp))
        }
        if (editing) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(BubbleShape)
                    .border(1.dp, palette.brandPrimary, BubbleShape)
                    .background(palette.bgBase)
                    .padding(horizontal = 14.46.dp, vertical = 9.23.dp),
            ) {
                BasicTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 24.dp, max = 180.dp),
                    textStyle = OdType.bubble.copy(color = palette.labelPrimary),
                    cursorBrush = SolidColor(palette.brandPrimary),
                    decorationBox = { inner ->
                        Box {
                            if (draft.isEmpty()) {
                                Text(text = "编辑消息", style = OdType.bubble, color = palette.labelPlaceholder)
                            }
                            inner()
                        }
                    },
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                ) {
                    EditAction(text = "取消", filled = false) { editing = false }
                    EditAction(
                        text = "发送",
                        filled = true,
                        enabled = draft.trim().isNotEmpty() && !busy,
                    ) {
                        editing = false
                        onCommitEdit(draft.trim())
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(x = ChatContentEnd - OdDimens.bubbleRightMargin),
                horizontalArrangement = Arrangement.End,
            ) {
                BoxWithConstraints(contentAlignment = Alignment.CenterEnd) {
                    Box(
                        modifier = Modifier
                            .widthIn(max = this.maxWidth * 0.79f)
                            .onGloballyPositioned { coordinates ->
                                origin = coordinates.localToWindow(Offset.Zero)
                            }
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onLongPress = { position ->
                                        onLongPress(
                                            (origin.x + position.x).toInt(),
                                            (origin.y + position.y).toInt(),
                                        )
                                    },
                                )
                            }
                            .clip(BubbleShape)
                            .background(palette.bubble)
                            .padding(
                                horizontal = OdDimens.bubblePaddingH,
                                vertical = OdDimens.bubblePaddingV,
                            ),
                    ) {
                        Text(
                            text = text,
                            style = OdType.bubble,
                            color = palette.bubbleText,
                        )
                    }
                }
            }
        }
        if (!editing && versions != null && versions.size > 1) {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(x = ChatContentEnd - OdDimens.bubbleRightMargin),
                horizontalArrangement = Arrangement.End,
            ) {
                BranchSwitcherPill(
                    versionIds = versions,
                    currentId = message.id,
                    disabled = versionDisabled,
                    onSelect = onSelectVersion,
                    onPosition = onSwitcherPosition,
                )
            }
        }
    }
}

@Composable
private fun EditAction(
    text: String,
    filled: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val palette = OdTheme.colors
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (filled) palette.brandPrimary else Color.Transparent)
            .border(
                width = if (filled) 0.dp else 1.dp,
                color = if (filled) palette.brandPrimary else palette.borderL2,
                shape = CircleShape,
            )
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                onClick = onClick,
            )
            .padding(horizontal = 14.dp, vertical = 5.dp),
    ) {
        Text(
            text = text,
            color = if (filled) Color.White else palette.labelPrimary,
            fontSize = 14.sp,
            lineHeight = 20.sp,
        )
    }
}

@Composable
fun AssistantMessageItem(
    message: ChatMessage,
    isLast: Boolean,
    streaming: Boolean,
    busy: Boolean,
    versions: List<Long>?,
    reading: ReadingState,
    onLongPress: (Int, Int) -> Unit,
    onCopy: (String) -> Unit,
    onReadAloud: (Long, String) -> Unit,
    onRegenerate: () -> Unit,
    onContinue: () -> Unit,
    onShare: () -> Unit,
    onOpenSources: (List<SearchResult>) -> Unit,
    onOpenCitation: (SearchResult) -> Unit,
    onSelectVersion: (Long, Float) -> Unit,
    onSwitcherPosition: (Float) -> Unit,
    thinkingAutoFold: Boolean = true,
    selectable: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val palette = OdTheme.colors
    val replySteps = remember(message.fragments, message.status, streaming) {
        buildReplySteps(message, streaming)
    }
    val steps = replySteps.steps
    val body = replySteps.answer
    val template = replySteps.template
    val sources = remember(message.fragments) { searchSources(message) }
    val citations = remember(message.fragments) { citationSources(message) }
    val errorTipText = remember(message.fragments) {
        message.fragments.filterIsInstance<Fragment.Tip>()
            .filter { it.style.equals("ERROR", ignoreCase = true) }
            .map { it.content }
            .filterNot { it.isAiDisclaimer() }
            .joinToString("\n")
    }
    val notice = remember(message.status, errorTipText) {
        if (errorTipText.isBlank()) statusNotice(message) else null
    }
    val tipText = remember(message.fragments) {
        message.fragments.filterIsInstance<Fragment.Tip>()
            .filterNot { it.style.equals("ERROR", ignoreCase = true) }
            .map { it.content }
            .filterNot { it.isAiDisclaimer() }
            .joinToString("\n")
    }
    val riskText = remember(message.fragments) {
        message.fragments.filterIsInstance<Fragment.ContentRisk>().joinToString("\n") { it.content }
    }
    val waiting = streaming && body.isBlank() &&
        steps.none { it is ReplyStep.Thinking || it is ReplyStep.Search } &&
        message.status == MessageStatus.WIP
    var origin by remember { mutableStateOf(Offset.Zero) }
    val readingThis = reading.messageId == message.id && reading.status != ReadingStatus.IDLE
    val canRegenerate = isLast && !busy && message.status != MessageStatus.CONTENT_FILTER

    Column(
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned { coordinates -> origin = coordinates.localToWindow(Offset.Zero) }
            .pointerInput(message.id) {
                detectTapGestures(
                    onLongPress = { position ->
                        onLongPress((origin.x + position.x).toInt(), (origin.y + position.y).toInt())
                    },
                )
            },
    ) {
        val thinkingSteps = steps.filter {
            it is ReplyStep.Thinking || it is ReplyStep.Search ||
                it is ReplyStep.Open || it is ReplyStep.Find || it is ReplyStep.ReadingLink
        }
        val hasThinking = thinkingSteps.any { it is ReplyStep.Thinking }
        if (hasThinking) {
            ThinkingBlock(
                steps = thinkingSteps,
                streaming = streaming,
                autoFold = thinkingAutoFold,
                stoppedWithoutBody = body.isBlank() && !streaming &&
                    (message.status == MessageStatus.INCOMPLETE || message.status == MessageStatus.WIP),
                hasFollowingContent = body.isNotBlank() || sources.isNotEmpty() || template.isNotBlank(),
                onOpenSources = onOpenSources,
            )
        } else {
            steps.forEachIndexed { index, step ->
                when (step) {
                    is ReplyStep.Search -> ToolSearchStepRow(step = step, messageStreaming = streaming)
                    is ReplyStep.Open -> ToolOpenStepRow(step = step, messageStreaming = streaming)
                    is ReplyStep.Find -> ToolFindStepRow(step = step, messageStreaming = streaming)
                    is ReplyStep.ReadingLink -> {
                        if (step.status == ToolStatus.WIP) {
                            ReadingLinkRow(title = step.title ?: step.url)
                        }
                    }
                    else -> Unit
                }
                val last = index == steps.lastIndex
                if (!last || body.isNotBlank() || sources.isNotEmpty()) {
                    Spacer(Modifier.height(12.17.dp))
                }
            }
        }
        if (body.isNotBlank()) {
            val smoothedBody = rememberSmoothedText(body, streaming)
            val markdown: @Composable () -> Unit = {
                AssistantMarkdown(
                    text = smoothedBody,
                    isStreaming = false,
                    onCitationClick = { index ->
                        val citation = citations.getOrNull(index - 1)
                        if (citation != null) onOpenCitation(citation)
                    },
                )
            }
            if (selectable) {
                androidx.compose.foundation.text.selection.SelectionContainer { markdown() }
            } else {
                markdown()
            }
        }
        if (template.isNotBlank() && template != body) {
            AssistantMarkdown(text = template, isStreaming = false)
        }
        if (waiting) {
            Spacer(Modifier.height(8.dp))
            WaitingDots()
        }
        if (notice != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Icon(
                    imageVector = AppIcons.warning,
                    contentDescription = null,
                    modifier = Modifier.padding(top = 4.dp).size(16.dp),
                    tint = palette.stateErrorPrimary,
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = notice,
                    color = palette.stateErrorPrimary,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                )
            }
        }
        if (errorTipText.isNotBlank()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Icon(
                    imageVector = AppIcons.warning,
                    contentDescription = null,
                    modifier = Modifier.padding(top = 4.dp).size(16.dp),
                    tint = palette.stateErrorPrimary,
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = errorTipText,
                    color = palette.stateErrorPrimary,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                )
            }
        }
        if (tipText.isNotBlank()) {
            Text(
                text = tipText,
                modifier = Modifier.padding(top = 8.dp),
                color = palette.labelSecondary,
                fontSize = 14.sp,
                lineHeight = 22.sp,
            )
        }
        if (riskText.isNotBlank()) {
            Text(
                text = riskText,
                modifier = Modifier.padding(top = 8.dp),
                color = palette.stateErrorPrimary,
                fontSize = 14.sp,
                lineHeight = 22.sp,
            )
        }
        if (message.status == MessageStatus.INCOMPLETE && !streaming && isLast) {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .border(1.dp, palette.borderL2, CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onContinue,
                    )
                    .padding(horizontal = 14.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "继续生成",
                    color = palette.labelPrimary,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                )
            }
        }
        if (!streaming && sources.isNotEmpty()) {
            Spacer(Modifier.height(14.dp))
            SourcesChip(sources = sources) { onOpenSources(sources) }
        }
        if (body.isNotBlank() && !streaming) {
            Spacer(Modifier.height(if (sources.isNotEmpty()) 12.dp else 20.dp))
            MessageActionsRow(
                readingThis = readingThis,
                canRegenerate = canRegenerate,
                onCopy = { onCopy(body) },
                onLike = {},
                onDislike = {},
                onReadAloud = { onReadAloud(message.id, body) },
                onShare = onShare,
                onRegenerate = onRegenerate,
            )
            if (versions != null && versions.size > 1) {
                Spacer(Modifier.height(8.dp))
                BranchSwitcherPill(
                    versionIds = versions,
                    currentId = message.id,
                    disabled = busy,
                    onSelect = onSelectVersion,
                    onPosition = onSwitcherPosition,
                )
            }
        }
    }
}

private fun String.isAiDisclaimer(): Boolean =
    contains("本回答由 AI 生成") || contains("内容仅供参考") || contains("请仔细甄别")

private fun citationSources(message: ChatMessage): List<SearchResult> = message.fragments
    .filterIsInstance<Fragment.Response>()
    .flatMap { it.citations }
    .map { citation ->
        SearchResult(
            url = citation.url,
            title = citation.title ?: citation.url,
            snippet = citation.citedText.orEmpty(),
        )
    }

@Composable
private fun SourcesChip(sources: List<SearchResult>, onClick: () -> Unit) {
    val palette = OdTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val shown = sources.take(4)
    val faviconSize = 16.dp
    val faviconStep = 9.4.dp
    val borderColor = if (OdTheme.isDark) palette.divider else palette.chipBorder
    Row(
        modifier = Modifier
            .height(OdDimens.chipHeight)
            .clip(CircleShape)
            .background(palette.menu)
            .border(0.5.dp, borderColor, CircleShape)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(start = 11.3.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(
                width = faviconSize + faviconStep * (shown.size - 1).coerceAtLeast(0),
                height = faviconSize,
            ),
        ) {
            shown.forEachIndexed { index, source ->
                SiteFavicon(
                    url = source.iconUrl,
                    label = source.siteName ?: source.url,
                    size = faviconSize,
                    modifier = Modifier.offset(x = faviconStep * index),
                )
            }
        }
        Spacer(Modifier.width(11.7.dp))
        Text(
            text = "${sources.size} 个网页",
            style = OdType.chip,
            color = palette.chipText,
        )
    }
}

@Composable
private fun MessageActionsRow(
    readingThis: Boolean,
    canRegenerate: Boolean,
    onCopy: () -> Unit,
    onLike: () -> Unit,
    onDislike: () -> Unit,
    onReadAloud: () -> Unit,
    onShare: () -> Unit,
    onRegenerate: () -> Unit,
) {
    val palette = OdTheme.colors
    Column(Modifier.fillMaxWidth()) {
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .offset(x = ActionRowOffset)
                    .requiredWidth(this.maxWidth + ActionRowExtra)
                    .height(ActionBoxSize),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ActionIcon(R.drawable.ic_copy_outline, "复制", onCopy)
            Spacer(Modifier.width(ActionGap))
            ActionIcon(R.drawable.ic_like_outline, "喜欢", onLike)
            Spacer(Modifier.width(ActionGap))
            ActionIcon(R.drawable.ic_dislike_outline, "不喜欢", onDislike)
            Spacer(Modifier.width(ReadGap))
            Box(
                modifier = Modifier
                    .size(width = ReadBoxWidth, height = ActionBoxSize)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onReadAloud,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (readingThis) {
                    LottieIcon(
                        asset = OdLottieAssets.playing,
                        size = ActionIconSize,
                        tint = palette.iconSecondary,
                        loop = true,
                    )
                } else {
                    OdIcon(R.drawable.ic_playing_fill_24, ActionIconSize, palette.iconSecondary)
                }
            }
            Spacer(Modifier.width(ActionGapAfterRead))
            ActionIcon(R.drawable.ic_share_outline_20, "分享", onShare)
            Spacer(Modifier.weight(1f))
                ActionIcon(
                    res = R.drawable.ic_regegerate_outline_24,
                    description = "重新生成",
                    onClick = onRegenerate,
                    enabled = canRegenerate,
                )
            }
        }
    }
}

@Composable
private fun ActionIcon(
    res: Int,
    description: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    val palette = OdTheme.colors
    Box(
        modifier = Modifier
            .size(ActionBoxSize)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = enabled,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        OdIcon(
            res = res,
            size = ActionIconSize,
            tint = if (enabled) palette.iconSecondary else palette.iconSecondary.copy(alpha = 0.35f),
        )
    }
}

@Composable
private fun BranchSwitcherPill(
    versionIds: List<Long>,
    currentId: Long,
    disabled: Boolean,
    onSelect: (Long, Float) -> Unit,
    onPosition: (Float) -> Unit,
) {
    val palette = OdTheme.colors
    val index = versionIds.indexOf(currentId)
    if (index < 0) return
    var anchorTop by remember { mutableStateOf(0f) }
    Row(
        modifier = Modifier
            .height(30.dp)
            .clip(CircleShape)
            .border(OdDimens.inputBorder, palette.divider, CircleShape)
            .onGloballyPositioned { coordinates ->
                val top = coordinates.positionInWindow().y
                anchorTop = top
                onPosition(top)
            }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OdIconButton(
            res = R.drawable.ic_chevron_left_outline_20,
            size = 20.dp,
            boxSize = 24.dp,
            onClick = { versionIds.getOrNull(index - 1)?.let { onSelect(it, anchorTop) } },
            tint = if (!disabled && index > 0) palette.brandPrimary else palette.labelDimmed,
            enabled = !disabled && index > 0,
        )
        Text(
            text = "${index + 1}/${versionIds.size}",
            modifier = Modifier.padding(horizontal = 4.dp),
            color = palette.iconSecondary,
            fontSize = 15.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
        )
        OdIconButton(
            res = R.drawable.ic_chevron_right_outline_20,
            size = 20.dp,
            boxSize = 24.dp,
            onClick = { versionIds.getOrNull(index + 1)?.let { onSelect(it, anchorTop) } },
            tint = if (!disabled && index < versionIds.size - 1) palette.brandPrimary else palette.labelDimmed,
            enabled = !disabled && index < versionIds.size - 1,
        )
    }
}
