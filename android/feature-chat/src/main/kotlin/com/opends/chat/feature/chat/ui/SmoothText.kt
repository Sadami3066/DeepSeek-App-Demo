package com.opends.chat.feature.chat.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import kotlin.math.min

@Composable
internal fun rememberSmoothedText(target: String, streaming: Boolean): String {
    var shownLength by remember { mutableIntStateOf(if (streaming) 0 else target.length) }
    val latest by rememberUpdatedState(target)
    LaunchedEffect(streaming) {
        if (!streaming) {
            shownLength = latest.length
            return@LaunchedEffect
        }
        var lastFrame = withFrameNanos { it }
        while (true) {
            val frame = withFrameNanos { it }
            val elapsed = ((frame - lastFrame) / 1_000_000_000f).coerceIn(0f, 0.1f)
            lastFrame = frame
            val text = latest
            if (shownLength > text.length) shownLength = text.length
            val backlog = text.length - shownLength
            if (backlog <= 0) continue
            val charsPerSecond = when {
                backlog > 1200 -> 420f
                backlog > 480 -> 240f
                backlog > 180 -> 130f
                backlog > 60 -> 85f
                else -> 55f
            }
            val step = (charsPerSecond * elapsed).toInt().coerceAtLeast(1)
            shownLength = min(text.length, shownLength + step)
        }
    }
    return if (!streaming) target else target.take(shownLength.coerceIn(0, target.length))
}
