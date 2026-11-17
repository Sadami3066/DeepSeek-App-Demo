package com.opends.chat.feature.chat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.opends.chat.design.theme.OdTheme

data class MessageMenuItem(
    val icon: ImageVector,
    val label: String,
    val danger: Boolean = false,
    val onClick: () -> Unit,
)

@Composable
fun MessageContextMenu(
    x: Int,
    y: Int,
    items: List<MessageMenuItem>,
    onDismiss: () -> Unit,
) {
    val palette = OdTheme.colors
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
    val menuHeight = (items.size * 45 + (items.size - 1)).toFloat() * density.density
    val menuWidth = with(density) { 198.5.dp.toPx() }
    val offsetX = with(density) { 23.6.dp.roundToPx() }
    val offsetY = y.coerceIn(0, (screenHeightPx - menuHeight - 80 * density.density).toInt())
    Popup(
        offset = IntOffset(offsetX, offsetY),
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true),
    ) {
        Column(
            modifier = Modifier
                .width(191.8.dp)
                .shadow(12.dp, RoundedCornerShape(14.dp))
                .clip(RoundedCornerShape(14.dp))
                .background(if (OdTheme.isDark) palette.attachTile else palette.menu),
        ) {
            items.forEachIndexed { index, item ->
                if (index > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(palette.divider),
                    )
                }
                Row(
                    modifier = Modifier
                        .height(45.dp)
                        .clickable {
                            onDismiss()
                            item.onClick()
                        }
                        .padding(horizontal = 0.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier.width(44.7.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = if (item.danger) palette.stateErrorPrimary else palette.iconPrimary,
                        )
                    }
                    Text(
                        text = item.label,
                        color = if (item.danger) palette.stateErrorPrimary else palette.labelPrimary,
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                    )
                }
            }
        }
    }
}

@Composable
fun MenuDividerSpacer() {
    Spacer(Modifier.height(1.dp).background(Color.Transparent))
}
