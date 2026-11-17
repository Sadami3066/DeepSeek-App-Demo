package com.opends.chat.feature.chat.ui

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opends.chat.core.SearchResult
import com.opends.chat.design.motion.OdLottie
import com.opends.chat.design.theme.OdTheme
import com.opends.chat.feature.chat.ToastState
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun OdIcon(
    @DrawableRes res: Int,
    size: Dp,
    tint: Color = OdTheme.colors.iconPrimary,
    modifier: Modifier = Modifier,
) {
    Icon(
        painter = painterResource(res),
        contentDescription = null,
        tint = tint,
        modifier = modifier.size(size),
    )
}

@Composable
fun OdIconButton(
    @DrawableRes res: Int,
    size: Dp,
    onClick: () -> Unit,
    tint: Color = OdTheme.colors.iconPrimary,
    modifier: Modifier = Modifier,
    boxSize: Dp = size,
    enabled: Boolean = true,
    ripple: Boolean = false,
    description: String? = null,
) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .size(boxSize)
            .then(if (description != null) Modifier.semantics { contentDescription = description } else Modifier)
            .clickable(
                interactionSource = interaction,
                indication = if (ripple) androidx.compose.material3.ripple() else null,
                enabled = enabled,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(res),
            contentDescription = null,
            tint = if (enabled) tint else tint.copy(alpha = 0.35f),
            modifier = Modifier.size(size),
        )
    }
}

@Composable
fun VectorIconButton(
    icon: ImageVector,
    size: Dp,
    onClick: () -> Unit,
    tint: Color = OdTheme.colors.iconPrimary,
    modifier: Modifier = Modifier,
    boxSize: Dp = size,
    enabled: Boolean = true,
    contentDescription: String? = null,
) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .size(boxSize)
            .semantics {
                if (!contentDescription.isNullOrBlank()) this.contentDescription = contentDescription
            }
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) tint else tint.copy(alpha = 0.35f),
            modifier = Modifier.size(size),
        )
    }
}

@Composable
fun LottieIcon(
    asset: String,
    size: Dp,
    tint: Color,
    modifier: Modifier = Modifier,
    playing: Boolean = true,
    loop: Boolean = false,
    boxSize: Dp = size,
    onClick: (() -> Unit)? = null,
    description: String? = null,
) {
    val interaction = remember { MutableInteractionSource() }
    var box = modifier.size(boxSize)
    if (description != null) {
        box = box.semantics { contentDescription = description }
    }
    if (onClick != null) {
        box = box.clickable(interactionSource = interaction, indication = null, onClick = onClick)
    }
    Box(modifier = box, contentAlignment = Alignment.Center) {
        OdLottie(
            asset = asset,
            modifier = Modifier.size(size),
            tint = tint,
            playing = playing,
            loop = loop,
        )
    }
}

fun Modifier.odShadow(shape: Shape, isDark: Boolean): Modifier {
    if (isDark) return this
    return this.drawBehind {
        if (shape !is RoundedCornerShape) return@drawBehind
        val radius = androidx.compose.ui.geometry.CornerRadius(shape.topStart.toPx(size, this))
        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.argb(16, 0, 0, 0)
            maskFilter = android.graphics.BlurMaskFilter(10.dp.toPx(), android.graphics.BlurMaskFilter.Blur.NORMAL)
        }
        val path = Path().apply {
            addRoundRect(
                RoundRect(
                    left = 0f,
                    top = 0f,
                    right = size.width,
                    bottom = size.height,
                    topLeftCornerRadius = radius,
                    topRightCornerRadius = radius,
                    bottomRightCornerRadius = radius,
                    bottomLeftCornerRadius = radius,
                ),
            )
        }
        drawIntoCanvas { canvas ->
            canvas.save()
            canvas.translate(0f, 1.5.dp.toPx())
            canvas.nativeCanvas.drawPath(path.asAndroidPath(), paint)
            canvas.restore()
        }
    }
}

@Composable
fun WaitingDots(modifier: Modifier = Modifier) {
    val color = OdTheme.colors.labelDimmed
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(3) { index ->
            val transition = rememberInfiniteTransition(label = "dot$index")
            val alpha by transition.animateFloat(
                initialValue = 0.3f,
                targetValue = 0.3f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = 900
                        delayMillis = index * 100
                        0.3f at 0
                        1f at 297
                        1f at 594
                        0.3f at 900
                    },
                    repeatMode = RepeatMode.Restart,
                ),
                label = "dotAlpha$index",
            )
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = alpha)),
            )
        }
    }
}

@Composable
fun LoadingSpinner(
    modifier: Modifier = Modifier,
    size: Dp = 16.dp,
    color: Color = OdTheme.colors.labelTertiary,
    strokeWidth: Dp = 1.5.dp,
) {
    val transition = rememberInfiniteTransition(label = "spin")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "spinAngle",
    )
    Canvas(modifier = modifier.size(size)) {
        drawArc(
            color = color.copy(alpha = 0.25f),
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = strokeWidth.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
            ),
        )
        drawArc(
            color = color,
            startAngle = angle,
            sweepAngle = 90f,
            useCenter = false,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = strokeWidth.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
            ),
        )
    }
}

@Composable
fun ToastHost(
    toast: ToastState?,
    onDismiss: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = OdTheme.colors
    Box(
        modifier = modifier.padding(top = 40.dp).padding(horizontal = 14.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        AnimatedVisibility(
            visible = toast != null,
            enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { -it / 2 },
            exit = ExitTransition.None,
        ) {
            val current = toast
            if (current != null) {
                LaunchedEffect(current.id) {
                    delay(current.durationMs)
                    onDismiss(current.id)
                }
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(palette.toastBg)
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    if (current.withIcon) {
                        Icon(
                            imageVector = AppIcons.warning,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = palette.stateErrorSecondary,
                        )
                    }
                    Text(
                        text = current.text,
                        color = Color.White,
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
fun SiteFavicon(
    url: String?,
    label: String,
    size: Dp,
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
) {
    val palette = OdTheme.colors
    val host = label.substringAfter("://", label).substringBefore("/")
    val letter = host.firstOrNull()?.uppercase(Locale.ROOT) ?: "•"
    val paletteColors = listOf(
        Color(0xFFE4572E), Color(0xFF4285F4), Color(0xFF34A853),
        Color(0xFFF9AB00), Color(0xFF8E44AD), Color(0xFF00ACC1),
        Color(0xFFD81B60), Color(0xFF5D4037),
    )
    val fallbackColor = paletteColors[(host.hashCode() and 0x7FFFFFFF) % paletteColors.size]
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(palette.bgBase)
            .padding(1.dp),
        contentAlignment = Alignment.Center,
    ) {
        com.opends.chat.render.OdRemoteImage(
            url = url.orEmpty(),
            modifier = Modifier.fillMaxSize().clip(shape),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape)
                    .background(fallbackColor),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = letter,
                    color = Color.White,
                    fontSize = 9.sp,
                    lineHeight = 9.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

fun formatPublishedAt(value: String?): String {
    if (value.isNullOrBlank()) return ""
    return runCatching {
        val trimmed = value.take(10)
        val parts = trimmed.split("-")
        if (parts.size == 3) "${parts[0]}/${parts[1]}/${parts[2]}" else trimmed
    }.getOrDefault(value)
}

@Composable
fun MicPermissionDialog(
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val palette = OdTheme.colors
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .width(277.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(palette.menu),
        ) {
            Text(
                text = "麦克风权限说明",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, start = 24.dp, end = 24.dp),
                color = palette.labelPrimary,
                fontSize = 16.sp,
                lineHeight = 22.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
            )
            Text(
                text = "请授权该权限以使用语音输入",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, start = 24.dp, end = 24.dp, bottom = 20.dp),
                color = palette.labelSecondary,
                fontSize = 14.sp,
                lineHeight = 20.sp,
            )
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(0.92.dp)
                    .background(palette.dividerStrong.copy(alpha = 0.36f)),
            )
            Row(Modifier.fillMaxWidth().height(56.5.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickableNoIndication(onDismiss),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "取消",
                        color = palette.labelSecondary,
                        fontSize = 16.sp,
                        lineHeight = 22.sp,
                    )
                }
                Box(
                    Modifier
                        .width(0.92.dp)
                        .fillMaxHeight()
                        .background(palette.dividerStrong.copy(alpha = 0.36f)),
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickableNoIndication(onOpenSettings),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "去设置",
                        color = palette.brandText,
                        fontSize = 16.sp,
                        lineHeight = 22.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                    )
                }
            }
        }
    }
}
