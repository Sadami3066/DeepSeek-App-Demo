package com.opends.chat.feature.chat.ui

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opends.chat.design.theme.OpenDSTheme

@Composable
fun ChatTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    OpenDSTheme(darkTheme = darkTheme, content = content)
}

object ChatMotion {
    val standard = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
    val default = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1f)
    val easeIn = CubicBezierEasing(0.4f, 0f, 1f, 1f)
    const val fastMs = 200
    const val normalMs = 300
    const val inputTransitionMs = 200
}

object ChatDimens {
    val pageHorizontal = 24.dp
    val firstMessageTop = 34.dp
    val userMessageBottom = 16.dp
    val columnBottom = 24.dp
    val topBarHeight = 46.dp
    val topBarButton = 46.dp
    val inputRadius = 24.dp
    val inputPaddingTop = 12.dp
    val inputPaddingStart = 16.dp
    val inputPaddingEnd = 12.dp
    val inputLineHeight = 24.sp
    val inputMinLines = 2.dp
    val inputMaxHeight = 336.dp
    val bubbleRadius = 22.dp
    val bubblePaddingV = 10.dp
    val bubblePaddingH = 16.dp
    val toggleHeight = 34.dp
    val toggleRadius = 18.dp
    val iconButton = 34.dp
    val sendButton = 34.dp
    val branchHeight = 28.dp
    val replyTitleHeight = 34.dp
    val toolRowHeight = 34.dp
    val dotSize = 6.dp
    val thinkingDot = 5.dp
    val timelineLeft = 22.dp
    val timelineLineX = 7.5f
    val attachmentThumb = 64.dp
    val attachmentThumbSingle = 120.dp
    val iconGlyph = 16.dp
}

object ChatType {
    val body = ChatTextStyle(16, 28)
    val bodyLine = 28.sp
    val small = ChatTextStyle(14, 24)
    val xs = ChatTextStyle(13, 20)
    val tiny = ChatTextStyle(11, 16)
    val title = ChatTextStyle(14, 22)
    val bubble = ChatTextStyle(16, 24)
    val branchCount = ChatTextStyle(15, 18)
}

data class ChatTextStyle(val size: Int, val lineHeight: Int)
