package com.jalmarquest.core.model

data class Character(
    val name: String,
    val stats: Stats,
    val inventory: MutableList<Item> = mutableListOf(),
    val equippedItems: MutableMap<Slot, Item?> = mutableMapOf(
        Slot.HEAD to null,
        Slot.BODY to null,
        Slot.WEAPON to null
    ),
    val gold: Int = 0
) {
    fun addItem(item: Item) {
        inventory.add(item)
    }
    
    fun removeItem(item: Item): Boolean {
        return inventory.remove(item)
    }
    
    fun hasItem(itemId: String): Boolean {
        return inventory.any { it.id == itemId }
    }
    
    fun getItemById(itemId: String): Item? {
        return inventory.find { it.id == itemId }
    }
    
    fun equipItem(item: Item, slot: Slot): Boolean {
        if (!hasItem(item.id)) return false
        
        // Replace any currently equipped item in this slot
        equippedItems[slot] = item
        return true
    }
    
    fun unequipItem(slot: Slot): Item? {
        val item = equippedItems[slot]
        equippedItems[slot] = null
        return item
    }
    
    fun isItemEquipped(itemId: String): Boolean {
        return equippedItems.values.any { it?.id == itemId }
    }
    
    fun getCurrentHealth(): Int = stats.health
    
    fun getMaxHealth(): Int = stats.maxHealth
    
    fun isAlive(): Boolean = stats.health > 0
    
    /**
     * Apply damage to this character.
     * @return New Character instance with reduced health
     */
    fun takeDamage(damage: Int): Character {
        return copy(stats = stats.takeDamage(damage))
    }
    
    /**
     * Heal this character.
     * @return New Character instance with increased health
     */
    fun heal(amount: Int): Character {
        return copy(stats = stats.heal(amount))
    }
}

data class Stats(
    val health: Int,
    val maxHealth: Int,
    val strength: Int,
    val defense: Int,
    val agility: Int,
    val intelligence: Int
) {
    init {
        require(health >= 0) { "Health cannot be negative" }
        require(maxHealth > 0) { "Max health must be positive" }
        require(health <= maxHealth) { "Health cannot exceed max health" }
        require(strength >= 0) { "Strength cannot be negative" }
        require(defense >= 0) { "Defense cannot be negative" }
        require(agility >= 0) { "Agility cannot be negative" }
        require(intelligence >= 0) { "Intelligence cannot be negative" }
    }
    
    /**
     * Apply damage to the character, reducing health.
     * Damage is mitigated by defense (defense / 2 reduces incoming damage).
     * @return New Stats instance with reduced health
     */
    fun takeDamage(damage: Int): Stats {
        val actualDamage = maxOf(0, damage - defense / 2)
        val newHealth = maxOf(0, health - actualDamage)
        return copy(health = newHealth)
    }
    
    fun heal(amount: Int): Stats {
        val newHealth = minOf(maxHealth, health + amount)
        return copy(health = newHealth)
    }
    
    fun withModifiedStat(modifier: StatModifier): Stats {
        return when (modifier) {
            is StatModifier.StrengthBonus -> copy(strength = strength + modifier.amount)
            is StatModifier.DefenseBonus -> copy(defense = defense + modifier.amount)
            is StatModifier.AgilityBonus -> copy(agility = agility + modifier.amount)
            is StatModifier.IntelligenceBonus -> copy(intelligence = intelligence + modifier.amount)
        }
    }
}

sealed class StatModifier {
    data class StrengthBonus(val amount: Int) : StatModifier()
    data class DefenseBonus(val amount: Int) : StatModifier()
    data class AgilityBonus(val amount: Int) : StatModifier()
    data class IntelligenceBonus(val amount: Int) : StatModifier()
}
