package com.opends.chat.design.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

private const val TARGET_WIDTH_DP = 380f
private const val TABLET_WIDTH_DP = 600f

fun resolveDensity(density: Density, widthPx: Int): Density {
    val widthDp = widthPx / density.density
    if (widthDp <= TARGET_WIDTH_DP || widthDp > TABLET_WIDTH_DP) return density
    return Density(widthPx / TARGET_WIDTH_DP, density.fontScale)
}

@Composable
fun OdDensity(content: @Composable () -> Unit) {
    val system = LocalDensity.current
    val widthPx = LocalContext.current.resources.displayMetrics.widthPixels
    val target = resolveDensity(system, widthPx)
    CompositionLocalProvider(LocalDensity provides target, content = content)
}

@Composable
fun OdOverlayDensity(content: @Composable () -> Unit) {
    val system = LocalDensity.current
    val widthPx = LocalContext.current.resources.displayMetrics.widthPixels
    CompositionLocalProvider(LocalDensity provides resolveDensity(system, widthPx), content = content)
}
