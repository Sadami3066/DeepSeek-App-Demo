package com.opends.chat.render

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import ru.noties.jlatexmath.JLatexMathAndroid
import ru.noties.jlatexmath.JLatexMathDrawable

internal class InlineMathContent(
    val width: TextUnit,
    val height: TextUnit,
    val verticalAlign: PlaceholderVerticalAlign,
    val content: @Composable () -> Unit,
)

internal class InlineMathSize(val widthPx: Float, val heightPx: Float)

internal object MathInlineSizing {
    fun compute(widthPx: Float, heightPx: Float, lineHeightPx: Float): InlineMathSize? {
        if (widthPx <= 1f || heightPx <= 1f || lineHeightPx <= 0f) return null
        val scale = if (heightPx > lineHeightPx) lineHeightPx / heightPx else 1f
        val scaledWidth = widthPx * scale
        if (scaledWidth <= 1f) return null
        return InlineMathSize(scaledWidth, heightPx * scale)
    }
}

internal val LocalInlineMathProvider = staticCompositionLocalOf<(String) -> InlineMathContent?> { { null } }

internal object MathBitmapRenderer {
    private var initialized = false
    private val cache = object : android.util.LruCache<String, Bitmap>(8 * 1024) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount / 1024
    }

    fun cached(
        latex: String,
        textSizePx: Float,
        colorArgb: Int,
        displayStyle: Boolean,
        styled: Boolean = true,
    ): Bitmap? = cache.get(cacheKey(latex.trim(), textSizePx, colorArgb, displayStyle, styled))

    fun render(
        context: Context,
        latex: String,
        textSizePx: Float,
        colorArgb: Int,
        displayStyle: Boolean,
        styled: Boolean = true,
    ): Bitmap? {
        val source = latex.trim()
        if (source.isEmpty()) return null
        val key = cacheKey(source, textSizePx, colorArgb, displayStyle, styled)
        cache.get(key)?.let { return it }
        if (!initialized) {
            runCatching { JLatexMathAndroid.init(context.applicationContext) }
            initialized = true
        }
        val wrapped = when {
            !styled -> source
            displayStyle -> "{\\displaystyle $source}"
            else -> "{\\textstyle $source}"
        }
        val bitmap = runCatching { drawFormula(wrapped, textSizePx, colorArgb) }.getOrNull() ?: return null
        if (bitmap.width <= 1 || bitmap.height <= 1 || !hasVisiblePixels(bitmap)) {
            bitmap.recycle()
            return null
        }
        cache.put(key, bitmap)
        return bitmap
    }

    private fun hasVisiblePixels(bitmap: Bitmap): Boolean {
        val stepX = (bitmap.width / 24).coerceAtLeast(1)
        val stepY = (bitmap.height / 24).coerceAtLeast(1)
        var y = 0
        while (y < bitmap.height) {
            var x = 0
            while (x < bitmap.width) {
                if (((bitmap.getPixel(x, y) ushr 24) and 0xFF) > 8) return true
                x += stepX
            }
            y += stepY
        }
        return false
    }

    private fun drawFormula(latex: String, textSizePx: Float, colorArgb: Int): Bitmap {
        val drawable = JLatexMathDrawable.builder(latex)
            .textSize(textSizePx)
            .color(colorArgb)
            .align(JLatexMathDrawable.ALIGN_LEFT)
            .build()
        val width = drawable.intrinsicWidth.coerceAtLeast(1)
        val height = drawable.intrinsicHeight.coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, width, height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun cacheKey(
        latex: String,
        textSizePx: Float,
        colorArgb: Int,
        displayStyle: Boolean,
        styled: Boolean,
    ): String =
        "$colorArgb|$textSizePx|$displayStyle|$styled|${latex.hashCode()}|${latex.length}"
}

@Composable
internal fun rememberInlineMathProvider(
    colors: MarkdownColors,
    typography: MarkdownTypography,
    contentColor: Color,
): (String) -> InlineMathContent? {
    val context = LocalContext.current
    val density = LocalDensity.current
    val textSizePx = with(density) { typography.formulaInlineFontSize.toPx() }
    val lineHeightPx = with(density) { typography.body.lineHeight.toPx() }
    val colorArgb = contentColor.toArgb()
    return remember(colors.isDark, textSizePx, lineHeightPx, colorArgb, typography) {
        { latex: String ->
            val source = latex.trim()
            var bitmap = MathBitmapRenderer.cached(source, textSizePx, colorArgb, displayStyle = false)
                ?: MathBitmapRenderer.render(context, source, textSizePx, colorArgb, displayStyle = false)
            if (bitmap == null) {
                bitmap = MathBitmapRenderer.cached(
                    source,
                    textSizePx,
                    colorArgb,
                    displayStyle = false,
                    styled = false,
                ) ?: MathBitmapRenderer.render(
                    context,
                    source,
                    textSizePx,
                    colorArgb,
                    displayStyle = false,
                    styled = false,
                )
            }
            val size = bitmap?.let {
                MathInlineSizing.compute(
                    widthPx = it.width.toFloat(),
                    heightPx = it.height.toFloat(),
                    lineHeightPx = lineHeightPx,
                )
            }
            if (bitmap != null && size != null) {
                InlineMathContent(
                    width = with(density) { size.widthPx.toSp() },
                    height = with(density) { size.heightPx.toSp() },
                    verticalAlign = PlaceholderVerticalAlign.Center,
                ) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = latex,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            } else {
                null
            }
        }
    }
}

@Composable
internal fun MathBlockView(latex: String, closed: Boolean) {
    val context = LocalContext.current
    val colors = LocalMarkdownColors.current
    val typography = LocalMarkdownTypography.current
    val density = LocalDensity.current
    val textSizePx = with(density) { typography.formulaBlockFontSize.toPx() }
    val colorArgb = markdownTextColor().toArgb()
    val streaming = LocalMarkdownStreaming.current
    var lastGood by remember { mutableStateOf<Bitmap?>(null) }
    val renderAllowed = closed || !streaming
    val current = if (renderAllowed) {
        remember(latex, textSizePx, colorArgb) {
            MathBitmapRenderer.render(context, latex, textSizePx, colorArgb, displayStyle = true)
                ?: MathBitmapRenderer.render(
                    context,
                    latex,
                    textSizePx,
                    colorArgb,
                    displayStyle = true,
                    styled = false,
                )
        }
    } else {
        null
    }
    if (current != null) {
        SideEffect { lastGood = current }
    }
    val bitmap = current ?: lastGood
    if (bitmap == null) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
                text = latex.ifBlank { "公式" },
                style = typography.small.merge(TextStyle(color = colors.tertiary)),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        return
    }
    val widthDp = with(density) { bitmap.width.toFloat().toDp() }
    val heightDp = with(density) { bitmap.height.toFloat().toDp() }
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                vertical = typography.formulaBlockPaddingVertical,
                horizontal = typography.formulaBlockPaddingHorizontal,
            ),
        contentAlignment = Alignment.Center,
    ) {
        val image: @Composable () -> Unit = {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = latex,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .width(widthDp)
                    .height(heightDp),
            )
        }
        if (widthDp > maxWidth) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
            ) {
                image()
            }
        } else {
            image()
        }
    }
}
