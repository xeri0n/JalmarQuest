package com.jalmarquest.backend.aidirector

import com.jalmarquest.core.model.ChapterEventResponse
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class GeminiAiDirectorClient(
    private val config: GeminiClientConfig,
    private val httpClient: GeminiHttpClient,
    private val json: Json = Json { ignoreUnknownKeys = true }
) : AiDirectorClient {

    override suspend fun generateChapterEvent(assembly: PromptAssemblyResult): ChapterEventResponse {
        val payload = encodeRequest(assembly)
        val response = performRequest(payload)
        return extractChapterEvent(response)
    }

    private fun encodeRequest(assembly: PromptAssemblyResult): String {
        return runCatching { json.encodeToString(assembly.payload) }
            .getOrElse { throwable ->
                throw GeminiClientException("Failed to encode Gemini request payload", throwable)
            }
    }

    private suspend fun performRequest(payload: String): GeminiHttpResponse {
        val apiPath = "v1beta/models/${config.model}:generateContent"
        return runCatching {
            httpClient.post(
                path = apiPath,
                body = payload,
                headers = mapOf("x-goog-api-key" to config.apiKey)
            )
        }.getOrElse { throwable ->
            throw GeminiClientException("Gemini request failed", throwable)
        }.also { response ->
            if (!response.isSuccessful) {
                throw GeminiClientException(
                    "Gemini request failed with status ${response.statusCode}",
                    null,
                    response
                )
            }
        }
    }

    private fun extractChapterEvent(response: GeminiHttpResponse): ChapterEventResponse {
        val parsed = runCatching { json.decodeFromString<GeminiGenerateContentResponse>(response.body) }
            .getOrElse { throwable ->
                throw GeminiClientException("Failed to decode Gemini response", throwable, response)
            }
        val textPayload = parsed.candidates
            ?.firstOrNull()
            ?.content
            ?.parts
            ?.firstOrNull()
            ?.text
            ?.takeIf { !it.isNullOrBlank() }
            ?: throw GeminiClientException("Gemini response did not include a text candidate", null, response)

        return runCatching { json.decodeFromString<ChapterEventResponse>(textPayload) }
            .getOrElse { throwable ->
                throw GeminiClientException("Gemini candidate text was not valid ChapterEventResponse JSON", throwable, response)
            }
    }
}

class GeminiClientException(
    override val message: String,
    override val cause: Throwable? = null,
    val response: GeminiHttpResponse? = null
) : RuntimeException(message, cause)
