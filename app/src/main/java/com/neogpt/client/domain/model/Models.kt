package com.neogpt.client.domain.model

import java.util.UUID

enum class Role { USER, ASSISTANT, SYSTEM }

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class Attachment(
    val uri: String,
    val name: String,
    val mimeType: String,
    val sizeBytes: Long? = null,
) {
    val isImage: Boolean get() = mimeType.startsWith("image/")
}

data class Message(
    val id: String = UUID.randomUUID().toString(),
    val conversationId: String,
    val role: Role,
    val content: String,
    val createdAt: Long,
    val streaming: Boolean = false,
    val error: String? = null,
    val attachments: List<Attachment> = emptyList(),
    val modelId: String? = null,
    val feedback: Int = 0,
)

data class Conversation(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val modelId: String? = null,
)

data class ChatModel(
    val id: String,
    val name: String,
    val description: String,
    val thinking: Boolean = false,
)

data class AppPreferences(
    val themeMode: ThemeMode = ThemeMode.LIGHT,
    val baseUrl: String = "",
    val apiKey: String = "",
    val modelId: String = "gpt-5",
    val sendOnEnter: Boolean = true,
    val haptics: Boolean = true,
    val onboardingDone: Boolean = false,
    val displayName: String = "",
) {
    val isProviderConfigured: Boolean
        get() = baseUrl.isNotBlank() && modelId.isNotBlank()
}

object ModelCatalog {
    val models: List<ChatModel> = listOf(
        ChatModel(
            id = "gpt-5",
            name = "GPT-5",
            description = "Smartest model for complex work",
        ),
        ChatModel(
            id = "gpt-5-thinking",
            name = "GPT-5 Thinking",
            description = "Thinks longer before answering",
            thinking = true,
        ),
        ChatModel(
            id = "gpt-5-mini",
            name = "GPT-5 mini",
            description = "Fast answers for everyday tasks",
        ),
        ChatModel(
            id = "gpt-4.1",
            name = "GPT-4.1",
            description = "Balanced everyday model",
        ),
        ChatModel(
            id = "gpt-4o",
            name = "GPT-4o",
            description = "Fast, general purpose model",
        ),
    )

    fun byId(id: String?): ChatModel? = models.firstOrNull { it.id == id }

    fun displayName(id: String?): String = byId(id)?.name ?: (id ?: "Model")
}
