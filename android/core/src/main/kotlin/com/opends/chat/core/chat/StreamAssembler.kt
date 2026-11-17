package com.opends.chat.core.chat

import com.google.gson.JsonObject
import com.opends.chat.core.util.array
import com.opends.chat.core.util.obj
import com.opends.chat.core.util.string

internal class StreamAssembler(initialBlocks: List<WireBlock> = emptyList()) {

    val blocks: MutableList<WireBlock> = initialBlocks.toMutableList()
    private val indexMap = HashMap<Int, WireBlock>()

    var stopReason: String? = null
        private set
    var title: String? = null
        private set
    var inputTokens: Int = 0
        private set
    var cacheCreationTokens: Int = 0
        private set
    var cacheReadTokens: Int = 0
        private set
    var outputTokens: Int = 0
        private set
    var canContinue: Boolean = true
        private set
    var thinkingActive: Boolean = false
        private set
    var endedNormally: Boolean = false
        private set
    var sawAnyEvent: Boolean = false
        private set
    var citationCount: Int = 0
        private set
    private var stageCounter: Int = 0
    private val openPageByToolId = HashMap<String, WireBlock.OpenPage>()
    private val findPageByToolId = HashMap<String, WireBlock.FindPage>()

    fun totalUsage(): Int = inputTokens + cacheCreationTokens + cacheReadTokens + outputTokens

    fun onEvent(eventName: String?, data: JsonObject) {
        sawAnyEvent = true
        val type = eventName ?: data.string("type")
        when (type) {
            "message_start" -> applyUsage(data.obj("message")?.obj("usage"))
            "content_block_start" -> {
                val index = indexOf(data) ?: return
                val blockJson = data.obj("content_block") ?: return
                val blockType = blockJson.string("type")
                val block = when (blockType) {
                    "text" -> WireBlock.Text().also { block ->
                        block.text.append(blockJson.string("text") ?: "")
                        blockJson.array("citations")?.forEach { element ->
                            if (element.isJsonObject) applyCitation(block, element.asJsonObject)
                        }
                    }
                    "thinking" -> WireBlock.Thinking().also {
                        it.text.append(blockJson.string("thinking") ?: "")
                        it.startedAt = System.currentTimeMillis()
                        stageCounter += 1
                        it.stageId = stageCounter
                    }
                    "server_tool_use" -> {
                        canContinue = false
                        val stage = stageCounter.takeIf { it > 0 }
                        val input = blockJson.obj("input")
                        when (input?.string("action")) {
                            "open_page" -> WireBlock.OpenPage().also { page ->
                                page.status = "WIP"
                                page.stageId = stage
                                page.toolUseId = blockJson.string("id") ?: ""
                                page.url = input.string("url")
                                openPageByToolId[page.toolUseId] = page
                            }
                            "find_in_page" -> WireBlock.FindPage().also { find ->
                                find.status = "WIP"
                                find.stageId = stage
                                find.toolUseId = blockJson.string("id") ?: ""
                                find.pattern = input.string("pattern")
                                findPageByToolId[find.toolUseId] = find
                            }
                            else -> WireBlock.Atomic(blockJson, stage)
                        }
                    }
                    "web_search_tool_result" -> {
                        canContinue = false
                        WireBlock.Atomic(blockJson, stageCounter.takeIf { it > 0 })
                    }
                    "web_fetch_tool_result" -> {
                        canContinue = false
                        val toolUseId = blockJson.string("tool_use_id") ?: ""
                        val page = openPageByToolId[toolUseId]
                        val find = findPageByToolId[toolUseId]
                        if (page != null) {
                            WireCodec.applyFetchResult(page, blockJson)
                            null
                        } else if (find != null) {
                            WireCodec.applyFetchResult(find, blockJson)
                            null
                        } else {
                            WireBlock.Atomic(blockJson, stageCounter.takeIf { it > 0 })
                        }
                    }
                    "tool_use" -> {
                        canContinue = false
                        null
                    }
                    else -> null
                }
                if (block != null) {
                    indexMap[index] = block
                    blocks.add(block)
                    thinkingActive = block is WireBlock.Thinking
                }
            }
            "content_block_delta" -> {
                val index = indexOf(data) ?: return
                val delta = data.obj("delta") ?: return
                val block = indexMap[index]
                when (delta.string("type")) {
                    "text_delta" -> (block as? WireBlock.Text)?.text?.append(delta.string("text") ?: "")
                    "thinking_delta" -> (block as? WireBlock.Thinking)?.text?.append(delta.string("thinking") ?: "")
                    "signature_delta" -> (block as? WireBlock.Thinking)?.signature?.append(delta.string("signature") ?: "")
                    "citations_delta" -> {
                        val textBlock = block as? WireBlock.Text
                        val citation = delta.obj("citation")
                        if (textBlock != null && citation != null) applyCitation(textBlock, citation)
                    }
                    "input_json_delta" -> Unit
                }
            }
            "content_block_stop" -> {
                val index = indexOf(data) ?: return
                val block = indexMap.remove(index)
                if (block is WireBlock.Thinking) {
                    block.endedAt = System.currentTimeMillis()
                    thinkingActive = false
                }
            }
            "message_delta" -> {
                val delta = data.obj("delta")
                stopReason = delta?.string("stop_reason") ?: stopReason
                title = delta?.string("title") ?: title
                applyUsage(data.obj("usage"))
            }
            "message_stop" -> endedNormally = true
            "ping" -> Unit
            else -> Unit
        }
    }

    private fun applyCitation(block: WireBlock.Text, citation: JsonObject) {
        val url = citation.string("url") ?: return
        val title = citation.string("title")
        val citedText = citation.string("cited_text")
        val number = ++citationCount
        val current = block.text.toString()
        val insertAt = if (citedText.isNullOrEmpty()) {
            current.length
        } else {
            val index = current.lastIndexOf(citedText)
            if (index >= 0) index + citedText.length else current.length
        }
        block.text.insert(insertAt, "[citation:$number]")
        block.citations.add(WireBlock.WireCitation(url, title, citedText, number))
    }

    private fun indexOf(data: JsonObject): Int? =
        data.get("index")?.let { runCatching { it.asInt }.getOrNull() }

    private fun applyUsage(usage: JsonObject?) {
        if (usage == null) return
        usage.get("input_tokens")?.let { inputTokens = runCatching { it.asInt }.getOrDefault(inputTokens) }
        usage.get("cache_creation_input_tokens")?.let {
            cacheCreationTokens = runCatching { it.asInt }.getOrDefault(cacheCreationTokens)
        }
        usage.get("cache_read_input_tokens")?.let {
            cacheReadTokens = runCatching { it.asInt }.getOrDefault(cacheReadTokens)
        }
        usage.get("output_tokens")?.let { outputTokens = runCatching { it.asInt }.getOrDefault(outputTokens) }
    }
}
