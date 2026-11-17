package com.opends.chat.render

import android.os.SystemClock
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.withFrameNanos
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

enum class OdMarkdownVariant { Body, Small }

@Composable
fun OdMarkdown(
    text: String,
    modifier: Modifier = Modifier,
    isStreaming: Boolean = false,
    variant: OdMarkdownVariant = OdMarkdownVariant.Body,
    contentColor: Color = Color.Unspecified,
    onCitationClick: ((Int) -> Unit)? = null,
    typography: MarkdownTypography? = null,
) {
    val colors = markdownColors()
    val resolvedTypography = typography ?: if (variant == OdMarkdownVariant.Small) {
        SmallMarkdownTypography
    } else {
        BodyMarkdownTypography
    }
    val resolvedColor = when {
        contentColor != Color.Unspecified -> contentColor
        variant == OdMarkdownVariant.Small -> colors.secondary
        else -> colors.text
    }
    val mathProvider = rememberInlineMathProvider(colors, resolvedTypography, resolvedColor)
    val citation = rememberStableCitationHandler(onCitationClick)
    CompositionLocalProvider(
        LocalMarkdownColors provides colors,
        LocalMarkdownTypography provides resolvedTypography,
        LocalInlineMathProvider provides mathProvider,
        LocalMarkdownStreaming provides isStreaming,
        LocalMarkdownTextColor provides resolvedColor,
        LocalCitationHandler provides citation,
    ) {
        val shownText = rememberStreamingMarkdown(text, isStreaming)
        val displayText = remember(shownText, isStreaming) {
            if (isStreaming) trimIncompleteMarkdownTail(shownText) else shownText
        }
        val parser = remember { MarkdownParser() }
        val normalizedText = remember(displayText) { normalizeCitationPunctuation(displayText) }
        val blocks = remember(normalizedText) { parser.parse(normalizedText) }
        MdBlocksProgressive(blocks = blocks, progressive = !isStreaming, modifier = modifier)
    }
}

@Composable
fun OdInlineMarkdown(
    text: String,
    modifier: Modifier = Modifier,
    variant: OdMarkdownVariant = OdMarkdownVariant.Body,
    contentColor: Color = Color.Unspecified,
    onCitationClick: ((Int) -> Unit)? = null,
    typography: MarkdownTypography? = null,
) {
    val colors = markdownColors()
    val resolvedTypography = typography ?: if (variant == OdMarkdownVariant.Small) {
        SmallMarkdownTypography
    } else {
        BodyMarkdownTypography
    }
    val resolvedColor = when {
        contentColor != Color.Unspecified -> contentColor
        variant == OdMarkdownVariant.Small -> colors.secondary
        else -> colors.text
    }
    val mathProvider = rememberInlineMathProvider(colors, resolvedTypography, resolvedColor)
    val citation = rememberStableCitationHandler(onCitationClick)
    CompositionLocalProvider(
        LocalMarkdownColors provides colors,
        LocalMarkdownTypography provides resolvedTypography,
        LocalInlineMathProvider provides mathProvider,
        LocalMarkdownTextColor provides resolvedColor,
        LocalCitationHandler provides citation,
    ) {
        MdInlineText(raw = text, style = resolvedTypography.body.merge(TextStyle(color = resolvedColor)), modifier = modifier)
    }
}

@Composable
private fun rememberStreamingMarkdown(text: String, isStreaming: Boolean): String {
    var shown by remember { mutableStateOf(text) }
    val latest = rememberUpdatedState(text)
    val streaming = rememberUpdatedState(isStreaming)
    LaunchedEffect(isStreaming) {
        if (!isStreaming) {
            shown = latest.value
            return@LaunchedEffect
        }
        var lastUpdate = 0L
        while (isActive) {
            delay(STREAMING_FRAME_MS)
            if (!streaming.value) {
                shown = latest.value
                continue
            }
            val now = SystemClock.uptimeMillis()
            val target = latest.value
            if (target != shown && now - lastUpdate >= STREAMING_MIN_INTERVAL_MS) {
                shown = target
                lastUpdate = now
            }
        }
    }
    return if (isStreaming) shown else text
}

@Composable
private fun rememberStableCitationHandler(handler: ((Int) -> Unit)?): ((Int) -> Unit)? {
    if (handler == null) return null
    val latest = rememberUpdatedState(handler)
    return remember { { number: Int -> latest.value?.invoke(number) } }
}

@Composable
private fun MdBlocksProgressive(
    blocks: List<MdBlock>,
    progressive: Boolean,
    modifier: Modifier,
) {
    if (!progressive || blocks.size <= INITIAL_BLOCK_BUDGET) {
        MdBlocksContent(blocks = blocks, modifier = modifier)
        return
    }
    var visible by remember { mutableIntStateOf(INITIAL_BLOCK_BUDGET) }
    LaunchedEffect(blocks) {
        while (visible < blocks.size) {
            withFrameNanos { }
            visible = minOf(blocks.size, visible + BLOCK_RENDER_BATCH)
        }
    }
    val shown = if (visible >= blocks.size) blocks else blocks.subList(0, minOf(visible, blocks.size))
    MdBlocksContent(blocks = shown, modifier = modifier)
}

@Composable
internal fun MdInlineText(
    raw: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    textAlign: androidx.compose.ui.text.style.TextAlign? = null,
) {
    val colors = LocalMarkdownColors.current
    val typography = LocalMarkdownTypography.current
    val mathProvider = LocalInlineMathProvider.current
    val citationHandler = LocalCitationHandler.current
    val textColor = markdownTextColor()
    val context = remember(colors, typography, mathProvider, citationHandler, textColor) {
        InlineRenderContext(
            colors = colors,
            typography = typography,
            strongWeight = typography.strongWeight,
            textColor = textColor,
            inlineMath = mathProvider,
            citationHandler = citationHandler,
        )
    }
    val prepared = remember(raw) { normalizeCitationPunctuation(raw) }
    val rendered = remember(prepared, context) { MdInlineParser.parse(prepared, context) }
    val staticStyle = remember(style) {
        style.merge(TextStyle(textMotion = androidx.compose.ui.text.style.TextMotion.Static))
    }
    androidx.compose.material3.Text(
        text = rendered.text,
        inlineContent = rendered.inlineContent,
        style = staticStyle,
        textAlign = textAlign,
        modifier = modifier,
    )
}

internal fun trimIncompleteMarkdownTail(text: String): String {
    var result = text
    result = removeUnmatchedRuns(result, '*', 1)
    result = removeUnmatchedRuns(result, '_', 1)
    result = removeUnmatchedRuns(result, '~', 2)
    result = removeUnmatchedBacktickRun(result)
    result = removeUnmatchedDollar(result)
    if (result.endsWith("\\")) result = result.dropLast(1)
    return result
}

private fun removeUnmatchedRuns(text: String, marker: Char, minLength: Int): String {
    val runs = ArrayList<IntRange>()
    var index = 0
    while (index < text.length) {
        if (text[index] != marker) {
            index++
            continue
        }
        val start = index
        while (index < text.length && text[index] == marker) index++
        runs.add(start until index)
    }
    if (runs.size % 2 == 0) return text
    val run = runs.last()
    if (run.last - run.first + 1 < minLength) return text
    val next = run.last + 1
    if (next >= text.length || text[next].isWhitespace()) return text
    return text.removeRange(run.first, run.last + 1)
}

private fun removeUnmatchedBacktickRun(text: String): String {
    val runs = ArrayList<IntRange>()
    var index = 0
    while (index < text.length) {
        if (text[index] != '`') {
            index++
            continue
        }
        val start = index
        while (index < text.length && text[index] == '`') index++
        runs.add(start until index)
    }
    if (runs.size % 2 == 0) return text
    val run = runs.last()
    return text.removeRange(run.first, run.last + 1)
}

private fun removeUnmatchedDollar(text: String): String {
    val positions = ArrayList<Int>()
    var index = 0
    while (index < text.length) {
        if (text[index] == '\\') {
            index += 2
            continue
        }
        if (text[index] == '$') positions.add(index)
        index++
    }
    if (positions.size % 2 == 0) return text
    val position = positions.last()
    if (position + 1 >= text.length || text[position + 1].isWhitespace()) return text
    return text.removeRange(position, position + 1)
}

private const val INITIAL_BLOCK_BUDGET = 48
private const val BLOCK_RENDER_BATCH = 36
private const val STREAMING_FRAME_MS = 16L
private const val STREAMING_MIN_INTERVAL_MS = 48L


internal fun normalizeCitationPunctuation(text: String): String {
    if (!text.contains("[citation:")) return text
    val sb = StringBuilder(text.length + 16)
    var i = 0
    while (i < text.length) {
        if (text.startsWith("[citation:", i)) {
            val runStart = i
            var j = i
            var valid = false
            while (true) {
                val close = text.indexOf(']', j + 10)
                if (close < 0) break
                if (text.substring(j + 10, close).toIntOrNull() == null) break
                j = close + 1
                valid = true
                var k = j
                while (k < text.length && (text[k] == ' ' || text[k] == '\u2060' || text[k] == '\u200b')) k++
                if (!text.startsWith("[citation:", k)) break
                j = k
            }
            if (valid) {
                var k = j
                while (k < text.length && (text[k].isWhitespace() || text[k] == '\u2060')) k++
                val following = text.getOrNull(k)
                if (following != null && following in "。，、；：！？") {
                    val markers = text.substring(runStart, j)
                        .replace(" ", "")
                        .replace("\u2060", "")
                        .replace("\u200b", "")
                    sb.append(following).append('\u2060')
                    sb.append(markers.replace("][citation:", "]\u2060[citation:"))
                    sb.append(text, j, k)
                    i = k + 1
                    continue
                }
                sb.append(text, runStart, j)
                i = j
                continue
            }
        }
        sb.append(text[i])
        i++
    }
    return sb.toString()
}
