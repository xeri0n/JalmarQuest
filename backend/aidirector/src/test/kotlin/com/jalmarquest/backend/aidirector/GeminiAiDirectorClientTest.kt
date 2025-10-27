package com.jalmarquest.backend.aidirector

import com.jalmarquest.core.model.ChapterEventRequest
import com.jalmarquest.core.model.ChapterEventResponse
import com.jalmarquest.core.model.ChoiceLog
import com.jalmarquest.core.model.ChoiceLogEntry
import com.jalmarquest.core.model.ChoiceTag
import com.jalmarquest.core.model.LoreSnippet
import com.jalmarquest.core.model.PlayerNarrativeState
import com.jalmarquest.core.model.QuestLog
import com.jalmarquest.core.model.QuestStatus
import com.jalmarquest.core.model.StatusEffect
import com.jalmarquest.core.model.StatusEffects
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GeminiAiDirectorClientTest {
    private val json = Json { ignoreUnknownKeys = true }
    private val config = GeminiClientConfig(
        apiKey = "test-key",
        model = "gemini-test",
        baseUrl = "https://example.com",
        timeoutMillis = 5_000L
    )
    private val sampleAssembly = PromptAssemblyResult(
        request = ChapterEventRequest(
            playerState = PlayerNarrativeState(
                id = "player-1",
                choiceLog = ChoiceLog(listOf(ChoiceLogEntry(ChoiceTag("first"), 1L))),
                questLog = QuestLog(),
                statusEffects = StatusEffects(listOf(StatusEffect("brave", null)))
            ),
            triggerReason = "test"
        ),
        systemPrompt = "system",
        userPrompt = "user",
        payload = GeminiGenerateContentRequest(
            systemInstruction = GeminiContent(role = "system", parts = listOf(GeminiPart("system"))),
            contents = listOf(GeminiContent(role = "user", parts = listOf(GeminiPart("user"))))
        )
    )

    @Test
    fun generatesChapterEventFromSuccessfulResponse() = runTest {
        val expected = ChapterEventResponse(
            worldEventTitle = "A Tiny Triumph",
            worldEventSummary = "Jalmar finds a glimmering seed.",
            snippets = listOf(
                LoreSnippet(
                    id = "snippet-1",
                    eventText = "A chance discovery.",
                    choiceOptions = listOf("Pocket it", "Share it", "Ignore it"),
                    consequences = buildJsonObject {
                        put("pocket", buildJsonArray { add(JsonPrimitive("gain_seed")) })
                    },
                    conditions = buildJsonObject { }
                )
            )
        )

        val candidateText = json.encodeToString(expected)
        val httpResponseBody = json.encodeToString(
            GeminiGenerateContentResponse(
                candidates = listOf(
                    GeminiResponseCandidate(
                        content = GeminiResponseContent(
                            parts = listOf(GeminiResponsePart(text = candidateText))
                        )
                    )
                )
            )
        )

        val httpClient = FakeGeminiHttpClient()
        httpClient.nextResponse = GeminiHttpResponse(statusCode = 200, body = httpResponseBody)

        val client = GeminiAiDirectorClient(config, httpClient, json)
        val response = client.generateChapterEvent(sampleAssembly)

        assertEquals(expected, response)
    assertEquals("v1beta/models/gemini-test:generateContent", httpClient.lastPath)
        assertEquals("test-key", httpClient.lastHeaders["x-goog-api-key"])
    }

    @Test
    fun throwsWhenHttpStatusIsNotSuccessful() = runTest {
        val httpClient = FakeGeminiHttpClient()
        httpClient.nextResponse = GeminiHttpResponse(statusCode = 401, body = "")

        val client = GeminiAiDirectorClient(config, httpClient, json)

        assertFailsWith<GeminiClientException> {
            client.generateChapterEvent(sampleAssembly)
        }
    }

    @Test
    fun throwsWhenResponseHasNoCandidates() = runTest {
        val httpClient = FakeGeminiHttpClient()
        val body = json.encodeToString(GeminiGenerateContentResponse(candidates = emptyList()))
        httpClient.nextResponse = GeminiHttpResponse(200, body)

        val client = GeminiAiDirectorClient(config, httpClient, json)

        assertFailsWith<GeminiClientException> {
            client.generateChapterEvent(sampleAssembly)
        }
    }

    @Test
    fun throwsWhenCandidateTextIsInvalidJson() = runTest {
        val httpClient = FakeGeminiHttpClient()
        val responseBody = json.encodeToString(
            GeminiGenerateContentResponse(
                candidates = listOf(
                    GeminiResponseCandidate(
                        content = GeminiResponseContent(parts = listOf(GeminiResponsePart(text = "not-json")))
                    )
                )
            )
        )
        httpClient.nextResponse = GeminiHttpResponse(200, responseBody)

        val client = GeminiAiDirectorClient(config, httpClient, json)

        assertFailsWith<GeminiClientException> {
            client.generateChapterEvent(sampleAssembly)
        }
    }

    private class FakeGeminiHttpClient : GeminiHttpClient {
        var lastPath: String? = null
        var lastBody: String? = null
        var lastHeaders: Map<String, String> = emptyMap()
        var nextResponse: GeminiHttpResponse = GeminiHttpResponse(200, "")
        var nextError: Throwable? = null

        override suspend fun post(path: String, body: String, headers: Map<String, String>): GeminiHttpResponse {
            nextError?.let { throw it }
            lastPath = path
            lastBody = body
            lastHeaders = headers
            return nextResponse
        }
    }
}
