package com.opends.chat.feature.chat.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.opends.chat.render.OdMarkdown
import com.opends.chat.render.OdMarkdownVariant

@Composable
fun AssistantMarkdown(
    text: String,
    isStreaming: Boolean,
    modifier: Modifier = Modifier,
    onCitationClick: ((Int) -> Unit)? = null,
) {
    if (text.isBlank()) return
    OdMarkdown(
        text = text,
        modifier = modifier.fillMaxWidth(),
        isStreaming = isStreaming,
        onCitationClick = onCitationClick,
    )
}

@Composable
fun ThinkingMarkdown(
    text: String,
    isStreaming: Boolean,
    modifier: Modifier = Modifier,
) {
    if (text.isBlank()) return
    OdMarkdown(
        text = text,
        modifier = modifier.fillMaxWidth(),
        isStreaming = isStreaming,
        variant = OdMarkdownVariant.Small,
    )
}
