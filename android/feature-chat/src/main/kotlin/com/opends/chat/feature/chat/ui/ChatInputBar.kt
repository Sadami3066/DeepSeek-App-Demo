package com.opends.chat.feature.chat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.opends.chat.design.R
import com.opends.chat.design.theme.OdDimens
import com.opends.chat.design.theme.OdTheme
import com.opends.chat.design.theme.OdType
import com.opends.chat.feature.chat.AttachmentState
import com.opends.chat.feature.chat.DraftAttachment
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val VoiceHoldRowHeight = 46.1.dp
private val VoiceDividerGap = 11.55.dp
private val TextToPillGap = 18.84.dp
private val CardBottomPadding = 11.76.dp
private val TextRowTopOffset = 0.dp
private val TextRowHeight = 24.dp
private const val MaxInputLines = 5
private val SendSize = 27.7.dp
private val SendArrowSize = 15.dp
private val AttachRingSize = 22.5.dp
private val AttachRingStroke = 1.23.dp
private val AttachCrossSize = 12.2.dp
private val VoiceCancelDistance = 64.dp
private const val VoiceLongPressMs = 250L

@Composable
fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    thinkOn: Boolean,
    searchOn: Boolean,
    onToggleThink: () -> Unit,
    onToggleSearch: () -> Unit,
    onSend: () -> Unit,
    onStop: () -> Unit,
    streaming: Boolean,
    voiceMode: Boolean,
    onToggleVoiceMode: () -> Unit,
    attachOpen: Boolean,
    onToggleAttach: () -> Unit,
    attachments: List<DraftAttachment>,
    onRemoveAttachment: (String) -> Unit,
    onRetryAttachment: (String) -> Unit,
    onVoiceGestureStart: () -> Unit,
    onVoiceCancelChange: (Boolean) -> Unit,
    onVoiceGestureEnd: (Boolean) -> Unit,
    onFocusedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = OdTheme.colors
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(OdDimens.inputRadius)
    val hasContent = text.isNotEmpty() || attachments.isNotEmpty()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .odShadow(shape, OdTheme.isDark)
            .background(palette.inputMajor, shape)
            .border(OdDimens.inputBorder, palette.borderL1, shape)
            .clip(shape)
            .padding(bottom = CardBottomPadding),
    ) {
        if (attachments.isNotEmpty()) {
            Spacer(Modifier.height(OdDimens.inputTopPadding))
            AttachmentStrip(
                attachments = attachments,
                onRemove = onRemoveAttachment,
                onRetry = onRetryAttachment,
                modifier = Modifier.padding(
                    start = OdDimens.inputTextPaddingStart,
                    end = OdDimens.inputTextPaddingStart,
                ),
            )
            Spacer(Modifier.height(8.dp))
        }
        if (voiceMode) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(VoiceHoldRowHeight)
                    .voicePressGesture(
                        onLongPressStart = onVoiceGestureStart,
                        onCancelChange = onVoiceCancelChange,
                        onRelease = onVoiceGestureEnd,
                        onTap = {},
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "按住说话",
                    style = OdType.input,
                    color = palette.labelPrimary,
                )
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(OdDimens.inputBorder)
                    .background(palette.borderL1),
            )
            Spacer(Modifier.height(VoiceDividerGap))
        } else {
            Spacer(Modifier.height(OdDimens.inputTopPadding))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .voicePressGesture(
                        onLongPressStart = onVoiceGestureStart,
                        onCancelChange = onVoiceCancelChange,
                        onRelease = onVoiceGestureEnd,
                        onTap = {
                            focusRequester.requestFocus()
                            keyboard?.show()
                        },
                    ),
            ) {
                BasicTextField(
                    value = text,
                    onValueChange = onTextChange,
                    textStyle = OdType.input.copy(color = palette.labelPrimary),
                    cursorBrush = SolidColor(palette.brandPrimary),
                    maxLines = MaxInputLines,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        focusManager.clearFocus()
                        onSend()
                    }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = OdDimens.inputTextPaddingStart, end = OdDimens.inputTextPaddingStart)
                        .padding(top = TextRowTopOffset)
                        .heightIn(min = TextRowHeight, max = TextRowHeight * MaxInputLines)
                        .focusRequester(focusRequester)
                        .onFocusChanged {
                            focused = it.isFocused
                            onFocusedChange(it.isFocused)
                        },
                    decorationBox = { inner ->
                        Box {
                            if (text.isEmpty()) {
                                Text(
                                    text = if (focused) "发消息" else "发消息或按住说话",
                                    style = OdType.input,
                                    color = palette.labelPlaceholder,
                                )
                            }
                            inner()
                        }
                    },
                )
            }
            Spacer(Modifier.height(TextToPillGap))
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = OdDimens.inputRowPaddingStart, end = OdDimens.inputRowPaddingEnd)
                .height(OdDimens.pillHeight),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ModePill(
                on = thinkOn,
                icon = R.drawable.ic_think_outline_16,
                label = "深度思考",
                onClick = onToggleThink,
            )
            Spacer(Modifier.width(OdDimens.pillGap))
            ModePill(
                on = searchOn,
                icon = R.drawable.ic_globe_language_outline_16,
                label = "智能搜索",
                onClick = onToggleSearch,
            )
            Spacer(Modifier.weight(1f))
            AttachToggle(open = attachOpen, onClick = onToggleAttach)
            Spacer(Modifier.width(OdDimens.inputIconGap))
            Box(Modifier.size(OdDimens.inputIconSize), contentAlignment = Alignment.Center) {
                when {
                    streaming -> StopButton(onClick = onStop)
                    voiceMode -> VectorIconButton(
                        icon = AppIcons.keyboard,
                        size = OdDimens.inputIconSize,
                        boxSize = OdDimens.inputIconSize,
                        onClick = onToggleVoiceMode,
                        tint = palette.iconPrimary,
                        contentDescription = "切换至文字",
                    )

                    hasContent -> SendButton(onClick = onSend)
                    else -> VectorIconButton(
                        icon = AppIcons.voice,
                        size = OdDimens.inputIconSize,
                        boxSize = OdDimens.inputIconSize,
                        onClick = onToggleVoiceMode,
                        tint = palette.iconPrimary,
                        contentDescription = "切换至语音",
                    )
                }
            }
        }
    }
}

private fun Modifier.voicePressGesture(
    onLongPressStart: () -> Unit,
    onCancelChange: (Boolean) -> Unit,
    onRelease: (Boolean) -> Unit,
    onTap: () -> Unit,
): Modifier = pointerInput(Unit) {
    val cancelDistancePx = VoiceCancelDistance.toPx()
    coroutineScope {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
            down.consume()
            var triggered = false
            var pressed = true
            var cancelled = false
            val timer = launch {
                delay(VoiceLongPressMs)
                if (pressed) {
                    triggered = true
                    onLongPressStart()
                }
            }
            try {
                while (pressed) {
                    val event = awaitPointerEvent(PointerEventPass.Initial)
                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                    if (!change.pressed) {
                        pressed = false
                        break
                    }
                    change.consume()
                    if (triggered) {
                        val movedUp = down.position.y - change.position.y
                        val nowCancelled = movedUp > cancelDistancePx
                        if (nowCancelled != cancelled) {
                            cancelled = nowCancelled
                            onCancelChange(nowCancelled)
                        }
                    }
                }
            } catch (error: CancellationException) {
                timer.cancel()
                if (triggered) onRelease(true)
                throw error
            }
            pressed = false
            timer.cancel()
            if (triggered) onRelease(cancelled) else onTap()
        }
    }
}

@Composable
private fun AttachToggle(open: Boolean, onClick: () -> Unit) {
    val palette = OdTheme.colors
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .size(OdDimens.inputIconSize)
            .clip(CircleShape)
            .semantics { contentDescription = if (open) "关闭附件面板" else "上传文件" }
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (open) {
            Box(
                Modifier
                    .size(AttachRingSize)
                    .border(AttachRingStroke, palette.iconPrimary, CircleShape),
            )
            OdIcon(R.drawable.ic_add_close_regular, AttachCrossSize, palette.iconPrimary)
        } else {
            OdIcon(R.drawable.ic_add_outline_26, OdDimens.inputIconSize, palette.iconPrimary)
        }
    }
}

@Composable
private fun SendButton(onClick: () -> Unit) {
    val palette = OdTheme.colors
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .size(SendSize)
            .clip(CircleShape)
            .background(palette.brandPrimary)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = androidx.compose.ui.res.painterResource(R.drawable.ic_send_fill_20),
            contentDescription = "发送",
            tint = Color.White,
            modifier = Modifier.size(SendArrowSize),
        )
    }
}

@Composable
private fun StopButton(onClick: () -> Unit) {
    val palette = OdTheme.colors
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .size(SendSize)
            .clip(CircleShape)
            .background(palette.brandPrimary)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = androidx.compose.ui.res.painterResource(R.drawable.ic_stop_fill_20),
            contentDescription = "停止生成",
            tint = Color.White,
            modifier = Modifier.size(SendArrowSize),
        )
    }
}

@Composable
private fun ModePill(
    on: Boolean,
    icon: Int,
    label: String,
    onClick: () -> Unit,
) {
    val palette = OdTheme.colors
    val interaction = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .height(OdDimens.pillHeight)
            .clip(CircleShape)
            .background(if (on) palette.brandContainer else Color.Transparent)
            .border(
                OdDimens.inputBorder,
                if (on) palette.brandContainerBorder else palette.borderL1,
                CircleShape,
            )
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = OdDimens.pillPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        OdIcon(
            res = icon,
            size = OdDimens.pillIcon,
            tint = if (on) palette.brandText else palette.labelSecondary,
        )
        Spacer(Modifier.width(OdDimens.pillIconGap))
        Text(
            text = label,
            style = OdType.pill,
            color = if (on) palette.brandText else palette.labelSecondary,
        )
    }
}

@Composable
private fun AttachmentStrip(
    attachments: List<DraftAttachment>,
    onRemove: (String) -> Unit,
    onRetry: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        attachments.forEach { attachment ->
            Box {
                if (attachment.ref.isImage) {
                    ImageThumb(
                        file = runCatching {
                            com.opends.chat.core.OpenDS.attachments.findLocal(attachment.ref)
                        }.getOrNull(),
                        size = 56.dp,
                        onRemove = { onRemove(attachment.ref.id) },
                    )
                } else {
                    FileCard(
                        name = attachment.ref.name,
                        subtitle = when (attachment.state) {
                            AttachmentState.ERROR -> "上传失败"
                            AttachmentState.READY -> formatFileSize(attachment.ref.size)
                            AttachmentState.UPLOADING -> "上传中..."
                        },
                        onRemove = { onRemove(attachment.ref.id) },
                    )
                }
                if (attachment.state == AttachmentState.UPLOADING) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        LoadingSpinner(size = 16.dp, color = Color.White)
                    }
                }
                if (attachment.state == AttachmentState.ERROR) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Black.copy(alpha = 0.48f))
                            .clickableNoIndication { onRetry(attachment.ref.id) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = AppIcons.refresh,
                            contentDescription = "重试",
                            modifier = Modifier.size(20.dp),
                            tint = Color.White,
                        )
                    }
                }
            }
        }
    }
}
