package com.jalmarquest.core.state.battlepass

import com.jalmarquest.core.model.*
import com.jalmarquest.core.state.GameStateManager
import com.jalmarquest.core.state.monetization.GlimmerWalletManager
import kotlinx.coroutines.test.runTest
import kotlin.test.*

/**
 * Unit tests for Seasonal Chronicle battle pass system.
 * 
 * NOTE: These are minimal smoke tests. Full comprehensive tests should be added
 * once the SeasonalChronicle models are fully integrated into the Player model.
 */
class SeasonalChronicleManagerTest {
    
    private var currentTime = 1704067200000L // 2024-01-01 00:00:00
    
    @Test
    fun `season catalog registers and retrieves seasons`() {
        val catalog = SeasonCatalog()
        val season = Season(
            seasonId = SeasonId("test"),
            seasonNumber = 1,
            name = "Test Season",
            description = "Test",
            startTimestamp = currentTime,
            endTimestamp = currentTime + 30L * 24 * 60 * 60 * 1000,
            tiers = listOf(
                SeasonTier(
                    tierId = TierId("t1"),
                    tierNumber = 1,
                    xpRequired = 100,
                    freeReward = SeasonReward(
                        type = SeasonRewardType.SEEDS,
                        quantity = 100,
                        displayName = "Seeds",
                        description = "Test reward"
                    ),
                    premiumReward = null
                )
            ),
            objectives = listOf(
                SeasonObjective(
                    objectiveId = "obj1",
                    type = SeasonObjectiveType.COMPLETE_DAILY_QUESTS,
                    description = "Complete quests",
                    frequency = ObjectiveFrequency.DAILY,
                    xpReward = 50
                )
            )
        )
        
        catalog.registerSeason(season)
        val retrieved = catalog.getSeason(SeasonId("test"))
        
        assertNotNull(retrieved)
        assertEquals("test", retrieved.seasonId.value)
        assertEquals(1, retrieved.seasonNumber)
    }
    
    @Test
    fun `season progress tracks XP correctly`() {
        val season = createTestSeason()
        val progress = SeasonProgress(seasonId = SeasonId("test"))
        
        val updated = progress.addXp(150, season)
        
        assertEquals(150, updated.currentXp)
        assertTrue(updated.currentTier > 0)
    }
    
    @Test
    fun `season reward validation requires positive quantity`() {
        assertFailsWith<IllegalArgumentException> {
            SeasonReward(
                type = SeasonRewardType.SEEDS,
                quantity = 0,
                displayName = "Invalid",
                description = "Invalid reward"
            )
        }
    }
    
    @Test
    fun `season tier validation requires at least one reward`() {
        assertFailsWith<IllegalArgumentException> {
            SeasonTier(
                tierId = TierId("t1"),
                tierNumber = 1,
                xpRequired = 100,
                freeReward = null,
                premiumReward = null
            )
        }
    }
    
    private fun createTestSeason(): Season {
        return Season(
            seasonId = SeasonId("test"),
            seasonNumber = 1,
            name = "Test",
            description = "Test Season",
            startTimestamp = currentTime,
            endTimestamp = currentTime + 30L * 24 * 60 * 60 * 1000,
            tiers = List(10) { i ->
                SeasonTier(
                    tierId = TierId("t${i+1}"),
                    tierNumber = i + 1,
                    xpRequired = 100,
                    freeReward = SeasonReward(
                        type = SeasonRewardType.SEEDS,
                        quantity = 100,
                        displayName = "Seeds",
                        description = "Test"
                    ),
                    premiumReward = SeasonReward(
                        type = SeasonRewardType.GLIMMER_SHARDS,
                        quantity = 50,
                        displayName = "Glimmer",
                        description = "Test"
                    )
                )
            },
            objectives = listOf(
                SeasonObjective(
                    objectiveId = "obj1",
                    type = SeasonObjectiveType.COMPLETE_DAILY_QUESTS,
                    description = "Complete quests",
                    frequency = ObjectiveFrequency.DAILY,
                    xpReward = 50
                )
            )
        )
    }
}
