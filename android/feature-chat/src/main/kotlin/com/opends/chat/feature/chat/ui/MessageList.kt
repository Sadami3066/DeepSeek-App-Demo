@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.opends.chat.feature.chat.ui

import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.opends.chat.core.ChatMessage
import com.opends.chat.core.Role
import com.opends.chat.core.SearchResult
import com.opends.chat.feature.chat.ChatUiState
import kotlinx.coroutines.launch
import kotlin.math.abs

private const val STICKY_THRESHOLD_DP = 40
private val FirstMessageTop = 20.3.dp
private val ColumnBottom = 25.7.dp
private val ItemGap = 22.dp
private val RevealTopPadding = 12.dp

private data class VersionAnchor(val targetId: Long, val switcherTop: Float)

@Composable
fun MessageList(
    state: ChatUiState,
    editRequestId: Long?,
    selectionMessageId: Long?,
    onEditMessage: (Long, String) -> Unit,
    onCopyText: (String) -> Unit,
    onRegenerate: () -> Unit,
    onContinue: () -> Unit,
    onReadAloud: (Long, String) -> Unit,
    onSelectVersion: (Long, Long) -> Unit,
    onShare: (ChatMessage) -> Unit,
    onOpenSources: (List<SearchResult>) -> Unit,
    onOpenCitation: (SearchResult) -> Unit,
    onLongPressMessage: (ChatMessage, Int, Int) -> Unit,
    scrollToMessageId: Long? = null,
    onScrollToMessageHandled: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var follow by remember { mutableStateOf(true) }
    val density = LocalDensity.current
    val thresholdPx = with(density) { STICKY_THRESHOLD_DP.dp.toPx() }
    val switcherTops = remember { HashMap<Long, Float>() }
    var switcherTick by remember { mutableIntStateOf(0) }
    var versionAnchor by remember { mutableStateOf<VersionAnchor?>(null) }

    fun nearBottom(): Boolean {
        val info = listState.layoutInfo
        if (info.totalItemsCount == 0) return true
        val last = info.visibleItemsInfo.lastOrNull() ?: return true
        if (last.index < info.totalItemsCount - 1) return false
        return (last.offset + last.size) - info.viewportEndOffset <= thresholdPx
    }

    LaunchedEffect(listState) {
        var previousIndex = listState.firstVisibleItemIndex
        var previousOffset = listState.firstVisibleItemScrollOffset
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .collect { (index, offset) ->
                if (nearBottom()) {
                    follow = true
                } else if (index < previousIndex || (index == previousIndex && offset < previousOffset)) {
                    follow = false
                }
                previousIndex = index
                previousOffset = offset
            }
    }

    var lastMessageCount by remember { mutableStateOf(state.messages.size) }
    val lastContentLength = state.messages.lastOrNull()?.let(::contentLength) ?: 0
    LaunchedEffect(state.messages.size, lastContentLength, state.streaming) {
        val grew = state.messages.size > lastMessageCount
        lastMessageCount = state.messages.size
        if (follow && state.messages.isNotEmpty() && (grew || state.streaming)) {
            listState.scrollToItem(state.messages.lastIndex, Int.MAX_VALUE / 2)
        }
    }

    val viewportHeight by remember { derivedStateOf { listState.layoutInfo.viewportSize.height } }
    LaunchedEffect(viewportHeight) {
        if (follow && state.messages.isNotEmpty()) {
            listState.scrollToItem(state.messages.lastIndex, Int.MAX_VALUE / 2)
        }
    }

    val imeVisible = WindowInsets.isImeVisible
    LaunchedEffect(imeVisible) {
        if (follow && state.messages.isNotEmpty()) {
            listState.scrollToItem(state.messages.lastIndex, Int.MAX_VALUE / 2)
        }
    }

    val followState = rememberUpdatedState(follow)
    LaunchedEffect(state.streaming) {
        if (!state.streaming) return@LaunchedEffect
        while (followState.value) {
            withFrameNanos { }
            if (!followState.value) break
            val count = listState.layoutInfo.totalItemsCount
            if (count > 0) {
                listState.scrollToItem(count - 1, Int.MAX_VALUE / 2)
            }
        }
    }

    fun onVersionSelected(messageId: Long, siblingId: Long, anchorTop: Float) {
        if (state.streaming) return
        follow = false
        switcherTops.remove(siblingId)
        versionAnchor = VersionAnchor(siblingId, anchorTop)
        onSelectVersion(messageId, siblingId)
    }

    fun onSwitcherPosition(messageId: Long, top: Float) {
        switcherTops[messageId] = top
        if (versionAnchor?.targetId == messageId) {
            switcherTick += 1
        }
    }

    LaunchedEffect(versionAnchor) {
        if (versionAnchor != null) {
            kotlinx.coroutines.delay(800)
            if (versionAnchor != null) versionAnchor = null
        }
    }

    LaunchedEffect(versionAnchor, state.messages, switcherTick) {
        val anchor = versionAnchor ?: return@LaunchedEffect
        if (state.messages.none { it.id == anchor.targetId }) return@LaunchedEffect
        val newTop = switcherTops[anchor.targetId] ?: return@LaunchedEffect
        val delta = newTop - anchor.switcherTop
        if (abs(delta) > 0.5f) {
            listState.scrollBy(delta)
        }
        versionAnchor = null
    }

    LaunchedEffect(scrollToMessageId, state.messages) {
        val target = scrollToMessageId ?: return@LaunchedEffect
        val index = state.messages.indexOfFirst { it.id == target }
        if (index < 0) return@LaunchedEffect
        follow = false
        withFrameNanos { }
        listState.scrollToItem(index, with(density) { RevealTopPadding.roundToPx() })
        onScrollToMessageHandled()
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = ChatContentStart,
                end = ChatContentEnd,
                top = FirstMessageTop,
                bottom = ColumnBottom,
            ),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(ItemGap),
        ) {
            itemsIndexed(state.messages, key = { _, message -> message.id }) { index, message ->
                val isLast = index == state.messages.lastIndex
                val streamingThis = state.streamingMessageId == message.id
                if (message.role == Role.USER) {
                    UserMessageItem(
                        message = message,
                        editRequested = editRequestId == message.id,
                        busy = state.streaming,
                        versions = state.versions[message.id],
                        versionDisabled = state.streaming,
                        onSelectVersion = { sibling, top -> onVersionSelected(message.id, sibling, top) },
                        onSwitcherPosition = { top -> onSwitcherPosition(message.id, top) },
                        onLongPress = { x, y -> onLongPressMessage(message, x, y) },
                        onCommitEdit = { text -> onEditMessage(message.id, text) },
                    )
                } else {
                    AssistantMessageItem(
                        message = message,
                        isLast = isLast,
                        streaming = streamingThis,
                        busy = state.streaming && !streamingThis,
                        versions = state.versions[message.id],
                        reading = state.reading,
                        onLongPress = { x, y -> onLongPressMessage(message, x, y) },
                        onCopy = onCopyText,
                        onReadAloud = onReadAloud,
                        onRegenerate = onRegenerate,
                        onContinue = onContinue,
                        onShare = { onShare(message) },
                        onOpenSources = onOpenSources,
                        onOpenCitation = onOpenCitation,
                        onSelectVersion = { sibling, top -> onVersionSelected(message.id, sibling, top) },
                        onSwitcherPosition = { top -> onSwitcherPosition(message.id, top) },
                        thinkingAutoFold = state.thinkingAutoFold,
                        selectable = selectionMessageId == message.id,
                    )
                }
            }
        }
        ScrollToBottomButton(
            visible = !follow,
            onClick = {
                follow = true
                scope.launch {
                    listState.scrollToItem(
                        (state.messages.size - 1).coerceAtLeast(0),
                        Int.MAX_VALUE / 2,
                    )
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 14.dp, bottom = 7.dp),
        )
    }
}

private fun contentLength(message: ChatMessage): Int =
    message.fragments.sumOf { fragment ->
        when (fragment) {
            is com.opends.chat.core.Fragment.Response -> fragment.content.length
            is com.opends.chat.core.Fragment.Think -> fragment.content.length
            is com.opends.chat.core.Fragment.Search -> fragment.results.size
            is com.opends.chat.core.Fragment.ReadLink -> 1
            is com.opends.chat.core.Fragment.TemplateResponse -> fragment.content.length
            is com.opends.chat.core.Fragment.Tip -> 1
            is com.opends.chat.core.Fragment.ContentRisk -> 1
            is com.opends.chat.core.Fragment.Request -> fragment.content.length
            is com.opends.chat.core.Fragment.PartialFileRead -> 1
            is com.opends.chat.core.Fragment.File -> 1
            is com.opends.chat.core.Fragment.ToolOpen -> 1
            is com.opends.chat.core.Fragment.ToolFind -> 1
        }
    }
