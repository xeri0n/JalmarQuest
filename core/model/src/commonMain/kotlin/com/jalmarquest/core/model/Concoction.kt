package com.jalmarquest.core.model

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * Represents an ingredient used in potion crafting.
 */
@Serializable
data class Ingredient(
    val id: IngredientId,
    val nameKey: String,
    val descriptionKey: String,
    val rarity: IngredientRarity,
    val harvestLocations: List<String>, // e.g., ["forest", "cave", "swamp"]
    val properties: Set<IngredientProperty> // e.g., [RESTORATIVE, TOXIC, ENERGIZING]
)

@Serializable
@JvmInline
value class IngredientId(val value: String)

/**
 * Rarity tiers for ingredients affecting spawn rates and recipe requirements.
 */
@Serializable
enum class IngredientRarity {
    COMMON,     // 50% spawn rate
    UNCOMMON,   // 30% spawn rate
    RARE,       // 15% spawn rate
    EXOTIC,     // 4% spawn rate
    LEGENDARY   // 1% spawn rate
}

/**
 * Properties that ingredients possess, used for recipe matching and effect generation.
 */
@Serializable
enum class IngredientProperty {
    RESTORATIVE,
    TOXIC,
    ENERGIZING,
    CALMING,
    FORTIFYING,
    VOLATILE,
    MYSTICAL,
    EARTHY,
    AQUATIC,
    FIERY
}

/**
 * A recipe for creating a concoction.
 */
@Serializable
data class Recipe(
    val id: RecipeId,
    val nameKey: String,
    val descriptionKey: String,
    val requiredIngredients: Map<IngredientId, Int>, // IngredientId -> quantity
    val discoveredAt: Long = 0, // Timestamp when recipe was discovered (0 = not yet discovered)
    val discoveryMethod: DiscoveryMethod = DiscoveryMethod.UNKNOWN,
    val resultingConcoction: ConcoctionTemplate
)

@Serializable
@JvmInline
value class RecipeId(val value: String)

/**
 * How a recipe can be discovered.
 */
@Serializable
enum class DiscoveryMethod {
    UNKNOWN,        // Not yet discovered
    MILESTONE,      // Unlocked via gameplay milestone
    EXPERIMENTATION, // Discovered through random crafting
    PURCHASE,       // Bought from vendor
    QUEST_REWARD,   // Received as quest reward
    COMPANION_GIFT  // Taught by companion
}

/**
 * Template for creating a concoction instance.
 */
@Serializable
data class ConcoctionTemplate(
    val id: ConcoctionId,
    val nameKey: String,
    val descriptionKey: String,
    val effects: List<ConcoctionEffect>,
    val durationSeconds: Int, // How long effects last
    val stackLimit: Int = 1   // Maximum stacks of this concoction
)

@Serializable
@JvmInline
value class ConcoctionId(val value: String)

/**
 * An active concoction instance with expiration tracking.
 */
@Serializable
data class ActiveConcoction(
    val template: ConcoctionTemplate,
    val appliedAt: Long,
    val expiresAt: Long,
    val stacks: Int = 1
)

/**
 * Effect applied by a concoction (buff or debuff).
 */
@Serializable
data class ConcoctionEffect(
    val type: EffectType,
    val magnitude: Int, // Percentage or flat value depending on type
    val isPositive: Boolean = true
)

/**
 * Types of effects that concoctions can apply.
 */
@Serializable
enum class EffectType {
    // Positive effects (buffs)
    HEALTH_REGEN,       // +magnitude HP/sec
    SEED_BOOST,         // +magnitude% Seed gain
    EXPERIENCE_BOOST,   // +magnitude% XP gain
    DAMAGE_BOOST,       // +magnitude% damage
    DEFENSE_BOOST,      // +magnitude% defense
    SPEED_BOOST,        // +magnitude% movement/action speed
    LUCK_BOOST,         // +magnitude% rare item/ingredient chance
    CLARITY,            // Reveals hidden lore snippets
    
    // Negative effects (debuffs)
    POISON,             // -magnitude HP/sec
    WEAKNESS,           // -magnitude% damage
    SLOWNESS,           // -magnitude% speed
    CONFUSION,          // Random choice selection chance
    VULNERABILITY,      // -magnitude% defense
    SEED_DRAIN,         // -magnitude Seeds/sec
    
    // Neutral/utility
    INVISIBILITY,       // Avoid certain encounters
    NIGHT_VISION,       // See in dark locations
    WATER_BREATHING     // Explore underwater areas
}

/**
 * Player's ingredient inventory.
 */
@Serializable
data class IngredientInventory(
    val ingredients: Map<IngredientId, Int> = emptyMap() // IngredientId -> quantity
) {
    fun hasIngredient(id: IngredientId, quantity: Int = 1): Boolean {
        return (ingredients[id] ?: 0) >= quantity
    }
    
    fun addIngredient(id: IngredientId, quantity: Int = 1): IngredientInventory {
        val current = ingredients[id] ?: 0
        return copy(ingredients = ingredients + (id to (current + quantity)))
    }
    
    fun removeIngredient(id: IngredientId, quantity: Int = 1): IngredientInventory {
        val current = ingredients[id] ?: 0
        val newQuantity = (current - quantity).coerceAtLeast(0)
        return if (newQuantity == 0) {
            copy(ingredients = ingredients - id)
        } else {
            copy(ingredients = ingredients + (id to newQuantity))
        }
    }
    
    fun getQuantity(id: IngredientId): Int = ingredients[id] ?: 0
}

/**
 * Player's discovered recipes collection.
 */
@Serializable
data class RecipeBook(
    val discoveredRecipes: Set<RecipeId> = emptySet(),
    val lastExperimentAt: Long = 0 // Cooldown for experimentation
) {
    fun hasRecipe(id: RecipeId): Boolean = discoveredRecipes.contains(id)
    
    fun discoverRecipe(id: RecipeId): RecipeBook {
        return copy(discoveredRecipes = discoveredRecipes + id)
    }
}

/**
 * Player's active concoctions with temporal tracking.
 */
@Serializable
data class ActiveConcoctions(
    val active: List<ActiveConcoction> = emptyList()
) {
    fun addConcoction(concoction: ActiveConcoction): ActiveConcoctions {
        // Check if same concoction type exists
        val existing = active.find { it.template.id == concoction.template.id }
        
        return if (existing != null && existing.stacks < existing.template.stackLimit) {
            // Stack it
            val updated = active.map {
                if (it.template.id == concoction.template.id) {
                    it.copy(stacks = it.stacks + 1, expiresAt = concoction.expiresAt)
                } else {
                    it
                }
            }
            copy(active = updated)
        } else if (existing == null) {
            // Add new
            copy(active = active + concoction)
        } else {
            // At stack limit, refresh duration
            val updated = active.map {
                if (it.template.id == concoction.template.id) {
                    it.copy(expiresAt = concoction.expiresAt)
                } else {
                    it
                }
            }
            copy(active = updated)
        }
    }
    
    fun removeExpired(currentTime: Long): ActiveConcoctions {
        return copy(active = active.filter { it.expiresAt > currentTime })
    }
    
    fun getActiveEffects(): List<ConcoctionEffect> {
        return active.flatMap { concoction ->
            concoction.template.effects.map { effect ->
                // Scale effect by stack count
                effect.copy(magnitude = effect.magnitude * concoction.stacks)
            }
        }
    }
}
