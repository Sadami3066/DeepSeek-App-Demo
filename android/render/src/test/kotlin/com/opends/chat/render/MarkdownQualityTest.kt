package com.opends.chat.render

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkdownQualityTest {
    private fun context(): InlineRenderContext {
        val colors = markdownColorsFrom(com.opends.chat.design.theme.OdLightColors)
        return InlineRenderContext(
            colors = colors,
            typography = BodyMarkdownTypography,
            strongWeight = BodyMarkdownTypography.strongWeight,
            textColor = colors.text,
            inlineMath = { null },
            citationHandler = null,
        )
    }

    @Test
    fun decodesHtmlEntitiesAndBreaks() {
        val result = MdInlineParser.parse("a &amp; b<br>c &lt;d&gt; &quot;e&quot;", context())
        assertEquals("a & b\nc <d> \"e\"", result.text.text)
    }

    @Test
    fun linkUsesSecondaryColorWithUnderline() {
        val result = MdInlineParser.parse("[链接](https://example.com)", context())
        val spans = result.text.spanStyles
        assertTrue(spans.any { it.item.textDecoration == androidx.compose.ui.text.style.TextDecoration.Underline })
        assertTrue(spans.any { it.item.color != Color.Unspecified })
    }

    @Test
    fun trimsUnmatchedStrongMarkerWhileStreaming() {
        assertEquals("这是粗", trimIncompleteMarkdownTail("这是**粗"))
        assertEquals("**粗体**", trimIncompleteMarkdownTail("**粗体**"))
        assertEquals("这是斜", trimIncompleteMarkdownTail("这是*斜"))
        assertEquals("价格 5", trimIncompleteMarkdownTail("价格 5"))
    }

    @Test
    fun keepsClosedInlineCodeAndDropsOpenOne() {
        assertEquals("代码 `x`", trimIncompleteMarkdownTail("代码 `x`"))
        assertEquals("代码 x", trimIncompleteMarkdownTail("代码 `x"))
    }

    @Test
    fun inlineParserEmitsInlineContentForSumOnlyFormula() {
        val colors = markdownColorsFrom(com.opends.chat.design.theme.OdLightColors)
        val context = InlineRenderContext(
            colors = colors,
            typography = BodyMarkdownTypography,
            strongWeight = BodyMarkdownTypography.strongWeight,
            textColor = colors.text,
            inlineMath = {
                InlineMathContent(
                    width = 20.sp,
                    height = 20.sp,
                    verticalAlign = androidx.compose.ui.text.PlaceholderVerticalAlign.Center,
                ) {}
            },
            citationHandler = null,
        )
        val result = MdInlineParser.parse(
            "行内公式：协程并发执行 \$\\sum_{i=1}^{n}\$ 的时间复杂度约为 \$O(n)\$，而串行执行为 \$\\sum_{i=1}^{n} t_i\$。",
            context,
        )
        assertEquals(3, result.inlineContent.size)
        assertTrue(!result.text.text.contains("$"))
    }

    @Test
    fun inlineParserFallsBackToLatexTextWhenProviderFails() {
        val result = MdInlineParser.parse("公式 \$\\sum_{i=1}^{n}\$ 结束", context())
        assertTrue(result.inlineContent.isEmpty())
        assertTrue(result.text.text.contains("\$\\sum_{i=1}^{n}\$"))
    }

    @Test
    fun citationPunctuationMovesBeforeBadges() {
        val result = MdInlineParser.parse("明显更陡[citation:1][citation:10]。", context())
        val text = result.text.text
        assertTrue(text.contains("\u2060"))
        assertTrue(text.indexOf("。") in 0 until text.indexOf("引用 1"))
        assertTrue(!text.contains("。\n"))
        assertEquals(2, result.inlineContent.size)
    }

    @Test
    fun linkPunctuationMovesBeforeLinkText() {
        val result = MdInlineParser.parse("详见[链接](https://x.com)。", context())
        val text = result.text.text
        assertTrue(text.contains("\u2060"))
        assertTrue(text.indexOf("。") < text.indexOf("链接"))
    }

    @Test
    fun mathPunctuationMovesBeforeFormula() {
        val colors = markdownColorsFrom(com.opends.chat.design.theme.OdLightColors)
        val mathContext = InlineRenderContext(
            colors = colors,
            typography = BodyMarkdownTypography,
            strongWeight = BodyMarkdownTypography.strongWeight,
            textColor = colors.text,
            inlineMath = {
                InlineMathContent(
                    width = 20.sp,
                    height = 20.sp,
                    verticalAlign = androidx.compose.ui.text.PlaceholderVerticalAlign.Center,
                ) {}
            },
            citationHandler = null,
        )
        val result = MdInlineParser.parse("复杂度为\$O(n)\$。", mathContext)
        val text = result.text.text
        assertTrue(text.contains("\u2060"))
        assertTrue(text.indexOf("。") < text.indexOf("O(n)"))
    }

    @Test
    fun citationPunctuationInsideEmphasisMovesBeforeBadge() {
        val result = MdInlineParser.parse("**更陡[citation:1]。**", context())
        val text = result.text.text
        assertTrue(text.indexOf("。") in 0 until text.indexOf("引用 1"))
    }

    @Test
    fun spaceSeparatedPunctuationAfterUrlIsJoinedWithWordJoiner() {
        val result = MdInlineParser.parse("详见 https://x.com 。", context())
        val text = result.text.text
        assertTrue(text.contains("\u2060"))
        assertTrue(!text.contains("。\n"))
        assertTrue(text.indexOf("https://x.com") < text.indexOf("。"))
    }

    @Test
    fun strongUsesBrandWeight() {
        val weight = BodyMarkdownTypography.strongWeight
        assertTrue(weight == FontWeight.SemiBold || weight == FontWeight.Bold)
    }
}
