package com.opends.chat.feature.chat

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.opends.chat.core.ChatMessage
import com.opends.chat.core.Fragment
import com.opends.chat.core.OpenDS
import com.opends.chat.core.SessionInfo
import com.opends.chat.design.R
import com.opends.chat.design.theme.OdTheme
import com.opends.chat.feature.chat.ui.OdIcon
import com.opends.chat.feature.chat.ui.OdIconButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

private val FieldHeight = 44.3.dp
private val FieldRadius = 22.15.dp
private val FieldIconSize = 16.5.dp
private val FieldIconGap = 7.8.dp
private val ClearSize = 18.5.dp
private val ClearIconSize = 11.dp
private val ResultRowHeight = 68.3.dp
private val CoverSize = 40.6.dp
private val CoverGap = 11.4.dp

private data class SessionSearchHit(
    val sessionId: String,
    val messageId: Long?,
    val title: String,
    val snippet: String,
    val timestamp: Long,
    val matchStart: Int,
    val matchEnd: Int,
)

@Composable
fun SessionSearchRoute(
    onBack: () -> Unit,
    onOpenSession: (String, Long?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = OdTheme.colors
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    var query by remember { mutableStateOf("") }
    var hits by remember { mutableStateOf<List<SessionSearchHit>>(emptyList()) }
    var searching by remember { mutableStateOf(false) }
    val sessions by OpenDS.sessions.observeSessions().collectAsStateWithLifecycle(initialValue = emptyList())

    LaunchedEffect(Unit) {
        delay(80)
        runCatching { focusRequester.requestFocus() }
        keyboard?.show()
    }

    LaunchedEffect(query, sessions) {
        val keyword = query.trim()
        if (keyword.isEmpty()) {
            hits = emptyList()
            searching = false
            return@LaunchedEffect
        }
        delay(220)
        searching = true
        hits = withContext(Dispatchers.IO) { searchSessions(sessions, keyword) }
        searching = false
    }

    BackHandler {
        onBack()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(palette.bgBase)
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 1.2.dp, end = 17.2.dp, top = 11.dp)
                .height(FieldHeight),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OdIconButton(
                res = R.drawable.ic_chevron_left_outline_20,
                size = 20.dp,
                boxSize = 44.dp,
                onClick = {
                    focusManager.clearFocus()
                    onBack()
                },
                tint = palette.iconPrimary,
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(FieldHeight)
                    .clip(RoundedCornerShape(FieldRadius))
                    .background(palette.selector)
                    .padding(start = 12.6.dp, end = 12.9.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OdIcon(
                    res = R.drawable.ic_ds_search_outline_16,
                    size = FieldIconSize,
                    tint = palette.iconSecondary,
                )
                Spacer(Modifier.width(FieldIconGap))
                Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (query.isEmpty()) {
                        Text(
                            text = "搜索对话内容...",
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = palette.labelPlaceholder,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    BasicTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = palette.labelPrimary,
                        ),
                        cursorBrush = SolidColor(palette.brandPrimary),
                    )
                }
                if (query.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(ClearSize)
                            .clip(CircleShape)
                            .background(palette.labelTertiary)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) { query = "" },
                        contentAlignment = Alignment.Center,
                    ) {
                        OdIcon(
                            res = R.drawable.ic_close_outline_20,
                            size = ClearIconSize,
                            tint = palette.bgBase,
                        )
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 7.7.dp, bottom = 24.dp),
            ) {
                items(hits, key = { it.sessionId + "-" + (it.messageId ?: -1L) + "-" + it.timestamp + "-" + it.matchStart }) { hit ->
                    SessionSearchRow(
                        hit = hit,
                        query = query.trim(),
                        onClick = {
                            focusManager.clearFocus()
                            onOpenSession(hit.sessionId, hit.messageId)
                        },
                    )
                }
            }
            if (query.isNotBlank() && hits.isEmpty() && !searching) {
                Text(
                    text = "暂无相关结果",
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 48.dp),
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    color = palette.labelTertiary,
                )
            }
        }
    }
}

@Composable
private fun SessionSearchRow(
    hit: SessionSearchHit,
    query: String,
    onClick: () -> Unit,
) {
    val palette = OdTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(ResultRowHeight)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 14.8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(CoverSize)
                .clip(CircleShape)
                .background(palette.bgLayer3),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = hit.title.take(1),
                fontSize = 17.sp,
                lineHeight = 22.sp,
                color = palette.labelSecondary,
            )
        }
        Spacer(Modifier.width(CoverGap))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = hit.title,
                    modifier = Modifier.weight(1f),
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = palette.labelPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = formatResultTime(hit.timestamp),
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = palette.labelTertiary,
                    maxLines = 1,
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = highlightSnippet(hit.snippet, query, palette.labelPrimary, hit.matchStart, hit.matchEnd),
                fontSize = 14.sp,
                lineHeight = 18.5.sp,
                color = palette.labelTertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private suspend fun searchSessions(sessions: List<SessionInfo>, keyword: String): List<SessionSearchHit> {
    val hits = ArrayList<SessionSearchHit>()
    for (session in sessions) {
        val title = session.title?.takeIf { it.isNotBlank() } ?: "新对话"
        val messages = runCatching { OpenDS.sessions.loadMessages(session.id) }.getOrDefault(emptyList())
        for (message in messages) {
            val text = searchableText(message)
            if (text.isBlank()) continue
            val index = text.indexOf(keyword, ignoreCase = true)
            if (index < 0) continue
            val start = (index - 18).coerceAtLeast(0)
            val end = (index + keyword.length + 42).coerceAtMost(text.length)
            val snippet = text.substring(start, end).replace('\n', ' ')
            val matchStart = index - start
            hits.add(
                SessionSearchHit(
                    sessionId = session.id,
                    messageId = message.id,
                    title = title,
                    snippet = snippet,
                    timestamp = message.insertedAt.takeIf { it > 0 } ?: session.updatedAt,
                    matchStart = matchStart,
                    matchEnd = matchStart + keyword.length,
                ),
            )
        }
        val titleIndex = title.indexOf(keyword, ignoreCase = true)
        if (titleIndex >= 0) {
            hits.add(
                SessionSearchHit(
                    sessionId = session.id,
                    messageId = null,
                    title = title,
                    snippet = title,
                    timestamp = session.updatedAt,
                    matchStart = titleIndex,
                    matchEnd = titleIndex + keyword.length,
                ),
            )
        }
        if (hits.size >= 240) break
    }
    return hits.sortedWith(
        compareByDescending<SessionSearchHit> { it.timestamp }
            .thenBy { it.sessionId }
            .thenBy { it.messageId ?: 0L },
    ).take(60)
}

private fun searchableText(message: ChatMessage): String = message.fragments
    .filterIsInstance<Fragment.Request>()
    .joinToString("\n") { it.content }
    .ifBlank {
        message.fragments
            .filterIsInstance<Fragment.Response>()
            .joinToString("\n") { it.content }
    }

private fun formatResultTime(epochMillis: Long): String {
    if (epochMillis <= 0L) return ""
    val zone = ZoneId.systemDefault()
    val now = ZonedDateTime.now(zone)
    val time = Instant.ofEpochMilli(epochMillis).atZone(zone)
    val today = now.toLocalDate()
    return when {
        time.toLocalDate() == today -> String.format("%02d:%02d", time.hour, time.minute)
        time.toLocalDate() == today.minusDays(1) -> "昨天"
        time.year == now.year -> "${time.monthValue}月${time.dayOfMonth}日"
        else -> "${time.year}年${time.monthValue}月${time.dayOfMonth}日"
    }
}

private fun highlightSnippet(
    text: String,
    keyword: String,
    highlightColor: androidx.compose.ui.graphics.Color,
    matchStart: Int,
    matchEnd: Int,
): AnnotatedString {
    if (matchStart in 0..text.length && matchEnd in matchStart..text.length && matchEnd > matchStart) {
        return buildAnnotatedString {
            append(text.substring(0, matchStart))
            withStyle(SpanStyle(color = highlightColor)) {
                append(text.substring(matchStart, matchEnd))
            }
            append(text.substring(matchEnd))
        }
    }
    val query = keyword.trim()
    if (query.isEmpty()) return AnnotatedString(text)
    return buildAnnotatedString {
        var cursor = 0
        while (cursor < text.length) {
            val index = text.indexOf(query, cursor, ignoreCase = true)
            if (index < 0) {
                append(text.substring(cursor))
                break
            }
            append(text.substring(cursor, index))
            withStyle(SpanStyle(color = highlightColor)) {
                append(text.substring(index, index + query.length))
            }
            cursor = index + query.length
        }
    }
}
