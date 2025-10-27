package com.jalmarquest.backend.aidirector

import java.nio.file.Path

data class AiDirectorConfig(
    val mode: AiDirectorMode = AiDirectorMode.Sandbox,
    val gemini: GeminiClientConfig? = null,
    val sandboxFixturesPath: Path? = null,
    val rateLimiting: RateLimitingConfig? = null
) {
    init {
        if (mode == AiDirectorMode.Live && gemini == null) {
            throw IllegalStateException("Live AI Director mode requires Gemini configuration")
        }
    }

    companion object {
        private const val MODE_ENV = "AI_DIRECTOR_MODE"
        private const val SANDBOX_FIXTURES_ENV = "AI_DIRECTOR_SANDBOX_FIXTURES"
        private const val RATE_LIMIT_MAX_ENV = "AI_DIRECTOR_RATE_LIMIT_MAX_REQUESTS"
        private const val RATE_LIMIT_INTERVAL_ENV = "AI_DIRECTOR_RATE_LIMIT_INTERVAL_MILLIS"

        fun fromEnvironment(env: Map<String, String> = System.getenv()): AiDirectorConfig {
            val mode = env[MODE_ENV]
                ?.trim()
                ?.lowercase()
                ?.let {
                    when (it) {
                        "live" -> AiDirectorMode.Live
                        "sandbox" -> AiDirectorMode.Sandbox
                        else -> throw IllegalArgumentException("Unsupported AI_DIRECTOR_MODE value: $it")
                    }
                } ?: AiDirectorMode.Sandbox

            val sandboxFixtures = env[SANDBOX_FIXTURES_ENV]
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?.let { Path.of(it) }

            val rateLimiting = when {
                env[RATE_LIMIT_MAX_ENV].isNullOrBlank() && env[RATE_LIMIT_INTERVAL_ENV].isNullOrBlank() -> null
                else -> {
                    val maxRequests = env[RATE_LIMIT_MAX_ENV]
                        ?.toIntOrNull()
                        ?: throw IllegalArgumentException("$RATE_LIMIT_MAX_ENV must be a positive integer")
                    val intervalMillis = env[RATE_LIMIT_INTERVAL_ENV]
                        ?.toLongOrNull()
                        ?: throw IllegalArgumentException("$RATE_LIMIT_INTERVAL_ENV must be a positive integer")
                    RateLimitingConfig(maxRequests, intervalMillis)
                }
            }

            val geminiConfig = when {
                mode == AiDirectorMode.Live -> GeminiClientConfig.fromEnvironment(env)
                env[GeminiClientConfig.API_KEY_ENV]?.isNullOrBlank() == false -> GeminiClientConfig.fromEnvironment(env)
                else -> null
            }

            return AiDirectorConfig(
                mode = mode,
                gemini = geminiConfig,
                sandboxFixturesPath = sandboxFixtures,
                rateLimiting = rateLimiting
            )
        }
    }
}
