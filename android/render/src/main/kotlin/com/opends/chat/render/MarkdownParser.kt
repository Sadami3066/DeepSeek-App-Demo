package com.opends.chat.render

internal class MarkdownParser {
    private var lastText: String = ""
    private var blocks: List<MdBlock> = emptyList()
    private var lastStartLine: Int = 0

    fun parse(text: String): List<MdBlock> {
        if (text == lastText) return blocks
        val lines = splitLines(text)
        val extended = text.startsWith(lastText) && blocks.isNotEmpty() && lastStartLine < lines.size
        if (!extended) {
            val parsed = parseBlocks(lines, 0)
            blocks = parsed.blocks
            lastStartLine = parsed.lastStartLine
            lastText = text
            return blocks
        }
        val keep = blocks.subList(0, blocks.size - 1)
        val parsed = parseBlocks(lines, lastStartLine)
        blocks = ArrayList<MdBlock>(keep.size + parsed.blocks.size).apply {
            addAll(keep)
            addAll(parsed.blocks)
        }
        if (parsed.blocks.isNotEmpty()) lastStartLine = parsed.lastStartLine
        lastText = text
        return blocks
    }

    private class Parsed(val blocks: List<MdBlock>, val lastStartLine: Int)

    private class Fence(val char: Char, val length: Int, val info: String)

    private class ListMarker(
        val indent: Int,
        val ordered: Boolean,
        val number: Int,
        val contentStart: Int,
        val contentIndent: Int,
    )

    private class TableStart(val header: List<String>, val alignments: List<TableAlign>)

    private fun splitLines(text: String): List<String> {
        val raw = text.split('\n')
        return raw.map { if (it.endsWith('\r')) it.dropLast(1) else it }
    }

    private fun parseBlocks(lines: List<String>, startLine: Int): Parsed {
        val blocks = ArrayList<MdBlock>()
        var lastStart = startLine
        var i = startLine
        while (i < lines.size) {
            val line = lines[i]
            if (line.isBlank()) {
                i++
                continue
            }
            val blockStart = i
            val fence = matchFence(line)
            if (fence != null) {
                val code = ArrayList<String>()
                var j = i + 1
                var closed = false
                while (j < lines.size) {
                    if (isClosingFence(lines[j], fence)) {
                        closed = true
                        j++
                        break
                    }
                    code.add(lines[j])
                    j++
                }
                blocks.add(MdBlock.Code(code.joinToString("\n"), fence.info, closed))
                lastStart = blockStart
                i = j
                continue
            }
            val heading = matchHeading(line)
            if (heading != null && heading.text.isNotBlank()) {
                blocks.add(MdBlock.Heading(heading.level, heading.text))
                lastStart = blockStart
                i++
                continue
            }
            if (isThematicBreak(line)) {
                blocks.add(MdBlock.Rule)
                lastStart = blockStart
                i++
                continue
            }
            if (isMathBlockStart(line)) {
                val math = collectMathBlock(lines, i)
                blocks.add(MdBlock.Math(math.latex, math.closed))
                lastStart = blockStart
                i = math.nextLine
                continue
            }
            if (matchQuoteStart(line) != null) {
                val inner = ArrayList<String>()
                var j = i
                while (j < lines.size) {
                    val current = lines[j]
                    val prefixLength = matchQuoteStart(current)
                    if (prefixLength != null) {
                        inner.add(current.substring(minOf(prefixLength, current.length)))
                        j++
                        continue
                    }
                    if (current.isBlank()) {
                        if (j + 1 < lines.size && matchQuoteStart(lines[j + 1]) != null) {
                            inner.add("")
                            j++
                            continue
                        }
                        break
                    }
                    if (j > i && !isBlockStartLine(current) && !isTableDelimiter(current)) {
                        inner.add(current)
                        j++
                        continue
                    }
                    break
                }
                blocks.add(MdBlock.Quote(parseBlocks(inner, 0).blocks))
                lastStart = blockStart
                i = j
                continue
            }
            if (matchListMarker(line) != null) {
                val collected = collectList(lines, i)
                blocks.add(collected.block)
                lastStart = blockStart
                i = collected.nextLine
                continue
            }
            val table = matchTable(lines, i)
            if (table != null) {
                val rows = ArrayList<List<String>>()
                var j = i + 2
                while (j < lines.size) {
                    val current = lines[j]
                    if (current.isBlank() || !current.contains('|') || isBlockStartLine(current)) break
                    rows.add(splitRow(current))
                    j++
                }
                blocks.add(
                    MdBlock.Table(
                        header = table.header,
                        alignments = table.alignments,
                        rows = rows,
                        closed = true,
                    )
                )
                lastStart = blockStart
                i = j
                continue
            }
            val paragraph = collectParagraph(lines, i)
            if (paragraph.text.isNotEmpty() || paragraph.headingLevel > 0) {
                if (paragraph.headingLevel > 0) {
                    blocks.add(MdBlock.Heading(paragraph.headingLevel, paragraph.text))
                } else {
                    blocks.add(MdBlock.Paragraph(paragraph.text))
                }
                lastStart = blockStart
            }
            i = if (paragraph.tableInterrupt > 0) paragraph.tableInterrupt else paragraph.nextLine
            if (paragraph.tableInterrupt > 0) lastStart = paragraph.tableInterrupt
            if (paragraph.text.isEmpty() && paragraph.tableInterrupt > 0) {
                i = paragraph.tableInterrupt
            }
        }
        if (blocks.isEmpty()) return Parsed(emptyList(), startLine)
        return Parsed(blocks, lastStart)
    }

    private class ParagraphResult(
        val text: String,
        val nextLine: Int,
        val headingLevel: Int,
        val tableInterrupt: Int,
    )

    private fun collectParagraph(lines: List<String>, start: Int): ParagraphResult {
        val content = ArrayList<String>()
        var j = start
        var headingLevel = 0
        var tableInterrupt = -1
        while (j < lines.size) {
            val line = lines[j]
            if (line.isBlank()) break
            if (j > start) {
                if (content.isNotEmpty() && isSetextUnderline(line)) {
                    headingLevel = if (line.trimStart().startsWith("=")) 1 else 2
                    j++
                    break
                }
                if (isTableDelimiter(line) && content.isNotEmpty() && content.last().contains('|')) {
                    tableInterrupt = j - 1
                    content.removeAt(content.size - 1)
                    j = j - 1
                    break
                }
                if (!isParagraphContinuation(line)) break
            }
            content.add(line)
            j++
        }
        return ParagraphResult(content.joinToString("\n"), j, headingLevel, tableInterrupt)
    }

    private fun isParagraphContinuation(line: String): Boolean {
        if (line.isBlank()) return false
        if (matchFence(line) != null) return false
        val heading = matchHeading(line)
        if (heading != null && heading.text.isNotBlank()) return false
        if (isThematicBreak(line)) return false
        if (matchQuoteStart(line) != null) return false
        if (isMathBlockStart(line)) return false
        if (matchListMarker(line) != null) return false
        return true
    }

    private class ListBlockResult(val block: MdBlock, val nextLine: Int)

    private fun collectList(lines: List<String>, start: Int): ListBlockResult {
        val first = matchListMarker(lines[start])!!
        val baseIndent = first.indent
        val ordered = first.ordered
        val itemLines = ArrayList<ArrayList<String>>()
        var currentItemIndent = first.contentIndent
        var current = ArrayList<String>()
        current.add(lines[start].substring(minOf(first.contentStart, lines[start].length)))
        var j = start + 1
        while (j < lines.size) {
            val line = lines[j]
            val marker = matchListMarker(line)
            if (marker != null && marker.indent == baseIndent && marker.ordered == ordered) {
                itemLines.add(current)
                current = ArrayList()
                current.add(line.substring(minOf(marker.contentStart, line.length)))
                currentItemIndent = marker.contentIndent
                j++
                continue
            }
            if (marker != null && marker.indent < baseIndent) break
            if (marker != null && marker.indent > baseIndent) {
                current.add(dedent(line, currentItemIndent))
                j++
                continue
            }
            if (line.isBlank()) {
                var k = j + 1
                while (k < lines.size && lines[k].isBlank()) k++
                if (k >= lines.size) break
                val next = lines[k]
                val nextMarker = matchListMarker(next)
                val continues = (nextMarker != null && nextMarker.indent >= baseIndent) ||
                    (!next.isBlank() && leadingSpaces(next) > baseIndent)
                if (!continues) break
                current.add("")
                j++
                continue
            }
            if (leadingSpaces(line) > baseIndent) {
                current.add(dedent(line, currentItemIndent))
                j++
                continue
            }
            if (isBlockStartLine(line) || isTableDelimiter(line)) break
            if (current.isNotEmpty() && current.last().isNotBlank()) {
                current.add(dedent(line, currentItemIndent))
                j++
                continue
            }
            break
        }
        itemLines.add(current)
        val items = itemLines.map { rawLines ->
            val cleaned = ArrayList(rawLines)
            while (cleaned.isNotEmpty() && cleaned.last().isBlank()) cleaned.removeAt(cleaned.size - 1)
            var checked: Boolean? = null
            if (cleaned.isNotEmpty()) {
                val head = cleaned[0]
                val trimmed = head.trimStart()
                if (trimmed.startsWith("[ ] ")) {
                    checked = false
                    cleaned[0] = trimmed.substring(4)
                } else if (trimmed.startsWith("[x] ") || trimmed.startsWith("[X] ")) {
                    checked = true
                    cleaned[0] = trimmed.substring(4)
                }
            }
            MdListItem(parseBlocks(cleaned, 0).blocks, checked)
        }
        val block = if (ordered) {
            MdBlock.OrderedList(first.number.coerceAtLeast(1), items)
        } else {
            MdBlock.BulletList(items)
        }
        return ListBlockResult(block, j)
    }

    private class MathResult(val latex: String, val closed: Boolean, val nextLine: Int)

    private fun collectMathBlock(lines: List<String>, start: Int): MathResult {
        val first = lines[start].trimStart()
        val doubleDollar = first.startsWith("$$")
        val open = if (doubleDollar) "$$" else "\\["
        val close = if (doubleDollar) "$$" else "\\]"
        val afterOpen = first.removePrefix(open)
        if (afterOpen.trimEnd().endsWith(close)) {
            val latex = afterOpen.trimEnd().removeSuffix(close)
            return MathResult(latex.trim(), true, start + 1)
        }
        val content = ArrayList<String>()
        if (afterOpen.isNotBlank()) content.add(afterOpen)
        var j = start + 1
        var closed = false
        while (j < lines.size) {
            val line = lines[j]
            val trimmedEnd = line.trimEnd()
            if (trimmedEnd.endsWith(close)) {
                val before = trimmedEnd.substring(0, trimmedEnd.length - close.length)
                if (before.isNotBlank()) content.add(before)
                closed = true
                j++
                break
            }
            content.add(line)
            j++
        }
        return MathResult(content.joinToString("\n").trim(), closed, j)
    }

    private fun matchTable(lines: List<String>, start: Int): TableStart? {
        if (start + 1 >= lines.size) return null
        val headerLine = lines[start]
        val delimiterLine = lines[start + 1]
        if (!headerLine.contains('|') || !isTableDelimiter(delimiterLine)) return null
        val header = splitRow(headerLine)
        val delimiterCells = splitRow(delimiterLine)
        if (header.isEmpty()) return null
        val alignments = header.indices.map { index ->
            val cell = delimiterCells.getOrNull(index)?.trim() ?: "---"
            when {
                cell.startsWith(":") && cell.endsWith(":") -> TableAlign.Center
                cell.endsWith(":") -> TableAlign.End
                else -> TableAlign.Start
            }
        }
        return TableStart(header, alignments)
    }

    private fun isTableDelimiter(line: String): Boolean {
        val trimmed = line.trim()
        if (trimmed.isEmpty()) return false
        if (!trimmed.contains('-')) return false
        val cells = splitRow(trimmed)
        if (cells.isEmpty()) return false
        return cells.all { cell ->
            val value = cell.trim()
            value.isNotEmpty() && value.all { it == '-' || it == ':' } && value.count { it == '-' } >= 1
        }
    }

    private fun splitRow(line: String): List<String> {
        var value = line.trim()
        if (value.startsWith("|")) value = value.substring(1)
        if (value.endsWith("|") && !value.endsWith("\\|")) value = value.substring(0, value.length - 1)
        val cells = ArrayList<String>()
        val builder = StringBuilder()
        var i = 0
        while (i < value.length) {
            val c = value[i]
            if (c == '\\' && i + 1 < value.length && value[i + 1] == '|') {
                builder.append('|')
                i += 2
                continue
            }
            if (c == '|') {
                cells.add(builder.toString().trim())
                builder.setLength(0)
                i++
                continue
            }
            builder.append(c)
            i++
        }
        cells.add(builder.toString().trim())
        return cells
    }

    private fun matchFence(line: String): Fence? {
        val indent = leadingSpaces(line)
        if (indent > 3) return null
        val rest = line.substring(indent)
        if (rest.length < 3) return null
        val char = rest[0]
        if (char != '`' && char != '~') return null
        var count = 0
        while (count < rest.length && rest[count] == char) count++
        if (count < 3) return null
        val info = rest.substring(count).trim().substringBefore(' ')
        return Fence(char, count, info)
    }

    private fun isClosingFence(line: String, fence: Fence): Boolean {
        val indent = leadingSpaces(line)
        if (indent > 3) return false
        val rest = line.substring(indent).trimEnd()
        if (rest.length < fence.length) return false
        var count = 0
        while (count < rest.length && rest[count] == fence.char) count++
        return count >= fence.length && count == rest.length
    }

    private class HeadingResult(val level: Int, val text: String)

    private fun matchHeading(line: String): HeadingResult? {
        val indent = leadingSpaces(line)
        if (indent > 3) return null
        val rest = line.substring(indent)
        var level = 0
        while (level < rest.length && rest[level] == '#' && level < 6) level++
        if (level == 0) return null
        if (level < rest.length && rest[level] != ' ') return null
        var text = rest.substring(level).trim()
        if (text.endsWith("#")) {
            val withoutHashes = text.trimEnd('#')
            if (withoutHashes.isEmpty() || withoutHashes.endsWith(" ")) text = withoutHashes.trim()
        }
        return HeadingResult(level, text)
    }

    private fun isThematicBreak(line: String): Boolean {
        val indent = leadingSpaces(line)
        if (indent > 3) return false
        val trimmed = line.substring(indent).trimEnd()
        if (trimmed.isEmpty()) return false
        val c = trimmed[0]
        if (c != '-' && c != '*' && c != '_') return false
        var count = 0
        for (ch in trimmed) {
            when {
                ch == c -> count++
                ch == ' ' -> Unit
                else -> return false
            }
        }
        return count >= 3
    }

    private fun isSetextUnderline(line: String): Boolean {
        val trimmed = line.trim()
        if (trimmed.isEmpty()) return false
        val c = trimmed[0]
        if (c != '=' && c != '-') return false
        return trimmed.length >= 1 && trimmed.all { it == c }
    }

    private fun matchQuoteStart(line: String): Int? {
        val indent = leadingSpaces(line)
        if (indent > 3) return null
        if (indent >= line.length || line[indent] != '>') return null
        var end = indent + 1
        if (end < line.length && line[end] == ' ') end++
        return end
    }

    private fun isMathBlockStart(line: String): Boolean {
        val trimmed = line.trimStart()
        if (trimmed.startsWith("$$")) return true
        return trimmed.startsWith("\\[")
    }

    private fun matchListMarker(line: String): ListMarker? {
        val indent = leadingSpaces(line)
        if (indent >= line.length) return null
        val rest = line.substring(indent)
        val c = rest[0]
        if (c == '-' || c == '*' || c == '+') {
            if (rest.length > 1 && rest[1] != ' ') return null
            var end = 1
            while (end < rest.length && rest[end] == ' ') end++
            val spaces = end - 1
            if (rest.length > 1 && spaces == 0) return null
            val contentStart = indent + end
            val contentIndent = indent + 1 + spaces.coerceIn(1, 4)
            return ListMarker(indent, false, 1, contentStart, contentIndent)
        }
        if (!c.isDigit()) return null
        var digits = 0
        while (digits < rest.length && rest[digits].isDigit() && digits < 9) digits++
        if (digits == 0 || digits >= rest.length) return null
        val delimiter = rest[digits]
        if (delimiter != '.' && delimiter != ')') return null
        val number = rest.substring(0, digits).toIntOrNull() ?: return null
        var end = digits + 1
        if (end < rest.length && rest[end] != ' ') return null
        while (end < rest.length && rest[end] == ' ') end++
        val spaces = end - digits - 1
        val contentStart = indent + end
        val contentIndent = indent + digits + 1 + spaces.coerceIn(1, 4)
        return ListMarker(indent, true, number, contentStart, contentIndent)
    }

    private fun isBlockStartLine(line: String): Boolean {
        if (line.isBlank()) return true
        if (matchFence(line) != null) return true
        val heading = matchHeading(line)
        if (heading != null && heading.text.isNotBlank()) return true
        if (isThematicBreak(line)) return true
        if (matchQuoteStart(line) != null) return true
        if (isMathBlockStart(line)) return true
        return false
    }

    private fun leadingSpaces(line: String): Int {
        var count = 0
        while (count < line.length && line[count] == ' ') count++
        return count
    }

    private fun dedent(line: String, indent: Int): String {
        var removed = 0
        while (removed < indent && removed < line.length && line[removed] == ' ') removed++
        return line.substring(removed)
    }
}
