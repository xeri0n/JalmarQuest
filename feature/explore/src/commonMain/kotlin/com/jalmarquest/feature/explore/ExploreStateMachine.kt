package com.jalmarquest.feature.explore

import com.jalmarquest.core.model.ChapterEventResponse
import com.jalmarquest.core.model.Player
import com.jalmarquest.core.state.GameStateManager
import com.jalmarquest.feature.eventengine.EventEngine
import com.jalmarquest.feature.eventengine.EventResolution
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock

class ExploreStateMachine(
    private val eventEngine: EventEngine,
    private val snippetRepository: LoreSnippetRepository,
    private val consequencesParser: ConsequencesParser,
    private val gameStateManager: GameStateManager,
    private val currentTimeMillis: () -> Long = { Clock.System.now().toEpochMilliseconds() }
) {
    private val mutex = Mutex()
    private val _state = MutableStateFlow(ExploreState())
    val state: StateFlow<ExploreState> = _state.asStateFlow()

    private var activeRecord: SnippetRecord? = null

    suspend fun beginExploration(now: Long = currentTimeMillis()) {
        mutex.withLock {
            activeRecord = null
            _state.value = _state.value.copy(phase = ExplorePhase.Loading)
        }
        val player = currentPlayer()
        val resolution = eventEngine.evaluateNextEncounter(player)
        mutex.withLock {
            when (resolution) {
                is EventResolution.Encounter -> presentSnippet(resolution.snippetId)
                is EventResolution.ChapterEvent -> presentChapter(resolution.payload)
                EventResolution.NoEvent -> presentNoEvent(now)
            }
        }
    }

    suspend fun chooseOption(optionIndex: Int, now: Long = currentTimeMillis()) {
        mutex.withLock {
            val record = activeRecord ?: run {
                _state.value = _state.value.copy(phase = ExplorePhase.Error("No active encounter to resolve."))
                return
            }
            val bundle = consequencesParser.parse(record, optionIndex)
            val autosaveTag = autosaveTag(record.snippet.id, now)
            applyRewards(bundle, autosaveTag)
            val historyEntry = toHistoryEntry(record, bundle, now, autosaveTag)
            val newHistory = _state.value.history + historyEntry
            activeRecord = null
            _state.value = ExploreState(
                phase = ExplorePhase.Resolution(
                    ResolutionSummary(
                        snippetId = record.snippet.id,
                        title = record.title,
                        choiceText = bundle.optionText,
                        rewardSummaries = historyEntry.rewardSummaries,
                        autosaveTag = autosaveTag,
                        timestampMillis = now
                    )
                ),
                history = newHistory
            )
        }
    }

    suspend fun continueAfterResolution(now: Long = currentTimeMillis()) {
        mutex.withLock {
            val current = _state.value
            val phase = current.phase
            val newHistory = when (phase) {
                is ExplorePhase.Chapter -> {
                    val autosaveTag = autosaveTag("chapter", now)
                    val summary = phase.response.worldEventSummary
                    gameStateManager.appendChoice(autosaveTag)
                    gameStateManager.appendChoice("chapter:" + phase.response.worldEventTitle.lowercase().replace("[^a-z0-9]+".toRegex(), "_").trim('_'))
                    current.history + ExploreHistoryEntry(
                        snippetId = "chapter:${phase.response.worldEventTitle}",
                        title = phase.response.worldEventTitle,
                        narratedSummary = summary,
                        choiceSummary = null,
                        rewardSummaries = phase.response.snippets.map { it.eventText },
                        autosaveTag = autosaveTag,
                        timestampMillis = now
                    )
                }
                else -> current.history
            }
            activeRecord = null
            _state.value = ExploreState(phase = ExplorePhase.Idle, history = newHistory)
        }
    }

    private fun presentSnippet(snippetId: String) {
        val record = snippetRepository.getSnippet(snippetId)
        if (record == null) {
            _state.value = _state.value.copy(
                phase = ExplorePhase.Error("Missing snippet for id=$snippetId")
            )
            return
        }
        activeRecord = record
        _state.value = _state.value.copy(phase = ExplorePhase.Encounter(record.snippet))
    }

    private fun presentChapter(response: ChapterEventResponse) {
        activeRecord = null
        _state.value = _state.value.copy(phase = ExplorePhase.Chapter(response))
    }

    private fun presentNoEvent(now: Long) {
        val autosaveTag = autosaveTag("idle", now)
        gameStateManager.appendChoice(autosaveTag)
        _state.value = _state.value.copy(
            phase = ExplorePhase.Resolution(
                ResolutionSummary(
                    snippetId = "no_event",
                    title = "Buttonburgh slumbers",
                    choiceText = null,
                    rewardSummaries = listOf("No new adventures tonight."),
                    autosaveTag = autosaveTag,
                    timestampMillis = now
                )
            )
        )
    }

    private fun applyRewards(bundle: ConsequenceBundle, autosaveTag: String) {
        bundle.choiceTags.forEach(gameStateManager::appendChoice)
        bundle.completionTag?.let(gameStateManager::appendChoice)
        bundle.statusEffects.forEach { grant ->
            gameStateManager.applyStatusEffect(grant.key, grant.duration)
        }
        gameStateManager.appendChoice(autosaveTag)
    }

    private fun toHistoryEntry(
        record: SnippetRecord,
        bundle: ConsequenceBundle,
        now: Long,
        autosaveTag: String
    ): ExploreHistoryEntry {
        val rewardSummaries = buildList {
            if (bundle.narration != null) add(bundle.narration)
            if (bundle.choiceTags.isNotEmpty()) {
                add("Choice tags: " + bundle.choiceTags.joinToString())
            }
            bundle.completionTag?.let {
                add("Milestone logged: $it")
            }
            if (bundle.statusEffects.isNotEmpty()) {
                add(
                    "Status: " + bundle.statusEffects.joinToString { grant ->
                        if (grant.duration != null) {
                            "${grant.key} (${grant.duration.inWholeMinutes} min)"
                        } else {
                            grant.key
                        }
                    }
                )
            }
            if (bundle.seedDelta > 0) {
                add("Seeds gathered: ${bundle.seedDelta}")
            }
        }
        val entry = ExploreHistoryEntry(
            snippetId = record.snippet.id,
            title = record.title,
            narratedSummary = bundle.narration ?: record.historySummary,
            choiceSummary = bundle.optionText,
            rewardSummaries = rewardSummaries,
            autosaveTag = autosaveTag,
            timestampMillis = now
        )
        return entry
    }

    private fun autosaveTag(seed: String, now: Long): String =
        "autosave:${seed.lowercase().replace("[^a-z0-9]+".toRegex(), "_").trim('_')}:$now"

    private fun currentPlayer(): Player = gameStateManager.playerState.value
}
