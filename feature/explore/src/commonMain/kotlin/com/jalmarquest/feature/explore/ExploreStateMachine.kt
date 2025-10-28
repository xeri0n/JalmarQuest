package com.jalmarquest.feature.explore

import com.jalmarquest.core.model.ChapterEventResponse
import com.jalmarquest.core.model.Player
import com.jalmarquest.core.state.GameStateManager
import com.jalmarquest.core.state.aidirector.AIDirectorManager
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
    private val borkenEventTrigger: BorkenEventTrigger? = null, // Alpha 2.2: Chaos events
    private val aiDirectorManager: AIDirectorManager? = null, // Alpha 2.2: Fatigue tracking
    private val currentTimeMillis: () -> Long = { Clock.System.now().toEpochMilliseconds() }
) {
    private val mutex = Mutex()
    private val _state = MutableStateFlow(ExploreState())
    val state: StateFlow<ExploreState> = _state.asStateFlow()

    private var activeRecord: SnippetRecord? = null
    private var isBorkenChaosEvent = false // Track if current event is from Borken

    suspend fun beginExploration(now: Long = currentTimeMillis()) {
        mutex.withLock {
            activeRecord = null
            isBorkenChaosEvent = false
            _state.value = _state.value.copy(phase = ExplorePhase.Loading)
        }
        val player = currentPlayer()
        
        // Log current location for AI Director context
        val currentLocation = player.worldExploration.currentLocationId
        gameStateManager.appendChoice("explore_at_$currentLocation")
        
        // Alpha 2.2: Check for Borken chaos event first (10% chance)
        val chaosSnippet = borkenEventTrigger?.evaluateChaosEvent(player.choiceLog)
        if (chaosSnippet != null) {
            mutex.withLock {
                presentBorkenChaosEvent(chaosSnippet)
            }
            return
        }
        
        // Normal event flow if no chaos event
        val resolution = eventEngine.evaluateNextEncounter(player)
        mutex.withLock {
            when (resolution) {
                is EventResolution.Encounter -> presentSnippet(resolution.snippetId)
                is EventResolution.ChapterEvent -> presentChapter(resolution.payload)
                EventResolution.NoEvent -> presentNoEvent(now)
                EventResolution.RestNeeded -> presentRestNeeded(now) // Alpha 2.2: Player fatigue
            }
        }
    }
    
    /**
     * Alpha 2.2: Player rests to recover from event fatigue.
     * Resets eventsSinceRest counter in AI Director.
     */
    suspend fun rest(now: Long = currentTimeMillis()) {
        mutex.withLock {
            // Record rest in AI Director
            aiDirectorManager?.recordRest()
            
            // Log rest choice for narrative tracking
            gameStateManager.appendChoice("player_rest_${now}")
            
            // Transition to Idle state
            _state.value = _state.value.copy(phase = ExplorePhase.Idle)
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

    /**
     * Alpha 2.2: Presents a Borken chaos event as a temporary snippet encounter.
     * Creates a virtual SnippetRecord so it can be processed like normal snippets.
     */
    private fun presentBorkenChaosEvent(chaosSnippet: com.jalmarquest.core.model.LoreSnippet) {
        isBorkenChaosEvent = true
        
        // Create a virtual SnippetRecord for the chaos event
        val virtualRecord = SnippetRecord(
            snippet = chaosSnippet,
            choiceKeyByOptionIndex = mapOf(
                0 to (chaosSnippet.choiceOptions.getOrNull(0)?.take(20)?.lowercase()?.replace("[^a-z0-9]+".toRegex(), "_") ?: "option_0"),
                1 to (chaosSnippet.choiceOptions.getOrNull(1)?.take(20)?.lowercase()?.replace("[^a-z0-9]+".toRegex(), "_") ?: "option_1"),
                2 to (chaosSnippet.choiceOptions.getOrNull(2)?.take(20)?.lowercase()?.replace("[^a-z0-9]+".toRegex(), "_") ?: "option_2")
            ),
            completionTag = null, // Chaos events can repeat
            prerequisites = emptySet(),
            title = "Borken Appears!",
            historySummary = "A chaotic encounter with Borken the button quail."
        )
        
        activeRecord = virtualRecord
        _state.value = _state.value.copy(phase = ExplorePhase.Encounter(chaosSnippet))
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
    
    /**
     * Alpha 2.2: Presents rest needed state when player has experienced too many events.
     * Player needs to rest before continuing exploration.
     */
    private fun presentRestNeeded(now: Long) {
        val eventsCount = aiDirectorManager?.getEventsSinceRest() ?: 0
        _state.value = _state.value.copy(
            phase = ExplorePhase.RestNeeded(eventsCount)
        )
    }

    private fun applyRewards(bundle: ConsequenceBundle, autosaveTag: String) {
        bundle.choiceTags.forEach(gameStateManager::appendChoice)
        bundle.completionTag?.let(gameStateManager::appendChoice)
        bundle.statusEffects.forEach { grant ->
            gameStateManager.applyStatusEffect(grant.key, grant.duration)
        }
        // Items and faction reputation adjustments
        bundle.itemGrants.forEach { grant ->
            gameStateManager.grantItem(grant.itemId, grant.quantity)
        }
        bundle.factionRepChanges.forEach { change ->
            gameStateManager.updateFactionReputation(change.factionId, change.amount)
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
