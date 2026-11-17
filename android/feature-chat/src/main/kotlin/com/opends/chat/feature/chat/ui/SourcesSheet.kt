package com.opends.chat.feature.chat.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opends.chat.core.SearchResult
import com.opends.chat.design.R
import com.opends.chat.design.theme.OdDimens
import com.opends.chat.design.theme.OdTheme
import com.opends.chat.design.theme.OdType

private val SheetTopInset = 40.5.dp
private val SheetTitleHeight = 52.dp
private val SheetEasing = CubicBezierEasing(0f, 0f, 0.2f, 1f)
private val SheetDurationMs = 220
private val CloseSize = 24.6.dp
private val SiteRowHeight = 18.5.dp
private val ContentHorizontal = 14.8.dp

@Composable
fun SourcesSheet(
    sources: List<SearchResult>?,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = OdTheme.colors
    val context = LocalContext.current
    var shown by remember { mutableStateOf<List<SearchResult>>(emptyList()) }
    LaunchedEffect(sources) {
        if (sources != null) shown = sources
    }
    val visible = sources != null
    val display = sources ?: shown
    BackHandler(enabled = visible) { onClose() }
    Box(modifier = modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(SheetDurationMs)),
            exit = fadeOut(tween(SheetDurationMs)),
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(palette.scrimSheet)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClose,
                    ),
            )
        }
        AnimatedVisibility(
            visible = visible,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(tween(SheetDurationMs, easing = SheetEasing)) { fullHeight -> fullHeight },
            exit = slideOutVertically(tween(SheetDurationMs, easing = SheetEasing)) { fullHeight -> fullHeight },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(top = SheetTopInset)
                    .clip(RoundedCornerShape(topStart = OdDimens.sheetRadius, topEnd = OdDimens.sheetRadius))
                    .background(palette.menu),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(SheetTitleHeight),
                ) {
                    Text(
                        text = "搜索结果",
                        modifier = Modifier.align(Alignment.Center),
                        color = palette.labelPrimary,
                        fontSize = 16.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 12.7.dp)
                            .size(CloseSize)
                            .clip(CircleShape)
                            .background(palette.selector)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onClose,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        OdIcon(
                            res = R.drawable.ic_close_outline_16,
                            size = 14.dp,
                            tint = palette.labelPrimary,
                        )
                    }
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .navigationBarsPadding(),
                    contentPadding = PaddingValues(
                        start = ContentHorizontal,
                        end = ContentHorizontal,
                        bottom = 24.dp,
                    ),
                ) {
                    itemsIndexed(display) { index, source ->
                        SourceRow(
                            source = source,
                            index = index + 1,
                            onClick = {
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
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SourceRow(
    source: SearchResult,
    index: Int,
    onClick: () -> Unit,
) {
    val palette = OdTheme.colors
    val siteName = source.siteName
        ?: source.url.substringAfter("://", source.url).substringBefore("/")
    val date = formatPublishedAt(source.publishedAt)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(vertical = 14.8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(SiteRowHeight),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SiteFavicon(
                url = source.iconUrl,
                label = siteName,
                size = OdDimens.sourceFavicon,
            )
            Spacer(Modifier.width(9.8.dp))
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = siteName,
                    style = OdType.sourceSite,
                    color = palette.labelPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (date.isNotBlank()) {
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = date,
                        style = OdType.caveat.copy(lineHeight = 18.sp),
                        color = palette.labelTertiary,
                        maxLines = 1,
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(OdDimens.sourceIndex)
                    .clip(CircleShape)
                    .background(if (OdTheme.isDark) palette.bgLayer2 else palette.bgLayer3),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = index.toString(),
                    color = palette.labelTertiary,
                    fontSize = 13.sp,
                    lineHeight = 13.sp,
                )
            }
        }
        Spacer(Modifier.height(11.3.dp))
        Text(
            text = source.title,
            style = OdType.sourceTitle,
            color = palette.labelPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(7.4.dp))
        Text(
            text = source.snippet,
            style = OdType.sourceDesc,
            color = palette.labelTertiary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
