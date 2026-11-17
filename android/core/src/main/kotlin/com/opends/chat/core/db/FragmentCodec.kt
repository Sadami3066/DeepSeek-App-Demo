package com.opends.chat.core.db

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.opends.chat.core.Fragment
import com.opends.chat.core.SearchResult
import com.opends.chat.core.util.TimeFormats
import com.opends.chat.core.util.array
import com.opends.chat.core.util.bool
import com.opends.chat.core.util.number
import com.opends.chat.core.util.string
import java.util.Locale

internal object FragmentCodec {
    const val TYPE_REQUEST = "REQUEST"
    const val TYPE_RESPONSE = "RESPONSE"
    const val TYPE_THINK = "THINK"
    const val TYPE_TEMPLATE_RESPONSE = "TEMPLATE_RESPONSE"
    const val TYPE_TIP = "TIP"
    const val TYPE_CONTENT_RISK = "CONTENT_RISK"
    const val TYPE_PARTIAL_FILE_READ = "PARTIAL_FILE_READ"
    const val TYPE_TOOL_SEARCH = "TOOL_SEARCH"
    const val TYPE_SEARCH = "SEARCH"
    const val TYPE_TOOL_OPEN = "TOOL_OPEN"
    const val TYPE_TOOL_FIND = "TOOL_FIND"
    const val TYPE_READ_LINK = "READ_LINK"
    const val TYPE_FILE = "FILE"
    const val TYPE_TOOL_IMAGE_SEARCH = "TOOL_IMAGE_SEARCH"

    fun parse(raw: String?): List<JsonObject> {
        if (raw.isNullOrBlank()) return emptyList()
        val parsed = runCatching { JsonParser.parseString(raw) }.getOrNull() ?: return emptyList()
        if (!parsed.isJsonArray) return emptyList()
        return parsed.asJsonArray.mapNotNull { element ->
            element.takeIf { it.isJsonObject }?.asJsonObject?.takeIf { it.string("type") != null }
        }
    }

    fun encode(items: List<JsonObject>): String {
        val array = JsonArray()
        items.forEach { array.add(it) }
        return array.toString()
    }

    fun toFragments(items: List<JsonObject>): List<Fragment> {
        val output = ArrayList<Fragment>(items.size)
        items.forEach { item ->
            when (item.string("type")?.uppercase(Locale.US)) {
                TYPE_REQUEST -> output.add(Fragment.Request(item.string("content") ?: ""))
                TYPE_RESPONSE -> output.add(
                    Fragment.Response(
                        content = item.string("content") ?: "",
                        citations = citationsOf(item),
                    )
                )
                TYPE_THINK -> output.add(
                    Fragment.Think(
                        content = item.string("content") ?: "",
                        elapsedSecs = item.number("elapsed_secs"),
                    )
                )
                TYPE_TEMPLATE_RESPONSE -> output.add(Fragment.TemplateResponse(item.string("content") ?: ""))
                TYPE_TIP -> output.add(
                    Fragment.Tip(
                        content = item.string("content") ?: "",
                        style = item.string("style")?.uppercase(Locale.US) ?: "INFO",
                    )
                )
                TYPE_CONTENT_RISK -> output.add(
                    Fragment.ContentRisk(
                        content = item.string("content") ?: "",
                        style = item.string("style")?.uppercase(Locale.US) ?: "INFO",
                    )
                )
                TYPE_PARTIAL_FILE_READ -> output.add(
                    Fragment.PartialFileRead(ratio = item.number("ratio")?.toInt() ?: 0)
                )
                TYPE_TOOL_SEARCH, TYPE_SEARCH -> output.add(
                    Fragment.Search(
                        queries = queriesOf(item),
                        results = resultsOf(item),
                        status = item.string("status"),
                        stageId = item.number("stage_id")?.toInt(),
                    )
                )
                TYPE_TOOL_OPEN -> output.add(
                    Fragment.ToolOpen(
                        status = item.string("status") ?: "FINISHED",
                        stageId = item.number("stage_id")?.toInt(),
                        result = toolOpenResultOf(item),
                    )
                )
                TYPE_TOOL_FIND -> output.add(
                    Fragment.ToolFind(
                        status = item.string("status") ?: "FINISHED",
                        stageId = item.number("stage_id")?.toInt(),
                        pattern = item.string("pattern") ?: queriesOf(item).firstOrNull(),
                        result = item.string("result"),
                    )
                )
                TYPE_READ_LINK -> output.addAll(readLinksOf(item))
                TYPE_FILE -> item.array("files")?.forEach { entry ->
                    if (entry.isJsonObject) {
                        fileOf(entry.asJsonObject)?.let { output.add(it) }
                    }
                }
                else -> Unit
            }
        }
        return output
    }

    fun toolOpenResultOf(item: JsonObject): com.opends.chat.core.ToolOpenResult? {
        val nested = item.get("result")?.takeIf { it.isJsonObject }?.asJsonObject
        val source = nested ?: item
        val url = source.string("url")
        val title = source.string("title")
        val snippet = source.string("snippet")
        val siteName = source.string("site_name")
        val siteIcon = source.string("site_icon")
        val publishedAt = timeString(source.get("published_at"))
        if (url == null && title == null && snippet == null && siteName == null &&
            siteIcon == null && publishedAt == null
        ) {
            return null
        }
        return com.opends.chat.core.ToolOpenResult(
            url = url,
            title = title,
            snippet = snippet,
            siteName = siteName,
            publishedAt = publishedAt,
            siteIcon = siteIcon,
        )
    }

    fun citationsOf(item: JsonObject): List<com.opends.chat.core.Citation> {
        val array = item.array("citations") ?: return emptyList()
        return array.mapNotNull { element ->
            if (!element.isJsonObject) return@mapNotNull null
            val citation = element.asJsonObject
            val url = citation.string("url") ?: return@mapNotNull null
            com.opends.chat.core.Citation(
                url = url,
                title = citation.string("title"),
                citedText = citation.string("citedText") ?: citation.string("cited_text"),
            )
        }
    }

    fun queriesOf(item: JsonObject): List<String> {
        val array = item.array("queries") ?: return emptyList()
        return array.mapNotNull { element ->
            when {
                element.isJsonPrimitive -> element.asString.takeIf { it.isNotBlank() }
                element.isJsonObject -> element.asJsonObject.string("query")
                else -> null
            }
        }
    }

    fun resultsOf(item: JsonObject): List<SearchResult> {
        val array = item.array("results") ?: return emptyList()
        return array.mapNotNull { element ->
            if (!element.isJsonObject) return@mapNotNull null
            val obj = element.asJsonObject
            SearchResult(
                url = obj.string("url") ?: "",
                title = obj.string("title") ?: "",
                snippet = obj.string("snippet") ?: "",
                publishedAt = timeString(obj.get("published_at")),
                siteName = obj.string("site_name"),
                siteIcon = obj.string("site_icon"),
            )
        }
    }

    fun readLinksOf(item: JsonObject): List<Fragment.ReadLink> {
        val array = item.array("results")
        if (array == null || array.size() == 0) {
            val url = item.string("url")
            return if (url.isNullOrEmpty()) {
                emptyList()
            } else {
                listOf(Fragment.ReadLink(url = url, title = item.string("title"), status = item.string("status")))
            }
        }
        return array.mapNotNull { element ->
            if (!element.isJsonObject) return@mapNotNull null
            val obj = element.asJsonObject
            val url = obj.string("url") ?: ""
            if (url.isEmpty() && obj.string("signature").isNullOrEmpty()) return@mapNotNull null
            Fragment.ReadLink(
                url = url,
                title = obj.string("title"),
                status = obj.string("status") ?: item.string("status"),
            )
        }
    }

    fun fileOf(obj: JsonObject): Fragment.File? {
        val id = obj.string("id")?.removePrefix("file-") ?: return null
        if (id.isEmpty()) return null
        return Fragment.File(
            fileId = id,
            name = obj.string("file_name") ?: obj.string("name"),
            mime = obj.string("mime") ?: obj.string("mime_type"),
            size = obj.number("file_size")?.toLong() ?: obj.number("size")?.toLong() ?: 0L,
            isImage = obj.bool("is_image") ?: false,
            localPath = obj.string("local_path"),
        )
    }

    fun timeString(element: JsonElement?): String? {
        if (element == null || element.isJsonNull || !element.isJsonPrimitive) return null
        val primitive = element.asJsonPrimitive
        if (primitive.isNumber) {
            val millis = TimeFormats.millisFromEpochSeconds(runCatching { primitive.asDouble }.getOrNull())
            return millis?.let { TimeFormats.formatMillis(it) }
        }
        val text = runCatching { primitive.asString }.getOrNull() ?: return null
        if (text.isBlank()) return null
        val millis = TimeFormats.parseMillis(text)
        return millis?.let { TimeFormats.formatMillis(it) } ?: text
    }

}
