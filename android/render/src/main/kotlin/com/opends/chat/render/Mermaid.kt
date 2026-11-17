package com.opends.chat.render

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.LruCache
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONObject
import java.util.concurrent.Executors
import kotlin.coroutines.resume

internal object MermaidEngine {
    private const val RENDER_TIMEOUT_MS = 20_000L
    private const val IDLE_DESTROY_MS = 60_000L
    private const val WEBVIEW_WIDTH = 1600
    private const val WEBVIEW_HEIGHT = 3000

    private val handler = Handler(Looper.getMainLooper())
    private val decoder = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "od-mermaid-decode").apply { isDaemon = true }
    }
    private val cache = object : LruCache<String, Bitmap>(24 * 1024) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount / 1024
    }
    private val queue = ArrayDeque<Pending>()
    private var current: Pending? = null
    private var webView: WebView? = null
    private var pageReady = false
    private var sequence = 0
    private var appContext: Context? = null

    private class Pending(
        val code: String,
        val dark: Boolean,
        val widthPx: Int,
        val callback: (Bitmap?) -> Unit,
        var requestId: Int = 0,
    )

    private val timeoutRunnable = Runnable { finishCurrent(null) }

    private val idleRunnable = Runnable {
        if (current == null) {
            webView?.destroy()
            webView = null
            pageReady = false
        }
    }

    fun cached(code: String, dark: Boolean, widthPx: Int): Bitmap? =
        cache.get(cacheKey(code, dark, widthPx))

    fun request(context: Context, code: String, dark: Boolean, widthPx: Int, callback: (Bitmap?) -> Unit) {
        if (code.isBlank()) {
            callback(null)
            return
        }
        cache.get(cacheKey(code, dark, widthPx))?.let {
            callback(it)
            return
        }
        appContext = context.applicationContext
        queue.addLast(Pending(code, dark, widthPx, callback))
        drain()
    }

    private fun drain() {
        if (current != null || queue.isEmpty()) return
        val pending = queue.removeFirst()
        current = pending
        val view = webView ?: createWebView(appContext ?: return finishCurrent(null))
        if (pageReady) {
            dispatch(pending)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun createWebView(context: Context): WebView {
        val view = WebView(context)
        view.setBackgroundColor(Color.TRANSPARENT)
        view.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            textZoom = 100
            builtInZoomControls = false
            displayZoomControls = false
            allowFileAccess = true
        }
        view.addJavascriptInterface(Bridge, "AndroidBridge")
        view.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                pageReady = true
                handler.post {
                    current?.let { dispatch(it) }
                }
            }
        }
        view.measure(
            View.MeasureSpec.makeMeasureSpec(WEBVIEW_WIDTH, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(WEBVIEW_HEIGHT, View.MeasureSpec.EXACTLY),
        )
        view.layout(0, 0, WEBVIEW_WIDTH, WEBVIEW_HEIGHT)
        view.loadUrl("file:///android_asset/generator.html")
        handler.removeCallbacks(idleRunnable)
        webView = view
        return view
    }

    private fun dispatch(pending: Pending) {
        val view = webView ?: return finishCurrent(null)
        pageReady = true
        pending.requestId = ++sequence
        handler.removeCallbacks(timeoutRunnable)
        handler.postDelayed(timeoutRunnable, RENDER_TIMEOUT_MS)
        val theme = if (pending.dark) "dark" else "light"
        val script = buildString {
            append("if (window.generateMermaid) { window.generateMermaid(")
            append(JSONObject.quote(pending.code))
            append(", ")
            append(JSONObject.quote(theme))
            append(", ")
            append(pending.widthPx)
            append(", ")
            append(WEBVIEW_HEIGHT)
            append("); }")
        }
        runCatching { view.evaluateJavascript(script, null) }
            .onFailure { finishCurrent(null) }
    }

    private fun onRendered(dataUrl: String?) {
        val pending = current ?: return
        if (dataUrl.isNullOrBlank()) {
            finishCurrent(null)
            return
        }
        val payload = dataUrl.substringAfter("base64,", dataUrl)
        val widthPx = pending.widthPx
        val dark = pending.dark
        val code = pending.code
        decoder.execute {
            val bitmap = runCatching {
                val bytes = Base64.decode(payload, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            }.getOrNull()
            handler.post {
                if (bitmap != null) cache.put(cacheKey(code, dark, widthPx), bitmap)
                finishCurrent(bitmap)
            }
        }
    }

    private fun onFailed() {
        finishCurrent(null)
    }

    private fun finishCurrent(bitmap: Bitmap?) {
        handler.removeCallbacks(timeoutRunnable)
        val pending = current
        current = null
        pending?.callback?.invoke(bitmap)
        drain()
        if (current == null) {
            handler.removeCallbacks(idleRunnable)
            handler.postDelayed(idleRunnable, IDLE_DESTROY_MS)
        }
    }

    private fun cacheKey(code: String, dark: Boolean, widthPx: Int): String =
        "${if (dark) 1 else 0}|$widthPx|${code.hashCode()}|${code.length}"

    private object Bridge {
        @JavascriptInterface
        fun onImageRendered(dataUrl: String?, diagramType: String?) {
            handler.post { onRendered(dataUrl) }
        }

        @JavascriptInterface
        fun onImageRenderFailed(reason: String?, message: String?) {
            handler.post { onFailed() }
        }
    }
}

@Composable
internal fun MermaidView(code: String, isStreaming: Boolean) {
    val context = LocalContext.current
    val colors = LocalMarkdownColors.current
    val typography = LocalMarkdownTypography.current
    val density = LocalDensity.current
    val screenWidthPx = with(density) { (androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp.dp).toPx() }
    val widthPx = screenWidthPx.toInt().coerceIn(480, 1600)
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var failed by remember { mutableStateOf(false) }
    var shownCode by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(code, isStreaming, colors.isDark, widthPx) {
        if (shownCode == code && bitmap != null) return@LaunchedEffect
        if (isStreaming) delay(350)
        val cached = MermaidEngine.cached(code, colors.isDark, widthPx)
        if (cached != null) {
            bitmap = cached
            shownCode = code
            failed = false
            return@LaunchedEffect
        }
        failed = false
        val result = suspendCancellableCoroutine<Bitmap?> { continuation ->
            MermaidEngine.request(context, code, colors.isDark, widthPx) { image ->
                if (continuation.isActive) continuation.resume(image)
            }
        }
        if (result != null) {
            bitmap = result
            shownCode = code
        } else if (!isStreaming && shownCode == null) {
            failed = true
        }
    }

    val current = bitmap
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        when {
            current != null -> Image(
                bitmap = current.asImageBitmap(),
                contentDescription = "Mermaid 图表",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth(),
            )
            failed -> Text(
                text = "图表渲染失败",
                style = typography.small.merge(androidx.compose.ui.text.TextStyle(color = colors.tertiary)),
            )
            else -> Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                Text(
                    text = if (isStreaming) "正在生成图表…" else "图表生成中…",
                    style = typography.small.merge(androidx.compose.ui.text.TextStyle(color = colors.tertiary)),
                )
            }
        }
    }
}
