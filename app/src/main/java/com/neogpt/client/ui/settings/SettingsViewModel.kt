package com.neogpt.client.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neogpt.client.AppContainer
import com.neogpt.client.domain.model.ModelCatalog
import com.neogpt.client.domain.model.ProviderPresets
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
    val displayName: String = "",
    val availableModels: List<String> = emptyList(),
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
                            displayName = prefs.displayName,
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            themeMode = prefs.themeMode,
                            sendOnEnter = prefs.sendOnEnter,
                            displayName = prefs.displayName,
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
                        val body = response.body?.string().orEmpty()
                        if (!response.isSuccessful) {
                            return@runCatching Triple(
                                "Endpoint answered HTTP ${response.code}. Some providers do not expose /models; sending a message still works.",
                                emptyList<String>(),
                                false,
                            )
                        }
                        val ids = runCatching {
                            val data = org.json.JSONObject(body).optJSONArray("data") ?: org.json.JSONArray()
                            (0 until data.length()).mapNotNull { index ->
                                data.optJSONObject(index)?.optString("id")?.takeIf { it.isNotBlank() }
                            }.distinct().take(60)
                        }.getOrDefault(emptyList())
                        val summary = if (ids.isEmpty()) {
                            "Connected. The endpoint answered HTTP ${response.code}."
                        } else {
                            "Connected. Found ${ids.size} models; tap one below to use it."
                        }
                        Triple(summary, ids, false)
                    }
                }.getOrElse { error ->
                    Triple(
                        "Could not reach the endpoint: ${error.message ?: error.javaClass.simpleName}",
                        emptyList<String>(),
                        true,
                    )
                }
            }
            _state.update {
                it.copy(
                    testing = false,
                    testResult = result.first,
                    availableModels = if (result.third) it.availableModels else result.second,
                )
            }
        }
    }


    fun showWelcomeAgain() {
        viewModelScope.launch { container.preferences.resetOnboarding() }
    }

    /**
     * Fills and saves a provider preset. "Ollama" and "LM Studio" assume a
     * model server on your own computer; no key is required, and usage is
     * limited only by that machine.
     */
    fun applyPreset(presetName: String) {
        val preset = ProviderPresets.byName(presetName) ?: return
        val apiKey = _state.value.apiKey
        _state.update {
            it.copy(baseUrl = preset.baseUrl, modelId = preset.modelId, message = null)
        }
        viewModelScope.launch {
            container.preferences.setProvider(preset.baseUrl, apiKey, preset.modelId)
            val hint = if (preset.requiresKey && apiKey.isBlank()) {
                " ${preset.name} needs an API key: paste it below, then tap Save."
            } else {
                " Tap Test connection."
            }
            _state.update {
                it.copy(message = "${preset.name} preset applied. ${preset.note}$hint")
            }
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
