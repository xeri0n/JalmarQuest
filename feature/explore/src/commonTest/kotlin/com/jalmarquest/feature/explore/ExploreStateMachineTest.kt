package com.jalmarquest.feature.explore

import com.jalmarquest.core.model.ChapterEventResponse
import com.jalmarquest.core.model.ChoiceLog
import com.jalmarquest.core.model.LoreSnippet
import com.jalmarquest.core.model.Player
import com.jalmarquest.core.model.QuestLog
import com.jalmarquest.core.model.StatusEffects
import com.jalmarquest.core.state.GameStateManager
import com.jalmarquest.feature.eventengine.EventEngine
import com.jalmarquest.feature.eventengine.EventResolution
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.buildJsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExploreStateMachineTest {
    private val repository = LoreSnippetRepository.defaultCatalog()

    @Test
    fun beginExplorationEmitsEncounter() = runTest {
        var now = 10_000L
        val manager = gameStateManager(nowProvider = { now })
        val machine = ExploreStateMachine(
            eventEngine = FakeEventEngine(EventResolution.Encounter("explore_garden_gate")),
            snippetRepository = repository,
            consequencesParser = ConsequencesParser(),
            gameStateManager = manager,
            currentTimeMillis = { now }
        )

        machine.beginExploration()

        val phase = machine.state.value.phase
        assertTrue(phase is ExplorePhase.Encounter)
        assertEquals("explore_garden_gate", phase.snippet.id)
    }

    @Test
    fun chooseOptionAppliesConsequencesAndAutosave() = runTest {
        var now = 20_000L
        val manager = gameStateManager(nowProvider = { now })
        val machine = ExploreStateMachine(
            eventEngine = FakeEventEngine(EventResolution.Encounter("explore_garden_gate")),
            snippetRepository = repository,
            consequencesParser = ConsequencesParser(),
            gameStateManager = manager,
            currentTimeMillis = { now }
        )

        machine.beginExploration()
        now = 21_000L
        machine.chooseOption(optionIndex = 1)

        val state = machine.state.value
        val resolution = state.phase as ExplorePhase.Resolution
        val player = manager.playerState.value
        val tags = player.choiceLog.entries.map { it.tag.value }
        assertTrue(tags.any { it == "explore_clover_trail" })
        assertTrue(tags.any { it == "explore_completed_garden_gate" })
        assertTrue(tags.any { it.startsWith("autosave:explore_garden_gate:") })
        val status = player.statusEffects.entries.single { it.key == "forest_poise" }
        assertEquals(now + 60 * 60 * 1000L, status.expiresAtMillis)
        assertEquals("Garden Gate Recon", resolution.summary.title)
        assertEquals(1, state.history.size)
        assertEquals(resolution.summary.autosaveTag, state.history.first().autosaveTag)
    }

    @Test
    fun chapterEventAddsHistoryOnDismissal() = runTest {
        var now = 50_000L
        val manager = gameStateManager(nowProvider = { now })
        val response = ChapterEventResponse(
            worldEventTitle = "Moonlit Assembly",
            worldEventSummary = "Critters gather to hear Jalmar's tale.",
            snippets = listOf(sampleSnippet("assembly_snippet"))
        )
        val machine = ExploreStateMachine(
            eventEngine = FakeEventEngine(EventResolution.ChapterEvent(response)),
            snippetRepository = repository,
            consequencesParser = ConsequencesParser(),
            gameStateManager = manager,
            currentTimeMillis = { now }
        )

        machine.beginExploration()
        now = 51_000L
        machine.continueAfterResolution()

        val state = machine.state.value
        assertTrue(state.phase is ExplorePhase.Idle)
        assertEquals(1, state.history.size)
        val history = state.history.first()
        assertEquals("chapter:Moonlit Assembly", history.snippetId)
        val tags = manager.playerState.value.choiceLog.entries.map { it.tag.value }
        assertTrue(tags.any { it.startsWith("autosave:chapter:") })
        assertTrue(tags.any { it == "chapter:moonlit_assembly" })
    }

    private fun gameStateManager(nowProvider: () -> Long): GameStateManager {
        return GameStateManager(
            initialPlayer = Player(
                id = "tester",
                name = "Tester",
                choiceLog = ChoiceLog(emptyList()),
                questLog = QuestLog(),
                statusEffects = StatusEffects(emptyList())
            ),
            timestampProvider = nowProvider
        )
    }

    private fun sampleSnippet(id: String): LoreSnippet = LoreSnippet(
        id = id,
        eventText = "Sample",
        choiceOptions = emptyList(),
        consequences = buildJsonObject {},
        conditions = buildJsonObject {}
    )

    private class FakeEventEngine(private val resolution: EventResolution) : EventEngine {
        override suspend fun evaluateNextEncounter(player: Player): EventResolution = resolution
    }
}
