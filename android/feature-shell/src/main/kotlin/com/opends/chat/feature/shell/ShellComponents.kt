package com.opends.chat.feature.shell

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opends.chat.design.R
import com.opends.chat.design.theme.OdDimens
import com.opends.chat.design.theme.OdOverlayDensity
import com.opends.chat.design.theme.OdTheme
import com.opends.chat.design.theme.OdType
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import kotlin.math.roundToInt

internal val ShellHairline = 0.92.dp

private val TopBarHeight = 40.dp
private val BackCircleSize = 29.5.dp
private val BackCircleStart = 12.9.dp
private val BackTouchSize = 44.dp
private val BackIconSize = 20.dp

private val SectionFirstTop = 11.8.dp
private val SectionTop = 26.4.dp
private val SectionNoTitleTop = 33.5.dp
private val SectionTitleBottom = 7.8.dp
private val GroupTitleStart = 30.15.dp

private val SettingsRowStart = 11.8.dp
private val SettingsRowEnd = 8.62.dp
private val RowIconSlot = 24.dp
private val RowIconGap = 10.3.dp
private val RowIconSize = 22.dp
private val RowSmallIconSize = 18.33.dp
private val ChevronSize = 20.dp
private val ValueGap = 8.dp
private val ValueChevronGap = 5.dp
private val CardBottomSpace = 24.dp

@Composable
internal fun Modifier.shellClickable(onClick: () -> Unit): Modifier {
    val interaction = remember { MutableInteractionSource() }
    return clickable(interactionSource = interaction, indication = null, onClick = onClick)
}

@Composable
internal fun Modifier.shellClickable(enabled: Boolean, onClick: () -> Unit): Modifier {
    val interaction = remember { MutableInteractionSource() }
    return clickable(
        interactionSource = interaction,
        indication = null,
        enabled = enabled,
        onClick = onClick,
    )
}

@Composable
internal fun ShellIcon(
    @DrawableRes res: Int,
    size: Dp,
    modifier: Modifier = Modifier,
    tint: Color = OdTheme.colors.iconPrimary,
) {
    Icon(
        painter = painterResource(res),
        contentDescription = null,
        tint = tint,
        modifier = modifier.size(size),
    )
}

@Composable
internal fun ShellIconButton(
    @DrawableRes res: Int,
    size: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = OdTheme.colors.iconPrimary,
) {
    Box(
        modifier = modifier
            .size(size)
            .shellClickable(onClick),
        contentAlignment = Alignment.Center,
    ) {
        ShellIcon(res = res, size = size, tint = tint)
    }
}

@Composable
internal fun ShellTopBar(title: String, onBack: () -> Unit) {
    val colors = OdTheme.colors
    val dark = OdTheme.isDark
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(TopBarHeight),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = BackCircleStart - (BackTouchSize - BackCircleSize) / 2)
                .size(BackTouchSize)
                .semantics { contentDescription = "返回" }
                .shellClickable(onBack),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(BackCircleSize)
                    .clip(CircleShape)
                    .background(if (dark) colors.menu else colors.bgLayer3),
                contentAlignment = Alignment.Center,
            ) {
                ShellIcon(
                    res = R.drawable.ic_chevron_left_outline_20,
                    size = BackIconSize,
                    tint = colors.iconPrimary,
                )
            }
        }
        Text(
            text = title,
            modifier = Modifier.align(Alignment.Center),
            style = OdType.barTitle,
            color = colors.labelPrimary,
        )
    }
}

@Composable
internal fun SettingsSectionStart(first: Boolean, hasTitle: Boolean) {
    Spacer(
        Modifier.height(
            when {
                first -> SectionFirstTop
                hasTitle -> SectionTop
                else -> SectionNoTitleTop
            },
        ),
    )
}

@Composable
internal fun SettingsSectionTitle(text: String) {
    val colors = OdTheme.colors
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = GroupTitleStart),
        style = OdType.groupTitle,
        color = colors.labelTertiary,
        maxLines = 1,
    )
}

@Composable
internal fun SettingsTitleGap() {
    Spacer(Modifier.height(SectionTitleBottom))
}

@Composable
internal fun SettingsCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = OdDimens.settingsCardMargin)
            .clip(RoundedCornerShape(OdDimens.settingsCardRadius))
            .background(OdTheme.colors.menu),
        content = content,
    )
}

@Composable
internal fun SettingsRow(
    title: String,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    value: String? = null,
    showChevron: Boolean = true,
    startPadding: Dp = SettingsRowStart,
    onClick: (() -> Unit)? = null,
) {
    val colors = OdTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(OdDimens.settingsRowHeight)
            .then(if (onClick != null) Modifier.shellClickable(onClick) else Modifier)
            .padding(start = startPadding, end = SettingsRowEnd),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Box(Modifier.size(RowIconSlot), contentAlignment = Alignment.Center) {
                icon()
            }
            Spacer(Modifier.width(RowIconGap))
        }
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = OdType.rowTitle,
            color = colors.labelPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (value != null) {
            Spacer(Modifier.width(ValueGap))
            Text(
                text = value,
                style = OdType.rowValue,
                color = colors.labelTertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (showChevron) {
            Spacer(Modifier.width(ValueChevronGap))
            ShellIcon(
                res = R.drawable.ic_chevron_right_outline_20,
                size = ChevronSize,
                tint = colors.iconWeak,
            )
        }
    }
}

@Composable
internal fun SettingsRowIcon(@DrawableRes res: Int, size: Dp = RowIconSize) {
    ShellIcon(res = res, size = size, tint = OdTheme.colors.iconPrimary)
}

@Composable
internal fun SettingsHairline(color: Color) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(ShellHairline)
            .background(color),
    )
}

@Composable
internal fun SettingsBottomSpace() {
    Spacer(Modifier.height(CardBottomSpace))
}

internal data class ShellMenuEntry(
    val label: String,
    @DrawableRes val iconRes: Int,
    val danger: Boolean = false,
    val onClick: () -> Unit,
)

private val MenuWidth = 192.dp
private val MenuItemHeight = 44.8.dp
private val MenuCorner = 16.dp
private val MenuIconSize = 20.dp
private val MenuIconStart = 14.dp
private val MenuIconGap = 10.dp
private val MenuDividerHeight = 0.36.dp
private val MenuAnchorGap = 9.5.dp
private val MenuEdgeInset = 0.dp

@Composable
internal fun ShellMenu(
    expanded: Boolean,
    anchor: Rect?,
    onDismiss: () -> Unit,
    entries: List<ShellMenuEntry>,
) {
    if (!expanded || anchor == null) return
    val colors = OdTheme.colors
    val density = LocalDensity.current
    val provider = remember(anchor, density) {
        val widthPx = with(density) { MenuWidth.toPx() }
        val edgePx = with(density) { MenuEdgeInset.toPx() }
        val gapPx = with(density) { MenuAnchorGap.toPx() }
        object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds: IntRect,
                windowSize: IntSize,
                layoutDirection: LayoutDirection,
                popupContentSize: IntSize,
            ): IntOffset {
                val x = (anchor.right - widthPx - edgePx).roundToInt()
                val below = (anchor.bottom + gapPx).roundToInt()
                val y = if (below + popupContentSize.height <= windowSize.height) {
                    below
                } else {
                    (anchor.top - gapPx - popupContentSize.height).roundToInt()
                }
                return IntOffset(x, y)
            }
        }
    }
    val shape = RoundedCornerShape(MenuCorner)
    Popup(
        popupPositionProvider = provider,
        onDismissRequest = onDismiss,
        properties = PopupProperties(
            focusable = true,
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        ),
    ) {
        OdOverlayDensity {
        Column(
            Modifier
                .width(MenuWidth)
                .shadow(elevation = 10.dp, shape = shape, clip = false)
                .clip(shape)
                .background(colors.menu),
        ) {
            entries.forEachIndexed { index, entry ->
                if (index > 0) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(MenuDividerHeight)
                            .background(colors.dividerStrong),
                    )
                }
                val color = if (entry.danger) colors.stateErrorPrimary else colors.labelPrimary
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(MenuItemHeight)
                        .shellClickable {
                            onDismiss()
                            entry.onClick()
                        }
                        .padding(start = MenuIconStart, end = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ShellIcon(res = entry.iconRes, size = MenuIconSize, tint = color)
                    Spacer(Modifier.width(MenuIconGap))
                    Text(
                        text = entry.label,
                        style = OdType.menuLabel,
                        color = color,
                        maxLines = 1,
                    )
                }
            }
        }
        }
    }
}

@Composable
internal fun ShellConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    cancelLabel: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val colors = OdTheme.colors
    ShellDialogFrame(onDismiss = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(55.4.dp)
                .padding(top = 10.2.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = title, style = OdType.dialogTitle, color = colors.labelPrimary)
        }
        SettingsHairline(colors.dividerStrong)
        Text(
            text = message,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.3.dp, end = 20.3.dp, top = 14.dp, bottom = 18.dp),
            style = OdType.dialogBody,
            color = colors.labelSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        SettingsHairline(colors.dividerStrong)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(OdDimens.settingsRowHeight),
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .shellClickable(onDismiss),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = cancelLabel, style = OdType.dialogAction, color = colors.labelSecondary)
            }
            Box(
                modifier = Modifier
                    .width(ShellHairline)
                    .fillMaxSize()
                    .background(colors.dividerStrong),
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .shellClickable(onConfirm),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = confirmLabel, style = OdType.dialogAction, color = colors.brandPrimary)
            }
        }
    }
}

@Composable
internal fun ShellInputDialog(
    title: String,
    initialValue: String,
    placeholder: String,
    confirmLabel: String,
    cancelLabel: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val colors = OdTheme.colors
    var value by rememberSaveable { androidx.compose.runtime.mutableStateOf(initialValue) }
    ShellDialogFrame(onDismiss = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(55.4.dp)
                .padding(top = 10.2.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = title, style = OdType.dialogTitle, color = colors.labelPrimary)
        }
        SettingsHairline(colors.dividerStrong)
        Box(Modifier.padding(horizontal = 20.3.dp, vertical = 14.dp)) {
            ShellTextField(
                value = value,
                onValueChange = { value = it },
                placeholder = placeholder,
                keyboardActions = KeyboardActions(onDone = { onConfirm(value.trim()) }),
            )
        }
        SettingsHairline(colors.dividerStrong)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(OdDimens.settingsRowHeight),
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .shellClickable(onDismiss),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = cancelLabel, style = OdType.dialogAction, color = colors.labelSecondary)
            }
            Box(
                modifier = Modifier
                    .width(ShellHairline)
                    .fillMaxSize()
                    .background(colors.dividerStrong),
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .shellClickable { onConfirm(value.trim()) },
                contentAlignment = Alignment.Center,
            ) {
                Text(text = confirmLabel, style = OdType.dialogAction, color = colors.brandPrimary)
            }
        }
    }
}

@Composable
private fun ShellDialogFrame(onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    val colors = OdTheme.colors
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.overlay)
            .shellClickable(onDismiss),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .width(OdDimens.dialogWidth)
                    .clip(RoundedCornerShape(OdDimens.dialogRadius))
                    .background(colors.menu)
                    .shellClickable {},
                content = content,
            )
        }
    }
}

@Composable
internal fun ShellTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val colors = OdTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.selector)
            .padding(start = 14.dp, end = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            textStyle = TextStyle(fontSize = 15.sp, lineHeight = 20.sp, color = colors.labelPrimary),
            cursorBrush = SolidColor(colors.brandPrimary),
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            decorationBox = { inner ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            fontSize = 15.sp,
                            lineHeight = 20.sp,
                            color = colors.labelPlaceholder,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    inner()
                }
            },
        )
    }
}

@Composable
internal fun ShellPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = OdTheme.colors
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(if (enabled) colors.brandPrimary else colors.brandPrimary.copy(alpha = 0.45f))
            .then(if (enabled) Modifier.shellClickable(onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = TextStyle(fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.Medium),
            color = colors.labelPrimaryForeground,
        )
    }
}

@Composable
internal fun OpenDSBrandMark(size: Dp = 64.dp) {
    val colors = OdTheme.colors
    Box(
        modifier = Modifier
            .size(size)
            .background(colors.brandPrimary, RoundedCornerShape(size * 0.28f)),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize(0.52f)) {
            val stroke = Stroke(width = this.size.minDimension * 0.13f, cap = StrokeCap.Round)
            val inset = stroke.width / 2f
            drawArc(
                color = Color.White,
                startAngle = 40f,
                sweepAngle = 280f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = Size(this.size.width - stroke.width, this.size.height - stroke.width),
                style = stroke,
            )
            drawCircle(
                color = Color.White,
                radius = this.size.minDimension * 0.10f,
                center = Offset(this.size.width * 0.76f, this.size.height * 0.22f),
            )
        }
    }
}

@Composable
internal fun GearGlyph(size: Dp = 20.dp, tint: Color = OdTheme.colors.iconSecondary) {
    Canvas(Modifier.size(size)) {
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        val outer = this.size.minDimension * 0.42f
        val inner = this.size.minDimension * 0.30f
        val tooth = this.size.minDimension * 0.10f
        val ringStroke = this.size.minDimension * 0.15f
        repeat(8) { index ->
            val angle = Math.toRadians((index * 45).toDouble())
            val dx = (Math.cos(angle) * (outer - inner / 2f)).toFloat()
            val dy = (Math.sin(angle) * (outer - inner / 2f)).toFloat()
            drawLine(
                color = tint,
                start = Offset(center.x + dx * 0.55f, center.y + dy * 0.55f),
                end = Offset(center.x + dx, center.y + dy),
                strokeWidth = tooth,
                cap = StrokeCap.Round,
            )
        }
        drawCircle(
            color = tint,
            radius = inner * 0.88f,
            center = center,
            style = Stroke(width = ringStroke),
        )
    }
}

