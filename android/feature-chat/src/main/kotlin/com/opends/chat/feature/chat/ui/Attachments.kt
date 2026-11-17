package com.opends.chat.feature.chat.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opends.chat.core.Fragment
import com.opends.chat.design.theme.OdTheme
import java.io.File

fun Fragment.File.resolveFile(): File? {
    val path = localPath ?: return null
    val file = File(path)
    return if (file.exists()) file else null
}

@Composable
fun rememberImageBitmap(file: File?, targetSizePx: Int = 320): Bitmap? {
    return remember(file?.absolutePath, targetSizePx, file?.length()) {
        if (file == null || !file.exists()) {
            null
        } else {
            runCatching {
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeFile(file.absolutePath, bounds)
                var sample = 1
                while (bounds.outWidth / (sample * 2) >= targetSizePx || bounds.outHeight / (sample * 2) >= targetSizePx) {
                    sample *= 2
                }
                BitmapFactory.decodeFile(
                    file.absolutePath,
                    BitmapFactory.Options().apply { inSampleSize = sample },
                )
            }.getOrNull()
        }
    }
}

@Composable
fun ImageThumb(
    file: File?,
    size: Dp,
    modifier: Modifier = Modifier,
    corner: Dp = 16.dp,
    onRemove: (() -> Unit)? = null,
    onPreview: (() -> Unit)? = null,
) {
    val palette = OdTheme.colors
    val bitmap = rememberImageBitmap(file)
    var previewing by remember { androidx.compose.runtime.mutableStateOf(false) }
    if (previewing && file != null) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { previewing = false }) {
            val full = rememberImageBitmap(file, targetSizePx = 1080)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(androidx.compose.ui.graphics.Color.Black)
                    .clickable { previewing = false },
                contentAlignment = Alignment.Center,
            ) {
                if (full != null) {
                    Image(
                        bitmap = full.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.Fit,
                    )
                }
            }
        }
    }
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(corner))
            .background(palette.markdownPlaceholder)
            .border(1.dp, palette.borderL2, RoundedCornerShape(corner))
            .then(
                if (onPreview != null) Modifier.clickableNoIndication(onPreview) else Modifier,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(palette.markdownPlaceholder),
            )
        }
        if (onRemove != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(palette.buttonContrastFill)
                    .clickableNoIndication(onRemove),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = AppIcons.close,
                    contentDescription = "删除",
                    modifier = Modifier.size(11.dp),
                    tint = palette.labelPrimaryForeground,
                )
            }
        }
    }
}

@Composable
fun FileCard(
    name: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onRemove: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    val palette = OdTheme.colors
    var boxModifier = modifier
        .width(240.dp)
        .clip(RoundedCornerShape(16.dp))
        .background(palette.markdownPlaceholder)
        .border(1.dp, palette.borderL2, RoundedCornerShape(16.dp))
    if (onClick != null) boxModifier = boxModifier.clickableNoIndication(onClick)
    Box(modifier = boxModifier) {
        Row(
            modifier = Modifier
                .padding(start = 14.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(palette.markdownTag),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = AppIcons.file,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = palette.labelSecondary,
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 10.dp, end = if (onRemove != null) 18.dp else 0.dp),
            ) {
                Text(
                    text = name,
                    color = palette.labelPrimary,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle,
                    color = palette.labelTertiary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (onRemove != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(palette.borderL3)
                    .clickableNoIndication(onRemove),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = AppIcons.close,
                    contentDescription = "删除",
                    modifier = Modifier.size(11.dp),
                    tint = palette.labelSecondary,
                )
            }
        }
    }
}

fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return ""
    val kb = bytes / 1024.0
    return when {
        kb < 1024 -> "${kb.toInt()}KB"
        else -> String.format("%.1fMB", kb / 1024.0)
    }
}

fun Fragment.File.subtitle(): String {
    val size = formatFileSize(size)
    val ext = name?.substringAfterLast('.', "")?.uppercase().orEmpty()
    return listOf(ext, size).filter { it.isNotBlank() }.joinToString(" ")
}

@Composable
fun MessageFiles(files: List<Fragment.File>, modifier: Modifier = Modifier) {
    if (files.isEmpty()) return
    val onlyImages = files.all { it.isImage }
    val imageSize = if (onlyImages) ChatDimens.attachmentThumbSingle else ChatDimens.attachmentThumb
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
        verticalAlignment = Alignment.Bottom,
    ) {
        files.take(6).forEach { file ->
            if (file.isImage) {
                val imageFile = file.resolveFile()
                var showPreview by remember { androidx.compose.runtime.mutableStateOf(false) }
                ImageThumb(
                    file = imageFile,
                    size = imageSize,
                    onPreview = if (imageFile != null) ({ showPreview = true }) else null,
                )
                if (showPreview && imageFile != null) {
                    ImagePreviewDialog(file = imageFile, onDismiss = { showPreview = false })
                }
            } else {
                FileCard(name = file.name ?: "文件", subtitle = file.subtitle())
            }
        }
    }
}

fun Modifier.clickableNoIndication(onClick: () -> Unit): Modifier =
    this.clickable(
        interactionSource = null,
        indication = null,
        onClick = onClick,
    )

@Composable
fun ImagePreviewDialog(file: File, onDismiss: () -> Unit) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        val full = rememberImageBitmap(file, targetSizePx = 1080)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(androidx.compose.ui.graphics.Color.Black)
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center,
        ) {
            if (full != null) {
                Image(
                    bitmap = full.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Fit,
                )
            }
        }
    }
}
