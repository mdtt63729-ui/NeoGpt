package com.neogpt.client.domain.model

data class ProviderPreset(
    val name: String,
    val baseUrl: String,
    val modelId: String,
    val requiresKey: Boolean,
    val note: String,
)

object ProviderPresets {
    val all: List<ProviderPreset> = listOf(
        ProviderPreset(
            name = "OpenAI",
            baseUrl = "https://api.openai.com/v1",
            modelId = "gpt-4o-mini",
            requiresKey = true,
            note = "Official OpenAI platform key.",
        ),
        ProviderPreset(
            name = "OpenRouter",
            baseUrl = "https://openrouter.ai/api/v1",
            modelId = "deepseek/deepseek-chat",
            requiresKey = true,
            note = "One key, many models. Add a :free suffix for free variants, for example deepseek/deepseek-r1:free.",
        ),
        ProviderPreset(
            name = "Gemini",
            baseUrl = "https://generativelanguage.googleapis.com/v1beta/openai",
            modelId = "gemini-2.0-flash",
            requiresKey = true,
            note = "Google AI Studio key, used through the OpenAI-compatible endpoint.",
        ),
        ProviderPreset(
            name = "Groq",
            baseUrl = "https://api.groq.com/openai/v1",
            modelId = "llama-3.3-70b-versatile",
            requiresKey = true,
            note = "GroqCloud key. Free tier is rate limited.",
        ),
        ProviderPreset(
            name = "DeepSeek",
            baseUrl = "https://api.deepseek.com/v1",
            modelId = "deepseek-chat",
            requiresKey = true,
            note = "DeepSeek platform key.",
        ),
        ProviderPreset(
            name = "Mistral",
            baseUrl = "https://api.mistral.ai/v1",
            modelId = "mistral-large-latest",
            requiresKey = true,
            note = "Mistral La Plateforme key.",
        ),
        ProviderPreset(
            name = "Ollama",
            baseUrl = "http://192.168.1.10:11434/v1",
            modelId = "llama3.2",
            requiresKey = false,
            note = "Runs on your own computer. No API key needed; replace the example IP with that computer's address.",
        ),
        ProviderPreset(
            name = "LM Studio",
            baseUrl = "http://192.168.1.10:1234/v1",
            modelId = "local-model",
            requiresKey = false,
            note = "Runs on your own computer. No API key needed; replace the example IP with that computer's address.",
        ),
    )

    fun byName(name: String): ProviderPreset? = all.firstOrNull { it.name == name }
}
