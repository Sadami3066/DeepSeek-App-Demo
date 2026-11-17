package com.opends.chat.render

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.opends.chat.design.theme.OdDarkColors
import com.opends.chat.design.theme.OdLightColors
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkdownThemeTest {
    @Test
    fun bodyTypographyMatchesOfficialRenderValues() {
        val t = BodyMarkdownTypography
        assertEquals(17f, t.body.fontSize.value, 0.01f)
        assertEquals(26f, t.body.lineHeight.value, 0.01f)
        assertEquals(15f, t.small.fontSize.value, 0.01f)
        assertEquals(24f, t.small.lineHeight.value, 0.01f)
        assertEquals(18.65f, t.blockGap.value, 0.01f)
        assertEquals(40.8f, t.headingTopGap.value, 0.01f)
        assertEquals(10f, t.headingBottomGap.value, 0.01f)
        assertEquals(23f, t.h1.fontSize.value, 0.01f)
        assertEquals(21f, t.h2.fontSize.value, 0.01f)
        assertEquals(19f, t.h3.fontSize.value, 0.01f)
        assertEquals(17f, t.h4.fontSize.value, 0.01f)
        assertEquals(17f, t.h5.fontSize.value, 0.01f)
        assertEquals(17f, t.h6.fontSize.value, 0.01f)
        assertEquals(34f, t.h1.lineHeight.value, 0.01f)
        assertEquals(30f, t.h2.lineHeight.value, 0.01f)
        assertEquals(28f, t.h3.lineHeight.value, 0.01f)
        assertEquals(t.strongWeight, t.h1.fontWeight)
        assertEquals(13f, t.codeBlock.fontSize.value, 0.01f)
        assertEquals(20f, t.codeBlock.lineHeight.value, 0.01f)
        assertEquals(13f, t.inlineCode.fontSize.value, 0.01f)
        assertEquals(18f, t.inlineCode.lineHeight.value, 0.01f)
        assertEquals(15f, t.table.fontSize.value, 0.01f)
        assertEquals(12.dp, t.codeRadius)
        assertEquals(3.dp, t.quoteRailWidth)
        assertEquals(12.dp, t.listItemGap)
        assertEquals(14.dp, t.listIndent)
        assertEquals(1.1f, t.ruleThickness.value, 0.01f)
        assertEquals(14f, t.formulaInlineFontSize.value, 0.01f)
        assertEquals(16f, t.formulaBlockFontSize.value, 0.01f)
    }

    @Test
    fun smallTypographyMatchesThinkingSpec() {
        val t = SmallMarkdownTypography
        assertEquals(15f, t.body.fontSize.value, 0.01f)
        assertEquals(24f, t.body.lineHeight.value, 0.01f)
        assertEquals(t.strongWeight, t.h3.fontWeight)
    }

    @Test
    fun colorsFollowDesignTokens() {
        val light = markdownColorsFrom(OdLightColors)
        assertEquals(OdLightColors.bodyText, light.text)
        assertEquals(OdLightColors.brandText, light.brand)
        assertEquals(OdLightColors.markdownCodeBlock, light.codeBackground)
        assertEquals(OdLightColors.rail, light.quoteBar)
        assertEquals(OdLightColors.rail, light.rule)
        assertEquals(OdLightColors.markdownCodeBlockBanner, light.codeBanner)
        assertEquals(OdLightColors.markdownCodeBlock, light.inlineCodeBackground)
        assertEquals(OdLightColors.markdownCitation, light.citationBackground)
        assertEquals(OdLightColors.rail, light.rail)
        assertEquals(OdLightColors.divider, light.divider)
        assertTrue(!light.isDark)

        val dark = markdownColorsFrom(OdDarkColors)
        assertEquals(OdDarkColors.bodyText, dark.text)
        assertEquals(OdDarkColors.markdownCitation, dark.citationBackground)
        assertTrue(dark.isDark)
    }
}
