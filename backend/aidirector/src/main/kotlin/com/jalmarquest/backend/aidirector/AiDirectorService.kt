package com.jalmarquest.backend.aidirector

import com.jalmarquest.core.model.ChapterEventRequest
import com.jalmarquest.core.model.ChapterEventResponse
import com.jalmarquest.core.model.NarrativeGenerationException
import com.jalmarquest.feature.eventengine.ChapterEventProvider
import com.jalmarquest.core.state.analytics.AnalyticsLogger
import com.jalmarquest.core.state.analytics.events.AnalyticsEvent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

class AiDirectorService(
    private val promptBuilder: GeminiPromptBuilder,
    private val liveClient: AiDirectorClient,
    private val sandboxClient: AiDirectorClient? = null,
    private val rateLimitingConfig: RateLimitingConfig? = null,
    private val clock: () -> Long = { System.currentTimeMillis() },
    private val delayFn: suspend (Long) -> Unit = { delay(it) },
    private val observer: PromptDispatchObserver = PromptDispatchObserver { _, _, _ -> },
    private val analyticsLogger: AnalyticsLogger = AnalyticsLogger()
) {
    private val livePipeline: AiDirectorClient by lazy {
        rateLimitingConfig?.let {
            RateLimitingAiDirectorClient(liveClient, it, clock, delayFn)
        } ?: liveClient
    }

    suspend fun generateChapterEvent(
        request: ChapterEventRequest,
        mode: AiDirectorMode = AiDirectorMode.Live
    ): ChapterEventResponse {
        val assembly = promptBuilder.assemble(request)
        // TODO: Integrate Butterfly Effect logging pipeline once available.
        val client = when (mode) {
            AiDirectorMode.Live -> livePipeline
            AiDirectorMode.Sandbox -> sandboxClient ?: livePipeline
        }
        return try {
            val response = client.generateChapterEvent(assembly)
            observer.onPromptDispatched(assembly, response, mode)
            analyticsLogger.log(
                AnalyticsEvent.NarrativeGenerated(
                    timestampMillis = clock(),
                    playerId = request.playerState.id,
                    mode = mode.name.lowercase(),
                    tokenLastFour = null
                )
            )
            response
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) throw throwable
            analyticsLogger.log(
                AnalyticsEvent.NarrativeGenerationFailed(
                    timestampMillis = clock(),
                    reason = throwable.message ?: "unknown",
                    mode = mode.name.lowercase()
                )
            )
            throw NarrativeGenerationException("Failed to generate chapter event", throwable)
        }
    }

    fun asChapterEventProvider(modeProvider: () -> AiDirectorMode = { AiDirectorMode.Live }): ChapterEventProvider {
        return ChapterEventProvider { request ->
            runBlocking { generateChapterEvent(request, modeProvider()) }
        }
    }
}

enum class AiDirectorMode {
    Live,
    Sandbox
}

fun interface PromptDispatchObserver {
    suspend fun onPromptDispatched(
        assembly: PromptAssemblyResult,
        response: ChapterEventResponse,
        mode: AiDirectorMode
    )
}
