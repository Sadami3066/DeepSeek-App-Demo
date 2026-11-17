package com.opends.chat.core.util

import com.google.gson.JsonArray
import com.google.gson.JsonObject

internal fun JsonObject.string(key: String): String? {
    val element = get(key) ?: return null
    if (element.isJsonNull || !element.isJsonPrimitive) return null
    return runCatching { element.asString }.getOrNull()
}

internal fun JsonObject.number(key: String): Double? {
    val element = get(key) ?: return null
    if (element.isJsonNull || !element.isJsonPrimitive) return null
    return runCatching { element.asDouble }.getOrNull()
}

internal fun JsonObject.bool(key: String): Boolean? {
    val element = get(key) ?: return null
    if (element.isJsonNull || !element.isJsonPrimitive) return null
    return runCatching { element.asBoolean }.getOrNull()
}

internal fun JsonObject.array(key: String): JsonArray? =
    get(key)?.takeIf { it.isJsonArray }?.asJsonArray

internal fun JsonObject.obj(key: String): JsonObject? =
    get(key)?.takeIf { it.isJsonObject }?.asJsonObject

internal fun JsonObject.stringOrEmpty(key: String): String = string(key) ?: ""
