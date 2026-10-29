package com.neogpt.client.domain.repository

import com.neogpt.client.domain.model.Conversation
import com.neogpt.client.domain.model.Message
import com.neogpt.client.network.CompletionEvent
import com.neogpt.client.network.CompletionRequest
import kotlinx.coroutines.flow.Flow

interface ConversationRepository {
    fun observeConversations(): Flow<List<Conversation>>
    fun observeConversation(id: String): Flow<Conversation?>
    fun observeMessages(conversationId: String): Flow<List<Message>>

    suspend fun conversation(id: String): Conversation?
    suspend fun messages(conversationId: String): List<Message>
    suspend fun createConversation(title: String, modelId: String?): Conversation
    suspend fun insertMessage(message: Message)
    suspend fun updateMessage(message: Message)
    suspend fun updateMessageContent(id: String, content: String, streaming: Boolean, error: String?)
    suspend fun deleteMessages(ids: List<String>)
    suspend fun setFeedback(messageId: String, value: Int)
    suspend fun renameConversation(id: String, title: String)
    suspend fun deleteConversation(id: String)
    suspend fun deleteAllConversations()
    suspend fun touchConversation(id: String, updatedAt: Long)
}

interface ChatCompletionClient {
    fun streamCompletion(request: CompletionRequest): Flow<CompletionEvent>
}
