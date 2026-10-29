package com.neogpt.client.network

import com.neogpt.client.domain.repository.ChatCompletionClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Talks to any provider that implements the OpenAI-compatible
 * `POST {baseUrl}/chat/completions` streaming contract.
 *
 * The ChatGPT backend used by the original app is account-bound and is not
 * reused here; point [CompletionRequest.baseUrl] at a service you are allowed
 * to call.
 */
class OpenAiCompatibleClient(
    private val httpClient: OkHttpClient = defaultClient(),
) : ChatCompletionClient {

    override fun streamCompletion(request: CompletionRequest): Flow<CompletionEvent> = callbackFlow {
        val url = buildUrl(request.baseUrl)
        val payload = JSONObject().apply {
            put("model", request.model)
            put("stream", true)
            put("temperature", request.temperature)
            request.maxTokens?.let { put("max_tokens", it) }
            put("messages", JSONArray().apply {
                request.messages.forEach { m ->
                    put(JSONObject().apply {
                        put("role", m.role)
                        put("content", m.content)
                    })
                }
            })
        }.toString()

        val httpRequest = Request.Builder()
            .url(url)
            .post(payload.toRequestBody(JSON))
            .header("Content-Type", "application/json")
            .header("Accept", "text/event-stream")
            .apply { if (request.apiKey.isNotBlank()) header("Authorization", "Bearer ${request.apiKey}") }
            .build()

        val call = httpClient.newCall(httpRequest)
        val job = launch(Dispatchers.IO) {
            try {
                call.execute().use { response ->
                    if (!response.isSuccessful) {
                        val body = response.body?.string().orEmpty()
                        close(CompletionException(describeError(response.code, body)))
                        return@use
                    }
                    val source = response.body?.source()
                        ?: run {
                            close(CompletionException("Provider returned an empty body."))
                            return@use
                        }
                    while (isActive && !source.exhausted()) {
                        val line = source.readUtf8Line() ?: break
                        if (line.isEmpty() || line.startsWith(":")) continue
                        if (!line.startsWith("data:")) continue
                        val data = line.removePrefix("data:").trim()
                        if (data == "[DONE]") break
                        val delta = parseDelta(data) ?: continue
                        trySend(CompletionEvent.Delta(delta))
                    }
                    trySend(CompletionEvent.Completed)
                    close()
                }
            } catch (t: Throwable) {
                if (!isActive) return@launch
                close(if (t is IOException) CompletionException("Connection failed: ${t.message}", t) else t)
            }
        }

        awaitClose {
            job.cancel()
            call.cancel()
        }
    }

    private fun buildUrl(baseUrl: String): String {
        val trimmed = baseUrl.trim().trimEnd('/')
        if (trimmed.isEmpty()) throw CompletionException("No provider configured.")
        val withScheme = if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            trimmed
        } else {
            "https://$trimmed"
        }
        return if (withScheme.endsWith("/chat/completions")) withScheme else "$withScheme/chat/completions"
    }

    private fun parseDelta(raw: String): String? = try {
        val json = JSONObject(raw)
        json.optJSONObject("error")?.let { err ->
            throw CompletionException(err.optString("message", "Provider error."))
        }
        val choices = json.optJSONArray("choices")
        if (choices == null || choices.length() == 0) {
            null
        } else {
            val choice = choices.getJSONObject(0)
            val delta = choice.optJSONObject("delta")
            val text = delta?.optString("content").orEmpty()
            text.ifEmpty { choice.optJSONObject("message")?.optString("content").orEmpty() }
                .ifEmpty { null }
        }
    } catch (e: CompletionException) {
        throw e
    } catch (t: Throwable) {
        null
    }

    private fun describeError(code: Int, body: String): String {
        val message = runCatching {
            JSONObject(body).optJSONObject("error")?.optString("message")
        }.getOrNull()
        return when {
            !message.isNullOrBlank() -> "Provider error $code: $message"
            code == 401 || code == 403 -> "Provider rejected the credentials ($code). Check the API key in Settings."
            code == 404 -> "Endpoint not found ($code). Check the base URL in Settings."
            code == 429 -> "Rate limited by the provider ($code). Try again shortly."
            else -> "Provider returned HTTP $code."
        }
    }

    companion object {
        private val JSON = "application/json; charset=utf-8".toMediaType()

        fun defaultClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }
}
