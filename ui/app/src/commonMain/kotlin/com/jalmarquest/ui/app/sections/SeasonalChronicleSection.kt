package com.jalmarquest.ui.app.sections

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jalmarquest.core.model.*
import com.jalmarquest.core.state.battlepass.*
import dev.icerock.moko.resources.compose.stringResource
import com.jalmarquest.ui.app.MR

/**
 * Controller for Seasonal Chronicle battle pass UI.
 */
class SeasonalChronicleController(
    private val manager: SeasonalChronicleManager
) {
    val currentSeason: Season? get() = manager.getCurrentSeason()
    val currentProgress: SeasonProgress? get() = manager.getCurrentProgress()
    
    suspend fun purchasePremium(): PremiumPurchaseResult {
        return manager.purchasePremiumTrack()
    }
    
    suspend fun claimFreeReward(tierNumber: Int): ClaimResult {
        return manager.claimFreeReward(tierNumber)
    }
    
    suspend fun claimPremiumReward(tierNumber: Int): ClaimResult {
        return manager.claimPremiumReward(tierNumber)
    }
}

/**
 * Main Seasonal Chronicle section with tabs for Progress, Rewards, and Objectives.
 */
@Composable
fun SeasonalChronicleSection(
    controller: SeasonalChronicleController,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Progress", "Rewards", "Objectives")
    
    val season = controller.currentSeason
    val progress = controller.currentProgress
    
    Column(modifier = modifier.fillMaxSize()) {
        // Season header
        season?.let {
            SeasonHeader(season = it, progress = progress)
        }
        
        // Tab row
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        
        // Tab content
        when (selectedTab) {
            0 -> ProgressTab(season = season, progress = progress, controller = controller)
            1 -> RewardsTab(season = season, progress = progress, controller = controller)
            2 -> ObjectivesTab(season = season, progress = progress)
        }
    }
}

@Composable
private fun SeasonHeader(
    season: Season,
    progress: SeasonProgress?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = season.name,
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = season.description,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            progress?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Current Tier: ${it.currentTier} / 50",
                    style = MaterialTheme.typography.titleMedium
                )
                LinearProgressIndicator(
                    progress = it.currentTier / 50f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
                
                if (it.hasPremiumTrack) {
                    Text(
                        text = "✨ Premium Track Active",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressTab(
    season: Season?,
    progress: SeasonProgress?,
    controller: SeasonalChronicleController,
    modifier: Modifier = Modifier
) {
    if (season == null || progress == null) {
        NoSeasonMessage(modifier)
        return
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Premium purchase button if not owned
        if (!progress.hasPremiumTrack) {
            var purchasing by remember { mutableStateOf(false) }
            var purchaseResult by remember { mutableStateOf<PremiumPurchaseResult?>(null) }
            
            Button(
                onClick = {
                    purchasing = true
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(stringResource(MR.strings.seasonal_unlock_premium_track, season.premiumCostGlimmer))
            }
            
            if (purchasing) {
                LaunchedEffect(Unit) {
                    purchaseResult = controller.purchasePremium()
                    purchasing = false
                }
            }
            
            purchaseResult?.let { result ->
                Text(
                    text = when (result) {
                        is PremiumPurchaseResult.Success -> "✅ Premium unlocked!"
                        is PremiumPurchaseResult.InsufficientGlimmer -> 
                            "❌ Need ${result.required} Glimmer (have ${result.available})"
                        is PremiumPurchaseResult.AlreadyOwned -> "Already own premium"
                        is PremiumPurchaseResult.NoActiveSeason -> "No active season"
                        is PremiumPurchaseResult.NoWalletManager -> "Wallet unavailable"
                        is PremiumPurchaseResult.Error -> "Error: ${result.message}"
                    },
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Tier grid visualization
        Text(
            text = "Season Tiers",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        LazyColumn {
            items(season.tiers.chunked(10)) { tierRow ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    tierRow.forEach { tier ->
                        TierIndicator(
                            tier = tier,
                            isUnlocked = progress.currentTier >= tier.tierNumber,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TierIndicator(
    tier: SeasonTier,
    isUnlocked: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.aspectRatio(1f),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = tier.tierNumber.toString(),
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun RewardsTab(
    season: Season?,
    progress: SeasonProgress?,
    controller: SeasonalChronicleController,
    modifier: Modifier = Modifier
) {
    if (season == null || progress == null) {
        NoSeasonMessage(modifier)
        return
    }
    
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(season.tiers) { tier ->
            TierRewardCard(
                tier = tier,
                progress = progress,
                controller = controller
            )
        }
    }
}

@Composable
private fun TierRewardCard(
    tier: SeasonTier,
    progress: SeasonProgress,
    controller: SeasonalChronicleController
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "Tier ${tier.tierNumber}",
                style = MaterialTheme.typography.titleMedium
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Free reward
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Free",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    tier.freeReward?.let { reward ->
                        RewardDisplay(
                            reward = reward,
                            canClaim = progress.currentTier >= tier.tierNumber && 
                                !progress.isFreeRewardClaimed(tier.tierNumber),
                            onClaim = {
                                // Claim logic would go here
                            }
                        )
                    } ?: Text("No reward", style = MaterialTheme.typography.bodySmall)
                }
                
                // Premium reward
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Premium",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    tier.premiumReward?.let { reward ->
                        RewardDisplay(
                            reward = reward,
                            canClaim = progress.hasPremiumTrack && 
                                progress.currentTier >= tier.tierNumber && 
                                !progress.isPremiumRewardClaimed(tier.tierNumber),
                            onClaim = {
                                // Claim logic would go here
                            }
                        )
                    } ?: Text("No reward", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun RewardDisplay(
    reward: SeasonReward,
    canClaim: Boolean,
    onClaim: () -> Unit
) {
    Column {
        Text(
            text = reward.displayName,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = reward.description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        if (canClaim) {
            Button(
                onClick = onClaim,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(stringResource(MR.strings.seasonal_claim_button))
            }
        }
    }
}

@Composable
private fun ObjectivesTab(
    season: Season?,
    progress: SeasonProgress?,
    modifier: Modifier = Modifier
) {
    if (season == null || progress == null) {
        NoSeasonMessage(modifier)
        return
    }
    
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Daily objectives
        item {
            Text(
                text = "Daily Objectives",
                style = MaterialTheme.typography.titleLarge
            )
        }
        
        items(season.objectives.filter { it.frequency == ObjectiveFrequency.DAILY }) { objective ->
            ObjectiveCard(objective = objective, progress = progress)
        }
        
        // Weekly objectives
        item {
            Text(
                text = "Weekly Objectives",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
        
        items(season.objectives.filter { it.frequency == ObjectiveFrequency.WEEKLY }) { objective ->
            ObjectiveCard(objective = objective, progress = progress)
        }
        
        // Seasonal objectives
        item {
            Text(
                text = "Seasonal Objectives",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
        
        items(season.objectives.filter { it.frequency == ObjectiveFrequency.SEASONAL }) { objective ->
            ObjectiveCard(objective = objective, progress = progress)
        }
    }
}

@Composable
private fun ObjectiveCard(
    objective: SeasonObjective,
    progress: SeasonProgress
) {
    val objectiveProgress = progress.objectiveProgress[objective.objectiveId]
    val currentValue = objectiveProgress?.currentValue ?: 0
    val isComplete = objectiveProgress?.completed ?: false
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = objective.description,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = if (isComplete) "✓" else "${objective.xpReward} XP",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isComplete) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            LinearProgressIndicator(
                progress = (currentValue.toFloat() / objective.targetValue).coerceIn(0f, 1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
            
            Text(
                text = "$currentValue / ${objective.targetValue}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun NoSeasonMessage(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No active season",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
