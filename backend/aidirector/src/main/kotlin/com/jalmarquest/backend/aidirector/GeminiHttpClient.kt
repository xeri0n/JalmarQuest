package com.jalmarquest.backend.aidirector

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import kotlin.math.max

interface GeminiHttpClient {
    suspend fun post(path: String, body: String, headers: Map<String, String> = emptyMap()): GeminiHttpResponse
}

data class GeminiHttpResponse(val statusCode: Int, val body: String) {
    val isSuccessful: Boolean get() = statusCode in 200..299
}

class JavaNetGeminiHttpClient(
    private val baseUrl: String,
    private val timeoutMillis: Long
) : GeminiHttpClient {
    private val httpClient: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofMillis(max(timeoutMillis, MIN_TIMEOUT_MILLIS)))
        .build()

    override suspend fun post(path: String, body: String, headers: Map<String, String>): GeminiHttpResponse {
        return withContext(Dispatchers.IO) {
            val request = HttpRequest.newBuilder()
                .uri(URI.create(resolveUrl(path)))
                .timeout(Duration.ofMillis(max(timeoutMillis, MIN_TIMEOUT_MILLIS)))
                .header("Content-Type", "application/json")
                .apply {
                    headers.forEach { (key, value) -> header(key, value) }
                }
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build()

            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            GeminiHttpResponse(response.statusCode(), response.body())
        }
    }

    private fun resolveUrl(path: String): String {
        val normalizedBase = if (baseUrl.endsWith('/')) baseUrl.dropLast(1) else baseUrl
        val normalizedPath = if (path.startsWith('/')) path.drop(1) else path
        return "$normalizedBase/$normalizedPath"
    }

    companion object {
        private const val MIN_TIMEOUT_MILLIS = 1_000L
    }
}
