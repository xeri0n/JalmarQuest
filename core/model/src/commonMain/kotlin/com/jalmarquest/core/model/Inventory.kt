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
@JvmInline
value class Inventory(
    @SerialName("items") val items: List<ItemStack>
) {
    constructor() : this(emptyList())

    fun totalQuantity(id: ItemId): Int = items.firstOrNull { it.id == id }?.quantity ?: 0

    fun hasQuantity(id: ItemId, required: Int): Boolean {
        require(required >= 0) { "Required quantity must be non-negative" }
        return totalQuantity(id) >= required
    }

    fun add(stack: ItemStack): Inventory {
        if (stack.quantity == 0) return this
        val updated = items.toMutableList()
        val index = updated.indexOfFirst { it.id == stack.id }
        if (index >= 0) {
            val existing = updated[index]
            updated[index] = existing.copy(quantity = existing.quantity + stack.quantity)
        } else {
            updated += stack
        }
        return Inventory(updated)
    }

    fun remove(id: ItemId, quantity: Int): Inventory {
        require(quantity >= 0) { "Quantity to remove must be non-negative" }
        if (quantity == 0) return this
        val updated = items.toMutableList()
        val index = updated.indexOfFirst { it.id == id }
        if (index < 0) return this
        val existing = updated[index]
        val newQuantity = existing.quantity - quantity
        if (newQuantity <= 0) {
            updated.removeAt(index)
        } else {
            updated[index] = existing.copy(quantity = newQuantity)
        }
        return Inventory(updated)
    }
}
