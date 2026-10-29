package com.neogpt.client.data.repository

import com.neogpt.client.data.local.AppDatabase
import com.neogpt.client.data.local.ConversationEntity
import com.neogpt.client.data.local.MessageEntity
import com.neogpt.client.domain.model.Attachment
import com.neogpt.client.domain.model.Conversation
import com.neogpt.client.domain.model.Message
import com.neogpt.client.domain.model.Role
import com.neogpt.client.domain.repository.ConversationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

class RoomConversationRepository(database: AppDatabase) : ConversationRepository {

    private val conversations = database.conversationDao()
    private val messages = database.messageDao()

    override fun observeConversations(): Flow<List<Conversation>> =
        conversations.observeConversations().map { list -> list.map { it.toDomain() } }

    override fun observeConversation(id: String): Flow<Conversation?> =
        conversations.observeConversation(id).map { it?.toDomain() }

    override fun observeMessages(conversationId: String): Flow<List<Message>> =
        messages.observeMessages(conversationId).map { list -> list.map { it.toDomain() } }

    override suspend fun conversation(id: String): Conversation? =
        conversations.conversation(id)?.toDomain()

    override suspend fun messages(conversationId: String): List<Message> =
        messages.messages(conversationId).map { it.toDomain() }

    override suspend fun createConversation(title: String, modelId: String?): Conversation {
        val now = System.currentTimeMillis()
        val conversation = Conversation(
            title = title,
            createdAt = now,
            updatedAt = now,
            modelId = modelId,
        )
        conversations.upsert(conversation.toEntity())
        return conversation
    }

    override suspend fun insertMessage(message: Message) {
        messages.upsert(message.toEntity())
    }

    override suspend fun updateMessage(message: Message) {
        messages.upsert(message.toEntity())
    }

    override suspend fun updateMessageContent(id: String, content: String, streaming: Boolean, error: String?) {
        messages.updateContent(id, content, error)
    }

    override suspend fun deleteMessages(ids: List<String>) {
        if (ids.isNotEmpty()) messages.deleteByIds(ids)
    }

    override suspend fun setFeedback(messageId: String, value: Int) {
        messages.updateFeedback(messageId, value)
    }

    override suspend fun renameConversation(id: String, title: String) {
        conversations.rename(id, title, System.currentTimeMillis())
    }

    override suspend fun deleteConversation(id: String) {
        conversations.delete(id)
    }

    override suspend fun deleteAllConversations() {
        conversations.deleteAll()
    }

    override suspend fun touchConversation(id: String, updatedAt: Long) {
        conversations.touch(id, updatedAt)
    }
}

private fun Conversation.toEntity() = ConversationEntity(
    id = id,
    title = title,
    createdAt = createdAt,
    updatedAt = updatedAt,
    modelId = modelId,
)

private fun ConversationEntity.toDomain() = Conversation(
    id = id,
    title = title,
    createdAt = createdAt,
    updatedAt = updatedAt,
    modelId = modelId,
)

private fun Message.toEntity() = MessageEntity(
    id = id,
    conversationId = conversationId,
    role = role.name,
    content = content,
    createdAt = createdAt,
    error = error,
    attachmentsJson = serializeAttachments(attachments),
    modelId = modelId,
    feedback = feedback,
)

private fun MessageEntity.toDomain() = Message(
    id = id,
    conversationId = conversationId,
    role = runCatching { Role.valueOf(role) }.getOrDefault(Role.ASSISTANT),
    content = content,
    createdAt = createdAt,
    error = error,
    attachments = deserializeAttachments(attachmentsJson),
    modelId = modelId,
    feedback = feedback,
)

private fun serializeAttachments(attachments: List<Attachment>): String {
    if (attachments.isEmpty()) return "[]"
    val array = JSONArray()
    attachments.forEach { attachment ->
        array.put(JSONObject().apply {
            put("uri", attachment.uri)
            put("name", attachment.name)
            put("mimeType", attachment.mimeType)
            attachment.sizeBytes?.let { put("sizeBytes", it) }
        })
    }
    return array.toString()
}

private fun deserializeAttachments(raw: String): List<Attachment> {
    if (raw.isBlank()) return emptyList()
    return runCatching {
        val array = JSONArray(raw)
        buildList {
            for (i in 0 until array.length()) {
                val item = array.optJSONObject(i) ?: continue
                add(
                    Attachment(
                        uri = item.optString("uri"),
                        name = item.optString("name"),
                        mimeType = item.optString("mimeType"),
                        sizeBytes = if (item.has("sizeBytes")) item.optLong("sizeBytes") else null,
                    )
                )
            }
        }
    }.getOrDefault(emptyList())
}
