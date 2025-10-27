package com.jalmarquest.backend.aidirector

import com.jalmarquest.core.state.analytics.AnalyticsLogger
import com.jalmarquest.feature.eventengine.ChapterEventProvider
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path

object AiDirectorFactory {
    fun createService(
        config: AiDirectorConfig,
        promptBuilder: GeminiPromptBuilder = GeminiPromptBuilder(),
        analyticsLogger: AnalyticsLogger = AnalyticsLogger(),
        observer: PromptDispatchObserver = PromptDispatchObserver { _, _, _ -> },
        json: Json = Json { ignoreUnknownKeys = true }
    ): AiDirectorService {
        val sandboxClient = resolveSandboxClient(config, json)
        val liveClient = resolveLiveClient(config, json) ?: sandboxClient ?: StubAiDirectorClient()

        return AiDirectorService(
            promptBuilder = promptBuilder,
            liveClient = liveClient,
            sandboxClient = sandboxClient,
            rateLimitingConfig = config.rateLimiting,
            observer = observer,
            analyticsLogger = analyticsLogger
        )
    }

    fun createChapterEventProvider(
        config: AiDirectorConfig,
        promptBuilder: GeminiPromptBuilder = GeminiPromptBuilder(),
        analyticsLogger: AnalyticsLogger = AnalyticsLogger(),
        observer: PromptDispatchObserver = PromptDispatchObserver { _, _, _ -> },
        json: Json = Json { ignoreUnknownKeys = true },
        modeProvider: () -> AiDirectorMode = { config.mode }
    ): ChapterEventProvider {
        val service = createService(config, promptBuilder, analyticsLogger, observer, json)
        return service.asChapterEventProvider(modeProvider)
    }

    private fun resolveLiveClient(config: AiDirectorConfig, json: Json): AiDirectorClient? {
        val geminiConfig = config.gemini ?: return null
        val httpClient = JavaNetGeminiHttpClient(
            baseUrl = geminiConfig.baseUrl,
            timeoutMillis = geminiConfig.timeoutMillis
        )
        return GeminiAiDirectorClient(geminiConfig, httpClient, json)
    }

    private fun resolveSandboxClient(config: AiDirectorConfig, json: Json): AiDirectorClient? {
        if (config.mode != AiDirectorMode.Sandbox && config.sandboxFixturesPath == null) {
            return null
        }
        val fixtures = loadSandboxFixtures(config.sandboxFixturesPath, json)
        return SandboxAiDirectorClient(fixtures.fixtures)
    }

    private fun loadSandboxFixtures(path: Path?, json: Json): SandboxFixtureDocument {
        val loader = if (path != null) {
            SandboxFixtureLoader(
                resourcePath = path.toString(),
                json = json,
                resourceReader = { resourcePath ->
                    runCatching { Files.readString(Path.of(resourcePath)) }.getOrNull()
                }
            )
        } else {
            SandboxFixtureLoader(json = json)
        }
        return loader.load()
    }
}
