package com.opends.chat.feature.chat.ui

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.opends.chat.design.theme.OdDimens
import com.opends.chat.design.theme.OdTheme
import com.opends.chat.design.theme.OdType
import com.opends.chat.feature.chat.DraftAttachment

private val ImeBottomMargin = 9.85.dp
private val FixedHintTopGap = 2.7.dp

@Composable
fun ChatInputDock(
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
    selectedPhotos: Set<String>,
    onSelectPhoto: (android.net.Uri) -> Unit,
    onRemoveAttachment: (String) -> Unit,
    onRetryAttachment: (String) -> Unit,
    recentPhotos: List<Uri>,
    onCamera: () -> Unit,
    onPhoto: () -> Unit,
    onFile: () -> Unit,
    onVoiceGestureStart: () -> Unit,
    onVoiceCancelChange: (Boolean) -> Unit,
    onVoiceGestureEnd: (Boolean) -> Unit,
    showFixedHint: Boolean,
    imeVisible: Boolean,
    onFocusedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = OdTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                bottom = when {
                    attachOpen -> OdDimens.inputBottomMargin
                    showFixedHint -> 0.dp
                    imeVisible -> ImeBottomMargin
                    else -> OdDimens.inputBottomMargin
                },
            ),
    ) {
        ChatInputBar(
            modifier = Modifier.padding(horizontal = OdDimens.screenPadding),
            text = text,
            onTextChange = onTextChange,
            thinkOn = thinkOn,
            searchOn = searchOn,
            onToggleThink = onToggleThink,
            onToggleSearch = onToggleSearch,
            onSend = onSend,
            onStop = onStop,
            streaming = streaming,
            voiceMode = voiceMode,
            onToggleVoiceMode = onToggleVoiceMode,
            attachOpen = attachOpen,
            onToggleAttach = onToggleAttach,
            attachments = attachments,
            onRemoveAttachment = onRemoveAttachment,
            onRetryAttachment = onRetryAttachment,
            onVoiceGestureStart = onVoiceGestureStart,
            onVoiceCancelChange = onVoiceCancelChange,
            onVoiceGestureEnd = onVoiceGestureEnd,
            onFocusedChange = onFocusedChange,
        )
        if (showFixedHint && !attachOpen) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = FixedHintTopGap)
                    .height(12.8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "回答由 AI 生成，仅供参考",
                    style = OdType.fixedHint,
                    color = palette.caveatFixed,
                    textAlign = TextAlign.Center,
                )
            }
        }
        AttachPanel(
            expanded = attachOpen,
            recentPhotos = recentPhotos,
            selectedPhotos = selectedPhotos,
            onSelectPhoto = onSelectPhoto,
            onPickPhoto = onPhoto,
            onCamera = onCamera,
            onPickFile = onFile,
        )
    }
}
