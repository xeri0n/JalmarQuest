package com.jalmarquest.ui.app.examples

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jalmarquest.feature.explore.ExploreController
import com.jalmarquest.feature.worldinfo.WorldInfoController
import com.jalmarquest.ui.app.ExploreSection
import com.jalmarquest.ui.app.components.WorldInfoCard
import com.jalmarquest.ui.app.components.WorldInfoCompact

/**
 * Example integration of WorldInfoCard with ExploreSection
 * 
 * This demonstrates how to display location/weather/season information
 * alongside the exploration UI using Phase 4 managers.
 * 
 * Usage pattern:
 * ```kotlin
 * val worldInfoController = WorldInfoController(
 *     locationTracker = resolvePlayerLocationTracker(),
 *     difficultyManager = resolveRegionDifficultyManager(),
 *     weatherSystem = resolveWeatherSystem(),
 *     seasonalManager = resolveSeasonalCycleManager()
 * )
 * 
 * ExploreSectionWithWorldInfo(
 *     exploreController = exploreController,
 *     worldInfoController = worldInfoController
 * )
 * ```
 */
@Composable
fun ExploreSectionWithWorldInfo(
    exploreController: ExploreController,
    worldInfoController: WorldInfoController,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // World information at the top
        val worldState by worldInfoController.state.collectAsState()
        WorldInfoCard(state = worldState)

        // Main exploration section
        ExploreSection(controller = exploreController)
    }
}

/**
 * Compact version with WorldInfoCompact for minimal UI
 */
@Composable
fun ExploreSectionWithCompactWorldInfo(
    exploreController: ExploreController,
    worldInfoController: WorldInfoController,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Compact world info bar
        val worldState by worldInfoController.state.collectAsState()
        WorldInfoCompact(state = worldState)

        // Main exploration section
        ExploreSection(controller = exploreController)
    }
}

/**
 * Example of updating WorldInfoController when location changes
 * 
 * This would typically be called:
 * 1. After player movement (PlayerLocationTracker.moveToLocation)
 * 2. During periodic WorldUpdateCoordinator cycles
 * 3. When weather/season changes
 */
@Composable
fun WorldInfoExample(worldInfoController: WorldInfoController) {
    val worldState by worldInfoController.state.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Full card display
        WorldInfoCard(state = worldState)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Compact display
        WorldInfoCompact(state = worldState)
    }
}
