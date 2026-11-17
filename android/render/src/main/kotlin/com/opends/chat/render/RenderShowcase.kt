package com.opends.chat.render

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.opends.chat.design.theme.OpenDSTheme

@Composable
fun RenderShowcase(modifier: Modifier = Modifier, text: String = ShowcaseMarkdown) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        OdMarkdown(text = text)
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 2600)
@Composable
private fun RenderShowcasePreview() {
    OpenDSTheme(darkTheme = false) {
        RenderShowcase()
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 2600)
@Composable
private fun RenderShowcaseDarkPreview() {
    OpenDSTheme(darkTheme = true) {
        RenderShowcase()
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 600)
@Composable
private fun ThinkingMarkdownPreview() {
    OpenDSTheme(darkTheme = false) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            OdMarkdown(text = ThinkingShowcaseMarkdown, variant = OdMarkdownVariant.Small)
        }
    }
}

private val ShowcaseMarkdown: String = buildString {
    append(
        """
        # 一级标题 23/34

        ## 二级标题 21/30

        ### 三级标题 19/28

        #### 四级标题 17/26

        正文 17sp/26sp：包含 **粗体 600/700**、*斜体*、~~删除线~~、`inline_code`、[链接](https://www.deepseek.com) 与引用角标[citation:3]。

        上一行末尾有换行<br>这一行由 HTML br 产生，实体 &amp; &lt; &gt; 也会解码。

        - 无序列表项一
        - 无序列表项二，含 `代码`
          - 嵌套列表项 A
          - 嵌套列表项 B
            1. 有序子项 1
            2. 有序子项 2
        - [x] 已完成任务
        - [ ] 未完成任务

        1. 有序列表第一项
        2. 有序列表第二项
        10. 两位序号对齐项

        > 引用文字，支持 **加粗**。
        > 第二行引用。

        ---

        """.trimIndent()
    )
    append("\n\n")
    append(
        "```kotlin\n" +
            "fun main() {\n" +
            "    val greeting = \"Hello, OpenDS\"\n" +
            "    println(greeting)\n" +
            "}\n" +
            "```\n"
    )
    append("\n\n")
    append(
        """
        | 语言 | 类型 | 语法 | 说明 |
        | --- | :---: | ---: | --- |
        | Kotlin | 静态 | 简洁 | JVM |
        | Python | 动态 | 灵活 | 数据 |

        行内公式 ${'$'}E=mc^2${'$'}，块级公式：

        ${'$'}${'$'}\int_0^1 x^2 dx = \frac{1}{3}${'$'}${'$'}

        ![示例图片](https://www.deepseek.com/favicon.ico)
        """.trimIndent()
    )
}

private val ThinkingShowcaseMarkdown: String = """
思考正文 15sp/24sp，包含 **加粗**、*斜体*、`行内代码` 与公式 ${'$'}a^2+b^2=c^2${'$'}。

- 列表项一
- 列表项二

> 引用内容[citation:12]
""".trimIndent()

@Composable
fun RenderShowcaseSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(vertical = 4.dp, horizontal = 8.dp),
    )
}

@Composable
fun RenderShowcaseDivider() {
    androidx.compose.material3.HorizontalDivider(modifier = Modifier.height(1.dp))
}
