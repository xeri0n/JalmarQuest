package com.jalmarquest.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StatsTest {
    
    @Test
    fun testStatsCreation() {
        val stats = Stats(
            health = 100,
            maxHealth = 100,
            strength = 15,
            defense = 10,
            agility = 12,
            intelligence = 8
        )
        
        assertEquals(100, stats.health)
        assertEquals(100, stats.maxHealth)
        assertEquals(15, stats.strength)
        assertEquals(10, stats.defense)
        assertEquals(12, stats.agility)
        assertEquals(8, stats.intelligence)
    }
    
    @Test
    fun testNegativeHealthFails() {
        assertFailsWith<IllegalArgumentException> {
            Stats(
                health = -10,
                maxHealth = 100,
                strength = 10,
                defense = 10,
                agility = 10,
                intelligence = 10
            )
        }
    }
    
    @Test
    fun testZeroMaxHealthFails() {
        assertFailsWith<IllegalArgumentException> {
            Stats(
                health = 0,
                maxHealth = 0,
                strength = 10,
                defense = 10,
                agility = 10,
                intelligence = 10
            )
        }
    }
    
    @Test
    fun testHealthExceedsMaxHealthFails() {
        assertFailsWith<IllegalArgumentException> {
            Stats(
                health = 150,
                maxHealth = 100,
                strength = 10,
                defense = 10,
                agility = 10,
                intelligence = 10
            )
        }
    }
    
    @Test
    fun testNegativeStrengthFails() {
        assertFailsWith<IllegalArgumentException> {
            Stats(
                health = 100,
                maxHealth = 100,
                strength = -5,
                defense = 10,
                agility = 10,
                intelligence = 10
            )
        }
    }
    
    @Test
    fun testTakeDamage() {
        val stats = Stats(
            health = 100,
            maxHealth = 100,
            strength = 10,
            defense = 10,
            agility = 10,
            intelligence = 10
        )
        
        val damagedStats = stats.takeDamage(30)
        
        // With defense 10, damage reduction is 10/2 = 5
        // Actual damage = 30 - 5 = 25
        assertEquals(75, damagedStats.health)
    }
    
    @Test
    fun testTakeDamageWithHighDefense() {
        val stats = Stats(
            health = 100,
            maxHealth = 100,
            strength = 10,
            defense = 60,
            agility = 10,
            intelligence = 10
        )
        
        val damagedStats = stats.takeDamage(20)
        
        // With defense 60, damage reduction is 60/2 = 30
        // Actual damage = 20 - 30 = -10, but minimum is 0
        assertEquals(100, damagedStats.health)
    }
    
    @Test
    fun testTakeFatalDamage() {
        val stats = Stats(
            health = 50,
            maxHealth = 100,
            strength = 10,
            defense = 0,
            agility = 10,
            intelligence = 10
        )
        
        val damagedStats = stats.takeDamage(100)
        
        assertEquals(0, damagedStats.health)
    }
    
    @Test
    fun testHeal() {
        val stats = Stats(
            health = 50,
            maxHealth = 100,
            strength = 10,
            defense = 10,
            agility = 10,
            intelligence = 10
        )
        
        val healedStats = stats.heal(30)
        
        assertEquals(80, healedStats.health)
    }
    
    @Test
    fun testHealBeyondMaxHealth() {
        val stats = Stats(
            health = 90,
            maxHealth = 100,
            strength = 10,
            defense = 10,
            agility = 10,
            intelligence = 10
        )
        
        val healedStats = stats.heal(50)
        
        assertEquals(100, healedStats.health)
    }
    
    @Test
    fun testHealAtFullHealth() {
        val stats = Stats(
            health = 100,
            maxHealth = 100,
            strength = 10,
            defense = 10,
            agility = 10,
            intelligence = 10
        )
        
        val healedStats = stats.heal(20)
        
        assertEquals(100, healedStats.health)
    }
    
    @Test
    fun testStrengthModifier() {
        val stats = Stats(
            health = 100,
            maxHealth = 100,
            strength = 10,
            defense = 10,
            agility = 10,
            intelligence = 10
        )
        
        val modifiedStats = stats.withModifiedStat(StatModifier.StrengthBonus(5))
        
        assertEquals(15, modifiedStats.strength)
        assertEquals(stats.defense, modifiedStats.defense)
        assertEquals(stats.agility, modifiedStats.agility)
        assertEquals(stats.intelligence, modifiedStats.intelligence)
    }
    
    @Test
    fun testDefenseModifier() {
        val stats = Stats(
            health = 100,
            maxHealth = 100,
            strength = 10,
            defense = 10,
            agility = 10,
            intelligence = 10
        )
        
        val modifiedStats = stats.withModifiedStat(StatModifier.DefenseBonus(8))
        
        assertEquals(18, modifiedStats.defense)
        assertEquals(stats.strength, modifiedStats.strength)
    }
    
    @Test
    fun testAgilityModifier() {
        val stats = Stats(
            health = 100,
            maxHealth = 100,
            strength = 10,
            defense = 10,
            agility = 10,
            intelligence = 10
        )
        
        val modifiedStats = stats.withModifiedStat(StatModifier.AgilityBonus(3))
        
        assertEquals(13, modifiedStats.agility)
    }
    
    @Test
    fun testIntelligenceModifier() {
        val stats = Stats(
            health = 100,
            maxHealth = 100,
            strength = 10,
            defense = 10,
            agility = 10,
            intelligence = 10
        )
        
        val modifiedStats = stats.withModifiedStat(StatModifier.IntelligenceBonus(7))
        
        assertEquals(17, modifiedStats.intelligence)
    }
    
    @Test
    fun testMultipleModifiers() {
        val stats = Stats(
            health = 100,
            maxHealth = 100,
            strength = 10,
            defense = 10,
            agility = 10,
            intelligence = 10
        )
        
        val modifiedStats = stats
            .withModifiedStat(StatModifier.StrengthBonus(5))
            .withModifiedStat(StatModifier.DefenseBonus(3))
            .withModifiedStat(StatModifier.AgilityBonus(2))
        
        assertEquals(15, modifiedStats.strength)
        assertEquals(13, modifiedStats.defense)
        assertEquals(12, modifiedStats.agility)
    }
}
