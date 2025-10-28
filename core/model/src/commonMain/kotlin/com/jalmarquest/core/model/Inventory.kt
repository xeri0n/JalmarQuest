package com.jalmarquest.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
@JvmInline
value class ItemId(val value: String)

@Serializable
@JvmInline
value class EnvironmentTag(val value: String)

@Serializable
@JvmInline
value class Quantity(val value: Int)

@Serializable
@JvmInline
value class SeedAmount(val value: Long)

@Serializable
@JvmInline
value class StatusKey(val value: String)

@Serializable
@JvmInline
value class InteractionId(val value: String)

@Serializable
@JvmInline
value class InteractionOptionId(val value: String)

@Serializable
@JvmInline
value class FailureReasonId(val value: String)

@Serializable
@JvmInline
value class SuccessReasonId(val value: String)

@Serializable
@JvmInline
value class NarrativeLine(val value: String)

@Serializable
@JvmInline
value class OptionTitle(val value: String)

@Serializable
@JvmInline
value class AvailabilityMessage(val value: String)

@Serializable
@JvmInline
value class ResolutionMessage(val value: String)

@Serializable
@JvmInline
value class SnippetId(val value: String)

@Serializable
data class ItemStack(
    val id: ItemId,
    val quantity: Int
) {
    init {
        require(quantity >= 0) { "Quantity cannot be negative" }
    }
}

@Serializable
data class Inventory(
    val items: Map<ItemId, Int> = emptyMap(),
    val maxCapacity: Int = 50
) {
    constructor() : this(emptyMap())

    fun totalQuantity(id: ItemId): Int = items[id] ?: 0

    fun hasQuantity(id: ItemId, required: Int): Boolean {
        require(required >= 0) { "Required quantity must be non-negative" }
        return totalQuantity(id) >= required
    }

    fun add(stack: ItemStack): Inventory {
        if (stack.quantity == 0) return this
        val updated = items.toMutableMap()
        val currentQuantity = updated[stack.id] ?: 0
        updated[stack.id] = currentQuantity + stack.quantity
        return Inventory(updated)
    }

    fun remove(id: ItemId, quantity: Int): Inventory {
        require(quantity >= 0) { "Quantity to remove must be non-negative" }
        if (quantity == 0) return this
        val updated = items.toMutableMap()
        val currentQuantity = updated[id] ?: return this
        val newQuantity = currentQuantity - quantity
        if (newQuantity <= 0) {
            updated.remove(id)
        } else {
            updated[id] = newQuantity
        }
        return Inventory(updated)
    }

    enum class SortType {
        BY_NAME,
        BY_TYPE,
        BY_RARITY,
        BY_QUANTITY,
        BY_VALUE
    }

    fun getSortedItems(sortType: SortType, itemCatalog: Map<ItemId, ItemDetails>): List<Pair<ItemId, Int>> {
        return items.entries.map { it.toPair() }.sortedBy { (itemId, quantity) ->
            val item = itemCatalog[itemId]
            when (sortType) {
                SortType.BY_NAME -> item?.name ?: itemId.value
                SortType.BY_TYPE -> item?.type?.ordinal?.toString() ?: "99"
                SortType.BY_RARITY -> item?.rarity?.ordinal?.toString() ?: "0"
                SortType.BY_QUANTITY -> (-quantity).toString()
                SortType.BY_VALUE -> (-(item?.seedValue ?: 0) * quantity).toString()
            }
        }
    }
}

@Serializable
data class ItemDetails(
    val name: String,
    val description: String,
    val type: ItemType,
    val rarity: ItemRarity,
    val seedValue: Int,
    val stackSize: Int = 99
)

@Serializable
enum class ItemType {
    CONSUMABLE,
    INGREDIENT,
    MATERIAL,
    EQUIPMENT,
    QUEST,
    COSMETIC,
    MISC
}

@Serializable
enum class ItemRarity(val colorHex: String) {
    COMMON("#808080"),
    UNCOMMON("#00FF00"),
    RARE("#0080FF"),
    EPIC("#A020F0"),
    LEGENDARY("#FF8000"),
    MYTHIC("#FF00FF")
}

/**
 * Alpha 2.3: Seed inventory for storing seeds.
 * Separate from generic inventory for easier balance and upgrade bonuses.
 */
@Serializable
data class SeedInventory(
    @SerialName("stored_seeds")
    val storedSeeds: Int = 0,

    @SerialName("max_capacity")
    val maxCapacity: Int = 1000
) {
    /**
     * Calculate effective capacity with nest upgrade bonuses.
     */
    fun getEffectiveCapacity(nestBonus: Float): Int {
        return (maxCapacity * (1f + nestBonus)).toInt()
    }

    /**
     * Check if can store more seeds.
     */
    fun canStore(amount: Int, nestBonus: Float = 0f): Boolean {
        return storedSeeds + amount <= getEffectiveCapacity(nestBonus)
    }
}

/**
 * Alpha 2.3: Crafting inventory for reagents and ingredients.
 * Distinct from generic item inventory and ingredient inventory (potions/concoctions).
 */
@Serializable
data class CraftingInventory(
    @SerialName("ingredients")
    val ingredients: Map<IngredientId, Int> = emptyMap(),

    @SerialName("known_recipes")
    val knownRecipes: Set<RecipeId> = emptySet()
) {
    /**
     * Get quantity of a specific ingredient.
     */
    fun getIngredientCount(id: IngredientId): Int {
        return ingredients[id] ?: 0
    }

    /**
     * Check if has required ingredients.
     */
    fun hasIngredients(required: Map<IngredientId, Int>): Boolean {
        return required.all { (id, count) ->
            getIngredientCount(id) >= count
        }
    }

    /**
     * Check if recipe is known.
     */
    fun knowsRecipe(recipeId: RecipeId): Boolean {
        return recipeId in knownRecipes
    }
}
