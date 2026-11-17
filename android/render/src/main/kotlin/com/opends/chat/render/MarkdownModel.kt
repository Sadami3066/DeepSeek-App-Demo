package com.opends.chat.render

internal sealed interface MdBlock {
    data class Paragraph(val text: String) : MdBlock

    data class Heading(val level: Int, val text: String) : MdBlock

    data class Code(val code: String, val language: String, val closed: Boolean) : MdBlock

    data class Quote(val blocks: List<MdBlock>) : MdBlock

    data class BulletList(val items: List<MdListItem>) : MdBlock

    data class OrderedList(val start: Int, val items: List<MdListItem>) : MdBlock

    data class Table(
        val header: List<String>,
        val alignments: List<TableAlign>,
        val rows: List<List<String>>,
        val closed: Boolean,
    ) : MdBlock

    data class Math(val latex: String, val closed: Boolean) : MdBlock

    data object Rule : MdBlock
}

internal data class MdListItem(val blocks: List<MdBlock>, val checked: Boolean?)

internal enum class TableAlign { Start, Center, End }

