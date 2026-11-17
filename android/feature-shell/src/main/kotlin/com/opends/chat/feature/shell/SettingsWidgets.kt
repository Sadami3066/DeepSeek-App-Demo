package com.opends.chat.feature.shell

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opends.chat.design.theme.OdDimens
import com.opends.chat.design.theme.OdTheme
import com.opends.chat.design.theme.OdType
import kotlin.math.roundToInt

internal val SwitchTrackColor = com.opends.chat.design.theme.DsStaticLight.dswStaticGreen500
private val SwitchWidth = 48.2.dp
private val SwitchHeight = 29.5.dp
private val SwitchThumb = 24.2.dp

@Composable
internal fun GreenSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = OdTheme.colors
    val track = when {
        !enabled -> colors.labelDimmed
        checked -> SwitchTrackColor
        else -> colors.labelDimmed
    }
    Box(
        modifier = modifier
            .width(SwitchWidth)
            .height(SwitchHeight)
            .clip(CircleShape)
            .background(track)
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { onCheckedChange(!checked) }
            .padding(horizontal = 3.dp),
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .size(SwitchThumb)
                .shadow(1.dp, CircleShape)
                .clip(CircleShape)
                .background(if (enabled) Color.White else colors.labelCaption),
        )
    }
}

@Composable
internal fun ShellDialogCard(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = OdTheme.colors
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.overlay)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss,
            ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = modifier
                    .widthIn(max = OdDimens.dialogWidth)
                    .clip(RoundedCornerShape(OdDimens.dialogRadius))
                    .background(colors.menu)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {},
                    ),
                content = content,
            )
        }
    }
}

@Composable
internal fun ShellDialogTitleBlock(title: String, iconRes: Int? = null) {
    val colors = OdTheme.colors
    if (iconRes == null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(55.4.dp)
                .padding(top = 10.2.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = title, style = OdType.dialogTitle, color = colors.labelPrimary)
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.5.dp)
                .padding(top = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ShellIcon(res = iconRes, size = 24.dp, tint = colors.labelPrimary)
            Spacer(Modifier.height(8.dp))
            Text(text = title, style = OdType.dialogTitle, color = colors.labelPrimary)
        }
    }
}

@Composable
internal fun ShellRadioMark(selected: Boolean) {
    val colors = OdTheme.colors
    Canvas(Modifier.size(OdDimens.dialogRadio)) {
        if (selected) {
            val stroke = 2.2.dp.toPx()
            val radius = (size.minDimension - stroke) / 2f
            drawCircle(color = colors.labelPrimary, radius = radius, style = Stroke(width = stroke))
            drawCircle(color = colors.labelPrimary, radius = size.minDimension * 0.26f)
        } else {
            val stroke = 1.6.dp.toPx()
            val radius = (size.minDimension - stroke) / 2f
            drawCircle(color = colors.labelPlaceholder, radius = radius, style = Stroke(width = stroke))
        }
    }
}

@Composable
internal fun ShellRadioDialog(
    title: String,
    options: List<String>,
    selectedIndex: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
    iconRes: Int? = null,
    rowHeight: Dp = OdDimens.dialogRowHeight,
    confirmLabel: String = "确认",
    width: Dp = OdDimens.dialogWidth,
) {
    val colors = OdTheme.colors
    var selected by rememberSaveable { mutableIntStateOf(selectedIndex.coerceAtLeast(0)) }
    ShellDialogCard(onDismiss = onDismiss, modifier = Modifier.width(width)) {
        ShellDialogTitleBlock(title = title, iconRes = iconRes)
        SettingsHairline(colors.dividerStrong)
        Column(Modifier.padding(vertical = 5.7.dp)) {
            options.forEachIndexed { index, label ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(rowHeight)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { selected = index }
                        .padding(horizontal = 20.3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ShellRadioMark(selected = index == selected)
                    Spacer(Modifier.width(17.2.dp))
                    Text(
                        text = label,
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        color = colors.labelPrimary,
                    )
                }
            }
        }
        SettingsHairline(colors.dividerStrong)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(OdDimens.settingsRowHeight)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { onConfirm(selected) },
            contentAlignment = Alignment.Center,
        ) {
            Text(text = confirmLabel, style = OdType.dialogAction, color = colors.brandPrimary)
        }
    }
}

@Composable
internal fun ShellBottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = OdTheme.colors
    val scrimTransition = remember { MutableTransitionState(false) }
    val sheetTransition = remember { MutableTransitionState(false) }
    LaunchedEffect(visible) {
        scrimTransition.targetState = visible
        sheetTransition.targetState = visible
    }
    AnimatedVisibility(
        visibleState = scrimTransition,
        enter = fadeIn(tween(120)),
        exit = fadeOut(tween(120)),
        modifier = Modifier.fillMaxSize(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.scrimSheet)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                ),
        )
    }
    AnimatedVisibility(
        visibleState = sheetTransition,
        enter = slideInVertically(tween(220)) { it },
        exit = slideOutVertically(tween(200)) { it },
        modifier = Modifier.fillMaxSize(),
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = OdDimens.sheetRadius, topEnd = OdDimens.sheetRadius))
                    .background(colors.menu)
                    .navigationBarsPadding(),
                content = content,
            )
        }
    }
}

private val SliderRailHeight = 2.dp
private val SliderThumbSize = 26.dp
private val SliderTickWidth = 2.dp
private val SliderTickHeight = 3.6.dp

@Composable
internal fun SettingsLevelSlider(
    level: Int,
    displayLevel: Int,
    onLevelChange: (Int) -> Unit,
) {
    val colors = OdTheme.colors
    val strings = shellStrings(rememberSettingsState().languageTag)
    val stops = 7
    val selected = displayLevel.coerceIn(1, stops)
    val startInset = 4.55.dp
    val endInset = 12.dp
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val startPx = with(density) { startInset.toPx() }
        val endPx = widthPx - with(density) { endInset.toPx() }
        val trackWidth = endPx - startPx
        val stepPx = trackWidth / (stops - 1)
        val thumbCenter = startPx + (selected - 1) * stepPx
        fun levelForX(x: Float): Int {
            val clamped = ((x - startPx) / trackWidth).coerceIn(0f, 1f)
            return (clamped * (stops - 1)).roundToInt() + 1
        }
        Column(Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(18.dp),
            ) {
                Text(
                    text = "A",
                    modifier = Modifier.align(Alignment.BottomStart),
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    color = colors.labelPrimary,
                )
                Text(
                    text = "A",
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 6.dp),
                    fontSize = 20.sp,
                    lineHeight = 24.sp,
                    color = colors.labelPrimary,
                )
                Text(
                    text = strings.fontLevels[selected - 1],
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                x = (thumbCenter - with(density) { 40.dp.toPx() }).roundToInt(),
                                y = 0,
                            )
                        }
                        .width(80.dp),
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = colors.labelSecondary,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.height(7.4.dp))
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(26.dp)
                    .pointerInput(level) {
                        detectTapGestures { offset -> onLevelChange(levelForX(offset.x)) }
                    }
                    .pointerInput(level) {
                        detectHorizontalDragGestures { change, _ ->
                            onLevelChange(levelForX(change.position.x))
                        }
                    },
            ) {
                val railY = size.height / 2f
                drawLine(
                    color = colors.labelPlaceholder,
                    start = Offset(startPx, railY),
                    end = Offset(endPx, railY),
                    strokeWidth = SliderRailHeight.toPx(),
                    cap = StrokeCap.Round,
                )
                for (index in 0 until stops) {
                    val x = startPx + index * stepPx
                    drawLine(
                        color = colors.labelPlaceholder,
                        start = Offset(x, railY - SliderTickHeight.toPx() / 2f),
                        end = Offset(x, railY + SliderTickHeight.toPx() / 2f),
                        strokeWidth = SliderTickWidth.toPx(),
                    )
                }
                drawCircle(
                    color = Color.White,
                    radius = SliderThumbSize.toPx() / 2f,
                    center = Offset(thumbCenter, railY),
                )
                drawCircle(
                    color = colors.labelPlaceholder,
                    radius = SliderThumbSize.toPx() / 2f,
                    center = Offset(thumbCenter, railY),
                    style = Stroke(width = 0.5.dp.toPx()),
                )
            }
        }
    }
}

@Composable
internal fun ShellMessageDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
) {
    val colors = OdTheme.colors
    ShellDialogCard(onDismiss = onDismiss, modifier = Modifier.width(OdDimens.dialogWidth)) {
        ShellDialogTitleBlock(title)
        SettingsHairline(colors.dividerStrong)
        Text(
            text = message,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.3.dp, vertical = 16.dp),
            fontSize = 13.sp,
            lineHeight = 19.sp,
            color = colors.labelSecondary,
            textAlign = TextAlign.Center,
        )
        SettingsHairline(colors.dividerStrong)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(OdDimens.settingsRowHeight)
                .shellClickable(onDismiss),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = confirmLabel, style = OdType.dialogAction, color = colors.brandPrimary)
        }
    }
}
