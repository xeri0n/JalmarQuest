package com.jalmarquest.backend.aidirector

import com.jalmarquest.core.model.ChapterEventResponse
import java.util.concurrent.atomic.AtomicInteger

class SandboxAiDirectorClient(
    private val fixtures: List<SandboxChapterEventFixture>
) : AiDirectorClient {

    init {
        require(fixtures.isNotEmpty()) { "SandboxAiDirectorClient requires at least one fixture" }
    }

    private val nextIndex = AtomicInteger(0)

    @Volatile
    private var lastAssemblyInternal: PromptAssemblyResult? = null

    @Volatile
    private var lastFixtureInternal: SandboxChapterEventFixture? = null

    val lastAssembly: PromptAssemblyResult?
        get() = lastAssemblyInternal

    val lastFixture: SandboxChapterEventFixture?
        get() = lastFixtureInternal

    override suspend fun generateChapterEvent(assembly: PromptAssemblyResult): ChapterEventResponse {
        val index = nextIndex.getAndUpdate { current -> (current + 1) % fixtures.size }
        val fixture = fixtures[index]
        lastAssemblyInternal = applyOverrides(fixture, assembly)
        lastFixtureInternal = fixture
        return fixture.response
    }

    private fun applyOverrides(
        fixture: SandboxChapterEventFixture,
        assembly: PromptAssemblyResult
    ): PromptAssemblyResult {
        val overrides = fixture.promptOverrides ?: return assembly
        val mergedSystem = overrides.systemPrompt ?: assembly.systemPrompt
        val mergedUser = overrides.userPrompt ?: assembly.userPrompt
        val mergedSystemInstruction = if (overrides.systemPrompt != null) {
            assembly.payload.systemInstruction.copy(parts = listOf(GeminiPart(mergedSystem)))
        } else {
            assembly.payload.systemInstruction
        }
        val mergedContents = if (overrides.userPrompt != null && assembly.payload.contents.isNotEmpty()) {
            val first = assembly.payload.contents.first().copy(parts = listOf(GeminiPart(mergedUser)))
            listOf(first) + assembly.payload.contents.drop(1)
        } else {
            assembly.payload.contents
        }
        val mergedPayload = assembly.payload.copy(
            systemInstruction = mergedSystemInstruction,
            contents = mergedContents
        )
        return assembly.copy(
            systemPrompt = mergedSystem,
            userPrompt = mergedUser,
            payload = mergedPayload
        )
    }
}
