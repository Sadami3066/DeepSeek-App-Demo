package com.opends.chat.render

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import com.opends.chat.design.theme.OdTheme
import com.opends.chat.design.theme.OpenDSTheme

const val SHARE_IMAGE_MAX_HEIGHT_PX = 16384

data class ShareImageGroup(
    val userText: String? = null,
    val assistantMarkdown: String? = null,
    val thinkingSeconds: Int? = null,
    val steps: List<String> = emptyList(),
)

data class ShareImageContent(
    val groups: List<ShareImageGroup>,
    val dark: Boolean = false,
    val qrPayload: String = "",
    val footerNote: String = "回答由 AI 生成，仅供参考",
)

internal object ShareImageMetrics {
    val width: Dp = 380.dp
    val contentPadding: Dp = 30.dp
    val topPadding: Dp = 32.dp
    val groupGap: Dp = 30.dp
    val bubbleRadius: Dp = 20.dp
    val bubblePaddingHorizontal: Dp = 14.46.dp
    val bubblePaddingVertical: Dp = 9.23.dp
    val bubbleMaxWidth: Dp = 300.dp
    val bubbleToThinking: Dp = 24.dp
    val bubbleToMarkdown: Dp = 20.dp
    val thinkingRowHeight: Dp = 24.dp
    val thinkingIndent: Dp = 2.dp
    val chevronGap: Dp = 4.dp
    val chevronWidth: Dp = 5.3.dp
    val chevronHeight: Dp = 10.2.dp
    val thinkingToMarkdown: Dp = 10.dp
    val stepGap: Dp = 4.dp
    val markdownToListIndent: Dp = 16.dp
    val markdownListItemGap: Dp = 12.7.dp
    val markdownToDisclaimer: Dp = 14.dp
    val disclaimerToDivider: Dp = 43.dp
    val dividerThickness: Dp = 1.dp
    val bottomPadding: Dp = 32.dp
}

private val thinkingStyle = TextStyle(fontSize = 14.sp, lineHeight = 24.sp)
private val stepStyle = TextStyle(fontSize = 14.sp, lineHeight = 20.sp)
private val disclaimerStyle = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontStyle = FontStyle.Italic)

private const val DISCLAIMER_TEXT = "本回答由 AI 生成，内容仅供参考，请仔细甄别"

@Composable
fun OdShareImage(content: ShareImageContent, modifier: Modifier = Modifier) {
    OpenDSTheme(darkTheme = content.dark) {
        val colors = OdTheme.colors
        val shareTypography = remember {
            BodyMarkdownTypography.copy(
                listIndent = ShareImageMetrics.markdownToListIndent,
                listItemGap = ShareImageMetrics.markdownListItemGap,
            )
        }
        Column(
            modifier = modifier
                .width(ShareImageMetrics.width)
                .background(colors.bgBase),
        ) {
            Spacer(Modifier.height(ShareImageMetrics.topPadding))
            content.groups.forEachIndexed { index, group ->
                if (index > 0) Spacer(Modifier.height(ShareImageMetrics.groupGap))
                ShareGroupView(group = group, colors = colors, shareTypography = shareTypography)
            }
            if (content.groups.isNotEmpty()) {
                Spacer(Modifier.height(ShareImageMetrics.markdownToDisclaimer))
                Text(
                    text = DISCLAIMER_TEXT,
                    style = disclaimerStyle.merge(TextStyle(color = colors.labelSecondary)),
                    modifier = Modifier.padding(horizontal = ShareImageMetrics.contentPadding),
                )
                Spacer(Modifier.height(ShareImageMetrics.disclaimerToDivider))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = ShareImageMetrics.contentPadding)
                        .height(ShareImageMetrics.dividerThickness)
                        .background(colors.divider),
                )
            }
            Spacer(Modifier.height(ShareImageMetrics.bottomPadding))
        }
    }
}

@Composable
private fun ShareGroupView(
    group: ShareImageGroup,
    colors: com.opends.chat.design.theme.OdColors,
    shareTypography: MarkdownTypography,
) {
    var hasPreviousBlock = false
    var hasProcessBlock = false
    group.userText?.takeIf { it.isNotBlank() }?.let { text ->
        ShareUserBubble(text = text, colors = colors)
        hasPreviousBlock = true
    }
    if (group.thinkingSeconds != null) {
        if (hasPreviousBlock) {
            Spacer(Modifier.height(ShareImageMetrics.bubbleToThinking))
        }
        ShareThinkingRow(seconds = group.thinkingSeconds, color = colors.labelSecondary)
        hasPreviousBlock = true
        hasProcessBlock = true
    }
    if (group.steps.isNotEmpty()) {
        if (hasPreviousBlock) {
            Spacer(Modifier.height(ShareImageMetrics.thinkingToMarkdown))
        }
        group.steps.forEachIndexed { index, step ->
            if (index > 0) Spacer(Modifier.height(ShareImageMetrics.stepGap))
            Text(
                text = step,
                style = stepStyle.merge(TextStyle(color = colors.labelSecondary)),
                modifier = Modifier.padding(horizontal = ShareImageMetrics.contentPadding),
            )
        }
        hasPreviousBlock = true
        hasProcessBlock = true
    }
    group.assistantMarkdown?.takeIf { it.isNotBlank() }?.let { markdown ->
        if (hasPreviousBlock) {
            Spacer(
                Modifier.height(
                    if (hasProcessBlock) ShareImageMetrics.thinkingToMarkdown else ShareImageMetrics.bubbleToMarkdown
                )
            )
        }
        OdMarkdown(
            text = markdown,
            modifier = Modifier.padding(horizontal = ShareImageMetrics.contentPadding),
            isStreaming = false,
            contentColor = colors.labelPrimary,
            typography = shareTypography,
        )
    }
}

@Composable
private fun ShareUserBubble(text: String, colors: com.opends.chat.design.theme.OdColors) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ShareImageMetrics.contentPadding),
        horizontalArrangement = Arrangement.End,
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = ShareImageMetrics.bubbleMaxWidth)
                .clip(RoundedCornerShape(ShareImageMetrics.bubbleRadius))
                .background(colors.bubble)
                .padding(
                    horizontal = ShareImageMetrics.bubblePaddingHorizontal,
                    vertical = ShareImageMetrics.bubblePaddingVertical,
                ),
        ) {
            Text(
                text = text,
                style = com.opends.chat.design.theme.OdType.bubble.merge(TextStyle(color = colors.bubbleText)),
            )
        }
    }
}

@Composable
private fun ShareThinkingRow(seconds: Int, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(ShareImageMetrics.thinkingRowHeight)
            .padding(
                start = ShareImageMetrics.contentPadding + ShareImageMetrics.thinkingIndent,
                end = ShareImageMetrics.contentPadding,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "已思考（用时 $seconds 秒）",
            style = thinkingStyle.merge(TextStyle(color = color)),
        )
        Spacer(Modifier.width(ShareImageMetrics.chevronGap))
        ShareChevron(color = color)
    }
}

@Composable
private fun ShareChevron(color: Color) {
    Canvas(
        modifier = Modifier
            .width(ShareImageMetrics.chevronWidth)
            .height(ShareImageMetrics.chevronHeight),
    ) {
        val stroke = 1.5.dp.toPx()
        drawLine(
            color = color,
            start = Offset(0f, 0f),
            end = Offset(size.width, size.height / 2f),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(0f, size.height),
            end = Offset(size.width, size.height / 2f),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, widthDp = 380, heightDp = 1100)
@Composable
private fun ShareImageLightPreview() {
    OdShareImage(previewShareContent(dark = false))
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, widthDp = 380, heightDp = 1100)
@Composable
private fun ShareImageDarkPreview() {
    OdShareImage(previewShareContent(dark = true))
}

private fun previewShareContent(dark: Boolean): ShareImageContent = ShareImageContent(
    dark = dark,
    groups = listOf(
        ShareImageGroup(
            userText = "什么是我的世界",
            thinkingSeconds = 2,
            assistantMarkdown = """
                你说的“我的世界”通常指游戏《Minecraft》（中文译名《我的世界》）。

                它是一款沙盒建造生存游戏：玩家在一个由方块组成的开放世界里，可以自由探索、采集资源、建造建筑、合成物品、生存冒险，也可以切换到创造模式无限建造。

                简单信息：

                - **开发商**：瑞典 Mojang Studios
                - **正式版发布**：2011 年
                - **中国版**：由网易代理
                - **平台**：PC、主机、手机等

                如果你说的“我的世界”不是指这款游戏，而是字面意思或某个其他作品，可以再说明一下。
            """.trimIndent(),
        )
    ),
)
