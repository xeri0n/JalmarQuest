package com.jalmarquest.core.state.thoughts

import com.jalmarquest.core.model.*
import com.jalmarquest.core.state.GameStateManager
import com.jalmarquest.core.state.skills.SkillManager
import com.jalmarquest.core.state.archetype.ArchetypeManager
import com.jalmarquest.core.state.perf.currentTimeMillis
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages the Thought Cabinet state machine and operations.
 * Handles thought discovery, internalization, completion, and effect application.
 * Integrates with SkillManager for Scholarship skill bonuses and ArchetypeManager for talent bonuses.
 */
class ThoughtCabinetManager(
    private val gameStateManager: GameStateManager,
    private val thoughtCatalog: ThoughtCatalogService,
    private val timestampProvider: () -> Long = { currentTimeMillis() },
    private val skillManager: SkillManager? = null,
    private val archetypeManager: ArchetypeManager? = null
) {
    
    private val _viewState = MutableStateFlow(ThoughtCabinetViewState())
    val viewState: StateFlow<ThoughtCabinetViewState> = _viewState.asStateFlow()
    
    init {
        // Subscribe to player state changes to update view
        gameStateManager.playerState.value.let { player ->
            refreshViewState(player)
        }
        
        // Auto-discover starting thoughts
        discoverStartingThoughts()
    }
    
    /**
     * Discover all starting thoughts automatically.
     */
    private fun discoverStartingThoughts() {
        val startingThoughts = thoughtCatalog.getStartingThoughts()
        startingThoughts.forEach { thought ->
            val player = gameStateManager.playerState.value
            if (!player.thoughtCabinet.hasDiscovered(thought.id)) {
                discoverThought(thought.id)
            }
        }
    }
    
    /**
     * Check for newly discoverable thoughts based on player state.
     * Call this after major state changes (choices, milestones, etc.)
     */
    fun checkForDiscoveries() {
        val player = gameStateManager.playerState.value
        val discoverableThoughts = thoughtCatalog.getDiscoverableThoughts(player)
        
        discoverableThoughts.forEach { thought ->
            discoverThought(thought.id)
        }
    }
    
    /**
     * Discover a specific thought (add to discovered list).
     */
    fun discoverThought(thoughtId: ThoughtId): DiscoveryResult {
        val player = gameStateManager.playerState.value
        val thought = thoughtCatalog.getThought(thoughtId)
            ?: return DiscoveryResult.ThoughtNotFound
        
        if (player.thoughtCabinet.hasDiscovered(thoughtId)) {
            return DiscoveryResult.AlreadyDiscovered
        }
        
        // Add to discovered thoughts
        val updatedCabinet = player.thoughtCabinet.discoverThought(thoughtId)
        gameStateManager.updatePlayer { it.copy(thoughtCabinet = updatedCabinet) }
        
        // Log discovery
        gameStateManager.appendChoice("thought_discovered_${thoughtId.value}")
        
        refreshViewState(gameStateManager.playerState.value)
        
        return DiscoveryResult.Success(thought)
    }
    
    /**
     * Begin internalizing a thought (start the timer).
     * Applies Scholarship skill bonus to internalization speed.
     */
    fun internalizeThought(thoughtId: ThoughtId): InternalizationResult {
        val player = gameStateManager.playerState.value
        val thought = thoughtCatalog.getThought(thoughtId)
            ?: return InternalizationResult.ThoughtNotFound
        
        // Validate preconditions
        if (!player.thoughtCabinet.hasDiscovered(thoughtId)) {
            return InternalizationResult.NotDiscovered
        }
        
        if (player.thoughtCabinet.isInternalized(thoughtId)) {
            return InternalizationResult.AlreadyInternalized
        }
        
        if (player.thoughtCabinet.isInternalizing(thoughtId)) {
            return InternalizationResult.AlreadyInternalizing
        }
        
        if (!player.thoughtCabinet.hasAvailableSlot()) {
            return InternalizationResult.NoAvailableSlot
        }
        
        // Check Seeds cost
        val seedsId = ItemId("seeds")
        val currentSeeds = player.inventory.totalQuantity(seedsId)
        if (currentSeeds < thought.internalizationCostSeeds) {
            return InternalizationResult.InsufficientSeeds(thought.internalizationCostSeeds, currentSeeds)
        }
        
        // Deduct Seeds if required
        var updatedPlayer = player
        if (thought.internalizationCostSeeds > 0) {
            val updatedInventory = player.inventory.remove(seedsId, thought.internalizationCostSeeds)
            updatedPlayer = updatedPlayer.copy(inventory = updatedInventory)
        }
        
        // Create internalization slot
        val now = timestampProvider()
        val durationMs = thought.internalizationTimeSeconds * 1000L
        
        // Apply internalization speed modifiers from active thoughts
        var speedModifier = getActiveEffectModifier(player, ThoughtEffectType.INTERNALIZATION_SPEED)
        
        // Add Scholarship skill bonus (reduce time by bonus percentage)
        val scholarshipBonus = skillManager?.getTotalBonus(AbilityType.INTERNALIZATION_SPEED) ?: 0
        
        // Add archetype internalization speed bonus
        val archetypeBonus = archetypeManager?.getTotalBonus(TalentType.INTERNALIZATION_SPEED_BONUS) ?: 0
        
        speedModifier += scholarshipBonus + archetypeBonus
        
        val adjustedDurationMs = (durationMs * (100 - speedModifier) / 100).toLong()
        
        val slot = ThoughtSlot(
            thoughtId = thoughtId,
            startedAt = now,
            completesAt = now + adjustedDurationMs
        )
        
        // Add slot to cabinet
        val updatedCabinet = updatedPlayer.thoughtCabinet.startInternalizing(slot)
        updatedPlayer = updatedPlayer.copy(thoughtCabinet = updatedCabinet)
        
        gameStateManager.updatePlayer { updatedPlayer }
        
        // Log internalization start
        gameStateManager.appendChoice("thought_internalizing_${thoughtId.value}")
        
        refreshViewState(gameStateManager.playerState.value)
        
        return InternalizationResult.Success(thought, slot)
    }
    
    /**
     * Check for completed internalizations and finalize them.
     * Should be called periodically (e.g., every second in UI).
     * Awards Scholarship XP for completing thoughts.
     */
    fun updateCompletedThoughts() {
        val player = gameStateManager.playerState.value
        val now = timestampProvider()
        
        val completedSlots = player.thoughtCabinet.activeSlots.filter { it.isComplete(now) }
        
        if (completedSlots.isEmpty()) {
            return
        }
        
        var updatedCabinet = player.thoughtCabinet
        
        completedSlots.forEach { slot ->
            updatedCabinet = updatedCabinet.completeInternalization(slot.thoughtId)
            
            // Log completion
            gameStateManager.appendChoice("thought_completed_${slot.thoughtId.value}")
            
            // Award Scholarship XP
            if (skillManager != null) {
                val scholarshipSkill = skillManager.getSkillByType(SkillType.SCHOLARSHIP)
                if (scholarshipSkill != null) {
                    val xpAmount = 30 // Base XP for completing a thought
                    skillManager.gainSkillXP(scholarshipSkill.id, xpAmount)
                }
            }
            
            // Check if this unlocks new milestones
            checkInternalizationMilestones(updatedCabinet)
        }
        
        gameStateManager.updatePlayer { it.copy(thoughtCabinet = updatedCabinet) }
        
        refreshViewState(gameStateManager.playerState.value)
    }
    
    /**
     * Abandon an active internalization (free the slot without completing).
     */
    fun abandonInternalization(thoughtId: ThoughtId): AbandonResult {
        val player = gameStateManager.playerState.value
        
        if (!player.thoughtCabinet.isInternalizing(thoughtId)) {
            return AbandonResult.NotInternalizing
        }
        
        val updatedCabinet = player.thoughtCabinet.abandonInternalization(thoughtId)
        gameStateManager.updatePlayer { it.copy(thoughtCabinet = updatedCabinet) }
        
        // Log abandonment
        gameStateManager.appendChoice("thought_abandoned_${thoughtId.value}")
        
        refreshViewState(gameStateManager.playerState.value)
        
        return AbandonResult.Success
    }
    
    /**
     * Forget an internalized thought (remove its effects, can re-internalize later).
     */
    fun forgetThought(thoughtId: ThoughtId): ForgetResult {
        val player = gameStateManager.playerState.value
        
        if (!player.thoughtCabinet.isInternalized(thoughtId)) {
            return ForgetResult.NotInternalized
        }
        
        val updatedCabinet = player.thoughtCabinet.forgetThought(thoughtId)
        gameStateManager.updatePlayer { it.copy(thoughtCabinet = updatedCabinet) }
        
        // Log forgetting
        gameStateManager.appendChoice("thought_forgotten_${thoughtId.value}")
        
        refreshViewState(gameStateManager.playerState.value)
        
        return ForgetResult.Success
    }
    
    /**
     * Get all active thought effects currently modifying the player.
     */
    fun getActiveEffects(player: Player): List<Pair<Thought, List<ThoughtEffect>>> {
        val effects = mutableListOf<Pair<Thought, List<ThoughtEffect>>>()
        
        // Effects from internalized thoughts (solution effects)
        player.thoughtCabinet.internalized.forEach { thoughtId ->
            val thought = thoughtCatalog.getThought(thoughtId)
            if (thought != null) {
                effects.add(thought to thought.solutionEffects)
            }
        }
        
        // Effects from actively internalizing thoughts (problem effects)
        player.thoughtCabinet.activeSlots.forEach { slot ->
            val thought = thoughtCatalog.getThought(slot.thoughtId)
            if (thought != null && thought.problemEffects.isNotEmpty()) {
                effects.add(thought to thought.problemEffects)
            }
        }
        
        return effects
    }
    
    /**
     * Get the total modifier for a specific effect type across all active thoughts.
     */
    fun getActiveEffectModifier(player: Player, effectType: ThoughtEffectType): Int {
        val allEffects = getActiveEffects(player)
        
        return allEffects.flatMap { it.second }
            .filter { it.type == effectType }
            .sumOf { it.magnitude }
    }
    
    /**
     * Check for milestones related to internalization count.
     */
    private fun checkInternalizationMilestones(cabinet: ThoughtCabinet) {
        val count = cabinet.internalized.size
        
        when (count) {
            5 -> gameStateManager.appendChoice("milestone_thoughts_internalized_5")
            10 -> gameStateManager.appendChoice("milestone_thoughts_internalized_10")
            15 -> gameStateManager.appendChoice("milestone_thoughts_internalized_15")
        }
    }
    
    /**
     * Refresh the view state based on current player state.
     */
    private fun refreshViewState(player: Player) {
        val allThoughts = thoughtCatalog.getAllThoughts()
        
        val discovered = allThoughts.filter { player.thoughtCabinet.hasDiscovered(it.id) }
        val internalized = allThoughts.filter { player.thoughtCabinet.isInternalized(it.id) }
        val internalizing = player.thoughtCabinet.activeSlots.mapNotNull { slot ->
            thoughtCatalog.getThought(slot.thoughtId)?.let { it to slot }
        }
        val available = discovered.filter { 
            !player.thoughtCabinet.isInternalized(it.id) && 
            !player.thoughtCabinet.isInternalizing(it.id)
        }
        
        val activeEffects = getActiveEffects(player)
        
        _viewState.value = ThoughtCabinetViewState(
            thoughtCabinet = player.thoughtCabinet,
            allThoughts = allThoughts,
            discoveredThoughts = discovered,
            internalizingThoughts = internalizing,
            internalizedThoughts = internalized,
            availableThoughts = available,
            activeEffects = activeEffects,
            currentSeeds = player.inventory.totalQuantity(ItemId("seeds"))
        )
    }
}

/**
 * View state for the Thought Cabinet UI.
 */
data class ThoughtCabinetViewState(
    val thoughtCabinet: ThoughtCabinet = ThoughtCabinet(),
    val allThoughts: List<Thought> = emptyList(),
    val discoveredThoughts: List<Thought> = emptyList(),
    val internalizingThoughts: List<Pair<Thought, ThoughtSlot>> = emptyList(),
    val internalizedThoughts: List<Thought> = emptyList(),
    val availableThoughts: List<Thought> = emptyList(),
    val activeEffects: List<Pair<Thought, List<ThoughtEffect>>> = emptyList(),
    val currentSeeds: Int = 0
)

/**
 * Result of attempting to discover a thought.
 */
sealed class DiscoveryResult {
    data class Success(val thought: Thought) : DiscoveryResult()
    data object ThoughtNotFound : DiscoveryResult()
    data object AlreadyDiscovered : DiscoveryResult()
}

/**
 * Result of attempting to internalize a thought.
 */
sealed class InternalizationResult {
    data class Success(val thought: Thought, val slot: ThoughtSlot) : InternalizationResult()
    data object ThoughtNotFound : InternalizationResult()
    data object NotDiscovered : InternalizationResult()
    data object AlreadyInternalized : InternalizationResult()
    data object AlreadyInternalizing : InternalizationResult()
    data object NoAvailableSlot : InternalizationResult()
    data class InsufficientSeeds(val required: Int, val current: Int) : InternalizationResult()
}

/**
 * Result of abandoning an internalization.
 */
sealed class AbandonResult {
    data object Success : AbandonResult()
    data object NotInternalizing : AbandonResult()
}

/**
 * Result of forgetting a thought.
 */
sealed class ForgetResult {
    data object Success : ForgetResult()
    data object NotInternalized : ForgetResult()
}
