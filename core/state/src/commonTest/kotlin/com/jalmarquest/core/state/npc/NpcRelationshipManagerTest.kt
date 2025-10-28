package com.jalmarquest.core.state.npc

import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class NpcRelationshipManagerTest {
    
    private lateinit var manager: NpcRelationshipManager
    
    @BeforeTest
    fun setup() {
        manager = NpcRelationshipManager(
            initialRelationships = NpcRelationships(),
            timestampProvider = { 0L }
        )
    }
    
    @Test
    fun addAffinity_increasesAffinityByAmount() = runTest {
        // Given a new NPC relationship starting at 0
        val npcId = "npc_exhausted_coder"
        val initialAffinity = manager.getAffinity(npcId)
        assertEquals(0, initialAffinity)
        
        // When we add affinity
        manager.addAffinity(npcId, 50)
        
        // Then affinity increases by the specified amount
        val finalAffinity = manager.getAffinity(npcId)
        assertEquals(50, finalAffinity)
    }
    
    @Test
    fun addAffinity_canBeCalledMultipleTimes() = runTest {
        val npcId = "npc_pack_rat"
        
        // Add affinity in multiple steps
        manager.addAffinity(npcId, 10)
        manager.addAffinity(npcId, 20)
        manager.addAffinity(npcId, 15)
        
        // Should accumulate to total
        val finalAffinity = manager.getAffinity(npcId)
        assertEquals(45, finalAffinity)
    }
    
    @Test
    fun addAffinity_supportsNegativeValues() = runTest {
        val npcId = "npc_borken"
        
        // Start with positive affinity
        manager.addAffinity(npcId, 30)
        assertEquals(30, manager.getAffinity(npcId))
        
        // Reduce by negative amount
        manager.addAffinity(npcId, -10)
        assertEquals(20, manager.getAffinity(npcId))
    }
    
    @Test
    fun addAffinity_handlesMultipleNpcs() = runTest {
        // Different NPCs should have independent affinity
        manager.addAffinity("npc_exhausted_coder", 50)
        manager.addAffinity("npc_pack_rat", 25)
        manager.addAffinity("npc_borken", 75)
        
        assertEquals(50, manager.getAffinity("npc_exhausted_coder"))
        assertEquals(25, manager.getAffinity("npc_pack_rat"))
        assertEquals(75, manager.getAffinity("npc_borken"))
    }
}
