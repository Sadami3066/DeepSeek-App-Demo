package com.opends.chat.domain

data class Session(
    val id: String,
    val title: String?,
    val pinned: Boolean,
    val currentMessageId: Long?,
    val insertedAt: Long,
    val updatedAt: Long,
)

data class ChatMessage(
    val id: Long,
    val parentId: Long?,
    val role: Role,
    val status: MessageStatus,
    val thinkingEnabled: Boolean,
    val searchEnabled: Boolean,
    val accumulatedTokenUsage: Int,
    val insertedAt: Long,
    val fragments: List<Fragment>,
)

enum class Role { USER, ASSISTANT }

enum class MessageStatus { WIP, FINISHED, CONTENT_FILTER, FAILED, INCOMPLETE, CONTEXT_LENGTH_EXCEEDED, TIMEOUT }

sealed interface Fragment {
    data class Request(val content: String) : Fragment
    data class Response(val content: String) : Fragment
    data class Think(val content: String, val elapsedSecs: Double?) : Fragment
    data class TemplateResponse(val content: String) : Fragment
    data class Tip(val content: String, val style: String) : Fragment
    data class ContentRisk(val content: String, val style: String) : Fragment
    data class Search(
        val queries: List<String>,
        val results: List<SearchResult>,
        val status: String?,
        val stageId: Int?,
    ) : Fragment
    data class ReadLink(
        val url: String,
        val title: String?,
        val status: String?,
    ) : Fragment
    data class File(val fileId: String, val fileName: String?, val isImage: Boolean) : Fragment
}

data class SearchResult(
    val url: String,
    val title: String,
    val snippet: String,
    val publishedAt: String?,
    val siteName: String?,
    val siteIcon: String?,
)
