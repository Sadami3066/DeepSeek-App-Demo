package com.opends.chat.render

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
internal fun MdTableView(block: MdBlock.Table) {
    val colors = LocalMarkdownColors.current
    val typography = LocalMarkdownTypography.current
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val columnCount = remember(block) {
        maxOf(block.header.size, block.rows.maxOfOrNull { it.size } ?: 0)
    }
    if (columnCount == 0) return
    val maxCellWidth = typography.tableMaxColumnWidth
    val minCellWidth = typography.tableMinColumnWidth
    val horizontalPadding = typography.tableCellHorizontalPadding
    val verticalPadding = typography.tableCellVerticalPadding
    val borderWidth = typography.tableBorderWidth
    val textColor = markdownTextColor()
    val borderColor = if (colors.isDark) colors.outlineStrong else colors.outline
    val widths = remember(block, colors.isDark, typography, maxCellWidth) {
        computeWidths(
            block = block,
            columnCount = columnCount,
            measurer = measurer,
            style = typography.table,
            minWidth = minCellWidth,
            maxWidth = maxCellWidth,
            horizontalPadding = horizontalPadding,
            density = density,
        )
    }
    val totalWidth = widths.fold(0.dp) { acc, value -> acc + value }

    Box(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
        Column(
            modifier = Modifier
                .width(totalWidth)
                .clip(RoundedCornerShape(typography.tableRadius))
                .border(borderWidth, borderColor, RoundedCornerShape(typography.tableRadius)),
        ) {
            MdTableRow(
                cells = block.header,
                alignments = block.alignments,
                widths = widths,
                style = typography.tableHead.merge(TextStyle(color = colors.secondary)),
                horizontalPadding = horizontalPadding,
                verticalPadding = verticalPadding,
                columnCount = columnCount,
                backgroundColor = colors.codeBackground,
                divider = false,
                dividerColor = borderColor,
                borderWidth = borderWidth,
            )
            block.rows.forEachIndexed { index, row ->
                MdTableRow(
                    cells = row,
                    alignments = block.alignments,
                    widths = widths,
                    style = typography.table.merge(TextStyle(color = textColor)),
                    horizontalPadding = horizontalPadding,
                    verticalPadding = verticalPadding,
                    columnCount = columnCount,
                    backgroundColor = null,
                    divider = index != block.rows.lastIndex,
                    dividerColor = borderColor,
                    borderWidth = borderWidth,
                )
            }
        }
    }
}

@Composable
private fun MdTableRow(
    cells: List<String>,
    alignments: List<TableAlign>,
    widths: List<Dp>,
    style: TextStyle,
    horizontalPadding: Dp,
    verticalPadding: Dp,
    columnCount: Int,
    backgroundColor: Color?,
    divider: Boolean,
    dividerColor: Color,
    borderWidth: Dp,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .let { if (backgroundColor != null) it.background(backgroundColor) else it },
        ) {
            for (column in 0 until columnCount) {
                val cell = cells.getOrNull(column) ?: ""
                val alignment = alignments.getOrNull(column) ?: TableAlign.Start
                Box(
                    modifier = Modifier
                        .width(widths.getOrElse(column) { 100.dp })
                        .padding(horizontal = horizontalPadding, vertical = verticalPadding),
                    contentAlignment = when (alignment) {
                        TableAlign.Center -> Alignment.Center
                        TableAlign.End -> Alignment.CenterEnd
                        else -> Alignment.CenterStart
                    },
                ) {
                    MdInlineText(
                        raw = cell,
                        style = style,
                        textAlign = when (alignment) {
                            TableAlign.Center -> TextAlign.Center
                            TableAlign.End -> TextAlign.End
                            else -> TextAlign.Start
                        },
                    )
                }
            }
        }
        if (divider) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(borderWidth)
                    .background(dividerColor),
            )
        }
    }
}

private fun computeWidths(
    block: MdBlock.Table,
    columnCount: Int,
    measurer: TextMeasurer,
    style: TextStyle,
    minWidth: Dp,
    maxWidth: Dp,
    horizontalPadding: Dp,
    density: androidx.compose.ui.unit.Density,
): List<Dp> {
    val paddingPx = with(density) { (horizontalPadding * 2).toPx() }
    val minPx = with(density) { minWidth.toPx() }
    val maxPx = with(density) { maxWidth.toPx() }
    val widths = FloatArray(columnCount) { minPx }
    fun measureCell(text: String, column: Int) {
        if (text.isEmpty()) return
        val measured = measurer.measure(
            text = AnnotatedString(text),
            style = style,
            softWrap = false,
            constraints = Constraints(maxWidth = maxPx.toInt()),
        )
        val target = (measured.size.width + paddingPx).coerceIn(minPx, maxPx)
        if (target > widths[column]) widths[column] = target
    }
    block.header.forEachIndexed { index, cell -> measureCell(cell, index) }
    block.rows.forEach { row ->
        row.forEachIndexed { index, cell -> if (index < columnCount) measureCell(cell, index) }
    }
    return widths.map { with(density) { it.toDp() } }
}
