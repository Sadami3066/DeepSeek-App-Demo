package com.opends.chat.feature.chat.ui

import android.content.ContentResolver
import android.content.ContentUris
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opends.chat.design.R
import com.opends.chat.design.theme.OdDimens
import com.opends.chat.design.theme.OdMotion
import com.opends.chat.design.theme.OdTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val ExpandDurationMs = 283
private val CollapseDurationMs = 267
private val PanelEasing = CubicBezierEasing(0f, 0f, 0.2f, 1f)
private val TileIconGap = 6.5.dp
private val TileIconSize = 20.dp

@Composable
fun AttachPanel(
    expanded: Boolean,
    recentPhotos: List<Uri>,
    selectedPhotos: Set<String>,
    onSelectPhoto: (Uri) -> Unit,
    onPickPhoto: () -> Unit,
    onCamera: () -> Unit,
    onPickFile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(expanded) {
        progress.animateTo(
            targetValue = if (expanded) 1f else 0f,
            animationSpec = tween(
                durationMillis = if (expanded) ExpandDurationMs else CollapseDurationMs,
                easing = PanelEasing,
            ),
        )
    }
    if (progress.value <= 0f) return
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(OdDimens.attachPanelHeight * progress.value)
            .clipToBounds(),
    ) {
        AttachPanelContent(
            recentPhotos = recentPhotos,
            selectedPhotos = selectedPhotos,
            onSelectPhoto = onSelectPhoto,
            onPickPhoto = onPickPhoto,
            onCamera = onCamera,
            onPickFile = onPickFile,
        )
    }
}

@Composable
private fun AttachPanelContent(
    recentPhotos: List<Uri>,
    selectedPhotos: Set<String>,
    onSelectPhoto: (Uri) -> Unit,
    onPickPhoto: () -> Unit,
    onCamera: () -> Unit,
    onPickFile: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .wrapContentHeight(Alignment.Top, unbounded = true),
    ) {
        Spacer(Modifier.height(OdDimens.attachPhotoTopGap))
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(OdDimens.attachPhoto),
            contentPadding = PaddingValues(horizontal = OdDimens.attachPadding),
            horizontalArrangement = Arrangement.spacedBy(OdDimens.attachGap),
        ) {
            items(recentPhotos, key = { it.toString() }) { uri ->
                RecentPhoto(
                    uri = uri,
                    selected = selectedPhotos.contains(uri.toString()),
                    onClick = { onSelectPhoto(uri) },
                )
            }
        }
        Spacer(Modifier.height(OdDimens.attachTileGap))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(OdDimens.attachTileHeight)
                .padding(horizontal = OdDimens.attachPadding),
            horizontalArrangement = Arrangement.spacedBy(OdDimens.attachGap),
        ) {
            AttachTile(
                icon = R.drawable.ic_tile_camera_ask,
                label = "拍照",
                onClick = onCamera,
                modifier = Modifier.weight(1f),
            )
            AttachTile(
                icon = R.drawable.ic_tile_picture_ask,
                label = "相册",
                onClick = onPickPhoto,
                modifier = Modifier.weight(1f),
            )
            AttachTile(
                icon = R.drawable.ic_paperclip_outline_20,
                label = "文件",
                onClick = onPickFile,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun RecentPhoto(
    uri: Uri,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val palette = OdTheme.colors
    var bitmap by remember(uri) { mutableStateOf<android.graphics.Bitmap?>(null) }
    LaunchedEffect(uri) {
        bitmap = withContext(Dispatchers.IO) { decodeThumbnail(context, uri) }
    }
    val shape = RoundedCornerShape(OdDimens.attachPhotoRadius)
    Box(
        modifier = modifier
            .size(OdDimens.attachPhoto)
            .clip(shape)
            .background(palette.bgLayer3)
            .then(
                if (selected) {
                    Modifier.border(2.dp, palette.brandPrimary, shape)
                } else {
                    Modifier
                },
            )
            .clickableNoIndication(onClick),
    ) {
        val loaded = bitmap
        if (loaded != null) {
            androidx.compose.foundation.Image(
                bitmap = loaded.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
        if (selected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(5.dp)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(palette.brandPrimary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = AppIcons.check,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = androidx.compose.ui.graphics.Color.White,
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(palette.brandPrimary.copy(alpha = 0.16f)),
            )
        }
    }
}

private fun decodeThumbnail(context: android.content.Context, uri: Uri): android.graphics.Bitmap? = runCatching {
    val resolver = context.contentResolver
    val bounds = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
    resolver.openInputStream(uri)?.use { android.graphics.BitmapFactory.decodeStream(it, null, bounds) }
    val target = 240
    var sample = 1
    while (bounds.outWidth / sample > target * 2 || bounds.outHeight / sample > target * 2) {
        sample *= 2
    }
    val options = android.graphics.BitmapFactory.Options().apply { inSampleSize = sample }
    resolver.openInputStream(uri)?.use { android.graphics.BitmapFactory.decodeStream(it, null, options) }
}.getOrNull()

@Composable
private fun AttachTile(
    @DrawableRes icon: Int,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = OdTheme.colors
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(OdDimens.attachTileRadius))
            .background(palette.attachTile)
            .clickableNoIndication(onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        OdIcon(icon, TileIconSize, palette.iconPrimary)
        Spacer(Modifier.height(TileIconGap))
        Text(
            text = label,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = palette.labelPrimary,
        )
    }
}

@Composable
fun rememberRecentPhotos(
    enabled: Boolean,
    limit: Int = 6,
    refreshKey: Any? = null,
): List<Uri> {
    val context = LocalContext.current
    var photos by remember { mutableStateOf<List<Uri>>(emptyList()) }
    LaunchedEffect(limit, enabled, refreshKey) {
        if (!enabled) return@LaunchedEffect
        photos = withContext(Dispatchers.IO) {
            queryRecentPhotos(context.contentResolver, limit)
        }
    }
    return photos
}

private fun queryRecentPhotos(resolver: ContentResolver, limit: Int): List<Uri> {
    val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
    } else {
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    }
    val projection = arrayOf(MediaStore.Images.Media._ID)
    val sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC"
    return runCatching {
        resolver.query(collection, projection, null, null, sortOrder)?.use { cursor ->
            buildList {
                while (cursor.moveToNext() && size < limit) {
                    val id = cursor.getLong(0)
                    add(ContentUris.withAppendedId(collection, id))
                }
            }
        } ?: emptyList()
    }.getOrDefault(emptyList())
}
