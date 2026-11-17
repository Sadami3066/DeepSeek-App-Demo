package com.opends.chat.feature.chat.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opends.chat.design.theme.OdTheme
import kotlin.math.min

enum class VoiceOverlayPhase { RECORDING, CANCEL, RECOGNIZING }

private val MaskHeight = 351.dp
private val LabelWaveformSpacing = 6.dp
private val WaveformBottomOffset = 101.dp
private val WaveformWidth = 272.dp
private val WaveformHeight = 54.dp
private val BarWidth = 2.dp
private val BarGap = 4.dp
private const val BarCount = 46
private val MinBarHeight = 6.dp

private val RecordingLabelColor = Color(0xE6FFFFFF)
private val CancelLabelColor = Color(0xFFFF6969)

private val BlueGradient = arrayOf(
    0.00f to Color(0x00FFFFFF),
    0.20f to Color(0x00E1ECFF),
    0.30f to Color(0x0B8CB9FF),
    0.50f to Color(0x718CB9FF),
    0.70f to Color(0xF78CB9FF),
    0.85f to Color(0xFF70A8FF),
    1.00f to Color(0xFF65A2FF),
)

private val RedGradient = arrayOf(
    0.00f to Color(0x00FFFFFF),
    0.20f to Color(0x00FFF4F4),
    0.30f to Color(0x0AFFB4B4),
    0.50f to Color(0x4BFFB4B4),
    0.70f to Color(0xABFFB4B4),
    0.90f to Color(0xE0FFB4B4),
    1.00f to Color(0xF8FFB4B4),
)

@Composable
fun VoiceInputOverlay(
    phase: VoiceOverlayPhase,
    remainingSeconds: Int,
    amplitudes: List<Float>,
    modifier: Modifier = Modifier,
) {
    val cancel = phase == VoiceOverlayPhase.CANCEL
    val label = when (phase) {
        VoiceOverlayPhase.RECOGNIZING -> "识别中"
        VoiceOverlayPhase.CANCEL -> "松手取消"
        VoiceOverlayPhase.RECORDING -> if (remainingSeconds in 1..10) {
            "录音将在 $remainingSeconds 秒后结束"
        } else {
            "松手发送，上滑取消"
        }
    }
    val barColor = if (cancel) Color(0xD9FFD7D7) else Color(0xD9FFFFFF)
    Box(modifier = modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(MaskHeight),
        ) {
            drawRect(
                brush = Brush.verticalGradient(
                    colorStops = if (cancel) RedGradient else BlueGradient,
                ),
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = WaveformBottomOffset),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = label,
                color = if (cancel) CancelLabelColor else RecordingLabelColor,
                fontSize = 12.sp,
                lineHeight = 18.sp,
            )
            Spacer(Modifier.height(LabelWaveformSpacing))
            if (phase == VoiceOverlayPhase.RECOGNIZING) {
                Box(
                    modifier = Modifier
                        .width(WaveformWidth)
                        .height(WaveformHeight),
                    contentAlignment = Alignment.Center,
                ) {
                    LoadingSpinner(
                        size = 22.dp,
                        color = if (cancel) CancelLabelColor else RecordingLabelColor,
                    )
                }
            } else {
                VoiceWaveform(amplitudes = amplitudes, color = barColor)
            }
        }
    }
}

@Composable
private fun VoiceWaveform(
    amplitudes: List<Float>,
    color: Color,
) {
    Row(
        modifier = Modifier
            .width(WaveformWidth)
            .height(WaveformHeight),
        horizontalArrangement = Arrangement.spacedBy(BarGap),
        verticalAlignment = Alignment.Bottom,
    ) {
        repeat(BarCount) { index ->
            val level = levelAt(index, amplitudes)
            val edge = min(index, BarCount - 1 - index)
            val edgeScale = if (edge >= 4) 1f else 0.2f + 0.8f * (edge / 4f)
            val target = (MinBarHeight.value + level * (WaveformHeight.value - MinBarHeight.value)) * edgeScale
            val height by animateFloatAsState(
                targetValue = target,
                animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
                label = "voiceBar$index",
            )
            Box(
                modifier = Modifier
                    .width(BarWidth)
                    .height(height.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(color),
            )
        }
    }
}

private fun levelAt(index: Int, amplitudes: List<Float>): Float {
    if (amplitudes.isEmpty()) return 0f
    val source = (index.toFloat() / BarCount * amplitudes.size).toInt().coerceIn(0, amplitudes.lastIndex)
    return amplitudes[source].coerceIn(0f, 1f)
}
