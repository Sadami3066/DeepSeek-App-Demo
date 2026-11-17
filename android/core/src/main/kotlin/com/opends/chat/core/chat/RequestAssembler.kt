package com.opends.chat.core.chat

import com.opends.chat.core.AppSettings
import com.opends.chat.core.MessageStatus
import com.opends.chat.core.db.StoredMessage
import com.opends.chat.core.util.TimeFormats
import com.opends.chat.core.util.string
import java.util.Calendar

internal object LatestReminder {
    private val weekdaysZh = listOf("星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六")
    private val weekdaysEn = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")

    fun build(settings: AppSettings, now: Long = System.currentTimeMillis()): String {
        val chinese = settings.languageTag.startsWith("zh", ignoreCase = true)
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = now
        val weekday = calendar.get(Calendar.DAY_OF_WEEK).coerceIn(1, 7) - 1
        val location = settings.location.orEmpty().replace("\n", " ").trim()
        val language = if (chinese) "中文" else "English"
        val day = if (chinese) weekdaysZh[weekday] else weekdaysEn[weekday]
        return listOf(TimeFormats.formatDate(now), day, location, "App", language).joinToString(",")
    }
}

internal class RequestAssembler(private val uploads: Map<String, String>) {

    fun buildMessages(
        history: List<StoredMessage>,
        currentUserItems: List<com.google.gson.JsonObject>,
        reminder: String?,
        prefixBlocks: List<WireBlock>?,
        includeThinking: Boolean,
    ): com.google.gson.JsonArray {
        val messages = com.google.gson.JsonArray()
        val rounds = usableRounds(history)
        rounds.forEach { round ->
            messages.add(
                WireCodec.userContextContent(
                    com.opends.chat.core.db.FragmentCodec.parse(round.user.fragmentsJson),
                    uploads,
                )
            )
            messages.add(assistantMessage(round.assistant, includeThinking = false))
        }
        if (reminder != null) {
            messages.add(com.google.gson.JsonObject().apply {
                addProperty("role", "latest_reminder")
                addProperty("content", reminder)
            })
        }
        messages.add(WireCodec.userContextContent(currentUserItems, uploads))
        if (prefixBlocks != null) {
            val content = WireCodec.assistantContextContent(prefixBlocks, includeThinking)
            if (content != null && content.size() > 0) {
                messages.add(com.google.gson.JsonObject().apply {
                    addProperty("role", "assistant")
                    add("content", content)
                })
            }
        }
        return messages
    }

    private fun usableRounds(history: List<StoredMessage>): List<Round> {
        val rounds = ArrayList<Round>()
        var index = 0
        while (index < history.size) {
            val message = history[index]
            if (message.role != com.opends.chat.core.Role.USER) {
                index += 1
                continue
            }
            val reply = history.getOrNull(index + 1)
            if (reply != null && reply.role == com.opends.chat.core.Role.ASSISTANT && isUsable(reply)) {
                rounds.add(Round(message, reply))
                index += 2
            } else {
                index += 1
            }
        }
        return rounds
    }

    private fun isUsable(message: StoredMessage): Boolean {
        if (message.status != MessageStatus.FINISHED && message.status != MessageStatus.INCOMPLETE) return false
        val items = com.opends.chat.core.db.FragmentCodec.parse(message.fragmentsJson)
        if (items.any { it.string("type") == com.opends.chat.core.db.FragmentCodec.TYPE_TEMPLATE_RESPONSE }) return false
        val blocks = WireCodec.blocks(items)
        return WireCodec.plainText(blocks).isNotBlank()
    }

    private fun assistantMessage(message: StoredMessage, includeThinking: Boolean): com.google.gson.JsonObject {
        val items = com.opends.chat.core.db.FragmentCodec.parse(message.fragmentsJson)
        val content = WireCodec.assistantContextContent(WireCodec.blocks(items), includeThinking)
        return com.google.gson.JsonObject().apply {
            addProperty("role", "assistant")
            if (content != null && content.size() > 0) {
                add("content", content)
            } else {
                addProperty("content", "")
            }
        }
    }

    private data class Round(val user: StoredMessage, val assistant: StoredMessage)
}
