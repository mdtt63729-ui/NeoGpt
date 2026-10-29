package com.neogpt.client.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neogpt.client.AppContainer
import com.neogpt.client.domain.model.Attachment
import com.neogpt.client.domain.model.Conversation
import com.neogpt.client.domain.model.Message
import com.neogpt.client.domain.model.Role
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val draft: String = "",
    val attachments: List<Attachment> = emptyList(),
    val modelId: String = "gpt-5",
    val providerConfigured: Boolean = false,
    val sendOnEnter: Boolean = true,
    val recentConversations: List<Conversation> = emptyList(),
)

class HomeViewModel(private val container: AppContainer) : ViewModel() {

    private val draft = MutableStateFlow("")
    private val attachments = MutableStateFlow<List<Attachment>>(emptyList())

    val uiState: StateFlow<HomeUiState> = combine(
        container.preferences.preferences,
        container.conversationRepository.observeConversations(),
        draft,
        attachments,
    ) { prefs, conversations, currentDraft, currentAttachments ->
        HomeUiState(
            draft = currentDraft,
            attachments = currentAttachments,
            modelId = prefs.modelId,
            providerConfigured = prefs.isProviderConfigured,
            sendOnEnter = prefs.sendOnEnter,
            recentConversations = conversations.take(20),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    fun setDraft(value: String) {
        draft.value = value
    }

    fun addAttachment(attachment: Attachment) {
        if (attachments.value.none { it.uri == attachment.uri }) {
            attachments.value = attachments.value + attachment
        }
    }

    fun removeAttachment(attachment: Attachment) {
        attachments.value = attachments.value.filterNot { it.uri == attachment.uri }
    }

    fun clearDraft() {
        draft.value = ""
        attachments.value = emptyList()
    }

    /**
     * Persists the draft as a new conversation and returns its id, or null when
     * there is nothing to send.
     */
    suspend fun send(): String? {
        val text = draft.value.trim()
        val files = attachments.value
        if (text.isEmpty() && files.isEmpty()) return null

        val modelId = uiState.value.modelId
        val title = text.replace('\n', ' ').trim().take(48).ifBlank { "New chat" }

        val conversation = container.conversationRepository.createConversation(title, modelId)
        container.conversationRepository.insertMessage(
            Message(
                conversationId = conversation.id,
                role = Role.USER,
                content = text,
                createdAt = System.currentTimeMillis(),
                attachments = files,
            )
        )
        clearDraft()
        return conversation.id
    }
}
