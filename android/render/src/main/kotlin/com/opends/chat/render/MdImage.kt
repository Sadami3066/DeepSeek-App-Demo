package com.opends.chat.render

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize

internal object ImageLoader {
    private val cache = object : android.util.LruCache<String, Bitmap>(16 * 1024) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount / 1024
    }

    fun cached(key: String): Bitmap? = cache.get(key)

    suspend fun load(context: android.content.Context, url: String): Bitmap? {
        cache.get(url)?.let { return it }
        val bitmap = withContext(Dispatchers.IO) {
            runCatching { decode(context, url) }.getOrNull()
        } ?: return null
        cache.put(url, bitmap)
        return bitmap
    }

    private fun decode(context: android.content.Context, url: String): Bitmap? {
        val bytes = readBytes(context, url) ?: return null
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        val width = bounds.outWidth
        val height = bounds.outHeight
        if (width <= 0 || height <= 0) return null
        var sample = 1
        while (width / (sample * 2) >= 1080 && height / (sample * 2) >= 540) sample *= 2
        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
    }

    private fun readBytes(context: android.content.Context, url: String): ByteArray? {
        return when {
            url.startsWith("http://") || url.startsWith("https://") -> {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.connectTimeout = 10_000
                connection.readTimeout = 15_000
                connection.instanceFollowRedirects = true
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 OpenDS")
                try {
                    connection.inputStream.use { it.readBytes() }
                } finally {
                    connection.disconnect()
                }
            }
            url.startsWith("content://") -> {
                context.contentResolver.openInputStream(Uri.parse(url))?.use { it.readBytes() }
            }
            url.startsWith("file://") -> File(Uri.parse(url).path ?: return null).takeIf { it.exists() }?.readBytes()
            else -> File(url).takeIf { it.exists() }?.readBytes()
        }
    }
}

@Composable
internal fun MdImage(url: String, alt: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val colors = LocalMarkdownColors.current
    val typography = LocalMarkdownTypography.current
    var bitmap by remember(url) { mutableStateOf(ImageLoader.cached(url)) }
    var failed by remember(url) { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(url) {
        if (bitmap == null) {
            val loaded = ImageLoader.load(context, url)
            if (loaded == null) failed = true else bitmap = loaded
        }
    }

    val image = bitmap
    when {
        image != null -> Image(
            bitmap = image.asImageBitmap(),
            contentDescription = alt.ifBlank { null },
            contentScale = ContentScale.FillWidth,
            modifier = modifier
                .fillMaxWidth()
                .heightIn(max = 480.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable { runCatching { uriHandler.openUri(url) } },
        )
        failed -> Text(
            text = alt.ifBlank { url },
            style = typography.body.merge(TextStyle(color = colors.brand, textDecoration = TextDecoration.Underline)),
            modifier = modifier.clickable { runCatching { uriHandler.openUri(url) } },
        )
        else -> Box(
            modifier = modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.placeholderBackground),
        )
    }
}
