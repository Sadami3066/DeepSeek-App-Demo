package com.opends.chat.design.theme

import androidx.compose.animation.core.CubicBezierEasing

object OdMotion {
    const val slideMs = 200
    const val fadeMs = 75
    const val fastMs = 120
    const val normalMs = 200
    const val slowMs = 300

    val decelerate = CubicBezierEasing(0f, 0f, 0.2f, 1f)
    val accelerate = CubicBezierEasing(0.4f, 0f, 1f, 1f)
    val standard = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
    val linearOut = CubicBezierEasing(0f, 0f, 1f, 1f)

    const val thinkLottieMs = 433
    const val searchLottieMs = 500
    const val playingLottieMs = 1200
    const val autoPlayLottieMs = 400
}
