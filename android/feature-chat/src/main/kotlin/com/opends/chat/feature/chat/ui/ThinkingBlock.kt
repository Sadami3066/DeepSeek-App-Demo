package com.opends.chat.feature.chat.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opends.chat.core.SearchResult
import com.opends.chat.design.R
import com.opends.chat.design.theme.OdTheme

private val ThinkHeaderIndent = 2.45.dp
private val ThinkHeaderHeight = 22.dp
private val ThinkHeaderGap = 10.6.dp
private val ThinkItemBottomPad = 10.7.dp
private val ThinkItemGap = 2.dp
private val ThinkTextIndent = 23.25.dp
private val ThinkIconAxis = 7.0.dp
private val ThinkRailWidth = 0.92.dp
private val ThinkBulletSize = 5.5.dp
private val ThinkStepIconSize = 16.dp
private val ThinkBodyFontSize = 14.sp
private val ThinkBodyLineHeight = 22.15.sp
private val ThinkStepFontSize = 15.sp
private val ThinkStepLineHeight = 24.sp
private val ThinkStepRowHeight = 24.dp
private val ThinkFaviconSize = 14.dp
private val ThinkFaviconStep = 10.5.dp
private const val ThinkMaxFavicons = 4

internal val ThinkingBlockHeaderSpacing = ThinkHeaderGap
internal val ThinkingBlockBottomSpacing = 12.5.dp

@Composable
internal fun ThinkingBlock(
    steps: List<ReplyStep>,
    streaming: Boolean,
    autoFold: Boolean,
    stoppedWithoutBody: Boolean,
    hasFollowingContent: Boolean,
    onOpenSources: (List<SearchResult>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = OdTheme.colors
    val seconds = steps.filterIsInstance<ReplyStep.Thinking>()
        .mapNotNull { it.elapsedSeconds }
        .takeIf { it.isNotEmpty() }
        ?.sum()
    val thinkingActive = steps.any { it is ReplyStep.Thinking && it.streaming }
    var expanded by rememberSaveable { mutableStateOf(streaming) }
    var lastActive by remember { mutableStateOf(thinkingActive) }
    LaunchedEffect(thinkingActive) {
        when {
            lastActive && !thinkingActive -> if (autoFold) expanded = false
            !lastActive && thinkingActive -> expanded = true
        }
        lastActive = thinkingActive
    }
    val header = when {
        thinkingActive -> "正在思考"
        stoppedWithoutBody -> "已停止"
        seconds != null -> "已思考（用时 ${seconds.toInt()} 秒）"
        else -> "深度思考"
    }
    val railColor = if (OdTheme.isDark) palette.divider else palette.rail
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .height(ThinkHeaderHeight)
                .padding(start = ThinkHeaderIndent)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { expanded = !expanded },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = header,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                color = palette.labelSecondary,
                maxLines = 1,
            )
            Spacer(Modifier.width(4.dp))
            if (expanded) {
                OdIcon(
                    res = R.drawable.ic_chevron_down_outline_20,
                    size = 24.dp,
                    tint = palette.iconSecondary,
                )
            } else {
                OdIcon(
                    res = R.drawable.ic_chevron_right_outline_20,
                    size = 20.dp,
                    tint = palette.iconSecondary,
                )
            }
        }
        if (expanded) {
            Spacer(Modifier.height(ThinkHeaderGap))
            Column(modifier = Modifier.fillMaxWidth()) {
                steps.forEachIndexed { index, step ->
                    if (index > 0) Spacer(Modifier.height(ThinkItemGap))
                    when (step) {
                        is ReplyStep.Thinking -> ThinkParagraphs(
                            step = step,
                            streaming = streaming,
                            railColor = railColor,
                        )
                        is ReplyStep.Search -> ThinkStepItem(
                            step = step,
                            streaming = streaming,
                            railColor = railColor,
                            onOpenSources = onOpenSources,
                        )
                        is ReplyStep.Open -> ThinkStepItem(
                            step = step,
                            streaming = streaming,
                            railColor = railColor,
                            onOpenSources = onOpenSources,
                        )
                        is ReplyStep.Find -> ThinkStepItem(
                            step = step,
                            streaming = streaming,
                            railColor = railColor,
                            onOpenSources = onOpenSources,
                        )
                        is ReplyStep.ReadingLink -> {
                            if (step.status == ToolStatus.WIP) {
                                ThinkStepItem(
                                    step = step,
                                    streaming = streaming,
                                    railColor = railColor,
                                    onOpenSources = onOpenSources,
                                )
                            }
                        }
                    }
                }
            }
        }
        if (hasFollowingContent) {
            Spacer(Modifier.height(if (expanded) ThinkingBlockBottomSpacing else ThinkHeaderGap))
        }
    }
}

@Composable
private fun ThinkParagraphs(
    step: ReplyStep.Thinking,
    streaming: Boolean,
    railColor: Color,
) {
    val text = rememberSmoothedText(step.content, streaming)
    val paragraphs = remember(text) {
        text.split(Regex("\\n+")).filter { it.isNotBlank() }
    }
    Column(modifier = Modifier.fillMaxWidth()) {
        paragraphs.forEachIndexed { index, paragraph ->
            if (index > 0) Spacer(Modifier.height(ThinkItemGap))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
            ) {
                TimelineGutter(
                    firstLineHeight = ThinkBodyLineHeight,
                    railColor = railColor,
                    iconSize = ThinkBulletSize,
                ) {
                    Box(
                        Modifier
                            .size(ThinkBulletSize)
                            .clip(CircleShape)
                            .background(OdTheme.colors.iconSecondary),
                    )
                }
                Text(
                    text = paragraph,
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = ThinkItemBottomPad),
                    fontSize = ThinkBodyFontSize,
                    lineHeight = ThinkBodyLineHeight,
                    color = OdTheme.colors.labelSecondary,
                )
            }
        }
    }
}

@Composable
private fun ThinkStepItem(
    step: ReplyStep,
    streaming: Boolean,
    railColor: Color,
    onOpenSources: (List<SearchResult>) -> Unit,
) {
    val palette = OdTheme.colors
    val label = thinkStepLabel(step, streaming) ?: return
    val (iconRes, favicons, sources) = thinkStepVisual(step)
    val interaction = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
    ) {
        TimelineGutter(
            firstLineHeight = ThinkStepLineHeight,
            railColor = railColor,
            iconSize = ThinkStepIconSize,
        ) {
            Icon(
                painter = androidx.compose.ui.res.painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(ThinkStepIconSize),
                tint = palette.labelPrimary,
            )
        }
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = ThinkItemBottomPad)
                .height(ThinkStepRowHeight)
                .then(
                    if (sources.isNotEmpty()) {
                        Modifier.clickable(interactionSource = interaction, indication = null) {
                            onOpenSources(sources)
                        }
                    } else {
                        Modifier
                    },
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                fontSize = ThinkStepFontSize,
                lineHeight = ThinkStepLineHeight,
                color = palette.labelPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            if (favicons.isNotEmpty()) {
                Spacer(Modifier.width(6.dp))
                ThinkFavicons(favicons)
            }
            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun TimelineGutter(
    firstLineHeight: androidx.compose.ui.unit.TextUnit,
    railColor: Color,
    iconSize: Dp,
    icon: @Composable () -> Unit,
) {
    val density = androidx.compose.ui.platform.LocalDensity.current
    val lineDp = with(density) { firstLineHeight.toDp() }
    val linePx = with(density) { lineDp.toPx() }
    Box(
        modifier = Modifier
            .width(ThinkTextIndent)
            .fillMaxHeight(),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val axis = ThinkIconAxis.toPx()
            if (size.height > linePx + 0.5f) {
                drawLine(
                    color = railColor,
                    start = Offset(axis, linePx),
                    end = Offset(axis, size.height),
                    strokeWidth = ThinkRailWidth.toPx(),
                )
            }
        }
        Box(
            modifier = Modifier
                .offset(
                    x = ThinkIconAxis - iconSize / 2,
                    y = (lineDp - iconSize) / 2,
                ),
        ) {
            icon()
        }
    }
}

private data class ThinkStepVisualData(
    val iconRes: Int,
    val favicons: List<Pair<String?, String>>,
    val sources: List<SearchResult>,
)

private fun thinkStepVisual(step: ReplyStep): ThinkStepVisualData = when (step) {
    is ReplyStep.Search -> ThinkStepVisualData(
        iconRes = R.drawable.ic_search_outline_16,
        favicons = step.results.map { it.iconUrl to (it.siteName ?: it.url) },
        sources = step.results,
    )
    is ReplyStep.Open -> ThinkStepVisualData(
        iconRes = R.drawable.ic_type_text,
        favicons = step.pages.map { it.siteIcon to (it.siteName ?: it.title ?: it.url.orEmpty()) },
        sources = emptyList(),
    )
    is ReplyStep.Find -> ThinkStepVisualData(
        iconRes = R.drawable.ic_search_outline_16,
        favicons = emptyList(),
        sources = emptyList(),
    )
    is ReplyStep.ReadingLink -> ThinkStepVisualData(
        iconRes = R.drawable.ic_link_outline_20,
        favicons = emptyList(),
        sources = emptyList(),
    )
    is ReplyStep.Thinking -> ThinkStepVisualData(
        iconRes = R.drawable.ic_search_outline_16,
        favicons = emptyList(),
        sources = emptyList(),
    )
}

private fun thinkStepLabel(step: ReplyStep, streaming: Boolean): String? = when (step) {
    is ReplyStep.Search -> when (step.status) {
        ToolStatus.WIP -> when {
            step.stopped || !streaming -> "搜索已停止"
            step.queries.isNotEmpty() -> "搜索 ${step.queries.first()}"
            else -> "正在搜索"
        }
        ToolStatus.FAILED -> "联网搜索暂不可用"
        ToolStatus.FINISHED -> when {
            step.results.isEmpty() -> "未搜索到合适的内容"
            step.deepSearch -> "搜索到 ${step.results.size} 个网页"
            else -> "已阅读 ${step.results.size} 个网页"
        }
    }
    is ReplyStep.Open -> when {
        step.status == ToolStatus.FINISHED && step.mergedWithSearch -> "已浏览搜索结果"
        step.status == ToolStatus.FINISHED -> "已浏览 ${step.pages.size} 个页面"
        step.status == ToolStatus.WIP -> if (step.stopped || !streaming) "浏览页面失败" else "正在浏览网页"
        else -> "浏览页面失败"
    }
    is ReplyStep.Find -> step.findLabel(streaming)
    is ReplyStep.ReadingLink -> if (step.status == ToolStatus.WIP) "正在阅读" else null
    is ReplyStep.Thinking -> null
}

@Composable
private fun ThinkFavicons(favicons: List<Pair<String?, String>>) {
    val shown = favicons.take(ThinkMaxFavicons)
    Box(
        modifier = Modifier.size(
            width = ThinkFaviconSize + ThinkFaviconStep * (shown.size - 1).coerceAtLeast(0),
            height = ThinkFaviconSize,
        ),
    ) {
        shown.forEachIndexed { index, favicon ->
            SiteFavicon(
                url = favicon.first,
                label = favicon.second,
                size = ThinkFaviconSize,
                modifier = Modifier.offset(x = ThinkFaviconStep * index),
            )
        }
    }
}
