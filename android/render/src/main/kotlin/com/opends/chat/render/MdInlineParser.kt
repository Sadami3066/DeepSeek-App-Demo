package com.opends.chat.render

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.runtime.Composable
import java.net.URI

internal sealed class MdInline {
    class Text(val text: String) : MdInline()

    class Delim(val char: Char, var count: Int, val canOpen: Boolean, val canClose: Boolean) : MdInline()

    class Code(val text: String) : MdInline()

    class Math(val latex: String) : MdInline()

    class Citation(val number: Int) : MdInline()

    class Link(val url: String, val children: List<MdInline>) : MdInline()

    class Styled(val kind: MdInlineKind, val children: List<MdInline>) : MdInline()
}

internal enum class MdInlineKind { Strong, Emphasis, Strike }

internal class InlineRenderResult(
    val text: AnnotatedString,
    val inlineContent: Map<String, InlineTextContent>,
)

internal class InlineRenderContext(
    val colors: MarkdownColors,
    val typography: MarkdownTypography,
    val strongWeight: FontWeight,
    val textColor: Color,
    val inlineMath: (String) -> InlineMathContent?,
    val citationHandler: ((Int) -> Unit)?,
)

internal val LocalCitationHandler = staticCompositionLocalOf<((Int) -> Unit)?> { null }

internal object MdInlineParser {
    fun parse(raw: String, ctx: InlineRenderContext): InlineRenderResult {
        val nodes = parseNodes(raw, 0)
        val builder = AnnotatedString.Builder()
        val inlineContent = LinkedHashMap<String, InlineTextContent>()
        var mathIndex = 0
        var citationIndex = 0
        val baseStyle = SpanStyle(color = ctx.textColor)

        fun render(nodes: List<MdInline>, style: SpanStyle) {
            for (node in nodes) {
                when (node) {
                    is MdInline.Text -> {
                        builder.pushStyle(style)
                        builder.append(node.text)
                        builder.pop()
                    }
                    is MdInline.Delim -> {
                        builder.pushStyle(style)
                        builder.append(node.char.toString().repeat(node.count))
                        builder.pop()
                    }
                    is MdInline.Code -> {
                        builder.pushStyle(
                            style.merge(
                                SpanStyle(
                                    fontFamily = ctx.typography.inlineCode.fontFamily ?: FontFamily.Monospace,
                                    fontSize = ctx.typography.inlineCode.fontSize,
                                    fontWeight = ctx.typography.inlineCode.fontWeight,
                                    background = ctx.colors.codeBackground,
                                    color = ctx.textColor,
                                )
                            )
                        )
                        builder.append(node.text)
                        builder.pop()
                    }
                    is MdInline.Math -> {
                        val math = runCatching { ctx.inlineMath(node.latex) }.getOrNull()
                        if (math != null) {
                            val id = "od-math-${mathIndex++}"
                            builder.appendInlineContent(id, node.latex)
                            inlineContent[id] = InlineTextContent(
                                placeholder = Placeholder(
                                    width = math.width,
                                    height = math.height,
                                    placeholderVerticalAlign = math.verticalAlign,
                                )
                            ) { math.content() }
                        } else {
                            builder.pushStyle(style.merge(SpanStyle(color = ctx.colors.secondary)))
                            builder.append("$" + node.latex + "$")
                            builder.pop()
                        }
                    }
                    is MdInline.Citation -> {
                        val id = "od-citation-${citationIndex++}"
                        builder.appendInlineContent(id, "引用 ${node.number}")
                        inlineContent[id] = citationContent(node.number, ctx)
                    }
                    is MdInline.Link -> {
                        val start = builder.length
                        val linkStyle = style.merge(
                            SpanStyle(
                                color = ctx.colors.secondary,
                                textDecoration = TextDecoration.Underline,
                            )
                        )
                        render(node.children, linkStyle)
                        val url = sanitizeUrl(node.url)
                        if (url != null && builder.length > start) {
                            builder.addLink(
                                LinkAnnotation.Url(
                                    url = url,
                                    styles = TextLinkStyles(
                                        style = SpanStyle(
                                            color = ctx.colors.secondary,
                                            textDecoration = TextDecoration.Underline,
                                        )
                                    ),
                                ),
                                start,
                                builder.length,
                            )
                        }
                    }
                    is MdInline.Styled -> {
                        val extra = when (node.kind) {
                            MdInlineKind.Strong -> SpanStyle(fontWeight = ctx.strongWeight)
                            MdInlineKind.Emphasis -> SpanStyle(fontStyle = FontStyle.Italic)
                            MdInlineKind.Strike -> SpanStyle(textDecoration = TextDecoration.LineThrough)
                        }
                        render(node.children, style.merge(extra))
                    }
                }
            }
        }

        render(nodes, baseStyle)
        return InlineRenderResult(builder.toAnnotatedString(), inlineContent)
    }

    private fun citationContent(number: Int, ctx: InlineRenderContext): InlineTextContent {
        val digits = number.toString().length
        val width = (5 + digits * 6).sp
        val shape = RoundedCornerShape(3.dp)
        return InlineTextContent(
            placeholder = Placeholder(
                width = width,
                height = 14.sp,
                placeholderVerticalAlign = PlaceholderVerticalAlign.AboveBaseline,
            )
        ) {
            val handler = ctx.citationHandler
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape)
                    .background(ctx.colors.placeholderBackground)
                    .semantics { contentDescription = "引用 $number" }
                    .clickable(enabled = handler != null) { handler?.invoke(number) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = number.toString(),
                    style = androidx.compose.ui.text.TextStyle(
                        color = ctx.colors.secondary,
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                )
            }
        }
    }

    private fun matchesBreakTag(text: String, start: Int): Boolean {
        val remaining = text.length - start
        if (remaining < 4) return false
        return text.regionMatches(start, "<br>", 0, 4, ignoreCase = true) ||
            (remaining >= 5 && text.regionMatches(start, "<br/>", 0, 5, ignoreCase = true)) ||
            (remaining >= 6 && text.regionMatches(start, "<br />", 0, 6, ignoreCase = true))
    }

    private fun breakTagLength(text: String, start: Int): Int = when {
        text.regionMatches(start, "<br />", 0, 6, ignoreCase = true) -> 6
        text.regionMatches(start, "<br/>", 0, 5, ignoreCase = true) -> 5
        else -> 4
    }

    private fun htmlEntityAt(text: String, start: Int): Pair<String, Int>? {
        val end = text.indexOf(';', start + 1)
        if (end < 0 || end - start > 10) return null
        val body = text.substring(start + 1, end)
        val decoded = when {
            body == "amp" -> "&"
            body == "lt" -> "<"
            body == "gt" -> ">"
            body == "quot" -> "\""
            body == "apos" -> "'"
            body == "nbsp" -> "\u00A0"
            body.startsWith("#x") || body.startsWith("#X") -> body.substring(2).toIntOrNull(16)?.let { codePointToText(it) }
            body.startsWith("#") -> body.substring(1).toIntOrNull()?.let { codePointToText(it) }
            else -> null
        } ?: return null
        return decoded to (end - start + 1)
    }

    private fun codePointToText(codePoint: Int): String {
        if (codePoint < 0 || codePoint > 0x10FFFF) return ""
        return try {
            String(Character.toChars(codePoint))
        } catch (error: IllegalArgumentException) {
            ""
        }
    }

    private fun parseNodes(text: String, depth: Int): List<MdInline> {
        val nodes = tokenize(text, depth)
        return reorderTrailingPunctuation(applyEmphasis(nodes))
    }

    private fun isAtomicInline(node: MdInline): Boolean =
        node is MdInline.Citation || node is MdInline.Link || node is MdInline.Math

    private fun reorderTrailingPunctuation(nodes: List<MdInline>): List<MdInline> {
        val result = ArrayList<MdInline>(nodes.size + 8)
        var index = 0
        while (index < nodes.size) {
            val node = nodes[index]
            if (isAtomicInline(node)) {
                var last = index
                while (last + 1 < nodes.size && isAtomicInline(nodes[last + 1])) last++
                val next = nodes.getOrNull(last + 1)
                if (next is MdInline.Text) {
                    val spaces = next.text.takeWhile { it == ' ' || it == '\u3000' }.length
                    val punctuation = next.text.substring(spaces).takeWhile { it in TRAILING_PUNCTUATION }
                    if (punctuation.isNotEmpty()) {
                        val rest = next.text.substring(spaces + punctuation.length)
                        if (spaces == 0) {
                            result.add(MdInline.Text(WORD_JOINER))
                            result.add(MdInline.Text(punctuation))
                            result.add(MdInline.Text(WORD_JOINER))
                            for (k in index..last) {
                                result.add(nodes[k])
                                if (k < last) result.add(MdInline.Text(WORD_JOINER))
                            }
                        } else {
                            for (k in index..last) {
                                result.add(nodes[k])
                                if (k < last) result.add(MdInline.Text(WORD_JOINER))
                            }
                            result.add(MdInline.Text(WORD_JOINER))
                            result.add(MdInline.Text(punctuation))
                        }
                        if (rest.isNotEmpty()) result.add(MdInline.Text(rest))
                        index = last + 2
                        continue
                    }
                }
            }
            when (node) {
                is MdInline.Styled -> result.add(MdInline.Styled(node.kind, reorderTrailingPunctuation(node.children)))
                else -> result.add(node)
            }
            index++
        }
        return result
    }

    private fun tokenize(text: String, depth: Int): MutableList<MdInline> {
        val nodes = ArrayList<MdInline>()
        val buffer = StringBuilder()
        fun flush() {
            if (buffer.isNotEmpty()) {
                nodes.add(MdInline.Text(buffer.toString()))
                buffer.setLength(0)
            }
        }
        var i = 0
        while (i < text.length) {
            val c = text[i]
            when {
                c == '<' && matchesBreakTag(text, i) -> {
                    flush()
                    nodes.add(MdInline.Text("\n"))
                    i += breakTagLength(text, i)
                }
                c == '&' && htmlEntityAt(text, i) != null -> {
                    val entity = htmlEntityAt(text, i)!!
                    buffer.append(entity.first)
                    i += entity.second
                }
                c == '\\' && i + 1 < text.length && (text[i + 1] == '(' || text[i + 1] == '[') -> {
                    val closeToken = if (text[i + 1] == '(') "\\)" else "\\]"
                    val close = text.indexOf(closeToken, i + 2)
                    if (close >= 0) {
                        val latex = text.substring(i + 2, close)
                        if (latex.isNotBlank() && !latex.contains('\n')) {
                            flush()
                            nodes.add(MdInline.Math(latex))
                            i = close + 2
                        } else {
                            buffer.append(text, i, i + 2)
                            i += 2
                        }
                    } else {
                        buffer.append(text, i, i + 2)
                        i += 2
                    }
                }
                c == '\\' && i + 1 < text.length && text[i + 1] in ESCAPABLE -> {
                    buffer.append(text[i + 1])
                    i += 2
                }
                c == '`' -> {
                    val run = runLength(text, i, '`')
                    val close = findRunEnd(text, i + run, '`', run)
                    if (close >= 0) {
                        flush()
                        var code = text.substring(i + run, close)
                        if (code.length >= 2 && code.startsWith(" ") && code.endsWith(" ") && code.isNotBlank()) {
                            code = code.substring(1, code.length - 1)
                        }
                        nodes.add(MdInline.Code(code.replace('\n', ' ')))
                        i = close + run
                    } else {
                        buffer.append(text, i, i + run)
                        i += run
                    }
                }
                c == '*' || c == '_' -> {
                    val run = runLength(text, i, c)
                    flush()
                    nodes.add(
                        MdInline.Delim(
                            char = c,
                            count = run,
                            canOpen = canOpen(text, i, run, c),
                            canClose = canClose(text, i, run, c),
                        )
                    )
                    i += run
                }
                c == '~' && i + 1 < text.length && text[i + 1] == '~' -> {
                    val run = runLength(text, i, '~')
                    flush()
                    nodes.add(
                        MdInline.Delim(
                            char = '~',
                            count = run,
                            canOpen = run >= 2 && canOpen(text, i, run, '~'),
                            canClose = run >= 2 && canClose(text, i, run, '~'),
                        )
                    )
                    i += run
                }
                c == '!' && i + 1 < text.length && text[i + 1] == '[' -> {
                    val parsed = parseLinkAt(text, i + 1, depth)
                    if (parsed != null) {
                        flush()
                        nodes.add(MdInline.Link(parsed.url, parsed.children))
                        i = parsed.end
                    } else {
                        buffer.append(c)
                        i++
                    }
                }
                c == '[' && text.startsWith("[citation:", i) -> {
                    val close = text.indexOf(']', i + 10)
                    val number = if (close > i) text.substring(i + 10, close).toIntOrNull() else null
                    if (number != null) {
                        flush()
                        nodes.add(MdInline.Citation(number))
                        i = close + 1
                    } else {
                        buffer.append(c)
                        i++
                    }
                }
                c == '[' -> {
                    val parsed = parseLinkAt(text, i, depth)
                    if (parsed != null) {
                        flush()
                        nodes.add(MdInline.Link(parsed.url, parsed.children))
                        i = parsed.end
                    } else {
                        buffer.append(c)
                        i++
                    }
                }
                c == '$' -> {
                    val parsed = parseMathAt(text, i)
                    if (parsed != null) {
                        flush()
                        nodes.add(MdInline.Math(parsed.latex))
                        i = parsed.end
                    } else {
                        buffer.append(c)
                        i++
                    }
                }
                c == 'h' && (text.startsWith("http://", i) || text.startsWith("https://", i)) -> {
                    var end = i
                    while (end < text.length && !text[end].isWhitespace() && text[end] !in URL_STOP) end++
                    val url = text.substring(i, end).trimEnd('.', ',', ';', ':', '!', '?', ')', ']', '，', '。')
                    if (url.length > 8) {
                        flush()
                        nodes.add(MdInline.Link(url, listOf(MdInline.Text(url))))
                        i = i + url.length
                    } else {
                        buffer.append(c)
                        i++
                    }
                }
                c == '<' && i + 1 < text.length && (text[i + 1] == 'h' || text[i + 1] == 'H') -> {
                    val close = text.indexOf('>', i + 1)
                    if (close > i) {
                        val inner = text.substring(i + 1, close)
                        if (inner.startsWith("http://") || inner.startsWith("https://")) {
                            flush()
                            nodes.add(MdInline.Link(inner, listOf(MdInline.Text(inner))))
                            i = close + 1
                        } else {
                            buffer.append(c)
                            i++
                        }
                    } else {
                        buffer.append(c)
                        i++
                    }
                }
                c == '\r' -> i++
                c == '\n' -> {
                    buffer.append('\n')
                    i++
                }
                else -> {
                    buffer.append(c)
                    i++
                }
            }
        }
        flush()
        return nodes
    }

    private fun applyEmphasis(nodes: MutableList<MdInline>): List<MdInline> {
        if (nodes.size < 3) return nodes
        var guard = 0
        while (guard++ < 512) {
            val match = findMatch(nodes) ?: break
            val openerIndex = match.opener
            val closerIndex = match.closer
            val opener = nodes[openerIndex] as MdInline.Delim
            val closer = nodes[closerIndex] as MdInline.Delim
            val use = match.use
            val children = ArrayList(nodes.subList(openerIndex + 1, closerIndex))
            nodes.subList(openerIndex + 1, closerIndex).clear()
            opener.count -= use
            closer.count -= use
            val kind = when {
                opener.char == '~' -> MdInlineKind.Strike
                use == 2 -> MdInlineKind.Strong
                else -> MdInlineKind.Emphasis
            }
            val styled = MdInline.Styled(kind, applyEmphasis(children))
            var insertAt = openerIndex + 1
            nodes.add(insertAt, styled)
            if (closer.count == 0) nodes.removeAt(insertAt + 1)
            if (opener.count == 0) nodes.removeAt(openerIndex)
        }
        return nodes
    }

    private class EmphasisMatch(val opener: Int, val closer: Int, val use: Int)

    private fun findMatch(nodes: List<MdInline>): EmphasisMatch? {
        val stack = ArrayList<Int>()
        for (index in nodes.indices) {
            val node = nodes[index]
            if (node !is MdInline.Delim) continue
            if (node.canClose && node.count > 0) {
                var s = stack.size - 1
                while (s >= 0) {
                    val openerIndex = stack[s]
                    val opener = nodes[openerIndex] as MdInline.Delim
                    if (opener.char != node.char || !opener.canOpen || opener.count <= 0) {
                        s--
                        continue
                    }
                    if (node.char == '~' && (opener.count < 2 || node.count < 2)) {
                        s--
                        continue
                    }
                    if (node.char != '~') {
                        val odd = (opener.canClose || node.canOpen) &&
                            (opener.count + node.count) % 3 == 0 &&
                            !(opener.count % 3 == 0 && node.count % 3 == 0)
                        if (odd) {
                            s--
                            continue
                        }
                    }
                    val use = when {
                        node.char == '~' -> 2
                        opener.count >= 2 && node.count >= 2 -> 2
                        else -> 1
                    }
                    return EmphasisMatch(openerIndex, index, use)
                }
                stack.clear()
            }
            if (node.canOpen && node.count > 0) stack.add(index)
        }
        return null
    }

    private class ParsedLink(val url: String, val children: List<MdInline>, val end: Int)

    private fun parseLinkAt(text: String, start: Int, depth: Int): ParsedLink? {
        if (start >= text.length || text[start] != '[') return null
        var i = start + 1
        var brackets = 1
        while (i < text.length) {
            when (text[i]) {
                '\\' -> i++
                '[' -> brackets++
                ']' -> {
                    brackets--
                    if (brackets == 0) break
                }
            }
            i++
        }
        if (i >= text.length) return null
        val label = text.substring(start + 1, i)
        var j = i + 1
        if (j >= text.length || text[j] != '(') {
            if (j < text.length && text[j] == '[') {
                return ParsedLink("", listOf(MdInline.Text(label)), j + 1)
            }
            return null
        }
        j++
        while (j < text.length && text[j] == ' ') j++
        val urlStart = j
        var parens = 1
        var angle = false
        if (j < text.length && text[j] == '<') {
            angle = true
            j++
            val close = text.indexOf('>', j)
            if (close < 0) return null
            val url = text.substring(j, close)
            j = close + 1
            while (j < text.length && text[j] != ')') j++
            if (j >= text.length) return null
            return ParsedLink(url, parseLabel(label, depth), j + 1)
        }
        while (j < text.length) {
            val ch = text[j]
            when {
                ch == '\\' -> j++
                ch == '(' -> parens++
                ch == ')' -> {
                    parens--
                    if (parens == 0) break
                }
            }
            j++
        }
        if (j >= text.length) return null
        var url = text.substring(urlStart, j).trim()
        val spaceIndex = url.indexOfFirst { it == ' ' || it == '\t' }
        if (spaceIndex >= 0) url = url.substring(0, spaceIndex)
        if (url.startsWith("<") && url.endsWith(">")) url = url.substring(1, url.length - 1)
        return ParsedLink(url, parseLabel(label, depth), j + 1)
    }

    private fun parseLabel(label: String, depth: Int): List<MdInline> {
        if (label.isEmpty()) return emptyList()
        if (depth > 4 || label.length > 4000) return listOf(MdInline.Text(label))
        return parseNodes(label, depth + 1)
    }

    private class ParsedMath(val latex: String, val end: Int)

    private fun parseMathAt(text: String, start: Int): ParsedMath? {
        if (start + 1 >= text.length) return null
        val doubleDollar = text[start + 1] == '$'
        val open = if (doubleDollar) 2 else 1
        var i = start + open
        if (i >= text.length) return null
        if (text[i] == ' ' || text[i] == '\n') return null
        while (i < text.length) {
            val c = text[i]
            if (c == '\\') {
                i += 2
                continue
            }
            if (c == '$') {
                if (doubleDollar) {
                    if (i + 1 < text.length && text[i + 1] == '$') {
                        val latex = text.substring(start + open, i)
                        if (latex.isNotEmpty() && !latex.endsWith(" ")) {
                            return ParsedMath(latex, i + 2)
                        }
                        return null
                    }
                } else {
                    if (i + 1 < text.length && text[i + 1] == '$') {
                        return null
                    }
                    val latex = text.substring(start + open, i)
                    if (latex.isNotEmpty() && !latex.endsWith(" ") && !latex.contains('\n')) {
                        return ParsedMath(latex, i + 1)
                    }
                    return null
                }
            }
            if (c == '\n' && !doubleDollar) return null
            i++
        }
        return null
    }

    private fun sanitizeUrl(url: String): String? {
        var value = url.trim()
        if (value.isEmpty()) return null
        if (value.startsWith("#")) return null
        if (value.startsWith("mailto:")) return null
        if (!value.contains("://")) {
            if (value.startsWith("www.")) value = "https://$value" else return null
        }
        return runCatching {
            val uri = URI(value)
            when (uri.scheme) {
                "http", "https" -> uri.toString()
                else -> null
            }
        }.getOrNull()
    }

    private fun runLength(text: String, start: Int, char: Char): Int {
        var count = 0
        while (start + count < text.length && text[start + count] == char) count++
        return count
    }

    private fun findRunEnd(text: String, from: Int, char: Char, length: Int): Int {
        var i = from
        while (i < text.length) {
            if (text[i] == char) {
                val run = runLength(text, i, char)
                if (run == length) return i
                i += run
            } else {
                i++
            }
        }
        return -1
    }

    private fun canOpen(text: String, start: Int, length: Int, char: Char): Boolean {
        val before = if (start == 0) '\n' else text[start - 1]
        val after = if (start + length >= text.length) '\n' else text[start + length]
        val leftFlanking = !after.isWhitespace() && (!isPunct(after) || before.isWhitespace() || isPunct(before))
        if (char != '_') return leftFlanking
        val rightFlanking = !before.isWhitespace() && (!isPunct(before) || after.isWhitespace() || isPunct(after))
        return leftFlanking && (!rightFlanking || isPunct(before))
    }

    private fun canClose(text: String, start: Int, length: Int, char: Char): Boolean {
        val before = if (start == 0) '\n' else text[start - 1]
        val after = if (start + length >= text.length) '\n' else text[start + length]
        val rightFlanking = !before.isWhitespace() && (!isPunct(before) || after.isWhitespace() || isPunct(after))
        if (char != '_') return rightFlanking
        val leftFlanking = !after.isWhitespace() && (!isPunct(after) || before.isWhitespace() || isPunct(before))
        return rightFlanking && (!leftFlanking || isPunct(after))
    }

    private fun isPunct(c: Char): Boolean = c in PUNCTUATION

    private const val ESCAPABLE = "\\`*_{}[]()#+-.!|~$<>@&;:'\""
    private const val URL_STOP = "<>\"'`"
    private const val PUNCTUATION = "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~，。！？；：、（）《》【】…—·“”‘’"
    private const val TRAILING_PUNCTUATION = "。，、；：！？,.;:!?"
    private const val WORD_JOINER = "\u2060"
}
