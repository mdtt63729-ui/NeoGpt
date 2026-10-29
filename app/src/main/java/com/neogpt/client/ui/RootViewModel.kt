package com.neogpt.client.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.neogpt.client.AppContainer
import com.neogpt.client.domain.model.Attachment
import com.neogpt.client.domain.model.Conversation
import com.neogpt.client.domain.model.ModelCatalog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RootUiState(
    val conversations: List<Conversation> = emptyList(),
    val activeConversationId: String? = null,
    val modelId: String = "gpt-5",
    val providerConfigured: Boolean = false,
    val onboardingDone: Boolean? = null,
    val displayName: String = "",
) {
    val modelLabel: String get() = ModelCatalog.displayName(modelId)
}

class RootViewModel(private val container: AppContainer) : ViewModel() {

    private val activeConversationId = MutableStateFlow<String?>(null)

    val uiState: StateFlow<RootUiState> = combine(
        container.conversationRepository.observeConversations(),
        container.preferences.preferences,
        activeConversationId,
    ) { conversations, prefs, activeId ->
        RootUiState(
            conversations = conversations,
            activeConversationId = activeId,
            modelId = prefs.modelId,
            providerConfigured = prefs.isProviderConfigured,
            onboardingDone = prefs.onboardingDone,
            displayName = prefs.displayName,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RootUiState(),
    )

    fun setActiveConversation(id: String?) {
        activeConversationId.value = id
    }

    fun selectModel(modelId: String) {
        viewModelScope.launch { container.preferences.setModel(modelId) }
    }

    fun skipOnboarding() {
        viewModelScope.launch { container.preferences.skipOnboarding() }
    }

    fun completeOnboarding(name: String) {
        viewModelScope.launch { container.preferences.completeOnboarding(name) }
    }

    fun showWelcomeAgain() {
        viewModelScope.launch { container.preferences.resetOnboarding() }
    }

    fun renameConversation(id: String, title: String) {
        val clean = title.trim()
        if (clean.isEmpty()) return
        viewModelScope.launch { container.conversationRepository.renameConversation(id, clean) }
    }

    fun deleteConversation(id: String) {
        viewModelScope.launch {
            if (activeConversationId.value == id) activeConversationId.value = null
            container.conversationRepository.deleteConversation(id)
        }
    }
}

class ContainerViewModelFactory<T : ViewModel>(
    private val create: () -> T,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <VM : ViewModel> create(modelClass: Class<VM>): VM = create() as VM
}

/**
 * A picked content URI plus the metadata the UI needs before it is persisted.
 */
data class PickedAttachment(
    val uri: Uri,
    val name: String,
    val mimeType: String,
    val sizeBytes: Long?,
) {
    fun toDomain(): Attachment = Attachment(
        uri = uri.toString(),
        name = name,
        mimeType = mimeType,
        sizeBytes = sizeBytes,
    )
}
