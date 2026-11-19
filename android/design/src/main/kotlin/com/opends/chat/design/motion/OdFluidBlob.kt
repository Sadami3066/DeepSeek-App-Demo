package com.opends.chat.design.motion

import android.graphics.Paint
import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.sin

object OdVoicePalettes {
    /** 兜底色板（无音色时使用），沿用原「贝壳」的蓝调 */
    val mira: List<Color> = listOf(Color(0xFFE9F7FF), Color(0xFFA1D0F5), Color(0xFF698FD5))
    val stella: List<Color> = listOf(Color(0xFFFFE8CD), Color(0xFFFFE19E), Color(0xFFF7C15E))
    val tide: List<Color> = listOf(Color(0xFFB4D1D9), Color(0xFF59889B), Color(0xFF4F6478))
}

private const val FLUID_BLOB_SHADER = "shaders/fluid_blob.agsl"

private fun loadShaderSource(context: android.content.Context): String? =
    runCatching { context.assets.open(FLUID_BLOB_SHADER).bufferedReader().use { it.readText() } }.getOrNull()

private fun normalizeColors(colors: List<Color>): List<Color> {
    val base = if (colors.isEmpty()) OdVoicePalettes.mira else colors.take(3)
    return when (base.size) {
        1 -> listOf(base[0], base[0].copy(alpha = 0.6f), base[0].copy(alpha = 0.3f))
        2 -> base + base[1].copy(alpha = 0.7f)
        else -> base
    }
}

private object FluidBlobApi33 {
    fun create(source: String): RuntimeShader = RuntimeShader(source)

    fun update(
        shader: RuntimeShader,
        paint: Paint,
        colors: List<Color>,
        width: Float,
        height: Float,
        density: Float,
        time: Float,
    ) {
        shader.setFloatUniform("color1", colors[0].red, colors[0].green, colors[0].blue, colors[0].alpha)
        shader.setFloatUniform("color2", colors[1].red, colors[1].green, colors[1].blue, colors[1].alpha)
        shader.setFloatUniform("color3", colors[2].red, colors[2].green, colors[2].blue, colors[2].alpha)
        shader.setFloatUniform("resolution", width, height)
        shader.setFloatUniform("offset", 0f, 0.8f)
        shader.setFloatUniform("time", time)
        shader.setFloatUniform("pixelRatio", density)
        shader.setFloatUniform("scale", 1.74f)
        shader.setFloatUniform("rotation", 21f / 90f)
        shader.setFloatUniform("colorCount", 3f)
        shader.setFloatUniform("grain", 0f)
        shader.setFloatUniform("proportion", 0.45f)
        shader.setFloatUniform("softness", 1f)
        shader.setFloatUniform("shape", 0f)
        shader.setFloatUniform("shapeScale", 0.18f)
        shader.setFloatUniform("distortion", 0.55f)
        shader.setFloatUniform("swirl", 0.08f)
        shader.setFloatUniform("swirlIterations", 10f)
        paint.shader = shader
    }
}

@Composable
fun OdFluidBlob(
    colors: List<Color>,
    modifier: Modifier = Modifier,
    animate: Boolean = true,
) {
    val context = LocalContext.current
    val density = LocalDensity.current.density
    val palette = remember(colors) { normalizeColors(colors) }
    val shaderSource = remember(context) { loadShaderSource(context) }
    var time by remember { mutableFloatStateOf(0f) }
    if (animate) {
        LaunchedEffect(Unit) {
            val start = withFrameNanos { it }
            while (true) {
                withFrameNanos { frame ->
                    time = (frame - start) / 1_000_000_000f
                }
            }
        }
    }
    val runtimeShader = remember(shaderSource) {
        if (shaderSource != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            runCatching { FluidBlobApi33.create(shaderSource) }.getOrNull()
        } else {
            null
        }
    }
    if (runtimeShader != null) {
        val paint = remember(runtimeShader) { Paint() }
        Canvas(modifier = modifier) {
            runCatching {
                FluidBlobApi33.update(runtimeShader, paint, palette, size.width, size.height, density, time)
                drawIntoCanvas { canvas ->
                    canvas.nativeCanvas.drawRect(0f, 0f, size.width, size.height, paint)
                }
            }
        }
    } else {
        GradientBlob(colors = palette, modifier = modifier, animate = animate, time = time)
    }
}

@Composable
private fun GradientBlob(
    colors: List<Color>,
    modifier: Modifier,
    animate: Boolean,
    time: Float,
) {
    val phase = if (animate) time else 0.35f
    Canvas(modifier = modifier) {
        val radius = min(size.width, size.height)
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(colors[2], colors[1], colors[0]),
                start = Offset(0f, size.height),
                end = Offset(size.width, 0f),
            ),
        )
        val wave = sin(phase * 0.8f * 2f * PI.toFloat()) * 0.06f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(colors[0], colors[0].copy(alpha = 0.35f), Color.Transparent),
                center = Offset(size.width * (0.32f + wave), size.height * 0.3f),
                radius = radius * 0.95f,
            ),
            radius = radius * 0.95f,
            center = Offset(size.width * (0.32f + wave), size.height * 0.3f),
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(colors[1].copy(alpha = 0.9f), colors[1].copy(alpha = 0.25f), Color.Transparent),
                center = Offset(size.width * (0.68f - wave), size.height * 0.72f),
                radius = radius * 0.85f,
            ),
            radius = radius * 0.85f,
            center = Offset(size.width * (0.68f - wave), size.height * 0.72f),
        )
    }
}
