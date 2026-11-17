package com.opends.chat.design.motion

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import org.json.JSONArray
import org.json.JSONObject

object OdLottieAssets {
    const val think = "lottie/think.json"
    const val search = "lottie/globe.json"
    const val flash = "lottie/flash.json"
    const val photo = "lottie/photo.json"
    const val expert = "lottie/expert.json"
    const val playing = "lottie/playing.json"
    const val autoPlayOn = "lottie/auto_play_on.json"
    const val autoPlayOff = "lottie/auto_play_off.json"
}

private fun readAsset(context: Context, path: String): String =
    context.assets.open(path).bufferedReader().use { it.readText() }

private fun recolor(node: Any?, color: Color) {
    when (node) {
        is JSONObject -> {
            val c = node.optJSONObject("c")
            if (c != null && c.optInt("a", 0) == 0) {
                val k = c.optJSONArray("k")
                if (k != null) {
                    when (k.length()) {
                        4 -> {
                            val a = k.optDouble(3, 1.0)
                            k.put(0, color.red.toDouble())
                            k.put(1, color.green.toDouble())
                            k.put(2, color.blue.toDouble())
                            k.put(3, color.alpha.toDouble() * a)
                        }
                        3 -> {
                            k.put(0, color.red.toDouble())
                            k.put(1, color.green.toDouble())
                            k.put(2, color.blue.toDouble())
                        }
                    }
                }
            }
            val keys = node.keys()
            while (keys.hasNext()) {
                recolor(node.opt(keys.next()), color)
            }
        }
        is JSONArray -> {
            for (i in 0 until node.length()) {
                recolor(node.opt(i), color)
            }
        }
    }
}

@Composable
fun OdLottie(
    asset: String,
    modifier: Modifier = Modifier,
    tint: Color? = null,
    playing: Boolean = true,
    loop: Boolean = false,
) {
    val context = LocalContext.current
    val spec = remember(asset, tint) {
        if (tint == null) {
            LottieCompositionSpec.Asset(asset)
        } else {
            val json = JSONObject(readAsset(context, asset))
            recolor(json, tint)
            LottieCompositionSpec.JsonString(json.toString())
        }
    }
    val composition by rememberLottieComposition(spec)
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = if (loop) LottieConstants.IterateForever else 1,
        isPlaying = playing,
    )
    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = modifier,
    )
}
