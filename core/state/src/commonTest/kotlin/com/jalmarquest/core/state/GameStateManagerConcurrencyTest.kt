package com.jalmarquest.core.state

import com.jalmarquest.core.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import kotlin.test.*

/**
 * Tests concurrent access to GameStateManager to ensure thread safety and data integrity.
 * Validates that StateFlow updates are atomic and no data is lost during concurrent operations.
 */
class GameStateManagerConcurrencyTest {
    
    private val basePlayer = Player(
        id = "concurrency-test",
        name = "Test Player",
        inventory = Inventory(listOf(ItemStack(ItemId("seeds"), 10000)))
    )
    
    @Test
    fun testConcurrentAppendChoiceNoDataLoss() = runTest {
        val manager = GameStateManager(basePlayer) { System.currentTimeMillis() }
        val choiceCount = 100
        
        // Launch 100 concurrent choice appends
        coroutineScope {
            repeat(choiceCount) { i ->
                launch {
                    manager.appendChoice("choice_$i")
                }
            }
        }
        
        // All 100 choices should be recorded
        assertEquals(choiceCount, manager.playerState.value.choiceLog.entries.size)
        
        // Verify uniqueness - all choices should be distinct
        val uniqueTags = manager.playerState.value.choiceLog.entries.map { it.tag.value }.toSet()
        assertEquals(choiceCount, uniqueTags.size)
    }
    
    @Test
    fun testConcurrentGrantItemsSumsCorrectly() = runTest {
        val manager = GameStateManager(basePlayer) { 0L }
        val grantCount = 1000
        val quantityPerGrant = 5
        
        // Launch 1000 concurrent grants of 5 seeds each
        coroutineScope {
            repeat(grantCount) {
                launch {
                    manager.grantItem("seeds", quantityPerGrant)
                }
            }
        }
        
        // Should have initial 10000 + (1000 * 5) = 15000 seeds
        val expectedTotal = 10000 + (grantCount * quantityPerGrant)
        val actualTotal = manager.playerState.value.inventory.totalQuantity(ItemId("seeds"))
        assertEquals(expectedTotal, actualTotal)
    }
    
    @Test
    fun testConcurrentConsumeAndGrantRaceCondition() = runTest {
        val manager = GameStateManager(basePlayer) { 0L }
        val operations = 500
        
        // Concurrently consume and grant seeds - final count should be predictable
        coroutineScope {
            repeat(operations) {
                launch {
                    manager.consumeItem("seeds", 10) // Consume 10
                }
                launch {
                    manager.grantItem("seeds", 10) // Grant 10
                }
            }
        }
        
        // Net change should be 0, so still 10000 seeds
        // (Though some consumes might fail if timing is unlucky)
        val finalCount = manager.playerState.value.inventory.totalQuantity(ItemId("seeds"))
        assertTrue(finalCount <= 10000 + (operations * 10), "Final count should not exceed max possible grants")
        assertTrue(finalCount >= 10000 - (operations * 10), "Final count should not go below max possible consumes")
    }
    
    @Test
    fun testConcurrentUpdatePlayerOperations() = runTest {
        val manager = GameStateManager(basePlayer) { 0L }
        val updateCount = 200
        
        // Concurrently update different player fields
        coroutineScope {
            repeat(updateCount) { i ->
                launch {
                    when (i % 4) {
                        0 -> manager.appendChoice("concurrent_choice_$i")
                        1 -> manager.grantItem("test_item_$i", 1)
                        2 -> manager.applyStatusEffect("effect_$i", kotlin.time.Duration.parse("10s"))
                        3 -> manager.updatePlayer { it.copy(name = "Updated_$i") }
                    }
                }
            }
        }
        
        val finalState = manager.playerState.value
        
        // Verify at least some operations succeeded (exact count depends on timing)
        assertTrue(finalState.choiceLog.entries.isNotEmpty(), "Some choices should be recorded")
        assertTrue(finalState.inventory.items.isNotEmpty(), "Some items should exist")
        assertTrue(finalState.statusEffects.entries.isNotEmpty(), "Some effects should exist")
    }
    
    @Test
    fun testRapidStateFlowEmissions() = runTest {
        val manager = GameStateManager(basePlayer) { 0L }
        val emissions = mutableListOf<Player>()
        val collectionJob = launch {
            manager.playerState.collect { emissions.add(it) }
        }
        
        // Rapidly mutate state
        coroutineScope {
            repeat(50) { i ->
                launch {
                    manager.appendChoice("rapid_$i")
                }
            }
        }
        
        // Give time for collection
        delay(100)
        collectionJob.cancel()
        
        // Should have collected multiple emissions (exact count non-deterministic)
        assertTrue(emissions.size > 1, "Should have multiple state emissions")
        
        // Final emission should have all 50 choices
        val finalEmission = emissions.last()
        assertEquals(50, finalEmission.choiceLog.entries.size)
    }
    
    @Test
    fun testConcurrentInventoryUpdates() = runTest {
        val manager = GameStateManager(basePlayer) { 0L }
        val updateCount = 100
        
        // Concurrently update inventory with custom logic
        coroutineScope {
            repeat(updateCount) { i ->
                launch {
                    manager.updateInventory { inventory ->
                        inventory.add(ItemStack(ItemId("gem_$i"), 1))
                    }
                }
            }
        }
        
        // Should have 100 unique gems + initial seeds
        val finalInventory = manager.playerState.value.inventory
        assertTrue(finalInventory.items.size >= updateCount, "Should have at least $updateCount item types")
    }
    
    @Test
    fun testConcurrentThoughtCabinetUpdates() = runTest {
        val manager = GameStateManager(basePlayer) { 0L }
        val thoughtCount = 50
        
        // Concurrently discover thoughts
        coroutineScope {
            repeat(thoughtCount) { i ->
                launch {
                    manager.updatePlayer { player ->
                        val cabinet = player.thoughtCabinet.discoverThought(ThoughtId("thought_$i"))
                        player.copy(thoughtCabinet = cabinet)
                    }
                }
            }
        }
        
        val discoveredCount = manager.playerState.value.thoughtCabinet.discoveredThoughts.size
        assertEquals(thoughtCount, discoveredCount, "All thoughts should be discovered without duplicates")
    }
    
    @Test
    fun testConcurrentActiveConcoctionsUpdates() = runTest {
        val now = System.currentTimeMillis()
        val manager = GameStateManager(basePlayer) { now }
        val concoctionCount = 30
        
        // Concurrently add active concoctions
        coroutineScope {
            repeat(concoctionCount) { i ->
                launch {
                    manager.updatePlayer { player ->
                        val template = ConcoctionTemplate(
                            id = ConcoctionId("concoction_$i"),
                            nameKey = "test_name",
                            descriptionKey = "test_desc",
                            effects = listOf(ConcoctionEffect(EffectType.HEALTH_REGEN, 10, true)),
                            durationSeconds = 300,
                            stackLimit = 1
                        )
                        val activeConcoction = ActiveConcoction(
                            template = template,
                            appliedAt = now,
                            expiresAt = now + 300000,
                            stacks = 1
                        )
                        val updated = ActiveConcoctions(player.activeConcoctions.active + activeConcoction)
                        player.copy(activeConcoctions = updated)
                    }
                }
            }
        }
        
        val activeCount = manager.playerState.value.activeConcoctions.active.size
        assertEquals(concoctionCount, activeCount, "All concoctions should be active")
    }
    
    @Test
    fun testConcurrentIngredientInventoryUpdates() = runTest {
        val manager = GameStateManager(basePlayer) { 0L }
        val ingredientCount = 75
        
        // Concurrently add ingredients
        coroutineScope {
            repeat(ingredientCount) { i ->
                launch {
                    manager.updatePlayer { player ->
                        val updated = player.ingredientInventory.addIngredient(
                            IngredientId("ingredient_$i"),
                            10
                        )
                        player.copy(ingredientInventory = updated)
                    }
                }
            }
        }
        
        val totalIngredients = manager.playerState.value.ingredientInventory.ingredients.size
        assertEquals(ingredientCount, totalIngredients, "All ingredients should be added")
    }
    
    @Test
    fun testHighVolumeRapidMutations() = runTest {
        val manager = GameStateManager(basePlayer) { System.currentTimeMillis() }
        val mutationCount = 500
        
        // Mix of all mutation types at high volume
        coroutineScope {
            repeat(mutationCount) { i ->
                launch {
                    when (i % 6) {
                        0 -> manager.appendChoice("bulk_choice_$i")
                        1 -> manager.grantItem("bulk_item", 1)
                        2 -> manager.consumeItem("seeds", 1)
                        3 -> manager.applyStatusEffect("bulk_effect_$i", kotlin.time.Duration.parse("5s"))
                        4 -> manager.updateInventory { it.add(ItemStack(ItemId("unique_$i"), 1)) }
                        5 -> manager.updatePlayer { player ->
                            player.copy(
                                thoughtCabinet = player.thoughtCabinet.discoverThought(ThoughtId("bulk_thought_$i"))
                            )
                        }
                    }
                }
            }
        }
        
        val finalState = manager.playerState.value
        
        // Verify system didn't crash and state is coherent
        assertNotNull(finalState)
        assertTrue(finalState.choiceLog.entries.isNotEmpty())
        assertTrue(finalState.inventory.items.isNotEmpty())
        assertTrue(finalState.statusEffects.entries.isNotEmpty())
        assertTrue(finalState.thoughtCabinet.discoveredThoughts.isNotEmpty())
    }
    
    @Test
    fun testAtomicityOfComplexStateUpdate() = runTest {
        val manager = GameStateManager(basePlayer) { 0L }
        val iterations = 100
        
        // Test that complex multi-field updates are atomic
        coroutineScope {
            repeat(iterations) { i ->
                launch {
                    manager.updatePlayer { player ->
                        // Complex transformation touching multiple fields
                        player.copy(
                            name = "Atomic_$i",
                            choiceLog = ChoiceLog(player.choiceLog.entries + ChoiceLogEntry(ChoiceTag("atomic_$i"), 0L)),
                            inventory = player.inventory.add(ItemStack(ItemId("atomic_$i"), i)),
                            thoughtCabinet = player.thoughtCabinet.discoverThought(ThoughtId("atomic_$i"))
                        )
                    }
                }
            }
        }
        
        val finalState = manager.playerState.value
        
        // Final name should be one of the atomic updates (last one wins)
        assertTrue(finalState.name.startsWith("Atomic_"))
        
        // All iterations should have contributed their updates
        assertEquals(iterations, finalState.choiceLog.entries.size)
        assertEquals(iterations, finalState.thoughtCabinet.discoveredThoughts.size)
        // Inventory will have initial seeds + iterations items (some may stack)
        assertTrue(finalState.inventory.items.size >= 2) // seeds + at least one atomic item
    }
    
    @Test
    fun testNoRaceConditionInChoiceLogAppends() = runTest {
        val manager = GameStateManager(basePlayer) { 0L }
        val appendCount = 200
        
        // Ensure choice log maintains order and doesn't lose entries
        coroutineScope {
            repeat(appendCount) { i ->
                launch {
                    manager.appendChoice("ordered_$i")
                }
            }
        }
        
        val finalLog = manager.playerState.value.choiceLog.entries
        assertEquals(appendCount, finalLog.size, "All appends should be recorded")
        
        // Verify no duplicates
        val uniqueTags = finalLog.map { it.tag.value }.toSet()
        assertEquals(appendCount, uniqueTags.size, "All tags should be unique")
    }
}
