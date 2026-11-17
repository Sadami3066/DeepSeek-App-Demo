package com.opends.chat.design.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object DsSize {
    val controlXl = 44.dp
    val controlL = 40.dp
    val controlM = 36.dp
    val controlS = 32.dp
    val controlXs = 28.dp
    val fontM = 14.sp
}

object DsMotion {
    const val fastMs = 200
    const val slowMs = 300
    val easeInOut = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
    val easeOut = CubicBezierEasing(0f, 0f, 0.2f, 1f)
    val easeIn = CubicBezierEasing(0.4f, 0f, 1f, 1f)
}

object DsLayout {
    val messageListMaxWidth = 840.dp
    val messageListPaddingHorizontal = 44.dp
    val messageListMaxWidthMobile = 712.dp
    val messageListPaddingHorizontalMobile = 36.dp
}
