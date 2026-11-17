package com.opends.chat.render

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.google.zxing.qrcode.encoder.Encoder
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

private const val SHARE_RENDER_TIMEOUT_MS = 20_000L
private const val SHARE_MAX_PIXELS = 64_000_000L
private const val SHARE_WIDTH_DP = 380f

fun renderShareImage(context: Context, content: ShareImageContent, widthPx: Int = 1080): Bitmap? {
    if (widthPx <= 0 || content.groups.isEmpty()) return null
    if (Looper.myLooper() == Looper.getMainLooper()) return null
    val latch = CountDownLatch(1)
    val result = AtomicReference<Bitmap?>()
    Handler(Looper.getMainLooper()).post {
        try {
            renderShareImageOnMain(context, content, widthPx) { bitmap ->
                result.set(bitmap)
                latch.countDown()
            }
        } catch (error: Throwable) {
            result.set(null)
            latch.countDown()
        }
    }
    return if (latch.await(SHARE_RENDER_TIMEOUT_MS, TimeUnit.MILLISECONDS)) result.get() else null
}

fun renderShareImageAsync(
    context: Context,
    content: ShareImageContent,
    widthPx: Int = 1080,
    onRendered: (Bitmap?) -> Unit,
) {
    val action = {
        try {
            renderShareImageOnMain(context, content, widthPx, onRendered)
        } catch (error: Throwable) {
            onRendered(null)
        }
    }
    if (Looper.myLooper() == Looper.getMainLooper()) {
        action()
    } else {
        Handler(Looper.getMainLooper()).post(action)
    }
}

private fun renderShareImageOnMain(
    context: Context,
    content: ShareImageContent,
    widthPx: Int,
    onRendered: (Bitmap?) -> Unit,
) {
    val activity = context.findActivity()
    val decor = activity?.window?.decorView as? ViewGroup
    if (activity == null || decor == null) {
        onRendered(null)
        return
    }
    val host = FrameLayout(activity).apply { alpha = 0f }
    val composeView = ComposeView(activity)
    host.addView(
        composeView,
        FrameLayout.LayoutParams(widthPx, 1),
    )
    var finished = false
    var timeout: Runnable? = null
    val mainHandler = Handler(Looper.getMainLooper())
    fun finish(bitmap: Bitmap?) {
        if (finished) return
        finished = true
        timeout?.let { mainHandler.removeCallbacks(it) }
        runCatching { decor.removeView(host) }
        onRendered(bitmap)
    }
    val timeoutAction = Runnable { finish(null) }
    timeout = timeoutAction
    mainHandler.postDelayed(timeoutAction, SHARE_RENDER_TIMEOUT_MS)
    val density = Density(density = widthPx / SHARE_WIDTH_DP, fontScale = 1f)
    composeView.setContent {
        CompositionLocalProvider(LocalDensity provides density) {
            OdShareImage(content)
            LaunchedEffect(Unit) {
                withFrameNanos { }
                composeView.post {
                    finish(captureShareImage(composeView, widthPx, content.dark))
                }
            }
        }
    }
    runCatching {
        decor.addView(
            host,
            FrameLayout.LayoutParams(widthPx, ViewGroup.LayoutParams.WRAP_CONTENT),
        )
    }.onFailure { finish(null) }
}

private fun captureShareImage(view: View, widthPx: Int, dark: Boolean): Bitmap? {
    val widthSpec = View.MeasureSpec.makeMeasureSpec(widthPx, View.MeasureSpec.EXACTLY)
    val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    view.measure(widthSpec, heightSpec)
    val height = view.measuredHeight
    if (height <= 0) return null
    if (height > SHARE_IMAGE_MAX_HEIGHT_PX) return null
    if (widthPx.toLong() * height.toLong() > SHARE_MAX_PIXELS) return null
    view.layout(0, 0, widthPx, height)
    return runCatching {
        val bitmap = Bitmap.createBitmap(widthPx, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(if (dark) 0xFF0F0F0F.toInt() else 0xFFFFFFFF.toInt())
        view.draw(canvas)
        bitmap
    }.getOrNull()
}

internal fun generateShareQrBitmap(
    payload: String,
    sizePx: Int,
    moduleColorArgb: Int,
    backgroundColorArgb: Int,
): Bitmap? {
    if (payload.isBlank() || sizePx <= 0) return null
    return runCatching {
        val code = Encoder.encode(payload, ErrorCorrectionLevel.M)
        val matrix = code.matrix
        val modules = matrix.width
        val quietZone = 2
        val modulePx = sizePx / (modules + quietZone * 2)
        if (modulePx <= 0) return null
        val rendered = modulePx * (modules + quietZone * 2)
        val offset = (sizePx - rendered) / 2
        val pixels = IntArray(sizePx * sizePx) { backgroundColorArgb }
        for (y in 0 until modules) {
            for (x in 0 until modules) {
                if (matrix.get(x, y).toInt() != 1) continue
                val startX = offset + (x + quietZone) * modulePx
                val startY = offset + (y + quietZone) * modulePx
                for (dy in 0 until modulePx) {
                    val rowStart = (startY + dy) * sizePx + startX
                    for (dx in 0 until modulePx) {
                        pixels[rowStart + dx] = moduleColorArgb
                    }
                }
            }
        }
        Bitmap.createBitmap(pixels, sizePx, sizePx, Bitmap.Config.ARGB_8888)
    }.getOrNull()
}

private fun Context.findActivity(): Activity? {
    var current: Context = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return if (current is Activity) current else null
}
