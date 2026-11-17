package com.opends.chat.design.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp

private val noFontPadding = PlatformTextStyle(includeFontPadding = false)

private val sans = FontFamily.Default

val OpenDSTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = sans,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = sans,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = sans,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = sans,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = sans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = sans,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = sans,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = sans,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp,
    ),
)

object OdType {
    val greeting = TextStyle(fontSize = 18.7.sp, lineHeight = 26.sp, fontWeight = FontWeight.SemiBold, platformStyle = noFontPadding)

    val barTitle = TextStyle(fontSize = 15.sp, lineHeight = 18.5.sp, fontWeight = FontWeight.Medium, platformStyle = noFontPadding)

    val input = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, platformStyle = noFontPadding)

    val pill = TextStyle(fontSize = 13.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium, platformStyle = noFontPadding)

    val bubble = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, platformStyle = noFontPadding)

    val body = TextStyle(fontSize = 15.7.sp, lineHeight = 26.sp, platformStyle = noFontPadding)

    val bodySmall = TextStyle(fontSize = 13.8.sp, lineHeight = 22.1.sp, platformStyle = noFontPadding)

    val caveat = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, platformStyle = noFontPadding)

    val fixedHint = TextStyle(fontSize = 11.sp, lineHeight = 12.8.sp, platformStyle = noFontPadding, lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.Both))

    val thinkingTitle = TextStyle(fontSize = 15.sp, lineHeight = 22.sp, platformStyle = noFontPadding)

    val messageAction = TextStyle(fontSize = 13.sp, lineHeight = 18.sp, platformStyle = noFontPadding)

    val drawerSession = TextStyle(fontSize = 15.sp, lineHeight = 22.sp, platformStyle = noFontPadding)

    val drawerGroup = TextStyle(fontSize = 13.sp, lineHeight = 18.5.sp, fontWeight = FontWeight.Medium, platformStyle = noFontPadding)

    val drawerSearch = TextStyle(fontSize = 15.sp, lineHeight = 22.sp, platformStyle = noFontPadding)
    val menuLabel = TextStyle(fontSize = 15.sp, lineHeight = 22.sp, platformStyle = noFontPadding)
    val selectionHeader = TextStyle(fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.Medium, platformStyle = noFontPadding)
    val selectionAction = TextStyle(fontSize = 15.sp, lineHeight = 20.sp, platformStyle = noFontPadding)

    val rowTitle = TextStyle(fontSize = 15.sp, lineHeight = 18.5.sp, platformStyle = noFontPadding)

    val rowValue = TextStyle(fontSize = 15.sp, lineHeight = 18.5.sp, platformStyle = noFontPadding)

    val groupTitle = TextStyle(fontSize = 13.sp, lineHeight = 16.6.sp, fontWeight = FontWeight.Medium, platformStyle = noFontPadding)

    val dialogTitle = TextStyle(fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.Medium, platformStyle = noFontPadding)

    val dialogAction = TextStyle(fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.Medium, platformStyle = noFontPadding)

    val dialogBody = TextStyle(fontSize = 13.sp, lineHeight = 19.sp, platformStyle = noFontPadding)

    val sourceSite = TextStyle(fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium, platformStyle = noFontPadding)

    val sourceTitle = TextStyle(fontSize = 15.sp, lineHeight = 23.sp, fontWeight = FontWeight.Medium, platformStyle = noFontPadding)

    val sourceDesc = TextStyle(fontSize = 13.sp, lineHeight = 20.3.sp, platformStyle = noFontPadding)

    val searchResultTitle = TextStyle(fontSize = 15.sp, lineHeight = 22.sp, platformStyle = noFontPadding)

    val searchResultDesc = TextStyle(fontSize = 14.sp, lineHeight = 18.5.sp, platformStyle = noFontPadding)

    val attachTile = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, platformStyle = noFontPadding)

    val chip = TextStyle(fontSize = 13.sp, lineHeight = 18.sp, platformStyle = noFontPadding)

    val caption = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, platformStyle = noFontPadding)

    val heading1 = TextStyle(fontSize = 23.9.sp, lineHeight = 35.9.sp, fontWeight = FontWeight.Bold, platformStyle = noFontPadding)

    val heading2 = TextStyle(fontSize = 21.2.sp, lineHeight = 31.3.sp, fontWeight = FontWeight.Bold, platformStyle = noFontPadding)

    val heading3 = TextStyle(fontSize = 19.3.sp, lineHeight = 27.6.sp, fontWeight = FontWeight.SemiBold, platformStyle = noFontPadding)

    val heading4 = TextStyle(fontSize = 17.5.sp, lineHeight = 25.8.sp, fontWeight = FontWeight.SemiBold, platformStyle = noFontPadding)

    val heading5 = TextStyle(fontSize = 15.7.sp, lineHeight = 25.8.sp, fontWeight = FontWeight.Medium, platformStyle = noFontPadding)

    val heading6 = TextStyle(fontSize = 15.7.sp, lineHeight = 25.8.sp, fontWeight = FontWeight.Medium, platformStyle = noFontPadding)

    val code = TextStyle(fontSize = 12.sp, lineHeight = 16.6.sp, fontFamily = FontFamily.Monospace, platformStyle = noFontPadding)

    val inlineCode = TextStyle(fontSize = 12.9.sp, platformStyle = noFontPadding)
}
