package com.opends.chat.feature.chat.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.opends.chat.core.ChatMessage
import com.opends.chat.core.Fragment
import com.opends.chat.core.MessageStatus
import com.opends.chat.core.Role
import com.opends.chat.design.theme.OdTheme
import com.opends.chat.feature.chat.ReadingState
import com.opends.chat.feature.chat.ui.AssistantMessageItem
import com.opends.chat.feature.chat.ui.ChatTheme

private fun findMessage(
    id: Long,
    status: MessageStatus,
    fragments: List<Fragment>,
): ChatMessage = ChatMessage(
    id = id,
    parentId = null,
    role = Role.ASSISTANT,
    status = status,
    thinkingEnabled = true,
    insertedAt = id,
    fragments = fragments,
    conversationMode = "DEEP_SEARCH",
)

private val body = Fragment.Response(
    content = "根据页面内容，账号状态可以在「设置 → 账号与安全」里查看。",
)

@Composable
private fun PreviewFrame(content: @Composable () -> Unit) {
    ChatTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(OdTheme.colors.bgBase)
                .padding(horizontal = 22.15.dp, vertical = 16.dp),
        ) {
            Column { content() }
        }
    }
}

@Composable
private fun PreviewMessage(message: ChatMessage, streaming: Boolean) {
    AssistantMessageItem(
        message = message,
        isLast = true,
        streaming = streaming,
        busy = false,
        versions = null,
        reading = ReadingState(),
        onLongPress = { _, _ -> },
        onCopy = {},
        onReadAloud = { _, _ -> },
        onRegenerate = {},
        onContinue = {},
        onShare = {},
        onOpenSources = {},
        onOpenCitation = {},
        onSelectVersion = {},
    )
}

@Preview(name = "页内查找-完成", widthDp = 393, heightDp = 852, showBackground = true)
@Composable
private fun ToolFindFinishedPreview() {
    PreviewFrame {
        PreviewMessage(
            message = findMessage(
                id = 1L,
                status = MessageStatus.FINISHED,
                fragments = listOf(
                    Fragment.ToolFind(
                        status = "FINISHED",
                        stageId = 1,
                        pattern = "账号状态",
                        result = "命中 3 处：账号状态、账号状态异常、账号状态恢复。",
                    ),
                    body,
                ),
            ),
            streaming = false,
        )
    }
}

@Preview(name = "页内查找-无结果", widthDp = 393, heightDp = 852, showBackground = true)
@Composable
private fun ToolFindNoResultPreview() {
    PreviewFrame {
        PreviewMessage(
            message = findMessage(
                id = 2L,
                status = MessageStatus.FINISHED,
                fragments = listOf(
                    Fragment.ToolFind(status = "NO_RESULT", stageId = 1, pattern = "解封时限"),
                    body,
                ),
            ),
            streaming = false,
        )
    }
}

@Preview(name = "页内查找-进行中", widthDp = 393, heightDp = 852, showBackground = true)
@Composable
private fun ToolFindWipPreview() {
    PreviewFrame {
        PreviewMessage(
            message = findMessage(
                id = 3L,
                status = MessageStatus.WIP,
                fragments = listOf(
                    Fragment.ToolFind(status = "WIP", stageId = 1, pattern = "申诉入口"),
                ),
            ),
            streaming = true,
        )
    }
}

@Preview(name = "页内查找-多关键词", widthDp = 393, heightDp = 852, showBackground = true)
@Composable
private fun ToolFindMultiPatternPreview() {
    PreviewFrame {
        PreviewMessage(
            message = findMessage(
                id = 4L,
                status = MessageStatus.FINISHED,
                fragments = listOf(
                    Fragment.ToolFind(status = "FINISHED", stageId = 1, pattern = "账号状态", result = "命中 2 处。"),
                    Fragment.ToolFind(status = "FINISHED", stageId = 1, pattern = "申诉入口", result = null),
                    Fragment.ToolFind(status = "FINISHED", stageId = 1, pattern = "人工客服", result = "命中 1 处。"),
                    body,
                ),
            ),
            streaming = false,
        )
    }
}

@Preview(name = "工具行回归-真实TOOL_OPEN样本", widthDp = 393, heightDp = 852, showBackground = true)
@Composable
private fun ToolOpenRegressionPreview() {
    val fragments = buildList {
        add(Fragment.Think(content = "先检索申诉流程。", elapsedSecs = 1.26))
        add(
            Fragment.Search(
                queries = listOf("小红书 账号误封 申诉流程", "小红书 账号被封 申诉入口"),
                results = List(18) { index ->
                    com.opends.chat.core.SearchResult(
                        url = "https://example.com/$index",
                        title = "搜索结果 $index",
                        snippet = "摘要 $index",
                        siteName = "站点 $index",
                    )
                },
                status = "FINISHED",
                stageId = 1,
            ),
        )
        add(Fragment.Think(content = "继续打开页面。", elapsedSecs = 1.18))
        repeat(8) { index ->
            add(
                Fragment.ToolOpen(
                    status = "FINISHED",
                    stageId = 2,
                    result = if (index < 2) {
                        com.opends.chat.core.ToolOpenResult(
                            url = "https://example.com/page/$index",
                            title = "页面标题 $index",
                            siteName = "站点 $index",
                        )
                    } else {
                        null
                    },
                ),
            )
        }
        add(Fragment.Think(content = "整理回答。", elapsedSecs = 1.04))
        add(Fragment.Response(content = "申诉流程共三步：提交申诉、等待审核、补充材料。"))
        add(Fragment.Tip(content = "本回答由 AI 生成，内容仅供参考，请仔细甄别", style = "WARNING"))
    }
    PreviewFrame {
        PreviewMessage(
            message = findMessage(id = 5L, status = MessageStatus.FINISHED, fragments = fragments),
            streaming = false,
        )
    }
}
