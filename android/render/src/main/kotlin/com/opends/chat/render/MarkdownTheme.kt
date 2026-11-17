package com.opends.chat.render

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opends.chat.design.theme.OdColors
import com.opends.chat.design.theme.OdLightColors
import com.opends.chat.design.theme.OdTheme
import com.opends.chat.design.theme.OdType

@Immutable
data class SyntaxPalette(
    val root: Color,
    val keyword: Color,
    val string: Color,
    val number: Color,
    val comment: Color,
    val function: Color,
    val type: Color,
    val builtin: Color,
    val operator: Color,
    val punctuation: Color,
    val property: Color,
    val tag: Color,
    val attribute: Color,
    val variable: Color,
    val constant: Color,
    val regex: Color,
    val meta: Color,
    val deleted: Color,
    val inserted: Color,
    val url: Color,
)

internal val LightSyntax = SyntaxPalette(
    root = Color(0xFF383A42),
    keyword = Color(0xFFA626A4),
    string = Color(0xFF50A14F),
    number = Color(0xFF986801),
    comment = Color(0xFFA0A1A7),
    function = Color(0xFF4078F2),
    type = Color(0xFF986801),
    builtin = Color(0xFFC18401),
    operator = Color(0xFF383A42),
    punctuation = Color(0xFF383A42),
    property = Color(0xFF986801),
    tag = Color(0xFFE45649),
    attribute = Color(0xFF50A14F),
    variable = Color(0xFF986801),
    constant = Color(0xFF986801),
    regex = Color(0xFF50A14F),
    meta = Color(0xFF4078F2),
    deleted = Color(0xFFE45649),
    inserted = Color(0xFF50A14F),
    url = Color(0xFF0184BB),
)

internal val DarkSyntax = SyntaxPalette(
    root = Color(0xFFABB2BF),
    keyword = Color(0xFFC678DD),
    string = Color(0xFF98C379),
    number = Color(0xFFD19A66),
    comment = Color(0xFF5C6370),
    function = Color(0xFF61AEEE),
    type = Color(0xFFE6C07B),
    builtin = Color(0xFFE6C07B),
    operator = Color(0xFFABB2BF),
    punctuation = Color(0xFFABB2BF),
    property = Color(0xFFD19A66),
    tag = Color(0xFFE06C75),
    attribute = Color(0xFF98C379),
    variable = Color(0xFFD19A66),
    constant = Color(0xFFD19A66),
    regex = Color(0xFF98C379),
    meta = Color(0xFF61AEEE),
    deleted = Color(0xFFE06C75),
    inserted = Color(0xFF98C379),
    url = Color(0xFF56B6C2),
)

@Immutable
data class MarkdownColors(
    val isDark: Boolean,
    val text: Color,
    val secondary: Color,
    val tertiary: Color,
    val brand: Color,
    val outline: Color,
    val outlineStrong: Color,
    val divider: Color,
    val rail: Color,
    val codeBackground: Color,
    val codeBanner: Color,
    val inlineCodeBackground: Color,
    val citationBackground: Color,
    val quoteBar: Color,
    val rule: Color,
    val tagBackground: Color,
    val placeholderBackground: Color,
    val error: Color,
    val syntax: SyntaxPalette,
)

@Composable
fun markdownColors(): MarkdownColors = markdownColorsFrom(OdTheme.colors)

fun markdownColorsFrom(colors: OdColors): MarkdownColors {
    val dark = colors.bgBase.luminance() < 0.5f
    return MarkdownColors(
        isDark = dark,
        text = colors.bodyText,
        secondary = colors.labelSecondary,
        tertiary = colors.labelTertiary,
        brand = colors.brandText,
        outline = colors.borderL2,
        outlineStrong = colors.borderL3,
        divider = colors.divider,
        rail = colors.rail,
        codeBackground = colors.markdownCodeBlock,
        codeBanner = colors.markdownCodeBlockBanner,
        inlineCodeBackground = colors.markdownCodeBlock,
        citationBackground = colors.markdownCitation,
        quoteBar = colors.rail,
        rule = colors.rail,
        tagBackground = colors.markdownTag,
        placeholderBackground = colors.markdownPlaceholder,
        error = colors.stateErrorPrimary,
        syntax = if (dark) DarkSyntax else LightSyntax,
    )
}

val LocalMarkdownColors = staticCompositionLocalOf<MarkdownColors> {
    markdownColorsFrom(OdLightColors)
}

@Immutable
data class MarkdownTypography(
    val body: TextStyle,
    val bodyStrong: TextStyle,
    val strongWeight: FontWeight,
    val h1: TextStyle,
    val h2: TextStyle,
    val h3: TextStyle,
    val h4: TextStyle,
    val h5: TextStyle,
    val h6: TextStyle,
    val small: TextStyle,
    val smallStrong: TextStyle,
    val table: TextStyle,
    val tableHead: TextStyle,
    val inlineCode: TextStyle,
    val codeBlock: TextStyle,
    val codeBanner: TextStyle,
    val blockGap: Dp,
    val headingTopGap: Dp,
    val headingBottomGap: Dp,
    val listIndent: Dp,
    val listItemGap: Dp,
    val listMarkerSize: Dp,
    val orderedMarkerCharWidth: Dp,
    val ruleGap: Dp,
    val ruleThickness: Dp,
    val quoteRailWidth: Dp,
    val quoteGap: Dp,
    val quoteVerticalPadding: Dp,
    val codeRadius: Dp,
    val codePaddingStart: Dp,
    val codePaddingVertical: Dp,
    val codeHeaderHeight: Dp,
    val codeHeaderDivider: Dp,
    val codeHeaderStartPadding: Dp,
    val codeHeaderEndPadding: Dp,
    val codeCopyIconSize: Dp,
    val codeCopyButtonRadius: Dp,
    val tableBorderWidth: Dp,
    val tableCellVerticalPadding: Dp,
    val tableCellHorizontalPadding: Dp,
    val tableRadius: Dp,
    val tableMinColumnWidth: Dp,
    val tableMaxColumnWidth: Dp,
    val imageRadius: Dp,
    val formulaInlineFontSize: androidx.compose.ui.unit.TextUnit,
    val formulaBlockFontSize: androidx.compose.ui.unit.TextUnit,
    val formulaBlockPaddingVertical: Dp,
    val formulaBlockPaddingHorizontal: Dp,
)

private val markdownStrongWeight: FontWeight =
    if (isXiaomiOrRedmi()) FontWeight.SemiBold else FontWeight.Bold

private fun isXiaomiOrRedmi(): Boolean {
    val brand = runCatching { android.os.Build.BRAND }.getOrNull() ?: return false
    return brand.equals("xiaomi", ignoreCase = true) || brand.equals("redmi", ignoreCase = true)
}

private val inlineCodeStyle = TextStyle(
    fontSize = 13.sp,
    lineHeight = 18.sp,
    fontWeight = FontWeight.Medium,
    fontFamily = FontFamily.Monospace,
)

private val codeBlockStyle = TextStyle(
    fontSize = 13.sp,
    lineHeight = 20.sp,
    fontFamily = FontFamily.Monospace,
)

private val codeBannerStyle = TextStyle(fontSize = 13.sp, lineHeight = 18.sp)

private val bodyStyle = TextStyle(fontSize = 17.sp, lineHeight = 26.sp)
private val bodySmallStyle = TextStyle(fontSize = 15.sp, lineHeight = 24.sp)

val BodyMarkdownTypography = MarkdownTypography(
    body = bodyStyle,
    bodyStrong = bodyStyle.copy(fontWeight = markdownStrongWeight),
    strongWeight = markdownStrongWeight,
    h1 = TextStyle(fontSize = 23.sp, lineHeight = 34.sp, fontWeight = markdownStrongWeight),
    h2 = TextStyle(fontSize = 21.sp, lineHeight = 30.sp, fontWeight = markdownStrongWeight),
    h3 = TextStyle(fontSize = 19.sp, lineHeight = 28.sp, fontWeight = markdownStrongWeight),
    h4 = TextStyle(fontSize = 17.sp, lineHeight = 26.sp, fontWeight = markdownStrongWeight),
    h5 = TextStyle(fontSize = 17.sp, lineHeight = 26.sp, fontWeight = markdownStrongWeight),
    h6 = TextStyle(fontSize = 17.sp, lineHeight = 26.sp, fontWeight = markdownStrongWeight),
    small = bodySmallStyle,
    smallStrong = bodySmallStyle.copy(fontWeight = markdownStrongWeight),
    table = bodySmallStyle,
    tableHead = bodySmallStyle.copy(fontWeight = FontWeight.Medium),
    inlineCode = inlineCodeStyle,
    codeBlock = codeBlockStyle,
    codeBanner = codeBannerStyle,
    blockGap = 18.65.dp,
    headingTopGap = 40.8.dp,
    headingBottomGap = 10.dp,
    listIndent = 14.dp,
    listItemGap = 12.dp,
    listMarkerSize = 4.6.dp,
    orderedMarkerCharWidth = 8.dp,
    ruleGap = 12.dp,
    ruleThickness = 1.1.dp,
    quoteRailWidth = 3.dp,
    quoteGap = 12.dp,
    quoteVerticalPadding = 4.dp,
    codeRadius = 12.dp,
    codePaddingStart = 12.dp,
    codePaddingVertical = 10.dp,
    codeHeaderHeight = 36.dp,
    codeHeaderDivider = 1.dp,
    codeHeaderStartPadding = 16.dp,
    codeHeaderEndPadding = 8.dp,
    codeCopyIconSize = 20.dp,
    codeCopyButtonRadius = 16.dp,
    tableBorderWidth = 1.dp,
    tableCellVerticalPadding = 8.dp,
    tableCellHorizontalPadding = 12.dp,
    tableRadius = 8.dp,
    tableMinColumnWidth = 72.dp,
    tableMaxColumnWidth = 240.dp,
    imageRadius = 12.dp,
    formulaInlineFontSize = 14.sp,
    formulaBlockFontSize = 16.sp,
    formulaBlockPaddingVertical = 24.dp,
    formulaBlockPaddingHorizontal = 8.dp,
)

val SmallMarkdownTypography = BodyMarkdownTypography.copy(
    body = bodySmallStyle,
    bodyStrong = bodySmallStyle.copy(fontWeight = markdownStrongWeight),
    h1 = bodySmallStyle.copy(fontWeight = markdownStrongWeight),
    h2 = bodySmallStyle.copy(fontWeight = markdownStrongWeight),
    h3 = bodySmallStyle.copy(fontWeight = markdownStrongWeight),
    h4 = bodySmallStyle.copy(fontWeight = markdownStrongWeight),
    h5 = bodySmallStyle.copy(fontWeight = markdownStrongWeight),
    h6 = bodySmallStyle.copy(fontWeight = markdownStrongWeight),
    blockGap = 16.6.dp,
    headingTopGap = 36.4.dp,
    headingBottomGap = 9.dp,
    listItemGap = 10.dp,
    listMarkerSize = 4.1.dp,
    ruleGap = 10.dp,
    quoteVerticalPadding = 3.dp,
    codePaddingStart = 10.dp,
    codePaddingVertical = 8.dp,
    tableCellVerticalPadding = 6.dp,
    tableCellHorizontalPadding = 10.dp,
)

val LocalMarkdownTypography = staticCompositionLocalOf { BodyMarkdownTypography }

val LocalMarkdownStreaming = staticCompositionLocalOf { false }

val LocalMarkdownTextColor = staticCompositionLocalOf { Color.Unspecified }

@Composable
internal fun markdownTextColor(): Color {
    val value = LocalMarkdownTextColor.current
    return if (value == Color.Unspecified) LocalMarkdownColors.current.text else value
}
