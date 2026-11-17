package com.opends.chat.render

import org.junit.Assert.assertTrue
import org.junit.Test

class MathFormulaSizeTest {
    private val inlineFormulas = listOf(
        "\\sum_{i=1}^{n}",
        "\\sum_{i=1}^{n} t_i",
        "\\int_0^1 x\\,dx",
        "\\frac{a}{b}",
        "\\sqrt{x}",
        "\\max_{1\\le i\\le n}",
    )

    @Test
    fun textstyleWrappedFormulasHavePositiveSize() {
        for (latex in inlineFormulas) {
            val icon = org.scilab.forge.jlatexmath.TeXFormula("{\\textstyle $latex}")
                .createTeXIcon(
                    org.scilab.forge.jlatexmath.TeXConstants.STYLE_TEXT,
                    FORMULA_TEST_SIZE,
                )
            assertTrue("inline textstyle width for $latex", icon.iconWidth > 0)
            assertTrue("inline textstyle height for $latex", icon.iconHeight > 0)
        }
    }

    @Test
    fun textstyleWrappedDrawableHasPositiveIntrinsicSize() {
        for (latex in inlineFormulas) {
            val drawable = ru.noties.jlatexmath.JLatexMathDrawable.builder("{\\textstyle $latex}")
                .textSize(FORMULA_TEST_SIZE)
                .color(0xFF0F0F0F.toInt())
                .align(ru.noties.jlatexmath.JLatexMathDrawable.ALIGN_LEFT)
                .build()
            assertTrue("drawable width for $latex", drawable.intrinsicWidth > 0)
            assertTrue("drawable height for $latex", drawable.intrinsicHeight > 0)
        }
    }

    @Test
    fun displaystyleWrappedFormulasHavePositiveSize() {
        for (latex in inlineFormulas) {
            val icon = org.scilab.forge.jlatexmath.TeXFormula("{\\displaystyle $latex}")
                .createTeXIcon(
                    org.scilab.forge.jlatexmath.TeXConstants.STYLE_DISPLAY,
                    FORMULA_TEST_SIZE,
                )
            assertTrue("block displaystyle width for $latex", icon.iconWidth > 0)
            assertTrue("block displaystyle height for $latex", icon.iconHeight > 0)
        }
    }

    @Test
    fun inlinePlaceholderNeverExceedsLineHeight() {
        for (latex in inlineFormulas) {
            val drawable = ru.noties.jlatexmath.JLatexMathDrawable.builder("{\\textstyle $latex}")
                .textSize(FORMULA_TEST_SIZE)
                .color(0xFF0F0F0F.toInt())
                .build()
            val size = MathInlineSizing.compute(
                widthPx = drawable.intrinsicWidth.toFloat(),
                heightPx = drawable.intrinsicHeight.toFloat(),
                lineHeightPx = FORMULA_LINE_HEIGHT_PX,
            )
            assertTrue("inline width for $latex", size != null && size.widthPx > 0f)
            assertTrue("inline height for $latex", size != null && size.heightPx <= FORMULA_LINE_HEIGHT_PX)
        }
    }

    @Test
    fun zeroSizeBitmapIsRejected() {
        assertTrue(MathInlineSizing.compute(0f, 10f, FORMULA_LINE_HEIGHT_PX) == null)
        assertTrue(MathInlineSizing.compute(10f, 0f, FORMULA_LINE_HEIGHT_PX) == null)
        assertTrue(MathInlineSizing.compute(10f, 10f, 0f) == null)
    }

    private companion object {
        const val FORMULA_TEST_SIZE = 39.8f
        const val FORMULA_LINE_HEIGHT_PX = 73.9f
    }
}
