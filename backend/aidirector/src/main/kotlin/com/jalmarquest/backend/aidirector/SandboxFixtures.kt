package com.jalmarquest.backend.aidirector

import com.jalmarquest.core.model.ChapterEventResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.io.readText

@Serializable
data class SandboxFixtureDocument(
    val fixtures: List<SandboxChapterEventFixture>,
    val metadata: SandboxFixtureMetadata = SandboxFixtureMetadata()
) {
    init {
        require(fixtures.isNotEmpty()) { "At least one sandbox fixture is required" }
    }
}

@Serializable
data class SandboxFixtureMetadata(
    @SerialName("default_mode") val defaultMode: String? = null,
    val notes: String? = null
)

@Serializable
data class SandboxChapterEventFixture(
    val id: String,
    val response: ChapterEventResponse,
    val notes: String? = null,
    @SerialName("prompt_overrides") val promptOverrides: SandboxPromptOverrides? = null
)

@Serializable
data class SandboxPromptOverrides(
    @SerialName("system_prompt") val systemPrompt: String? = null,
    @SerialName("user_prompt") val userPrompt: String? = null
)

class SandboxFixtureLoader(
    private val resourcePath: String = DEFAULT_RESOURCE_PATH,
    private val json: Json = Json { ignoreUnknownKeys = true; prettyPrint = false },
    private val resourceReader: (String) -> String? = { path ->
        SandboxFixtureLoader::class.java.classLoader.getResource(path)?.readText()
    }
) {
    fun load(): SandboxFixtureDocument {
        val raw = resourceReader(resourcePath)
            ?: throw IllegalStateException("Sandbox fixture resource not found: $resourcePath")
        return runCatching { json.decodeFromString(SandboxFixtureDocument.serializer(), raw) }
            .getOrElse { throwable ->
                throw IllegalStateException("Failed to parse sandbox fixtures", throwable)
            }
    }

    companion object {
        const val DEFAULT_RESOURCE_PATH = "aidirector/fixtures/sandbox_prompt.json"
    }
}
