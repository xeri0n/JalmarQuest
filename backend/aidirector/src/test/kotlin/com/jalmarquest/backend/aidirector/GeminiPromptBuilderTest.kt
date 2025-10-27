package com.jalmarquest.backend.aidirector

import com.jalmarquest.core.model.ChapterEventRequest
import com.jalmarquest.core.model.ChoiceLog
import com.jalmarquest.core.model.ChoiceLogEntry
import com.jalmarquest.core.model.ChoiceTag
import com.jalmarquest.core.model.PlayerNarrativeState
import com.jalmarquest.core.model.Quest
import com.jalmarquest.core.model.QuestId
import com.jalmarquest.core.model.QuestLog
import com.jalmarquest.core.model.QuestObjective
import com.jalmarquest.core.model.QuestObjectiveType
import com.jalmarquest.core.model.QuestProgress
import com.jalmarquest.core.model.QuestStatus
import com.jalmarquest.core.model.StatusEffect
import com.jalmarquest.core.model.StatusEffects
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GeminiPromptBuilderTest {
    private val builder = GeminiPromptBuilder()

    @Test
    fun `system prompt reflects design pillars and player id`() {
        val prompt = builder.buildSystemPrompt(request)

        assertTrue(prompt.contains("Jalmar Quest"))
        assertTrue(prompt.contains("Butterfly Effect"))
        assertTrue(prompt.contains("player-123"))
    }

    @Test
    fun `request payload embeds user context`() {
        val assembly = builder.assemble(request)
        val payload = assembly.payload

        assertEquals("system", payload.systemInstruction.role)
        assertTrue(payload.systemInstruction.parts.first().text.contains("tiny-hero"))

        assertEquals(1, payload.contents.size)
        val userContent = payload.contents.first()
        assertEquals("user", userContent.role)
        val text = userContent.parts.first().text
        assertTrue(text.contains("player_id: player-123"))
        assertTrue(text.contains("tag: brave_waddle"))
        assertTrue(text.contains("trigger_reason: milestone"))

        assertEquals("player-123", assembly.request.playerState.id)
        assertTrue(assembly.systemPrompt.contains("Butterfly"))
        assertTrue(assembly.userPrompt.contains("quest_log"))
    }

    private val request = ChapterEventRequest(
        playerState = PlayerNarrativeState(
            id = "player-123",
            choiceLog = ChoiceLog(
                entries = listOf(
                    ChoiceLogEntry(tag = ChoiceTag("brave_waddle"), timestampMillis = 42L)
                )
            ),
            questLog = QuestLog(
                activeQuests = listOf(
                    QuestProgress(
                        questId = QuestId("feathered_favor"),
                        status = QuestStatus.ACTIVE,
                        objectives = listOf(
                            QuestObjective(
                                objectiveId = "collect_feathers",
                                description = "Collect 5 feathers",
                                type = QuestObjectiveType.COLLECT_ITEMS,
                                targetQuantity = 5,
                                currentProgress = 1
                            )
                        ),
                        acceptedAt = 1000000L
                    )
                )
            ),
            statusEffects = StatusEffects(
                entries = listOf(
                    StatusEffect(key = "seed_glow", expiresAtMillis = 999L)
                )
            )
        ),
        triggerReason = "milestone"
    )
}
