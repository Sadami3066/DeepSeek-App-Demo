package com.opends.chat.render

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext

@Composable
fun OdRemoteImage(
    url: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    fallback: @Composable () -> Unit = {},
) {
    val context = LocalContext.current
    val key = url.trim()
    var bitmap by remember(key) { mutableStateOf(ImageLoader.cached(key)) }

    LaunchedEffect(key) {
        if (bitmap != null || key.isEmpty()) return@LaunchedEffect
        val loaded = ImageLoader.load(context, key)
        if (loaded != null) bitmap = loaded
    }

    val image = bitmap
    if (image != null) {
        Image(
            bitmap = image.asImageBitmap(),
            contentDescription = null,
            contentScale = contentScale,
            modifier = modifier,
        )
    } else {
        fallback()
    }
}
