package com.opends.chat.render

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.opends.chat.design.R
import kotlinx.coroutines.delay

@Composable
internal fun MdBlocksContent(
    blocks: List<MdBlock>,
    modifier: Modifier = Modifier,
    gap: Dp = LocalMarkdownTypography.current.blockGap,
    nested: Boolean = false,
) {
    if (blocks.isEmpty()) return
    val typography = LocalMarkdownTypography.current
    Column(modifier = modifier) {
        blocks.forEachIndexed { index, block ->
            val previous = if (index > 0) blocks[index - 1] else null
            val topGap = when {
                index == 0 -> 0.dp
                block is MdBlock.Heading -> typography.headingTopGap
                previous is MdBlock.Heading -> typography.headingBottomGap
                block is MdBlock.Rule || previous is MdBlock.Rule -> typography.ruleGap
                else -> gap
            }
            Box(modifier = Modifier.padding(top = topGap).fillMaxWidth()) {
                MdBlockView(block = block, nested = nested)
            }
        }
    }
}

@Composable
private fun MdBlockView(block: MdBlock, nested: Boolean) {
    val colors = LocalMarkdownColors.current
    val typography = LocalMarkdownTypography.current
    val textColor = markdownTextColor()
    when (block) {
        is MdBlock.Paragraph -> {
            val image = remember(block.text) { parseStandaloneImage(block.text) }
            if (image != null) {
                MdImage(url = image.second, alt = image.first, modifier = Modifier.fillMaxWidth())
            } else {
                MdInlineText(raw = block.text, style = typography.body.merge(TextStyle(color = textColor)))
            }
        }
        is MdBlock.Heading -> {
            val style = when (block.level) {
                1 -> typography.h1
                2 -> typography.h2
                3 -> typography.h3
                4 -> typography.h4
                5 -> typography.h5
                else -> typography.h6
            }
            MdInlineText(raw = block.text, style = style.merge(TextStyle(color = textColor)))
        }
        is MdBlock.Code -> MdCodeBlockView(block)
        is MdBlock.Quote -> Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(vertical = typography.quoteVerticalPadding),
        ) {
            Box(
                modifier = Modifier
                    .width(typography.quoteRailWidth)
                    .fillMaxHeight()
                    .background(colors.quoteBar, RoundedCornerShape(percent = 50)),
            )
            Spacer(modifier = Modifier.width(typography.quoteGap))
            MdBlocksContent(
                blocks = block.blocks,
                modifier = Modifier.weight(1f),
                gap = typography.blockGap,
                nested = true,
            )
        }
        is MdBlock.BulletList -> MdListView(block.items, ordered = false, start = 1, nested = nested)
        is MdBlock.OrderedList -> MdListView(block.items, ordered = true, start = block.start, nested = nested)
        is MdBlock.Table -> MdTableView(block)
        is MdBlock.Math -> MathBlockView(block.latex, block.closed)
        MdBlock.Rule -> Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(typography.ruleThickness)
                .background(colors.rule),
        )
    }
}

@Composable
private fun MdListView(
    items: List<MdListItem>,
    ordered: Boolean,
    start: Int,
    nested: Boolean,
) {
    val colors = LocalMarkdownColors.current
    val typography = LocalMarkdownTypography.current
    val textColor = markdownTextColor()
    Column(verticalArrangement = Arrangement.spacedBy(typography.listItemGap)) {
        items.forEachIndexed { index, item ->
            Row(modifier = Modifier.fillMaxWidth()) {
                if (!ordered && item.checked == null) {
                    val markerRadius = typography.listMarkerSize / 2
                    Canvas(
                        modifier = Modifier
                            .width(typography.listIndent)
                            .height(typography.body.lineHeight.value.dp),
                    ) {
                        drawCircle(
                            color = colors.secondary,
                            radius = markerRadius.toPx(),
                            center = Offset(
                                x = markerRadius.toPx() + 1.05.dp.toPx(),
                                y = size.height / 2f,
                            ),
                        )
                    }
                } else {
                    val marker = when {
                        item.checked == true -> "☑"
                        item.checked == false -> "□"
                        else -> "${start + index}."
                    }
                    val markerWidth = typography.listIndent +
                        typography.orderedMarkerCharWidth * (marker.length - 2).coerceAtLeast(0)
                    Box(modifier = Modifier.width(markerWidth)) {
                        Text(
                            text = marker,
                            style = typography.body.merge(TextStyle(color = colors.secondary)),
                        )
                    }
                }
                if (item.blocks.size == 1 && item.blocks[0] is MdBlock.Paragraph) {
                    MdInlineText(
                        raw = (item.blocks[0] as MdBlock.Paragraph).text,
                        style = typography.body.merge(TextStyle(color = textColor)),
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    MdBlocksContent(
                        blocks = item.blocks,
                        modifier = Modifier.weight(1f),
                        gap = typography.blockGap,
                        nested = true,
                    )
                }
            }
        }
    }
}

@Composable
private fun MdCodeBlockView(block: MdBlock.Code) {
    val colors = LocalMarkdownColors.current
    val typography = LocalMarkdownTypography.current
    val context = LocalContext.current
    var copied by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    val isMermaid = block.language.trim().equals("mermaid", ignoreCase = true)
    val lineCount = remember(block.code) { if (block.code.isEmpty()) 1 else block.code.count { it == '\n' } + 1 }
    val streaming = LocalMarkdownStreaming.current
    val collapsible = !isMermaid && !streaming && lineCount > COLLAPSE_LINE_COUNT
    val density = LocalDensity.current
    val collapsedHeight = remember(typography) {
        with(density) { (typography.codeBlock.lineHeight * COLLAPSE_LINE_COUNT).toDp() }
    }

    LaunchedEffect(copied) {
        if (copied) {
            delay(1500)
            copied = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(typography.codeRadius))
            .background(colors.codeBackground),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(typography.codeHeaderHeight)
                .background(colors.codeBanner)
                .padding(start = typography.codeHeaderStartPadding, end = typography.codeHeaderEndPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = languageLabel(block.language),
                style = typography.codeBanner.merge(TextStyle(color = colors.secondary)),
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(typography.codeCopyButtonRadius))
                    .clickable {
                        copyToClipboard(context, block.code)
                        copied = true
                    }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(
                        id = if (copied) R.drawable.ic_check_outline_20 else R.drawable.ic_copy_outline
                    ),
                    contentDescription = "复制",
                    colorFilter = ColorFilter.tint(if (copied) colors.brand else colors.secondary),
                    modifier = Modifier.size(typography.codeCopyIconSize),
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(typography.codeHeaderDivider)
                .background(colors.quoteBar),
        )
        if (isMermaid) {
            MermaidView(code = block.code, isStreaming = LocalMarkdownStreaming.current)
        } else {
            val codeScrollState = rememberScrollState()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .let {
                        if (collapsible && !expanded) {
                            it.heightIn(max = collapsedHeight + typography.codePaddingVertical * 2)
                        } else {
                            it
                        }
                    },
            ) {
                Text(
                    text = rememberCodeText(block.code, block.language, colors),
                    style = typography.codeBlock.merge(TextStyle(color = colors.syntax.root)),
                    softWrap = false,
                    modifier = Modifier
                        .horizontalScroll(codeScrollState)
                        .padding(
                            start = typography.codePaddingStart,
                            end = typography.codePaddingStart,
                            top = typography.codePaddingVertical,
                            bottom = typography.codePaddingVertical,
                        ),
                )
            }
        }
        if (collapsible) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (expanded) "收起" else "展开",
                    style = typography.codeBanner.merge(TextStyle(color = colors.tertiary)),
                )
            }
        }
    }
}

private fun parseStandaloneImage(text: String): Pair<String, String>? {
    val trimmed = text.trim()
    if (!trimmed.startsWith("![")) return null
    val close = trimmed.indexOf("](")
    if (close < 0 || !trimmed.endsWith(")")) return null
    val alt = trimmed.substring(2, close)
    var url = trimmed.substring(close + 2, trimmed.length - 1).trim()
    if (url.contains(' ')) url = url.substringBefore(' ')
    if (url.isEmpty()) return null
    return alt to url
}

private fun languageLabel(language: String): String {
    val value = language.trim()
    return when {
        value.isEmpty() -> "text"
        value.startsWith(".") -> value.substring(1)
        else -> value
    }
}

private fun copyToClipboard(context: Context, text: String) {
    runCatching {
        val manager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        manager.setPrimaryClip(ClipData.newPlainText("code", text))
    }
}

private const val COLLAPSE_LINE_COUNT = 18
