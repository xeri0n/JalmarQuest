package com.jalmarquest.backend.aidirector

import com.jalmarquest.backend.aidirector.AiDirectorMode
import com.jalmarquest.backend.aidirector.deploy.AiDirectorDeployment
import com.jalmarquest.core.model.ChapterEventRequest
import com.jalmarquest.core.model.ChoiceLog
import com.jalmarquest.core.model.PlayerNarrativeState
import com.jalmarquest.core.model.QuestLog
import com.jalmarquest.core.model.StatusEffects
import com.jalmarquest.core.state.analytics.AnalyticsLogger
import com.jalmarquest.feature.eventengine.ChapterEventProvider
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.koin.core.context.stopKoin
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertSame

class AiDirectorDeploymentTest {
    @AfterEach
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `bootstrap wires sandbox default`() {
        val handles = AiDirectorDeployment.bootstrap(
            env = emptyMap(),
            json = Json { ignoreUnknownKeys = true }
        )

        assertNotNull(handles.koin)
        assertIs<AiDirectorService>(handles.service)
    assertEquals(AiDirectorMode.Sandbox, handles.koin.get<AiDirectorConfig>().mode)

        val response = runBlocking {
            handles.chapterEventProvider.generateChapterEvent(
                ChapterEventRequest(
                    playerState = PlayerNarrativeState(
                        id = "player",
                        choiceLog = ChoiceLog(emptyList()),
                        questLog = QuestLog(),
                        statusEffects = StatusEffects(emptyList())
                    )
                )
            )
        }
    assertNotNull(response)
    assertEquals("Lampglow Over Buttonburgh", response.worldEventTitle)
    }

    @Test
    fun `module reuses injected components`() {
        val config = AiDirectorConfig(mode = AiDirectorMode.Live, gemini = GeminiClientConfig(apiKey = "key"))
        val promptBuilder = GeminiPromptBuilder()
        val analytics = AnalyticsLogger()
        val observer = PromptDispatchObserver { _, _, _ -> }
        val json = Json { ignoreUnknownKeys = true }

        val module = AiDirectorDeployment.createModule(config, promptBuilder, analytics, observer, json)
        val koin = org.koin.core.context.startKoin { modules(module) }.koin

        val service1 = koin.get<AiDirectorService>()
        val service2 = koin.get<AiDirectorService>()
        assertSame(service1, service2)

        val provider1 = koin.get<ChapterEventProvider>()
        val provider2 = koin.get<ChapterEventProvider>()
        assertSame(provider1, provider2)
    }
}
