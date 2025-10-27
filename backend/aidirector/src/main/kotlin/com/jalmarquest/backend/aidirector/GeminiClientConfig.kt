package com.jalmarquest.backend.aidirector

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

data class GeminiClientConfig(
    val apiKey: String,
    val model: String = DEFAULT_MODEL,
    val baseUrl: String = DEFAULT_BASE_URL,
    val timeoutMillis: Long = DEFAULT_TIMEOUT_MILLIS
) {
    init {
        require(apiKey.isNotBlank()) { "Gemini API key must not be blank" }
        require(model.isNotBlank()) { "Gemini model must not be blank" }
        require(baseUrl.isNotBlank()) { "Gemini base URL must not be blank" }
        require(timeoutMillis > 0) { "Gemini timeout must be positive" }
    }

    val timeout: Duration get() = timeoutMillis.milliseconds

    companion object {
        const val DEFAULT_BASE_URL = "https://generativelanguage.googleapis.com"
    const val DEFAULT_MODEL = "gemini-1.5-pro"
        const val DEFAULT_TIMEOUT_MILLIS = 15_000L

    const val API_KEY_ENV = "GEMINI_API_KEY"
    private const val MODEL_ENV = "GEMINI_MODEL"
    private const val BASE_URL_ENV = "GEMINI_BASE_URL"
    private const val TIMEOUT_ENV = "GEMINI_TIMEOUT_MILLIS"

        fun fromEnvironment(env: Map<String, String> = System.getenv()): GeminiClientConfig {
            val apiKey = env[API_KEY_ENV]?.trim().orEmpty()
            if (apiKey.isBlank()) {
                throw IllegalStateException("$API_KEY_ENV environment variable is required for Gemini integration")
            }
            val model = env[MODEL_ENV]?.takeIf { it.isNotBlank() } ?: DEFAULT_MODEL
            val baseUrl = env[BASE_URL_ENV]?.takeIf { it.isNotBlank() } ?: DEFAULT_BASE_URL
            val timeout = env[TIMEOUT_ENV]
                ?.takeIf { it.isNotBlank() }
                ?.toLongOrNull()
                ?: DEFAULT_TIMEOUT_MILLIS
            return GeminiClientConfig(
                apiKey = apiKey,
                model = model,
                baseUrl = baseUrl,
                timeoutMillis = timeout
            )
        }
    }
}
