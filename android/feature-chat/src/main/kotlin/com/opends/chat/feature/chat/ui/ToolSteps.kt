package com.opends.chat.feature.chat.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opends.chat.core.SearchResult
import com.opends.chat.design.theme.OdTheme
import kotlinx.coroutines.delay

private val ToolRowHeight = 24.dp
private val ToolIconBox = 24.dp
private val ToolIconSize = 16.dp
private val ToolChevronSize = 20.dp

@Composable
internal fun ToolSearchStepRow(
    step: ReplyStep.Search,
    messageStreaming: Boolean,
) {
    val palette = OdTheme.colors
    var expanded by rememberSaveable(step.key) { mutableStateOf(false) }
    var queryIndex by remember(step.queries) { mutableIntStateOf(0) }
    LaunchedEffect(step.status, step.queries, messageStreaming) {
        if (step.status == ToolStatus.WIP && step.queries.size > 1) {
            while (true) {
                delay(2000)
                queryIndex = (queryIndex + 1) % step.queries.size
            }
        }
    }
    val label = step.searchLabel(messageStreaming, queryIndex) ?: "正在搜索"
    val tint = if (step.status == ToolStatus.FAILED) palette.stateWarningPrimary else palette.labelSecondary
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(ToolRowHeight)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    enabled = step.results.isNotEmpty(),
                ) { expanded = !expanded },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(ToolIconBox), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = AppIcons.search,
                    contentDescription = null,
                    modifier = Modifier.size(ToolIconSize),
                    tint = tint,
                )
            }
            Spacer(Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 15.sp,
                lineHeight = 24.sp,
                color = palette.labelSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            Spacer(Modifier.weight(1f))
            if (step.results.isNotEmpty()) {
                Icon(
                    imageVector = AppIcons.chevronDown,
                    contentDescription = null,
                    modifier = Modifier
                        .size(ToolChevronSize)
                        .rotate(if (expanded) 180f else 0f),
                    tint = palette.iconSecondary,
                )
            }
        }
        AnimatedVisibility(
            visible = expanded && step.results.isNotEmpty(),
            enter = expandVertically(tween(180)),
            exit = shrinkVertically(tween(180)),
        ) {
            SearchSourceRow(results = step.results)
        }
    }
}

@Composable
internal fun ToolOpenStepRow(step: ReplyStep.Open, messageStreaming: Boolean) {
    val palette = OdTheme.colors
    var expanded by rememberSaveable(step.key) { mutableStateOf(false) }
    val label = step.openLabel(messageStreaming) ?: return
    val hasPages = step.pages.any { !it.url.isNullOrBlank() }
    val tint = if (step.status == ToolStatus.FAILED) palette.stateWarningPrimary else palette.labelSecondary
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(ToolRowHeight)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    enabled = hasPages,
                ) { expanded = !expanded },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(ToolIconBox), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = AppIcons.browse,
                    contentDescription = null,
                    modifier = Modifier.size(ToolIconSize),
                    tint = tint,
                )
            }
            Spacer(Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 15.sp,
                lineHeight = 24.sp,
                color = palette.labelSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            Spacer(Modifier.weight(1f))
            if (hasPages) {
                Icon(
                    imageVector = AppIcons.chevronDown,
                    contentDescription = null,
                    modifier = Modifier
                        .size(ToolChevronSize)
                        .rotate(if (expanded) 180f else 0f),
                    tint = palette.iconSecondary,
                )
            }
        }
        AnimatedVisibility(
            visible = expanded && hasPages,
            enter = expandVertically(tween(180)),
            exit = shrinkVertically(tween(180)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp, bottom = 4.dp),
            ) {
                step.pages.filter { !it.url.isNullOrBlank() }.take(5).forEach { page ->
                    OpenPageRow(page = page)
                }
            }
        }
    }
}

@Composable
private fun OpenPageRow(page: OpenPage) {
    val palette = OdTheme.colors
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 32.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = !page.url.isNullOrBlank(),
            ) {
                val url = page.url ?: return@clickable
                runCatching {
                    context.startActivity(
                        android.content.Intent(
                            android.content.Intent.ACTION_VIEW,
                            android.net.Uri.parse(url),
                        ),
                    )
                }
            }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SiteFavicon(
            url = page.siteIcon,
            label = page.siteName ?: page.url.orEmpty(),
            size = 16.dp,
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = page.title ?: page.siteName ?: page.url.orEmpty(),
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = if (page.failed) palette.labelTertiary else palette.labelSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
internal fun ToolFindStepRow(step: ReplyStep.Find, messageStreaming: Boolean) {
    val palette = OdTheme.colors
    var expanded by rememberSaveable(step.key) { mutableStateOf(false) }
    val label = step.findLabel(messageStreaming) ?: return
    val tint = if (step.status == ToolStatus.FAILED) palette.stateWarningPrimary else palette.labelSecondary
    val canExpand = step.patterns.isNotEmpty() || step.results.isNotEmpty()
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(ToolRowHeight)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    enabled = canExpand,
                ) { expanded = !expanded },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(ToolIconBox), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = AppIcons.find,
                    contentDescription = null,
                    modifier = Modifier.size(ToolIconSize),
                    tint = tint,
                )
            }
            Spacer(Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 15.sp,
                lineHeight = 24.sp,
                color = palette.labelSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            Spacer(Modifier.weight(1f))
            if (canExpand) {
                Icon(
                    imageVector = AppIcons.chevronDown,
                    contentDescription = null,
                    modifier = Modifier
                        .size(ToolChevronSize)
                        .rotate(if (expanded) 180f else 0f),
                    tint = palette.iconSecondary,
                )
            }
        }
        AnimatedVisibility(
            visible = expanded && canExpand,
            enter = expandVertically(tween(180)),
            exit = shrinkVertically(tween(180)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp, bottom = 4.dp),
            ) {
                if (step.patterns.isNotEmpty()) {
                    FindPatternChips(patterns = step.patterns)
                }
                step.results.forEach { result ->
                    Text(
                        text = result,
                        modifier = Modifier.padding(top = 6.dp),
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = palette.labelTertiary,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun FindPatternChips(patterns: List<String>) {
    val palette = OdTheme.colors
    androidx.compose.foundation.layout.FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        patterns.forEach { pattern ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(palette.bgLayer3)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(
                    text = pattern,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = palette.labelSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun SearchSourceRow(results: List<SearchResult>) {
    val palette = OdTheme.colors
    val context = LocalContext.current
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 4.dp),
        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(results, key = { it.url + it.title }) { source ->
            Column(
                modifier = Modifier
                    .width(232.dp)
                    .heightIn(min = 72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(palette.bgLayer3)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {
                        if (source.url.isNotBlank()) {
                            runCatching {
                                context.startActivity(
                                    android.content.Intent(
                                        android.content.Intent.ACTION_VIEW,
                                        android.net.Uri.parse(source.url),
                                    ),
                                )
                            }
                        }
                    }
                    .padding(12.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SiteFavicon(
                        url = source.iconUrl,
                        label = source.siteName ?: source.url,
                        size = 16.dp,
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = source.siteName
                            ?: source.url.substringAfter("://", source.url).substringBefore("/"),
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = palette.labelSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = source.title,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = palette.labelPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
internal fun ReadingLinkRow(title: String) {
    val palette = OdTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(ToolRowHeight),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(ToolIconBox), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = AppIcons.link,
                contentDescription = null,
                modifier = Modifier.size(ToolIconSize),
                tint = palette.labelSecondary,
            )
        }
        Spacer(Modifier.width(4.dp))
        Text(
            text = "正在阅读",
            fontSize = 15.sp,
            lineHeight = 24.sp,
            color = palette.labelSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = title,
            fontSize = 13.sp,
            lineHeight = 24.sp,
            color = palette.labelTertiary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
    }
}
