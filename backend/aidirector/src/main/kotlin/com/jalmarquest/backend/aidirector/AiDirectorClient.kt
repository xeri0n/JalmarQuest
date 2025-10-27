package com.jalmarquest.backend.aidirector

import com.jalmarquest.core.model.ChapterEventResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Abstraction over remote or local AI Director backends. Implementations should remain side-effect free
 * beyond the external call they encapsulate so the service layer can decorate them safely (e.g., rate limiting).
 */
fun interface AiDirectorClient {
    suspend fun generateChapterEvent(assembly: PromptAssemblyResult): ChapterEventResponse
}

/**
 * Metadata describing the rate limiting policy applied before delegating to the underlying client.
 */
@Serializable
data class RateLimitingConfig(
    @SerialName("max_requests") val maxRequests: Int,
    @SerialName("interval_millis") val intervalMillis: Long
) {
    init {
        require(maxRequests > 0) { "maxRequests must be positive" }
        require(intervalMillis > 0) { "intervalMillis must be positive" }
    }
}
