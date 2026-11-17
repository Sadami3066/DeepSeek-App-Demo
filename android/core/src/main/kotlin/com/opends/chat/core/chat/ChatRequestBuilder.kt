package com.opends.chat.core.chat

import com.google.gson.JsonArray
import com.google.gson.JsonObject

internal object ChatRequestBuilder {
    const val MODEL = "deepseek-flash"
    const val SYSTEM_PROMPT = "该助手为DeepSeek，由深度求索公司创造。"

    fun build(
        messages: JsonArray,
        thinkingEnabled: Boolean,
        searchEnabled: Boolean,
        titleEnabled: Boolean,
    ): JsonObject {
        val tools = JsonArray()
        tools.add(JsonObject().apply {
            addProperty("type", "deepseek_read_url")
            addProperty("name", "deepseek_read_url")
        })
        if (searchEnabled) {
            tools.add(JsonObject().apply {
                addProperty("type", "deepseek_web_search")
                addProperty("name", "deepseek_web_search")
            })
        }
        return JsonObject().apply {
            addProperty("model", MODEL)
            addProperty("system", SYSTEM_PROMPT)
            add("messages", messages)
            add("thinking", JsonObject().apply {
                addProperty("type", if (thinkingEnabled) "enabled" else "disabled")
            })
            add("tools", tools)
            if (titleEnabled) {
                add("title", JsonObject().apply { addProperty("enabled", true) })
            }
            addProperty("stream", true)
        }
    }
}
