package com.neogpt.client.network

data class CompletionMessage(val role: String, val content: String)

data class CompletionRequest(
    val baseUrl: String,
    val apiKey: String,
    val model: String,
    val messages: List<CompletionMessage>,
    val temperature: Double = 0.7,
    val maxTokens: Int? = null,
)

sealed interface CompletionEvent {
    data class Delta(val text: String) : CompletionEvent
    data object Completed : CompletionEvent
}

class CompletionException(message: String, cause: Throwable? = null) : Exception(message, cause)
