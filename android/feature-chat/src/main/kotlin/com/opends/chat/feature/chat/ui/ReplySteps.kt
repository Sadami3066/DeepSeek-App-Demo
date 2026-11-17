package com.opends.chat.feature.chat.ui

import com.opends.chat.core.ChatMessage
import com.opends.chat.core.Fragment
import com.opends.chat.core.MessageStatus
import com.opends.chat.core.SearchResult

enum class ToolStatus { WIP, FINISHED, FAILED }

data class OpenPage(
    val url: String?,
    val title: String?,
    val siteName: String?,
    val siteIcon: String?,
    val failed: Boolean,
)

sealed interface ReplyStep {
    val key: String

    data class Thinking(
        override val key: String,
        val content: String,
        val elapsedSeconds: Double?,
        val streaming: Boolean,
    ) : ReplyStep

    data class Search(
        override val key: String,
        val stage: Int?,
        val queries: List<String>,
        val results: List<SearchResult>,
        val status: ToolStatus,
        val deepSearch: Boolean,
        val stopped: Boolean,
    ) : ReplyStep

    data class Open(
        override val key: String,
        val stage: Int?,
        val pages: List<OpenPage>,
        val status: ToolStatus,
        val mergedWithSearch: Boolean,
        val stopped: Boolean,
    ) : ReplyStep

    data class Find(
        override val key: String,
        val stage: Int?,
        val patterns: List<String>,
        val results: List<String>,
        val status: ToolStatus,
        val noResult: Boolean,
        val stopped: Boolean,
    ) : ReplyStep

    data class ReadingLink(
        override val key: String,
        val url: String,
        val title: String?,
        val status: ToolStatus,
        val stopped: Boolean,
    ) : ReplyStep
}

data class ReplyStepList(
    val steps: List<ReplyStep>,
    val answer: String,
    val template: String,
    val deepSearch: Boolean,
)

private class StageKeys {
    private var synthetic = 0
    private var inNullRun = false

    fun keyOf(stageId: Int?): Int {
        if (stageId != null) {
            inNullRun = false
            return stageId
        }
        if (!inNullRun) {
            synthetic -= 1
            inNullRun = true
        }
        return synthetic
    }

    fun reset() {
        inNullRun = false
    }
}

private fun aggregateStatus(first: ToolStatus, second: ToolStatus): ToolStatus = when {
    first == ToolStatus.WIP || second == ToolStatus.WIP -> ToolStatus.WIP
    first == ToolStatus.FAILED && second == ToolStatus.FAILED -> ToolStatus.FAILED
    else -> ToolStatus.FINISHED
}

internal fun buildReplySteps(
    message: ChatMessage,
    streaming: Boolean,
): ReplyStepList {
    val hasBody = message.fragments.any { it is Fragment.Response && it.content.isNotBlank() }
    val searchStages = HashSet<Int>()
    run {
        val keys = StageKeys()
        message.fragments.forEach { fragment ->
            when (fragment) {
                is Fragment.Search -> searchStages.add(keys.keyOf(fragment.stageId))
                is Fragment.ToolOpen -> keys.keyOf(fragment.stageId)
                is Fragment.ToolFind -> keys.keyOf(fragment.stageId)
                else -> keys.reset()
            }
        }
    }
    val steps = ArrayList<ReplyStep>()
    val answer = StringBuilder()
    val template = StringBuilder()
    val keys = StageKeys()
    var lastSearchStage: Int? = null
    var lastOpenStage: Int? = null
    var lastFindStage: Int? = null
    var hasOpenOrFind = false
    val stopped = !streaming && (message.status == MessageStatus.INCOMPLETE ||
        message.status == MessageStatus.WIP)

    message.fragments.forEachIndexed { index, fragment ->
        when (fragment) {
            is Fragment.Think -> {
                if (fragment.content.isBlank()) return@forEachIndexed
                keys.reset()
                lastSearchStage = null
                lastOpenStage = null
                lastFindStage = null
                val last = steps.lastOrNull()
                if (last is ReplyStep.Thinking) {
                    steps[steps.lastIndex] = last.copy(
                        content = last.content + fragment.content,
                        elapsedSeconds = (last.elapsedSeconds ?: 0.0) + (fragment.elapsedSecs ?: 0.0),
                        streaming = streaming && !hasBody,
                    )
                } else {
                    steps.add(
                        ReplyStep.Thinking(
                            key = "think-$index",
                            content = fragment.content,
                            elapsedSeconds = fragment.elapsedSecs,
                            streaming = streaming && !hasBody,
                        ),
                    )
                }
            }
            is Fragment.Search -> {
                lastOpenStage = null
                lastFindStage = null
                val stage = keys.keyOf(fragment.stageId)
                val status = toolStatusOf(fragment.status)
                val last = steps.lastOrNull()
                if (last is ReplyStep.Search && lastSearchStage == stage) {
                    steps[steps.lastIndex] = last.copy(
                        queries = (last.queries + fragment.queries).distinct(),
                        results = (last.results + fragment.results).distinctBy { it.url },
                        status = aggregateStatus(last.status, status),
                    )
                } else {
                    steps.add(
                        ReplyStep.Search(
                            key = "search-$index",
                            stage = stage,
                            queries = fragment.queries.distinct(),
                            results = fragment.results.distinctBy { it.url },
                            status = status,
                            deepSearch = false,
                            stopped = stopped && status == ToolStatus.WIP,
                        ),
                    )
                }
                lastSearchStage = stage
            }
            is Fragment.ToolOpen -> {
                hasOpenOrFind = true
                lastSearchStage = null
                lastFindStage = null
                val stage = keys.keyOf(fragment.stageId)
                val status = toolStatusOf(fragment.status)
                val page = OpenPage(
                    url = fragment.result?.url,
                    title = fragment.result?.title,
                    siteName = fragment.result?.siteName,
                    siteIcon = fragment.result?.siteIcon,
                    failed = status == ToolStatus.FAILED,
                )
                val last = steps.lastOrNull()
                if (last is ReplyStep.Open && lastOpenStage == stage) {
                    steps[steps.lastIndex] = last.copy(
                        pages = last.pages + page,
                        status = aggregateStatus(last.status, status),
                    )
                } else {
                    steps.add(
                        ReplyStep.Open(
                            key = "open-$index",
                            stage = stage,
                            pages = listOf(page),
                            status = status,
                            mergedWithSearch = stage in searchStages,
                            stopped = stopped && status == ToolStatus.WIP,
                        ),
                    )
                }
                lastOpenStage = stage
            }
            is Fragment.ToolFind -> {
                hasOpenOrFind = true
                lastSearchStage = null
                lastOpenStage = null
                val stage = keys.keyOf(fragment.stageId)
                val status = toolStatusOf(fragment.status)
                val pattern = fragment.pattern?.takeIf { it.isNotBlank() }
                val last = steps.lastOrNull()
                if (last is ReplyStep.Find && lastFindStage == stage) {
                    steps[steps.lastIndex] = last.copy(
                        patterns = (last.patterns + listOfNotNull(pattern)).distinct(),
                        results = (last.results + listOfNotNull(fragment.result?.takeIf { it.isNotBlank() })).distinct(),
                        status = aggregateStatus(last.status, status),
                    )
                } else {
                    steps.add(
                        ReplyStep.Find(
                            key = "find-$index",
                            stage = stage,
                            patterns = listOfNotNull(pattern),
                            results = listOfNotNull(fragment.result?.takeIf { it.isNotBlank() }),
                            status = status,
                            noResult = fragment.status.uppercase() == "NO_RESULT",
                            stopped = stopped && status == ToolStatus.WIP,
                        ),
                    )
                }
                lastFindStage = stage
            }
            is Fragment.ReadLink -> {
                keys.reset()
                lastSearchStage = null
                lastOpenStage = null
                lastFindStage = null
                steps.add(
                    ReplyStep.ReadingLink(
                        key = "read-$index",
                        url = fragment.url,
                        title = fragment.title,
                        status = toolStatusOf(fragment.status),
                        stopped = stopped,
                    ),
                )
            }
            is Fragment.Response -> {
                keys.reset()
                if (answer.isNotEmpty()) answer.append("\n\n")
                answer.append(fragment.content)
            }
            is Fragment.TemplateResponse -> {
                keys.reset()
                if (template.isNotEmpty()) template.append("\n\n")
                template.append(fragment.content)
            }
            else -> keys.reset()
        }
    }
    val deepSearch = message.conversationMode?.uppercase()?.contains("DEEP_SEARCH") == true ||
        hasOpenOrFind
    val normalized = steps.map { step ->
        when (step) {
            is ReplyStep.Search -> step.copy(deepSearch = deepSearch)
            is ReplyStep.Open -> step.copy(
                mergedWithSearch = step.stage != null && step.stage in searchStages,
            )
            else -> step
        }
    }
    return ReplyStepList(
        steps = normalized,
        answer = answer.toString(),
        template = template.toString(),
        deepSearch = deepSearch,
    )
}

internal fun toolStatusOf(raw: String?): ToolStatus = when (raw?.uppercase()) {
    "FINISHED" -> ToolStatus.FINISHED
    "FAILED", "NO_RESULT" -> ToolStatus.FAILED
    else -> ToolStatus.WIP
}

internal fun ReplyStep.searchLabel(streaming: Boolean, queryIndex: Int): String? {
    val step = this as? ReplyStep.Search ?: return null
    return when (step.status) {
        ToolStatus.WIP -> when {
            step.stopped || !streaming -> "搜索已停止"
            step.queries.isNotEmpty() -> "搜索 ${step.queries[queryIndex % step.queries.size]}"
            else -> "正在搜索"
        }
        ToolStatus.FAILED -> "联网搜索暂不可用"
        ToolStatus.FINISHED -> when {
            step.results.isEmpty() -> "未搜索到合适的内容"
            step.deepSearch -> if (step.results.size == 1) {
                "已搜索到 1 个网页"
            } else {
                "已搜索到 ${step.results.size} 个网页"
            }
            else -> "已阅读 ${step.results.size} 个网页"
        }
    }
}

internal fun ReplyStep.openLabel(streaming: Boolean): String? {
    val step = this as? ReplyStep.Open ?: return null
    return when {
        step.status == ToolStatus.FINISHED && step.mergedWithSearch -> "已浏览搜索结果"
        step.status == ToolStatus.FINISHED -> if (step.pages.size == 1) {
            "已浏览 1 个页面"
        } else {
            "已浏览 ${step.pages.size} 个页面"
        }
        step.status == ToolStatus.WIP -> if (step.stopped || !streaming) "浏览页面失败" else "正在浏览网页"
        else -> "浏览页面失败"
    }
}

internal fun ReplyStep.findLabel(streaming: Boolean): String? {
    val step = this as? ReplyStep.Find ?: return null
    val pattern = step.patterns.firstOrNull()
    return when {
        step.status == ToolStatus.WIP -> when {
            step.stopped || !streaming -> "查找失败"
            pattern != null -> "正在查找“$pattern”"
            else -> "正在查找"
        }
        step.noResult -> if (pattern != null) "未查找到“$pattern”" else "未查找到相关内容"
        step.status == ToolStatus.FAILED -> "查找失败"
        pattern != null -> "已查找到“$pattern”"
        else -> "查找关键词"
    }
}
