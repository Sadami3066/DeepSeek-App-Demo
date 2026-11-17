package com.opends.chat.core

data class SessionInfo(
    val id: String,
    val title: String?,
    val pinned: Boolean = false,
    val currentMessageId: Long? = null,
    val insertedAt: Long = 0L,
    val updatedAt: Long = 0L,
)

enum class Role { USER, ASSISTANT }

enum class MessageStatus { WIP, FINISHED, CONTENT_FILTER, FAILED, INCOMPLETE, CONTEXT_LENGTH_EXCEEDED, TIMEOUT }

data class ChatMessage(
    val id: Long,
    val parentId: Long?,
    val role: Role,
    val status: MessageStatus,
    val thinkingEnabled: Boolean = false,
    val searchEnabled: Boolean = false,
    val accumulatedTokenUsage: Int = 0,
    val insertedAt: Long = 0L,
    val prefix: Boolean = false,
    val fragments: List<Fragment> = emptyList(),
    val conversationMode: String? = null,
)

sealed interface Fragment {
    data class Request(val content: String) : Fragment
    data class Response(
        val content: String,
        val citations: List<Citation> = emptyList(),
    ) : Fragment
    data class Think(val content: String, val elapsedSecs: Double? = null) : Fragment
    data class TemplateResponse(val content: String) : Fragment
    data class Tip(val content: String, val style: String = "INFO") : Fragment
    data class ContentRisk(val content: String, val style: String = "INFO") : Fragment
    data class PartialFileRead(val ratio: Int) : Fragment
    data class Search(
        val queries: List<String> = emptyList(),
        val results: List<SearchResult> = emptyList(),
        val status: String? = null,
        val stageId: Int? = null,
    ) : Fragment
    data class ToolOpen(
        val status: String = "FINISHED",
        val stageId: Int? = null,
        val result: ToolOpenResult? = null,
    ) : Fragment
    data class ToolFind(
        val status: String = "FINISHED",
        val stageId: Int? = null,
        val pattern: String? = null,
        val result: String? = null,
    ) : Fragment
    data class ReadLink(val url: String, val title: String? = null, val status: String? = null) : Fragment
    data class File(
        val fileId: String,
        val name: String? = null,
        val mime: String? = null,
        val size: Long = 0L,
        val isImage: Boolean = false,
        val localPath: String? = null,
    ) : Fragment
}

data class Citation(
    val url: String,
    val title: String? = null,
    val citedText: String? = null,
)

data class ToolOpenResult(
    val url: String? = null,
    val title: String? = null,
    val snippet: String? = null,
    val siteName: String? = null,
    val publishedAt: String? = null,
    val siteIcon: String? = null,
)

data class SearchResult(
    val url: String,
    val title: String = "",
    val snippet: String = "",
    val publishedAt: String? = null,
    val siteName: String? = null,
    val siteIcon: String? = null,
) {
    val iconUrl: String? get() = com.opends.chat.core.util.SiteIcons.favicon(url, siteIcon)
}

data class AttachmentRef(
    val id: String,
    val uri: String,
    val name: String,
    val mime: String,
    val size: Long,
    val isImage: Boolean,
)

data class SendInput(
    val text: String,
    val thinkingEnabled: Boolean,
    val searchEnabled: Boolean,
    val attachments: List<AttachmentRef> = emptyList(),
)

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class AppSettings(
    val apiKey: String = "",
    val apiBaseUrl: String = "https://api.deepseek.com",
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val languageTag: String = "zh-CN",
    val ttsVoice: String = "mira",
    val locationEnabled: Boolean = false,
    val location: String? = null,
    val thinkingDefault: Boolean = true,
    val searchDefault: Boolean = false,
    val fontSizeLevel: Int = 0,
    val inputMode: String = "Text",
    val thinkingAutoFold: Boolean = true,
)

data class ImportResult(
    val sessions: Int = 0,
    val messages: Int = 0,
    val images: Int = 0,
    val skipped: Int = 0,
    val warnings: List<String> = emptyList(),
)

sealed interface GenerationState {
    data object Idle : GenerationState
    data class Streaming(
        val sessionId: String,
        val assistantMessageId: Long,
        val thinkingActive: Boolean = false,
    ) : GenerationState
}
