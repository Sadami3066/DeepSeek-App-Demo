package com.opends.chat.feature.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opends.chat.design.R
import com.opends.chat.design.motion.OdFluidBlob
import com.opends.chat.design.motion.OdVoicePalettes
import com.opends.chat.design.theme.OdTheme

internal data class ShellVoice(
    val voiceId: String,
    val nameOf: (ShellStrings) -> String,
    val descOf: (ShellStrings) -> String,
    val palette: List<Color>,
)

/*
 * 只保留官方 TTS 的两个原生音色：
 *   海星 = stella（女声） / 暗潮 = tide（男声）
 * 「贝壳」「白浪」是靠变调伪造出来的，部分设备会整轨无声，已下线。
 */
internal val ShellVoices = listOf(
    ShellVoice(
        voiceId = "stella",
        nameOf = { it.voiceStella },
        descOf = { it.voiceStellaDesc },
        palette = OdVoicePalettes.stella,
    ),
    ShellVoice(
        voiceId = "tide",
        nameOf = { it.voiceTide },
        descOf = { it.voiceTideDesc },
        palette = OdVoicePalettes.tide,
    ),
)

private val BlobWidth = 166.1.dp
private val BlobHeight = 92.2.dp
private val DotSize = 8.dp
private val DotGap = 6.dp
private val ConfirmHeight = 44.dp

@Composable
internal fun VoiceSheet(
    visible: Boolean,
    strings: ShellStrings,
    currentVoiceId: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    onAudition: (String) -> Unit,
) {
    val colors = OdTheme.colors
    val initialPage = remember {
        ShellVoices.indexOfFirst { it.voiceId == currentVoiceId }.coerceAtLeast(0)
    }
    val pagerState = rememberPagerState(initialPage = initialPage) { ShellVoices.size }
    ShellBottomSheet(visible = visible, onDismiss = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = strings.voiceName,
                fontSize = 17.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Medium,
                color = colors.labelPrimary,
            )
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 4.3.dp)
                    .size(34.5.dp)
                    .clip(CircleShape)
                    .background(colors.bgLayer3)
                    .shellClickable(onDismiss),
                contentAlignment = Alignment.Center,
            ) {
                ShellIcon(
                    res = R.drawable.ic_close_outline_16,
                    size = 16.dp,
                    tint = colors.iconPrimary,
                )
            }
        }
        Spacer(Modifier.height(133.1.dp))
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth()) { index ->
            val voice = ShellVoices[index]
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .width(BlobWidth)
                        .height(BlobHeight)
                        .clip(RoundedCornerShape(24.dp))
                        .shellClickable { onAudition(voice.voiceId) },
                ) {
                    OdFluidBlob(
                        colors = voice.palette,
                        modifier = Modifier.fillMaxSize(),
                        animate = visible && pagerState.currentPage == index,
                    )
                }
                Spacer(Modifier.height(15.1.dp))
                Text(
                    text = voice.nameOf(strings),
                    fontSize = 18.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.labelPrimary,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = voice.descOf(strings),
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = colors.labelTertiary,
                    textAlign = TextAlign.Center,
                )
            }
        }
        Spacer(Modifier.height(32.1.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            ShellVoices.indices.forEach { index ->
                if (index > 0) {
                    Spacer(Modifier.width(DotGap))
                }
                Box(
                    modifier = Modifier
                        .size(DotSize)
                        .clip(CircleShape)
                        .background(
                            if (pagerState.currentPage == index) colors.brandPrimary else colors.labelTertiary,
                        ),
                )
            }
        }
        Spacer(Modifier.height(92.1.dp))
        val selected = ShellVoices[pagerState.currentPage]
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 22.2.dp, end = 22.5.dp)
                .height(ConfirmHeight)
                .clip(RoundedCornerShape(ConfirmHeight / 2))
                .background(colors.brandPrimary)
                .shellClickable { onConfirm(selected.voiceId) },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = strings.voiceConfirm,
                fontSize = 16.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Medium,
                color = colors.labelPrimaryForeground,
            )
        }
        Spacer(Modifier.height(14.8.dp))
    }
}
