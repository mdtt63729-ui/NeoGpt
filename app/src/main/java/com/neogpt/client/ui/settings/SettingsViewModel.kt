package com.neogpt.client.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neogpt.client.AppContainer
import com.neogpt.client.domain.model.ModelCatalog
import com.neogpt.client.domain.model.ThemeMode
import com.neogpt.client.network.OpenAiCompatibleClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.util.concurrent.TimeUnit

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val baseUrl: String = "",
    val apiKey: String = "",
    val modelId: String = "gpt-5",
    val sendOnEnter: Boolean = true,
    val testing: Boolean = false,
    val testResult: String? = null,
    val message: String? = null,
)

class SettingsViewModel(private val container: AppContainer) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _state.asStateFlow()

    private var initialized = false

    init {
        viewModelScope.launch {
            container.preferences.preferences.collect { prefs ->
                if (!initialized) {
                    initialized = true
                    _state.update {
                        it.copy(
                            themeMode = prefs.themeMode,
                            baseUrl = prefs.baseUrl,
                            apiKey = prefs.apiKey,
                            modelId = prefs.modelId,
                            sendOnEnter = prefs.sendOnEnter,
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            themeMode = prefs.themeMode,
                            sendOnEnter = prefs.sendOnEnter,
                        )
                    }
                }
            }
        }
    }

    fun updateBaseUrl(value: String) = _state.update { it.copy(baseUrl = value, message = null) }

    fun updateApiKey(value: String) = _state.update { it.copy(apiKey = value, message = null) }

    fun updateModelId(value: String) = _state.update { it.copy(modelId = value, message = null) }

    fun setThemeMode(mode: ThemeMode) {
        _state.update { it.copy(themeMode = mode) }
        viewModelScope.launch { container.preferences.setThemeMode(mode) }
    }

    fun setSendOnEnter(enabled: Boolean) {
        _state.update { it.copy(sendOnEnter = enabled) }
        viewModelScope.launch { container.preferences.setSendOnEnter(enabled) }
    }

    fun saveProvider() {
        val current = _state.value
        val baseUrl = current.baseUrl.trim()
        val modelId = current.modelId.trim()
        if (baseUrl.isEmpty()) {
            _state.update { it.copy(message = "Enter the provider base URL, for example https://api.example.com/v1") }
            return
        }
        if (modelId.isEmpty()) {
            _state.update { it.copy(message = "Enter the model id your provider expects.") }
            return
        }
        viewModelScope.launch {
            container.preferences.setProvider(baseUrl, current.apiKey, modelId)
            container.preferences.setModel(modelId)
            _state.update { it.copy(message = "Provider saved.") }
        }
    }

    fun testConnection() {
        val current = _state.value
        if (current.baseUrl.isBlank()) {
            _state.update { it.copy(testResult = "Enter a base URL first.") }
            return
        }
        _state.update { it.copy(testing = true, testResult = null) }
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val base = current.baseUrl.trim().trimEnd('/')
                        .removeSuffix("/chat/completions")
                    val client = OpenAiCompatibleClient.defaultClient().newBuilder()
                        .connectTimeout(15, TimeUnit.SECONDS)
                        .readTimeout(20, TimeUnit.SECONDS)
                        .build()
                    val request = Request.Builder()
                        .url("$base/models")
                        .apply { if (current.apiKey.isNotBlank()) header("Authorization", "Bearer ${current.apiKey.trim()}") }
                        .build()
                    client.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            "Connected. The endpoint answered HTTP ${response.code}."
                        } else {
                            "Endpoint answered HTTP ${response.code}. Some providers do not expose /models; sending a message still works."
                        }
                    }
                }.getOrElse { error ->
                    "Could not reach the endpoint: ${error.message ?: error.javaClass.simpleName}"
                }
            }
            _state.update { it.copy(testing = false, testResult = result) }
        }
    }

    fun clearAllChats() {
        viewModelScope.launch {
            container.conversationRepository.deleteAllConversations()
            _state.update { it.copy(message = "All chats deleted.") }
        }
    }

    fun modelChoices(): List<String> = ModelCatalog.models.map { it.id }
}
