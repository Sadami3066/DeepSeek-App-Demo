package com.opends.chat.render

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.system.measureTimeMillis

class MarkdownParserTest {
    private fun parse(text: String): List<MdBlock> = MarkdownParser().parse(text)

    @Test
    fun parsesHeadingsAndParagraphs() {
        val blocks = parse("# 标题\n\n正文第一行\n正文第二行\n\n### 三级")
        assertEquals(3, blocks.size)
        assertEquals(MdBlock.Heading(1, "标题"), blocks[0])
        assertEquals(MdBlock.Paragraph("正文第一行\n正文第二行"), blocks[1])
        assertEquals(MdBlock.Heading(3, "三级"), blocks[2])
    }

    @Test
    fun parsesEmphasisMarkersAsRawTextForInlineStage() {
        val blocks = parse("这是 **粗体** 和 *斜体* 与 `代码`")
        assertEquals(1, blocks.size)
        assertEquals("这是 **粗体** 和 *斜体* 与 `代码`", (blocks[0] as MdBlock.Paragraph).text)
    }

    @Test
    fun parsesBulletAndOrderedLists() {
        val blocks = parse("- 第一项\n- 第二项\n  - 嵌套项\n\n1. 有序一\n2. 有序二")
        assertEquals(2, blocks.size)
        val bullet = blocks[0] as MdBlock.BulletList
        assertEquals(2, bullet.items.size)
        assertTrue(bullet.items[1].blocks.any { it is MdBlock.BulletList })
        val ordered = blocks[1] as MdBlock.OrderedList
        assertEquals(1, ordered.start)
        assertEquals(2, ordered.items.size)
    }

    @Test
    fun parsesTaskList() {
        val blocks = parse("- [x] 已完成\n- [ ] 未完成")
        val list = blocks[0] as MdBlock.BulletList
        assertEquals(true, list.items[0].checked)
        assertEquals(false, list.items[1].checked)
    }

    @Test
    fun parsesQuoteWithNestedMarkdown() {
        val blocks = parse("> 引用 **粗体**\n> 第二行\n\n正文")
        val quote = blocks[0] as MdBlock.Quote
        assertEquals(1, quote.blocks.size)
        assertEquals("引用 **粗体**\n第二行", (quote.blocks[0] as MdBlock.Paragraph).text)
    }

    @Test
    fun parsesFencedCodeWithLanguage() {
        val blocks = parse("```kotlin\nfun main() {}\n```")
        val code = blocks[0] as MdBlock.Code
        assertEquals("kotlin", code.language)
        assertEquals("fun main() {}", code.code)
        assertTrue(code.closed)
    }

    @Test
    fun unclosedFenceStillRendersAsCode() {
        val blocks = parse("说明\n\n```python\ndef broken():\n    pass")
        assertEquals(2, blocks.size)
        val code = blocks[1] as MdBlock.Code
        assertEquals("python", code.language)
        assertEquals("def broken():\n    pass", code.code)
        assertTrue(!code.closed)
    }

    @Test
    fun parsesTableWithAlignment() {
        val blocks = parse("| 名称 | 数量 |\n| --- | :---: |\n| 苹果 | 3 |\n| 香蕉 | 5 |")
        val table = blocks[0] as MdBlock.Table
        assertEquals(listOf("名称", "数量"), table.header)
        assertEquals(listOf(TableAlign.Start, TableAlign.Center), table.alignments)
        assertEquals(2, table.rows.size)
        assertEquals(listOf("苹果", "3"), table.rows[0])
    }

    @Test
    fun tableInterruptsParagraph() {
        val blocks = parse("下面是表格：\n| a | b |\n| --- | --- |\n| 1 | 2 |")
        assertEquals(2, blocks.size)
        assertEquals(MdBlock.Paragraph("下面是表格："), blocks[0])
        assertTrue(blocks[1] is MdBlock.Table)
    }

    @Test
    fun parsesBlockMathAndKeepsUnclosed() {
        val closed = parse("$$\n\\frac{1}{2}\n$$")
        assertEquals(MdBlock.Math("\\frac{1}{2}", true), closed[0])

        val unclosed = parse("$$\na^2 + b^2")
        val math = unclosed[0] as MdBlock.Math
        assertEquals("a^2 + b^2", math.latex)
        assertTrue(!math.closed)
    }

    @Test
    fun parsesRuleAndSetextHeading() {
        val blocks = parse("大写标题\n===\n\n---")
        assertEquals(MdBlock.Heading(1, "大写标题"), blocks[0])
        assertEquals(MdBlock.Rule, blocks[1])
    }

    @Test
    fun incrementalParseMatchesFullParse() {
        val source = buildString {
            append("# 流式文档\n\n")
            append("这是第一段，包含 **粗体** 与 `代码`。\n\n")
            append("- 列表项一\n- 列表项二\n\n")
            append("> 引用一行\n\n")
            append("```kotlin\n")
            append("fun demo(): Int {\n    return 42\n}\n")
            append("```\n\n")
            append("| 列 A | 列 B |\n| --- | --- |\n| 1 | 2 |\n\n")
            append("结尾段落，公式 \$E=mc^2\$。\n")
        }
        val incremental = MarkdownParser()
        incremental.parse("")
        val step = 7
        var offset = 0
        while (offset < source.length) {
            offset = minOf(source.length, offset + step)
            incremental.parse(source.substring(0, offset))
        }
        val incrementalBlocks = incremental.parse(source)
        val fullBlocks = MarkdownParser().parse(source)
        assertEquals(fullBlocks, incrementalBlocks)
    }

    @Test
    fun longDocumentParsesWithinBudget() {
        val paragraph = "这是一段用于压力测试的中文正文，包含 **加粗**、`行内代码`、[链接](https://example.com) 等行内语法。"
        val code = "```kotlin\n" + (1..400).joinToString("\n") { "    val value$it = $it * 2" } + "\n```"
        val document = buildString {
            repeat(420) { index ->
                append("## 第 $index 节\n\n")
                append(paragraph).append('\n')
                append(paragraph).append("\n\n")
                append("- 列表项 $index\n- 另一个列表项\n\n")
            }
            append(code)
        }
        val cost = measureTimeMillis { MarkdownParser().parse(document) }
        assertTrue("完整解析耗时 ${cost}ms 超出预算", cost < 800)
        assertTrue("文档过短，未达到压力测试目标", document.length > 50_000)

        val incremental = MarkdownParser()
        val chunks = document.chunked(64)
        var shown = ""
        val streamingCost = measureTimeMillis {
            chunks.forEach { chunk ->
                shown += chunk
                incremental.parse(shown)
            }
        }
        assertTrue("流式增量解析总耗时 ${streamingCost}ms 超出预算", streamingCost < 5_000)
    }
}

