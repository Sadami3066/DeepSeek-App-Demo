package com.opends.chat.feature.chat

import com.opends.chat.core.AttachmentRef
import com.opends.chat.core.ChatMessage
import com.opends.chat.core.GenerationState
import com.opends.chat.core.SearchResult

enum class AttachmentState { UPLOADING, READY, ERROR }

data class DraftAttachment(
    val ref: AttachmentRef,
    val state: AttachmentState = AttachmentState.UPLOADING,
    val error: String? = null,
)

enum class ReadingStatus { IDLE, LOADING, PLAYING }

data class ReadingState(
    val messageId: Long? = null,
    val status: ReadingStatus = ReadingStatus.IDLE,
)

data class SearchPanelState(
    val entryKey: String,
    val sources: List<SearchResult>,
)

data class ToastState(
    val id: Long,
    val text: String,
    val withIcon: Boolean = true,
    val durationMs: Long = 3000L,
)

data class ChatDraftState(
    val text: String = "",
    val attachments: List<DraftAttachment> = emptyList(),
) {
    val canSend: Boolean
        get() = (text.isNotBlank() || attachments.isNotEmpty()) &&
            attachments.all { it.state == AttachmentState.READY }
}

data class ChatVoiceState(
    val recording: Boolean = false,
    val recordingSeconds: Int = 0,
    val transcribing: Boolean = false,
    val amplitudes: List<Float> = emptyList(),
)

data class ChatUiState(
    val sessionId: String? = null,
    val title: String = "",
    val messages: List<ChatMessage> = emptyList(),
    val allMessages: List<ChatMessage> = emptyList(),
    val versions: Map<Long, List<Long>> = emptyMap(),
    val generation: GenerationState = GenerationState.Idle,
    val loaded: Boolean = true,
    val hasApiKey: Boolean = false,
    val thinkingEnabled: Boolean = true,
    val searchEnabled: Boolean = false,
    val toast: ToastState? = null,
    val reading: ReadingState = ReadingState(),
    val searchPanel: SearchPanelState? = null,
    val autoRead: Boolean = false,
    val thinkingAutoFold: Boolean = true,
) {
    val streamingMessageId: Long?
        get() = (generation as? GenerationState.Streaming)?.assistantMessageId

    val hasMessages: Boolean get() = messages.isNotEmpty()

    val streaming: Boolean get() = generation is GenerationState.Streaming
}
