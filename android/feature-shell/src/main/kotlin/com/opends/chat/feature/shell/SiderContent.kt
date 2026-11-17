package com.opends.chat.feature.shell

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.opends.chat.core.SessionInfo
import com.opends.chat.design.R
import com.opends.chat.design.theme.OdDimens
import com.opends.chat.design.theme.OdTheme
import com.opends.chat.design.theme.OdType
import kotlinx.coroutines.launch
import java.util.Calendar

private val SearchTop = 11.dp
private val SearchRadius = 22.15.dp
private val SearchIconFrame = 20.dp
private val SearchIconSize = 16.5.dp
private val SearchIconPadding = 11.dp
private val SearchIconGap = 6.1.dp

private val HighlightMargin = 9.2.dp
private val HighlightRadius = 14.8.dp
private val RowContentPadding = 11.1.dp
private val HeaderTopGap = 6.8.dp
private val HeaderHeight = 18.5.dp
private val HeaderBottomGap = 3.66.dp
private val FirstHeaderTop = 16.7.dp
private val DividerTopGap = 16.dp
private val DividerBottomGap = 15.4.dp
private val DividerHeight = 0.92.dp
private val AvatarSize = 30.dp
private val AvatarGap = 9.5.dp
private val UserBottomGap = 6.7.dp
private val ListBottomFade = 40.dp
private val EllipsisSize = 18.33.dp
private val SelectCheckSize = 18.6.dp
private val SelectCheckGap = 9.5.dp
private val SelectCloseSize = 32.4.dp
private val SelectCloseIconSize = 18.6.dp
private val SelectHeaderStart = 20.4.dp
private val SelectHeaderEnd = 13.4.dp
private val SelectActionHeight = 58.2.dp
private val SelectActionHairline = 0.36.dp
private val SelectActionDividerWidth = 0.7.dp

private enum class TimelineGroup { PINNED, TODAY, YESTERDAY, WEEK, MONTH, EARLIER }

private sealed interface DrawerItem {
    val key: String
}

private data class GroupHeaderItem(val group: TimelineGroup, val first: Boolean) : DrawerItem {
    override val key: String get() = "header_" + group.name
}

private data object PinnedDividerItem : DrawerItem {
    override val key: String get() = "pinned_divider"
}

private data class SessionRowItem(val session: SessionInfo) : DrawerItem {
    override val key: String get() = "session_" + session.id
}

private fun groupLabel(group: TimelineGroup, strings: ShellStrings): String = when (group) {
    TimelineGroup.PINNED -> strings.groupPinned
    TimelineGroup.TODAY -> strings.groupToday
    TimelineGroup.YESTERDAY -> strings.groupYesterday
    TimelineGroup.WEEK -> strings.groupWeek
    TimelineGroup.MONTH -> strings.groupMonth
    TimelineGroup.EARLIER -> strings.groupEarlier
}

private fun startOfDay(time: Long): Long {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = time
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}

private fun bucketOf(updatedAt: Long, now: Long): TimelineGroup {
    val days = (startOfDay(now) - startOfDay(updatedAt)) / 86_400_000L
    return when {
        days <= 0L -> TimelineGroup.TODAY
        days == 1L -> TimelineGroup.YESTERDAY
        days <= 7L -> TimelineGroup.WEEK
        days <= 30L -> TimelineGroup.MONTH
        else -> TimelineGroup.EARLIER
    }
}

private fun buildDrawerItems(sessions: List<SessionInfo>, now: Long): List<DrawerItem> {
    val sorted = sessions.sortedByDescending { it.updatedAt }
    val items = ArrayList<DrawerItem>()
    val pinned = sorted.filter { it.pinned }
    if (pinned.isNotEmpty()) {
        items.add(GroupHeaderItem(TimelineGroup.PINNED, first = true))
        pinned.forEach { items.add(SessionRowItem(it)) }
    }
    val buckets = LinkedHashMap<TimelineGroup, MutableList<SessionInfo>>()
    sorted.filterNot { it.pinned }.forEach { session ->
        buckets.getOrPut(bucketOf(session.updatedAt, now)) { mutableListOf() }.add(session)
    }
    if (pinned.isNotEmpty() && buckets.isNotEmpty()) {
        items.add(PinnedDividerItem)
    }
    TimelineGroup.entries.forEach { group ->
        val list = buckets[group] ?: return@forEach
        if (list.isEmpty()) return@forEach
        items.add(GroupHeaderItem(group, first = items.isEmpty()))
        list.forEach { items.add(SessionRowItem(it)) }
    }
    return items
}

@Composable
fun SiderContent(
    currentSessionId: String?,
    onSelectSession: (String?) -> Unit,
    onOpenSettings: () -> Unit,
    onClose: () -> Unit,
    onOpenSearch: () -> Unit = {},
) {
    val colors = OdTheme.colors
    val strings = shellStrings(rememberSettingsState().languageTag)
    val sessions = rememberSessionList()
    val store = remember { runningSessions() }
    val toast = rememberShellToast()
    val scope = rememberCoroutineScope()
    val items = remember(sessions) { buildDrawerItems(sessions, System.currentTimeMillis()) }

    var renaming by remember { mutableStateOf<SessionInfo?>(null) }
    var deleting by remember { mutableStateOf<SessionInfo?>(null) }
    var deleteBusy by remember { mutableStateOf(false) }
    var selecting by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var deletingSelection by remember { mutableStateOf(false) }
    val selectedSessions = remember(sessions, selectedIds) { sessions.filter { it.id in selectedIds } }

    fun enterSelection(ids: Set<String>) {
        if (ids.isEmpty()) return
        selecting = true
        selectedIds = ids
    }

    fun exitSelection() {
        selecting = false
        selectedIds = emptySet()
    }

    fun toggleSelection(id: String) {
        selectedIds = if (id in selectedIds) selectedIds - id else selectedIds + id
    }

    fun pinSelection() {
        val targets = selectedSessions.filterNot { it.pinned }
        scope.launch {
            if (targets.isNotEmpty()) {
                runCatching {
                    targets.forEach { store?.setPinned(it.id, true) ?: error("unavailable") }
                }.onFailure { toast.show(strings.operationFailed, true) }
            }
            exitSelection()
        }
    }

    fun deleteSelection() {
        if (deleteBusy) return
        deleteBusy = true
        scope.launch {
            val targets = selectedSessions
            val failed = targets.any { session ->
                runCatching { store?.deleteSession(session.id) ?: error("unavailable") }.isFailure
            }
            deleteBusy = false
            deletingSelection = false
            exitSelection()
            if (failed) toast.show(strings.deleteFailed, true) else toast.show(strings.deleteSuccess)
        }
    }

    fun rename(session: SessionInfo, title: String) {
        val trimmed = title.trim()
        if (trimmed.isEmpty() || trimmed == (session.title ?: "")) {
            return
        }
        scope.launch {
            runCatching { store?.renameSession(session.id, trimmed) ?: error("unavailable") }
                .onFailure { toast.show(strings.operationFailed, true) }
        }
    }

    fun togglePin(session: SessionInfo) {
        scope.launch {
            runCatching { store?.setPinned(session.id, !session.pinned) ?: error("unavailable") }
                .onFailure { toast.show(strings.operationFailed, true) }
        }
    }

    fun delete(session: SessionInfo) {
        if (deleteBusy) return
        deleteBusy = true
        scope.launch {
            val result = runCatching { store?.deleteSession(session.id) ?: error("unavailable") }
            deleteBusy = false
            if (result.isSuccess) {
                deleting = null
                toast.show(strings.deleteSuccess)
            } else {
                toast.show(strings.deleteFailed, true)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.sidebarFill)
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            if (selecting) {
                SelectionHeader(
                    count = selectedIds.size,
                    strings = strings,
                    onClose = { exitSelection() },
                )
            } else {
                Row(
                    modifier = Modifier
                        .padding(horizontal = OdDimens.drawerSearchMargin)
                        .padding(top = SearchTop)
                        .fillMaxWidth()
                        .height(OdDimens.drawerSearchHeight)
                        .clip(RoundedCornerShape(SearchRadius))
                        .background(colors.selector)
                        .shellClickable(onOpenSearch)
                        .padding(start = SearchIconPadding),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(SearchIconFrame), contentAlignment = Alignment.Center) {
                        ShellIcon(
                            res = R.drawable.ic_ds_search_outline_16,
                            size = SearchIconSize,
                            tint = colors.labelTertiary,
                        )
                    }
                    Spacer(Modifier.width(SearchIconGap))
                    Text(
                        text = strings.searchHint,
                        style = OdType.drawerSearch,
                        color = colors.labelTertiary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 6.dp),
                ) {
                    items(items = items, key = { it.key }) { item ->
                        when (item) {
                            is GroupHeaderItem -> GroupHeaderRow(
                                label = groupLabel(item.group, strings),
                                first = item.first,
                                showBatchIcon = !selecting,
                                onBatchClick = if (selecting) null else {
                                    {
                                        val now = System.currentTimeMillis()
                                        val ids = if (item.group == TimelineGroup.PINNED) {
                                            sessions.filter { it.pinned }
                                        } else {
                                            sessions.filter {
                                                !it.pinned && bucketOf(it.updatedAt, now) == item.group
                                            }
                                        }.map { it.id }.toSet()
                                        enterSelection(ids)
                                    }
                                },
                            )

                            PinnedDividerItem -> PinnedDividerRow()

                            is SessionRowItem -> SessionRowView(
                                session = item.session,
                                selected = item.session.id == currentSessionId && !selecting,
                                selectionMode = selecting,
                                checked = item.session.id in selectedIds,
                                strings = strings,
                                onClick = { onSelectSession(item.session.id) },
                                onToggleSelect = { toggleSelection(item.session.id) },
                                onEnterSelection = { enterSelection(setOf(item.session.id)) },
                                onTogglePin = { togglePin(item.session) },
                                onRename = { renaming = item.session },
                                onDelete = { deleting = item.session },
                            )
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(ListBottomFade)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, colors.sidebarFill),
                            ),
                        ),
                )
            }
            if (selecting) {
                SelectionActionBar(
                    strings = strings,
                    enabled = selectedIds.isNotEmpty(),
                    onPin = { pinSelection() },
                    onDelete = {
                        if (selectedIds.isNotEmpty()) {
                            deletingSelection = true
                        }
                    },
                )
            } else {
                UserArea(
                    name = strings.localUser,
                    onClick = onOpenSettings,
                )
                Spacer(Modifier.height(UserBottomGap))
            }
        }
        ShellToastHost(state = toast, modifier = Modifier.align(Alignment.TopCenter))
    }

    val renameTarget = renaming
    if (renameTarget != null) {
        ShellInputDialog(
            title = strings.renameTitle,
            initialValue = renameTarget.title.orEmpty(),
            placeholder = strings.renameHint,
            confirmLabel = strings.confirm,
            cancelLabel = strings.cancel,
            onDismiss = { renaming = null },
            onConfirm = { title ->
                renaming = null
                rename(renameTarget, title)
            },
        )
    }

    if (deletingSelection) {
        ShellConfirmDialog(
            title = strings.deleteSelectionTitle(selectedIds.size),
            message = strings.deleteSessionMessage,
            confirmLabel = strings.delete,
            cancelLabel = strings.cancel,
            onDismiss = {
                if (!deleteBusy) {
                    deletingSelection = false
                }
            },
            onConfirm = { deleteSelection() },
        )
    }

    val deleteTarget = deleting
    if (deleteTarget != null) {
        ShellConfirmDialog(
            title = strings.deleteSessionTitle,
            message = strings.deleteSessionMessage,
            confirmLabel = strings.delete,
            cancelLabel = strings.cancel,
            onDismiss = {
                if (!deleteBusy) {
                    deleting = null
                }
            },
            onConfirm = { delete(deleteTarget) },
        )
    }
}

@Composable
private fun PinnedDividerRow() {
    val colors = OdTheme.colors
    Column(Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(DividerTopGap))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = OdDimens.drawerDividerInset)
                .height(DividerHeight)
                .background(colors.divider),
        )
        Spacer(Modifier.height(DividerBottomGap))
    }
}

@Composable
private fun GroupHeaderRow(
    label: String,
    first: Boolean,
    showBatchIcon: Boolean,
    onBatchClick: (() -> Unit)?,
) {
    val colors = OdTheme.colors
    Column(Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(if (first) FirstHeaderTop else HeaderTopGap))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(HeaderHeight)
                .padding(horizontal = OdDimens.drawerListPadding),
            contentAlignment = Alignment.CenterStart,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = label,
                    style = OdType.drawerGroup,
                    color = colors.labelTertiary,
                    maxLines = 1,
                )
                Spacer(Modifier.weight(1f))
                if (showBatchIcon) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .shellClickable { onBatchClick?.invoke() },
                        contentAlignment = Alignment.Center,
                    ) {
                        ShellIcon(
                            res = R.drawable.ic_checklist_outline_24,
                            size = 16.5.dp,
                            tint = colors.labelTertiary,
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(HeaderBottomGap))
    }
}

@Composable
private fun SessionRowView(
    session: SessionInfo,
    selected: Boolean,
    selectionMode: Boolean,
    checked: Boolean,
    strings: ShellStrings,
    onClick: () -> Unit,
    onToggleSelect: () -> Unit,
    onEnterSelection: () -> Unit,
    onTogglePin: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    val colors = OdTheme.colors
    val title = session.title?.takeIf { it.isNotBlank() } ?: strings.newChat
    var menuOpen by remember(session.id) { mutableStateOf(false) }
    var rowBounds by remember(session.id) { mutableStateOf<Rect?>(null) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(OdDimens.drawerRowHeight)
            .padding(horizontal = HighlightMargin)
            .onGloballyPositioned { rowBounds = it.boundsInWindow() },
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 0.9.dp)
                    .clip(RoundedCornerShape(HighlightRadius))
                    .background(colors.sidebarActiveAccent),
            )
        }
        Row(
            modifier = Modifier
                .fillMaxSize()
                .combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { if (selectionMode) onToggleSelect() else onClick() },
                    onLongClick = { if (!selectionMode) menuOpen = true },
                )
                .padding(start = RowContentPadding, end = RowContentPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (selectionMode) {
                SelectionCheck(checked = checked)
                Spacer(Modifier.width(SelectCheckGap))
            }
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = OdType.drawerSession,
                color = if (selected) colors.sidebarActiveText else colors.labelPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (selected && !selectionMode) {
                Spacer(Modifier.width(4.dp))
                ShellIconButton(
                    res = R.drawable.ic_ellipsis,
                    size = EllipsisSize,
                    onClick = { menuOpen = true },
                    tint = colors.sidebarActiveText,
                )
            }
        }
        if (!selectionMode) {
            ShellMenu(
                expanded = menuOpen,
                anchor = rowBounds,
                onDismiss = { menuOpen = false },
                entries = listOf(
                    ShellMenuEntry(
                        label = strings.rename,
                        iconRes = R.drawable.ic_edit_outline_20,
                        onClick = onRename,
                    ),
                    ShellMenuEntry(
                        label = if (session.pinned) strings.unpin else strings.pin,
                        iconRes = R.drawable.ic_pin_outline_20,
                        onClick = onTogglePin,
                    ),
                    ShellMenuEntry(
                        label = strings.multiSelect,
                        iconRes = R.drawable.ic_checklist_outline_24,
                        onClick = onEnterSelection,
                    ),
                    ShellMenuEntry(
                        label = strings.delete,
                        iconRes = R.drawable.ic_trash_outline_20,
                        danger = true,
                        onClick = onDelete,
                    ),
                ),
            )
        }
    }
}

@Composable
private fun SelectionCheck(checked: Boolean) {
    val colors = OdTheme.colors
    Box(
        modifier = Modifier.size(SelectCheckSize),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Box(
                Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(colors.brandPrimary),
            )
            ShellIcon(
                res = R.drawable.ic_check_outline_20,
                size = 12.dp,
                tint = colors.labelPrimaryInverted,
            )
        } else {
            Box(
                Modifier
                    .fillMaxSize()
                    .border(1.2.dp, colors.labelDimmed, CircleShape),
            )
        }
    }
}

@Composable
private fun SelectionHeader(
    count: Int,
    strings: ShellStrings,
    onClose: () -> Unit,
) {
    val colors = OdTheme.colors
    Row(
        modifier = Modifier
            .padding(start = SelectHeaderStart, end = SelectHeaderEnd)
            .padding(top = SearchTop)
            .fillMaxWidth()
            .height(OdDimens.drawerSearchHeight),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = strings.selectionCount(count),
            modifier = Modifier.weight(1f),
            style = OdType.selectionHeader,
            color = colors.labelPrimary,
            maxLines = 1,
        )
        Box(
            modifier = Modifier
                .size(SelectCloseSize)
                .shadow(elevation = 2.5.dp, shape = CircleShape, clip = false)
                .clip(CircleShape)
                .background(colors.buttonElevatedFill)
                .shellClickable(onClose),
            contentAlignment = Alignment.Center,
        ) {
            ShellIcon(
                res = R.drawable.ic_close_outline_16,
                size = SelectCloseIconSize,
                tint = colors.labelPrimary,
            )
        }
    }
}

@Composable
private fun SelectionActionBar(
    strings: ShellStrings,
    enabled: Boolean,
    onPin: () -> Unit,
    onDelete: () -> Unit,
) {
    val colors = OdTheme.colors
    val pinColor = if (enabled) colors.labelPrimary else colors.labelDimmed
    val deleteColor = if (enabled) colors.stateErrorPrimary else colors.labelDimmed
    Column(Modifier.fillMaxWidth()) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(SelectActionHairline)
                .background(colors.divider),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(SelectActionHeight),
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .shellClickable(enabled, onPin),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ShellIcon(
                    res = R.drawable.ic_pin_outline_20,
                    size = 20.dp,
                    tint = pinColor,
                )
                Spacer(Modifier.width(8.dp))
                Text(text = strings.pin, style = OdType.selectionAction, color = pinColor)
            }
            Box(
                Modifier
                    .width(SelectActionDividerWidth)
                    .height(SelectActionHeight)
                    .background(colors.divider),
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .shellClickable(enabled, onDelete),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ShellIcon(
                    res = R.drawable.ic_trash_outline_20,
                    size = 20.dp,
                    tint = deleteColor,
                )
                Spacer(Modifier.width(8.dp))
                Text(text = strings.delete, style = OdType.selectionAction, color = deleteColor)
            }
        }
    }
}

@Composable
private fun UserArea(name: String, onClick: () -> Unit) {
    val colors = OdTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(OdDimens.drawerRowHeight)
            .shellClickable(onClick)
            .padding(horizontal = OdDimens.drawerSearchMargin),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(AvatarSize)
                .clip(CircleShape),
        ) {
            Image(
                painter = painterResource(R.drawable.ic_profile),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Spacer(Modifier.width(AvatarGap))
        Text(
            text = name,
            modifier = Modifier.weight(1f),
            style = OdType.drawerSession,
            color = colors.labelPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.width(8.dp))
        ShellIcon(res = R.drawable.ic_ellipsis, size = EllipsisSize, tint = colors.labelTertiary)
    }
}
