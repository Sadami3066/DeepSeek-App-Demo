package com.opends.chat.core.chat

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.opends.chat.core.Citation
import com.opends.chat.core.Fragment
import com.opends.chat.core.Role
import com.opends.chat.core.SearchResult
import com.opends.chat.core.db.FragmentCodec
import com.opends.chat.core.util.array
import com.opends.chat.core.util.TextMarkers
import com.opends.chat.core.util.number
import com.opends.chat.core.util.obj
import com.opends.chat.core.util.string
import com.opends.chat.core.util.TimeFormats

internal sealed class WireBlock {
    class Text : WireBlock() {
        val text = StringBuilder()
        val citations = ArrayList<WireCitation>()
    }

    data class WireCitation(
        val url: String,
        val title: String?,
        val citedText: String?,
        val number: Int,
    )

    class Thinking : WireBlock() {
        val text = StringBuilder()
        val signature = StringBuilder()
        var startedAt: Long = 0L
        var endedAt: Long? = null
        var elapsedOverride: Double? = null
        var stageId: Int? = null
    }

    class Template(val content: String) : WireBlock()

    class Notice(val content: String, val style: String) : WireBlock()

    class OpenPage : WireBlock() {
        var status: String = "WIP"
        var stageId: Int? = null
        var toolUseId: String = ""
        var url: String? = null
        var result: JsonObject? = null
    }

    class FindPage : WireBlock() {
        var status: String = "WIP"
        var stageId: Int? = null
        var toolUseId: String = ""
        var pattern: String? = null
        var result: String? = null
    }

    class Atomic(val json: JsonObject, var stageId: Int? = null) : WireBlock()
}

internal object WireCodec {
    const val TYPE_REQUEST = "REQUEST"
    const val TYPE_RESPONSE = "RESPONSE"
    const val TYPE_THINK = "THINK"
    const val TYPE_TEMPLATE_RESPONSE = "TEMPLATE_RESPONSE"
    const val TYPE_TIP = "TIP"
    const val TYPE_CONTENT_RISK = "CONTENT_RISK"
    const val TYPE_PARTIAL_FILE_READ = "PARTIAL_FILE_READ"
    const val TYPE_SERVER_TOOL_USE = "SERVER_TOOL_USE"
    const val TYPE_WEB_SEARCH_RESULT = "WEB_SEARCH_TOOL_RESULT"
    const val TYPE_WEB_FETCH_RESULT = "WEB_FETCH_TOOL_RESULT"
    const val TYPE_FILE = "FILE"
    const val TYPE_META = "META"

    fun canonical(blocks: List<WireBlock>): MutableList<JsonObject> = blocks.mapNotNullTo(ArrayList()) { block ->
        when (block) {
            is WireBlock.Text -> JsonObject().apply {
                addProperty("type", TYPE_RESPONSE)
                addProperty("content", block.text.toString())
                if (block.citations.isNotEmpty()) {
                    add("citations", JsonArray().apply {
                        block.citations.forEach { citation ->
                            add(JsonObject().apply {
                                addProperty("url", citation.url)
                                if (citation.title != null) addProperty("title", citation.title)
                                if (citation.citedText != null) addProperty("citedText", citation.citedText)
                            })
                        }
                    })
                }
            }
            is WireBlock.Thinking -> JsonObject().apply {
                addProperty("type", TYPE_THINK)
                addProperty("content", block.text.toString())
                val elapsed = elapsedSeconds(block)
                if (elapsed == null) add("elapsed_secs", com.google.gson.JsonNull.INSTANCE) else
                    addProperty("elapsed_secs", elapsed)
                if (block.signature.isNotEmpty()) addProperty("signature", block.signature.toString())
                block.stageId?.let { addProperty("stage_id", it) }
            }
            is WireBlock.Template -> JsonObject().apply {
                addProperty("type", TYPE_TEMPLATE_RESPONSE)
                addProperty("content", block.content)
            }
            is WireBlock.Notice -> JsonObject().apply {
                addProperty("type", TYPE_TIP)
                addProperty("content", block.content)
                addProperty("style", block.style)
            }
            is WireBlock.OpenPage -> JsonObject().apply {
                addProperty("type", FragmentCodec.TYPE_TOOL_OPEN)
                addProperty("status", block.status)
                block.stageId?.let { addProperty("stage_id", it) }
                if (block.result == null) add("result", com.google.gson.JsonNull.INSTANCE) else add("result", block.result)
            }
            is WireBlock.FindPage -> JsonObject().apply {
                addProperty("type", FragmentCodec.TYPE_TOOL_FIND)
                addProperty("status", block.status)
                block.stageId?.let { addProperty("stage_id", it) }
                block.pattern?.let { addProperty("pattern", it) }
                block.result?.let { addProperty("result", it) }
            }
            is WireBlock.Atomic -> {
                val type = block.json.string("type")
                val canonicalType = when (type) {
                    "server_tool_use" -> TYPE_SERVER_TOOL_USE
                    "web_search_tool_result" -> TYPE_WEB_SEARCH_RESULT
                    "web_fetch_tool_result" -> TYPE_WEB_FETCH_RESULT
                    else -> null
                }
                canonicalType?.let {
                    JsonObject().apply {
                        addProperty("type", it)
                        block.stageId?.let { stageId -> addProperty("stage_id", stageId) }
                        add("block", block.json)
                    }
                }
            }
        }
    }

    fun appendMeta(items: MutableList<JsonObject>, stopReason: String?, title: String?) {
        if (stopReason == null && title == null) return
        val meta = JsonObject().apply {
            addProperty("type", TYPE_META)
            if (stopReason != null) addProperty("stop_reason", stopReason)
            if (title != null) addProperty("title", title)
        }
        items.add(meta)
    }

    fun meta(items: List<JsonObject>): JsonObject? =
        items.lastOrNull { it.string("type") == TYPE_META }

    fun blocks(items: List<JsonObject>): List<WireBlock> =
        migrateLegacyToolBlocks(items.mapNotNull { item -> parseBlock(item) })

    private fun parseBlock(item: JsonObject): WireBlock? =
        when (item.string("type")) {
            TYPE_RESPONSE -> WireBlock.Text().also { block ->
                block.text.append(item.string("content") ?: "")
                item.array("citations")?.forEachIndexed { index, element ->
                    if (!element.isJsonObject) return@forEachIndexed
                    val citation = element.asJsonObject
                    val url = citation.string("url") ?: return@forEachIndexed
                    block.citations.add(
                        WireBlock.WireCitation(
                            url = url,
                            title = citation.string("title"),
                            citedText = citation.string("citedText"),
                            number = index + 1,
                        )
                    )
                }
            }
            TYPE_THINK -> WireBlock.Thinking().also {
                it.text.append(item.string("content") ?: "")
                it.signature.append(item.string("signature") ?: "")
                it.endedAt = null
                it.elapsedOverride = item.number("elapsed_secs")
                it.stageId = item.number("stage_id")?.toInt()
            }
            TYPE_TEMPLATE_RESPONSE -> WireBlock.Template(item.string("content") ?: "")
            TYPE_TIP -> item.string("style").takeIf { it == "ERROR" }
                ?.let { WireBlock.Notice(item.string("content") ?: "", it) }
            TYPE_SERVER_TOOL_USE, TYPE_WEB_SEARCH_RESULT, TYPE_WEB_FETCH_RESULT -> {
                val json = item.get("block")?.takeIf { it.isJsonObject }?.asJsonObject
                json?.let { WireBlock.Atomic(it, item.number("stage_id")?.toInt()) }
            }
            FragmentCodec.TYPE_TOOL_OPEN -> WireBlock.OpenPage().also { page ->
                page.status = item.string("status") ?: "FINISHED"
                page.stageId = item.number("stage_id")?.toInt()
                page.result = item.get("result")?.takeIf { it.isJsonObject }?.asJsonObject
                page.url = item.string("url") ?: page.result?.string("url")
            }
            FragmentCodec.TYPE_TOOL_FIND -> WireBlock.FindPage().also { find ->
                find.status = item.string("status") ?: "FINISHED"
                find.stageId = item.number("stage_id")?.toInt()
                find.pattern = item.string("pattern") ?: item.string("query")
                find.result = item.string("result")
            }
            else -> null
        }

    private fun migrateLegacyToolBlocks(input: List<WireBlock>): List<WireBlock> {
        val hasLegacy = input.any { block ->
            block is WireBlock.Atomic && block.json.string("type") == "server_tool_use" &&
                block.json.obj("input")?.string("action") in setOf("open_page", "find_in_page")
        }
        if (!hasLegacy) return input
        val output = ArrayList<WireBlock>(input.size)
        val openPages = HashMap<String, WireBlock.OpenPage>()
        val findPages = HashMap<String, WireBlock.FindPage>()
        input.forEach { block ->
            when {
                block is WireBlock.Atomic && block.json.string("type") == "server_tool_use" -> {
                    val inputJson = block.json.obj("input")
                    when (inputJson?.string("action")) {
                        "open_page" -> {
                            val page = WireBlock.OpenPage()
                            page.status = "WIP"
                            page.stageId = block.stageId
                            page.toolUseId = block.json.string("id") ?: ""
                            page.url = inputJson.string("url")
                            openPages[page.toolUseId] = page
                            output.add(page)
                        }
                        "find_in_page" -> {
                            val find = WireBlock.FindPage()
                            find.status = "WIP"
                            find.stageId = block.stageId
                            find.toolUseId = block.json.string("id") ?: ""
                            find.pattern = inputJson.string("pattern")
                            findPages[find.toolUseId] = find
                            output.add(find)
                        }
                        else -> output.add(block)
                    }
                }
                block is WireBlock.Atomic && block.json.string("type") == "web_fetch_tool_result" -> {
                    val id = block.json.string("tool_use_id") ?: ""
                    val page = openPages[id]
                    val find = findPages[id]
                    if (page != null) {
                        applyFetchResult(page, block.json)
                    } else if (find != null) {
                        applyFetchResult(find, block.json)
                    } else {
                        output.add(block)
                    }
                }
                else -> output.add(block)
            }
        }
        return output
    }

    fun applyFetchResult(page: WireBlock.OpenPage, fetch: JsonObject) {
        val content = fetch.obj("content")
        page.status = if (content?.string("type") == "web_fetch_tool_result_error") "FAILED" else "FINISHED"
        if (content?.string("type") == "web_fetch_result") {
            val result = JsonObject()
            content.string("url")?.let { result.addProperty("url", it) }
            val document = content.obj("content")
            document?.string("title")?.let { result.addProperty("title", it) }
            (content.string("published_at") ?: document?.string("published_at"))
                ?.let { result.addProperty("published_at", it) }
            page.result = result
        }
    }

    fun applyFetchResult(find: WireBlock.FindPage, fetch: JsonObject) {
        val content = fetch.obj("content")
        find.status = if (content?.string("type") == "web_fetch_tool_result_error") "FAILED" else "FINISHED"
        content?.obj("content")?.string("title")?.let { find.result = it }
    }

    fun modelFragments(items: List<JsonObject>, role: Role): List<Fragment> =
        if (role == Role.ASSISTANT) toModelFragments(blocks(items)) else FragmentCodec.toFragments(items)

    fun toModelFragments(blocks: List<WireBlock>): List<Fragment> {
        val output = ArrayList<Fragment>()
        val searchIndexes = HashMap<String, Int>()
        val readLinkIndexes = HashMap<String, Int>()
        var lastSearchIndex = -1
        blocks.forEach { block ->
            when (block) {
                is WireBlock.Text -> output.add(
                    Fragment.Response(
                        content = block.text.toString(),
                        citations = block.citations.map { citation ->
                            Citation(url = citation.url, title = citation.title, citedText = citation.citedText)
                        },
                    )
                )
                is WireBlock.Thinking -> output.add(
                    Fragment.Think(
                        content = block.text.toString(),
                        elapsedSecs = elapsedSeconds(block),
                    )
                )
                is WireBlock.Template -> output.add(Fragment.TemplateResponse(block.content))
                is WireBlock.Notice -> output.add(Fragment.Tip(block.content, block.style))
                is WireBlock.OpenPage -> output.add(
                    Fragment.ToolOpen(
                        status = block.status,
                        stageId = block.stageId,
                        result = block.result?.let { raw ->
                            com.opends.chat.core.ToolOpenResult(
                                url = raw.string("url"),
                                title = raw.string("title"),
                                snippet = raw.string("snippet"),
                                siteName = raw.string("site_name"),
                                publishedAt = FragmentCodec.timeString(raw.get("published_at")),
                                siteIcon = raw.string("site_icon"),
                            )
                        },
                    )
                )
                is WireBlock.FindPage -> output.add(
                    Fragment.ToolFind(
                        status = block.status,
                        stageId = block.stageId,
                        pattern = block.pattern,
                        result = block.result,
                    )
                )
                is WireBlock.Atomic -> when (block.json.string("type")) {
                    "server_tool_use" -> {
                        val id = block.json.string("id") ?: ""
                        val input = block.json.get("input")?.takeIf { it.isJsonObject }?.asJsonObject
                        when (input?.string("action")) {
                            "read_link" -> {
                                readLinkIndexes[id] = output.size
                                output.add(
                                    Fragment.ReadLink(
                                        url = input.string("url") ?: "",
                                        title = null,
                                        status = "WIP",
                                    )
                                )
                            }
                            "open_page", "find_in_page" -> Unit
                            else -> {
                                val queries = input?.array("queries")?.mapNotNull { element ->
                                    when {
                                        element.isJsonPrimitive -> runCatching { element.asString }.getOrNull()
                                        element.isJsonObject -> element.asJsonObject.string("query")
                                        else -> null
                                    }
                                } ?: emptyList()
                                lastSearchIndex = output.size
                                searchIndexes[id] = lastSearchIndex
                                output.add(
                                    Fragment.Search(
                                        queries = queries,
                                        results = emptyList(),
                                        status = "WIP",
                                        stageId = block.stageId,
                                    )
                                )
                            }
                        }
                    }
                    "web_search_tool_result" -> {
                        val id = block.json.string("tool_use_id") ?: ""
                        val index = searchIndexes[id] ?: lastSearchIndex
                        val results = searchResultsOf(block.json)
                        val hasError = block.json.array("content")?.any { element ->
                            element.isJsonObject && element.asJsonObject.string("type") == "web_search_tool_result_error"
                        } ?: false
                        val status = if (hasError) "FAILED" else "FINISHED"
                        if (index >= 0 && index < output.size && output[index] is Fragment.Search) {
                            val existing = output[index] as Fragment.Search
                            output[index] = existing.copy(results = results, status = status)
                        } else {
                            output.add(Fragment.Search(results = results, status = status, stageId = block.stageId))
                        }
                    }
                    "web_fetch_tool_result" -> {
                        val id = block.json.string("tool_use_id") ?: ""
                        val index = readLinkIndexes[id]
                        val content = block.json.get("content")?.takeIf { it.isJsonObject }?.asJsonObject
                        val failed = content?.string("type") == "web_fetch_tool_result_error"
                        if (index != null && index < output.size && output[index] is Fragment.ReadLink) {
                            val existing = output[index] as Fragment.ReadLink
                            val title = content?.get("content")?.takeIf { it.isJsonObject }
                                ?.asJsonObject?.string("title")
                            output[index] = existing.copy(title = title ?: existing.title, status = if (failed) "FAILED" else "FINISHED")
                        }
                    }
                }
            }
        }
        return output
    }

    fun plainText(blocks: List<WireBlock>): String = blocks
        .filterIsInstance<WireBlock.Text>()
        .joinToString("") { TextMarkers.stripCitations(it.text.toString()) }

    fun elapsedSeconds(block: WireBlock.Thinking): Double? {
        block.elapsedOverride?.let { return it }
        val ended = block.endedAt ?: return null
        if (block.startedAt <= 0L) return null
        return (ended - block.startedAt) / 1000.0
    }

    fun searchResultsOf(json: JsonObject): List<SearchResult> {
        val array = json.array("content") ?: return emptyList()
        return array.mapNotNull { element ->
            if (!element.isJsonObject) return@mapNotNull null
            val item = element.asJsonObject
            when (item.string("type")) {
                "web_search_result" -> SearchResult(
                    url = item.string("url") ?: "",
                    title = item.string("title") ?: "",
                    snippet = item.string("snippet") ?: "",
                    publishedAt = item.string("page_age"),
                    siteName = item.string("site_name"),
                    siteIcon = item.string("site_icon"),
                )
                else -> null
            }
        }
    }

    fun userFragments(text: String, files: List<Fragment.File>): List<JsonObject> {
        val items = ArrayList<JsonObject>(2)
        if (files.isNotEmpty()) {
            val array = JsonArray()
            files.forEach { file ->
                array.add(JsonObject().apply {
                    addProperty("id", file.fileId)
                    addProperty("file_name", file.name ?: "")
                    addProperty("file_size", file.size)
                    addProperty("is_image", file.isImage)
                    file.mime?.let { addProperty("mime", it) }
                })
            }
            items.add(JsonObject().apply {
                addProperty("type", TYPE_FILE)
                add("files", array)
            })
        }
        items.add(JsonObject().apply {
            addProperty("type", TYPE_REQUEST)
            addProperty("content", text)
        })
        return items
    }

    fun requestText(items: List<JsonObject>): String =
        items.filter { it.string("type") == TYPE_REQUEST }.joinToString("") { it.string("content") ?: "" }

    fun filesOf(items: List<JsonObject>): List<Fragment.File> =
        items.filter { it.string("type") == TYPE_FILE }
            .flatMap { item -> item.array("files")?.mapNotNull { entry ->
                if (entry.isJsonObject) FragmentCodec.fileOf(entry.asJsonObject) else null
            } ?: emptyList() }

    fun officialFragments(blocks: List<WireBlock>, messageId: Long): List<JsonObject> {
        val output = ArrayList<JsonObject>()
        var textBuffer: StringBuilder? = null

        fun flushText() {
            val buffer = textBuffer ?: return
            textBuffer = null
            val content = TextMarkers.stripAll(buffer.toString())
            if (content.isBlank()) return
            output.add(JsonObject().apply {
                addProperty("type", FragmentCodec.TYPE_RESPONSE)
                addProperty("content", content)
            })
        }

        blocks.forEachIndexed { index, block ->
            when (block) {
                is WireBlock.Text -> {
                    if (textBuffer == null) textBuffer = StringBuilder()
                    textBuffer?.append(block.text)
                }
                is WireBlock.Template -> {
                    flushText()
                    output.add(JsonObject().apply {
                        addProperty("type", FragmentCodec.TYPE_TEMPLATE_RESPONSE)
                        addProperty("content", block.content)
                    })
                }
                is WireBlock.Notice -> Unit
                is WireBlock.OpenPage -> {
                    flushText()
                    output.add(JsonObject().apply {
                        addProperty("type", FragmentCodec.TYPE_TOOL_OPEN)
                        addProperty("status", block.status)
                        val result = block.result
                        addPropertyOrNull("url", result?.string("url"))
                        addPropertyOrNull("title", result?.string("title"))
                        addPropertyOrNull("snippet", result?.string("snippet"))
                        addPropertyOrNull("site_name", result?.string("site_name"))
                        add("published_at", isoOrNull(result?.string("published_at")))
                        block.stageId?.let { addProperty("stage_id", it) }
                    })
                }
                is WireBlock.FindPage -> {
                    flushText()
                    output.add(JsonObject().apply {
                        addProperty("type", FragmentCodec.TYPE_TOOL_FIND)
                        addProperty("status", block.status)
                        block.pattern?.let { addProperty("pattern", it) }
                        block.stageId?.let { addProperty("stage_id", it) }
                    })
                }
                is WireBlock.Thinking -> {
                    flushText()
                    val content = block.text.toString()
                    if (content.isNotBlank()) {
                        output.add(JsonObject().apply {
                            addProperty("type", FragmentCodec.TYPE_THINK)
                            addProperty("content", content)
                            add("elapsed_secs", elapsedSeconds(block)?.let { JsonPrimitive(it) } ?: com.google.gson.JsonNull.INSTANCE)
                            block.stageId?.let { addProperty("stage_id", it) }
                        })
                    }
                }
                is WireBlock.Atomic -> {
                    if (block.json.string("type") == "server_tool_use") {
                        flushText()
                        val input = block.json.get("input")?.takeIf { it.isJsonObject }?.asJsonObject
                        when (input?.string("action")) {
                            "search" -> output.add(searchFragment(blocks, block.json, input, block.stageId))
                            "read_link" -> {
                                val result = atomicResultFor(
                                    blocks,
                                    "web_fetch_tool_result",
                                    block.json.string("id") ?: "",
                                )
                                readLinkFragment(input, result)?.let { output.add(it) }
                            }
                            "open_page" -> output.add(openFragment(blocks, block.json, input, block.stageId))
                            "find_in_page" -> output.add(findFragment(blocks, block.json, input, block.stageId))
                        }
                    }
                }
            }
        }
        flushText()
        return output
    }

    private fun atomicResultFor(
        blocks: List<WireBlock>,
        type: String,
        toolUseId: String,
    ): JsonObject? = blocks.filterIsInstance<WireBlock.Atomic>()
        .map { it.json }
        .lastOrNull { it.string("type") == type && it.string("tool_use_id") == toolUseId }

    private fun searchFragment(blocks: List<WireBlock>, toolUse: JsonObject, input: JsonObject, stageId: Int?): JsonObject {
        val id = toolUse.string("id") ?: ""
        val queries = input.array("queries") ?: JsonArray()
        val result = atomicResultFor(blocks, "web_search_tool_result", id)
        val content = result?.array("content")
        val failed = content?.any { element ->
            element.isJsonObject && element.asJsonObject.string("type") == "web_search_tool_result_error"
        } ?: false
        val status = when {
            result == null -> "WIP"
            failed -> "FAILED"
            else -> "FINISHED"
        }
        return JsonObject().apply {
            addProperty("type", FragmentCodec.TYPE_TOOL_SEARCH)
            add("queries", queries)
            add("results", if (content == null || failed) JsonArray() else archiveSearchResults(content))
            addProperty("status", status)
            addProperty("stage_id", stageId ?: 1)
        }
    }

    private fun archiveSearchResults(content: JsonArray): JsonArray {
        val output = JsonArray()
        content.forEach { element ->
            if (!element.isJsonObject) return@forEach
            val item = element.asJsonObject
            if (item.string("type") != "web_search_result") return@forEach
            output.add(JsonObject().apply {
                addProperty("url", item.string("url") ?: "")
                addProperty("title", item.string("title") ?: "")
                addProperty("snippet", item.string("snippet") ?: "")
                add("published_at", isoOrNull(item.string("page_age")))
                val siteName = item.string("site_name")
                if (siteName == null) add("site_name", com.google.gson.JsonNull.INSTANCE) else addProperty("site_name", siteName)
                val siteIcon = item.string("site_icon")
                if (siteIcon == null) add("site_icon", com.google.gson.JsonNull.INSTANCE) else addProperty("site_icon", siteIcon)
            })
        }
        return output
    }

    private fun flatToolOpenResult(item: JsonObject): JsonObject? {
        val keys = listOf("url", "title", "snippet", "site_name", "site_icon", "published_at")
        if (keys.none { key -> item.get(key)?.let { !it.isJsonNull } == true }) return null
        return JsonObject().apply {
            keys.forEach { key ->
                item.get(key)?.takeIf { element -> !element.isJsonNull }?.let { element -> add(key, element) }
            }
        }
    }

    private fun JsonObject.addPropertyOrNull(name: String, value: String?) {
        if (value == null) add(name, com.google.gson.JsonNull.INSTANCE) else addProperty(name, value)
    }

    private fun isoOrNull(raw: String?): com.google.gson.JsonElement {
        val millis = TimeFormats.parseMillis(raw) ?: return com.google.gson.JsonNull.INSTANCE
        return JsonPrimitive(TimeFormats.formatMillis(millis))
    }

    private fun fetchContentOf(result: JsonObject?): JsonObject? =
        result?.get("content")?.takeIf { it.isJsonObject }?.asJsonObject

    private fun readLinkFragment(input: JsonObject, result: JsonObject?): JsonObject? {
        val fetchContent = fetchContentOf(result) ?: return null
        if (fetchContent.string("type") != "web_fetch_result") return null
        val signature = result?.string("signature") ?: return null
        val url = fetchContent.string("url") ?: input.string("url") ?: ""
        val title = fetchContent.get("content")?.takeIf { it.isJsonObject }
            ?.asJsonObject?.string("title")
        return JsonObject().apply {
            addProperty("type", FragmentCodec.TYPE_READ_LINK)
            add("results", JsonArray().apply {
                add(JsonObject().apply {
                    if (url.isNotEmpty()) addProperty("url", url)
                    if (title != null) addProperty("title", title)
                    addProperty("signature", signature)
                })
            })
        }
    }

    private fun openFragment(blocks: List<WireBlock>, toolUse: JsonObject, input: JsonObject, stageId: Int?): JsonObject {
        val result = atomicResultFor(blocks, "web_fetch_tool_result", toolUse.string("id") ?: "")
        val content = fetchContentOf(result)
        val failed = content?.string("type") == "web_fetch_tool_result_error"
        val status = when {
            result == null -> "WIP"
            failed -> "FAILED"
            else -> "FINISHED"
        }
        val pageUrl = input.string("url") ?: content?.string("url")
        val title = content?.get("content")?.takeIf { it.isJsonObject }?.asJsonObject?.string("title")
        return JsonObject().apply {
            addProperty("type", FragmentCodec.TYPE_TOOL_OPEN)
            addProperty("status", status)
            if (pageUrl == null) add("url", com.google.gson.JsonNull.INSTANCE) else addProperty("url", pageUrl)
            if (title == null) add("title", com.google.gson.JsonNull.INSTANCE) else addProperty("title", title)
            if (status == "FINISHED") addProperty("snippet", "") else add("snippet", com.google.gson.JsonNull.INSTANCE)
            add("site_name", com.google.gson.JsonNull.INSTANCE)
            add("published_at", isoOrNull(if (status == "FINISHED") content?.string("published_at") else null))
            stageId?.let { addProperty("stage_id", it) }
        }
    }

    private fun findFragment(blocks: List<WireBlock>, toolUse: JsonObject, input: JsonObject, stageId: Int?): JsonObject {
        val result = atomicResultFor(blocks, "web_fetch_tool_result", toolUse.string("id") ?: "")
        val content = fetchContentOf(result)
        val failed = content?.string("type") == "web_fetch_tool_result_error"
        val status = when {
            result == null -> "WIP"
            failed -> "FAILED"
            else -> "FINISHED"
        }
        return JsonObject().apply {
            addProperty("type", FragmentCodec.TYPE_TOOL_FIND)
            addProperty("status", status)
            input.string("pattern")?.let { addProperty("pattern", it) }
            stageId?.let { addProperty("stage_id", it) }
        }
    }

    private fun stripReferenceMarks(text: String): String = TextMarkers.stripReferences(text)

    private fun isoFromElement(element: com.google.gson.JsonElement?): String? {
        if (element == null || element.isJsonNull || !element.isJsonPrimitive) return null
        val primitive = element.asJsonPrimitive
        if (primitive.isNumber) {
            val millis = TimeFormats.millisFromEpochSeconds(runCatching { primitive.asDouble }.getOrNull())
            return millis?.let { TimeFormats.formatMillis(it) }
        }
        return runCatching { primitive.asString }.getOrNull()
    }

    fun fromOfficialFragments(items: List<JsonObject>, messageId: Long): List<WireBlock> {
        val output = ArrayList<WireBlock>()
        val fetchResults = HashMap<String, JsonObject>()
        items.filter { it.string("type") == FragmentCodec.TYPE_READ_LINK }.forEach { item ->
            item.array("results")?.forEachIndexed { resultIndex, element ->
                if (!element.isJsonObject) return@forEachIndexed
                val obj = element.asJsonObject
                val url = obj.string("url") ?: ""
                val signature = obj.string("signature") ?: ""
                if (url.isEmpty() && signature.isEmpty()) return@forEachIndexed
                fetchResults["history-$messageId-$resultIndex"] = JsonObject().apply {
                    addProperty("type", "web_fetch_tool_result")
                    addProperty("tool_use_id", "history-$messageId-$resultIndex")
                    addProperty("kind", "read_link")
                    addProperty("signature", signature)
                    add("content", JsonObject().apply {
                        addProperty("type", "web_fetch_result")
                        addProperty("url", url)
                        add("content", JsonObject().apply {
                            addProperty("type", "document")
                            add("source", JsonObject().apply {
                                addProperty("type", "text")
                                addProperty("media_type", "text/plain")
                                addProperty("data", "")
                            })
                            obj.string("title")?.let { addProperty("title", it) }
                        })
                    })
                }
            }
        }
        items.forEachIndexed { index, item ->
            when (item.string("type")) {
                FragmentCodec.TYPE_REQUEST -> Unit
                FragmentCodec.TYPE_RESPONSE -> output.add(WireBlock.Text().also {
                    it.text.append(stripReferenceMarks(item.string("content") ?: ""))
                })
                FragmentCodec.TYPE_TEMPLATE_RESPONSE -> output.add(WireBlock.Template(item.string("content") ?: ""))
                FragmentCodec.TYPE_THINK -> output.add(WireBlock.Thinking().also {
                    it.text.append(item.string("content") ?: "")
                    it.endedAt = null
                    it.elapsedOverride = item.number("elapsed_secs")
                    it.stageId = item.number("stage_id")?.toInt()
                })
                FragmentCodec.TYPE_TOOL_SEARCH, FragmentCodec.TYPE_SEARCH -> {
                    val queries = FragmentCodec.queriesOf(item)
                    val stageId = item.number("stage_id")?.toInt()
                    val id = "history-$messageId-$index"
                    output.add(WireBlock.Atomic(JsonObject().apply {
                        addProperty("type", "server_tool_use")
                        addProperty("id", id)
                        addProperty("name", "deepseek_web_search")
                        addProperty("kind", "search")
                        add("input", JsonObject().apply {
                            addProperty("action", "search")
                            add("queries", JsonArray().apply { queries.forEach { add(JsonPrimitive(it)) } })
                        })
                        add("caller", JsonObject().apply { addProperty("type", "direct") })
                    }, stageId))
                    val status = item.string("status") ?: "FINISHED"
                    if (status != "WIP") {
                        val content = JsonArray()
                        if (status == "FAILED") {
                            content.add(JsonObject().apply {
                                addProperty("type", "web_search_tool_result_error")
                                addProperty("error_code", "unavailable")
                            })
                        } else {
                            FragmentCodec.resultsOf(item).forEach { result ->
                                content.add(JsonObject().apply {
                                    addProperty("type", "web_search_result")
                                    addProperty("url", result.url)
                                    addProperty("title", result.title)
                                    addProperty("snippet", result.snippet)
                                    result.siteName?.let { addProperty("site_name", it) }
                                    result.siteIcon?.let { addProperty("site_icon", it) }
                                    result.publishedAt?.let { addProperty("page_age", it) }
                                })
                            }
                        }
                        output.add(WireBlock.Atomic(JsonObject().apply {
                            addProperty("type", "web_search_tool_result")
                            addProperty("tool_use_id", id)
                            addProperty("kind", "search")
                            add("content", content)
                        }, stageId))
                    }
                }
                FragmentCodec.TYPE_READ_LINK -> {
                    item.array("results")?.forEachIndexed { resultIndex, element ->
                        if (!element.isJsonObject) return@forEachIndexed
                        val obj = element.asJsonObject
                        val id = "history-$messageId-$resultIndex"
                        output.add(WireBlock.Atomic(JsonObject().apply {
                            addProperty("type", "server_tool_use")
                            addProperty("id", id)
                            addProperty("name", "deepseek_read_url")
                            addProperty("kind", "read_link")
                            add("input", JsonObject().apply {
                                addProperty("action", "read_link")
                                addProperty("url", obj.string("url") ?: "")
                            })
                            add("caller", JsonObject().apply { addProperty("type", "direct") })
                        }))
                        fetchResults[id]?.let { block -> output.add(WireBlock.Atomic(block)) }
                    }
                }
                FragmentCodec.TYPE_TOOL_OPEN -> {
                    val status = item.string("status") ?: "FINISHED"
                    val stageId = item.number("stage_id")?.toInt()
                    val result = item.get("result")?.takeIf { it.isJsonObject }?.asJsonObject
                        ?: flatToolOpenResult(item)
                    output.add(WireBlock.OpenPage().also { page ->
                        page.status = status
                        page.stageId = stageId
                        page.result = result
                        page.url = result?.string("url")
                    })
                }
                FragmentCodec.TYPE_TOOL_FIND -> {
                    output.add(WireBlock.FindPage().also { find ->
                        find.status = item.string("status") ?: "FINISHED"
                        find.stageId = item.number("stage_id")?.toInt()
                        find.pattern = item.string("pattern") ?: FragmentCodec.queriesOf(item).firstOrNull()
                        find.result = item.string("result")
                    })
                }
                else -> Unit
            }
        }
        return output
    }

    fun assistantContextContent(blocks: List<WireBlock>, includeThinking: Boolean): JsonArray? {
        val content = JsonArray()
        val fetchResults = blocks.filterIsInstance<WireBlock.Atomic>()
            .map { it.json }
            .filter { it.string("type") == "web_fetch_tool_result" }
            .associateBy { it.string("tool_use_id") ?: "" }
        var textBuffer: StringBuilder? = null
        var pendingSeparator = false
        var wroteAnyText = false
        val includedFetchResults = HashSet<String>()

        fun flushText() {
            val buffer = textBuffer ?: return
            textBuffer = null
            val text = TextMarkers.stripCitations(buffer.toString())
            if (text.isNotEmpty()) {
                content.add(JsonObject().apply {
                    addProperty("type", "text")
                    addProperty("text", text)
                })
                wroteAnyText = true
            }
        }

        blocks.forEach { block ->
            when (block) {
                is WireBlock.Text -> {
                    if (textBuffer == null) textBuffer = StringBuilder()
                    if (pendingSeparator) {
                        if (textBuffer!!.isNotEmpty() || wroteAnyText) textBuffer!!.append("\n\n")
                        pendingSeparator = false
                    }
                    textBuffer?.append(block.text)
                }
                is WireBlock.Thinking -> {
                    if (includeThinking && block.text.isNotBlank()) {
                        flushText()
                        content.add(JsonObject().apply {
                            addProperty("type", "thinking")
                            addProperty("thinking", block.text.toString())
                            if (block.signature.isNotEmpty()) addProperty("signature", block.signature.toString())
                        })
                        pendingSeparator = false
                    } else {
                        pendingSeparator = true
                    }
                }
                is WireBlock.Template, is WireBlock.Notice -> pendingSeparator = true
                is WireBlock.OpenPage, is WireBlock.FindPage -> pendingSeparator = true
                is WireBlock.Atomic -> {
                    val json = block.json
                    val type = json.string("type")
                    if (type == "server_tool_use") {
                        val input = json.get("input")?.takeIf { it.isJsonObject }?.asJsonObject
                        val kind = json.string("kind")
                        val isReadLink = kind == "read_link" || input?.string("action") == "read_link"
                        val id = json.string("id") ?: ""
                        val result = fetchResults[id]
                        val signature = result?.string("signature") ?: ""
                        if (isReadLink && signature.isNotEmpty() && result != null) {
                            flushText()
                            content.add(deepCopy(json))
                            content.add(deepCopy(result))
                            includedFetchResults.add(result.string("tool_use_id") ?: "")
                            pendingSeparator = false
                        } else {
                            pendingSeparator = true
                        }
                    } else if (type == "web_fetch_tool_result" && includedFetchResults.contains(json.string("tool_use_id") ?: "")) {
                        Unit
                    } else {
                        pendingSeparator = true
                    }
                }
            }
        }
        flushText()
        return if (content.size() == 0) null else content
    }

    fun userContextContent(items: List<JsonObject>, uploads: Map<String, String>): JsonObject {
        val text = requestText(items)
        val fileBlocks = JsonArray()
        filesOf(items).forEach { file ->
            val serverId = uploads[file.fileId] ?: return@forEach
            if (serverId.isEmpty()) return@forEach
            fileBlocks.add(JsonObject().apply {
                addProperty("type", if (file.isImage) "image" else "document")
                add("source", JsonObject().apply {
                    addProperty("type", "file")
                    addProperty("file_id", serverId)
                })
            })
        }
        return if (fileBlocks.size() == 0) {
            JsonObject().apply {
                addProperty("role", "user")
                addProperty("content", text)
            }
        } else {
            val content = JsonArray()
            fileBlocks.forEach { content.add(it) }
            if (text.isNotBlank()) {
                content.add(JsonObject().apply {
                    addProperty("type", "text")
                    addProperty("text", text)
                })
            }
            JsonObject().apply {
                addProperty("role", "user")
                add("content", content)
            }
        }
    }

    fun deepCopy(json: JsonObject): JsonObject =
        com.google.gson.JsonParser.parseString(json.toString()).asJsonObject
}
