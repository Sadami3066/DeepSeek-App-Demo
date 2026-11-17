package com.opends.chat.feature.chat.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opends.chat.design.R
import com.opends.chat.design.motion.OdLottieAssets
import com.opends.chat.design.theme.OdDimens
import com.opends.chat.design.theme.OdTheme
import com.opends.chat.design.theme.OdType

val HomeGreetings = listOf(
    "欢迎回来，随时开始吧",
    "你好，有什么我能帮你的吗？",
    "你好，让我们开始聊天吧",
    "嗨！今天想聊些什么？",
    "欢迎回来，想从哪里开始？",
    "你好，我能帮什么忙吗？",
)

private val TopBarHotArea = 44.dp

@Composable
fun ChatTopBar(
    title: String?,
    autoRead: Boolean,
    onOpenSider: () -> Unit,
    onToggleAutoRead: () -> Unit,
    onNewChat: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = OdTheme.colors
    val startPadding = OdDimens.barStartPadding - (TopBarHotArea - OdDimens.topBarIcon) / 2
    val iconGap = OdDimens.topBarIconGap - (TopBarHotArea - OdDimens.topBarIcon)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(OdDimens.barHeight)
            .padding(start = startPadding, end = OdDimens.barEndPadding - (TopBarHotArea - OdDimens.topBarIcon) / 2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OdIconButton(
            res = R.drawable.ic_menu_outline_24,
            size = OdDimens.topBarIcon,
            boxSize = TopBarHotArea,
            onClick = onOpenSider,
            tint = palette.iconPrimary,
            description = "打开侧边栏",
        )
        if (title != null) {
            Spacer(Modifier.width(OdDimens.barTitleStart - TopBarHotArea - startPadding))
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = OdType.barTitle,
                color = palette.bodyText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.width(8.dp))
        } else {
            Spacer(Modifier.weight(1f))
        }
        LottieIcon(
            asset = if (autoRead) OdLottieAssets.autoPlayOn else OdLottieAssets.autoPlayOff,
            size = OdDimens.topBarIcon,
            boxSize = TopBarHotArea,
            tint = palette.iconPrimary,
            onClick = onToggleAutoRead,
            description = "自动朗读",
        )
        Spacer(Modifier.width(iconGap))
        OdIconButton(
            res = R.drawable.ic_new_chat_outline_24,
            size = OdDimens.topBarIcon,
            boxSize = TopBarHotArea,
            onClick = onNewChat,
            tint = palette.iconPrimary,
            description = "开启新对话",
        )
    }
}

@Composable
fun HomeGreeting(
    greeting: String,
    modifier: Modifier = Modifier,
) {
    val palette = OdTheme.colors
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(R.drawable.ic_deepfish_logo),
                contentDescription = "DeepFish",
                modifier = Modifier.width(64.dp),
            )
            Spacer(Modifier.height(19.26.dp))
            Text(
                text = greeting,
                style = OdType.greeting,
                color = palette.labelPrimary,
            )
        }
    }
}

@Composable
fun ScrollToBottomButton(
    visible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = OdTheme.colors
    if (!visible) return
    Box(
        modifier = modifier
            .size(44.3.dp)
            .clickableNoIndication(onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(29.5.dp)
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .background(if (OdTheme.isDark) palette.menu else palette.bgBase),
            contentAlignment = Alignment.Center,
        ) {
            OdIcon(
                res = R.drawable.ic_chevron_down_outline_20,
                size = 22.dp,
                tint = if (OdTheme.isDark) palette.iconPrimary else palette.bodyText,
            )
        }
    }
}
