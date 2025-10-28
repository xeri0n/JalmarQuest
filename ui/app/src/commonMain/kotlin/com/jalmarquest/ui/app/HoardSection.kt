package com.jalmarquest.ui.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jalmarquest.core.model.HoardRankTier
import com.jalmarquest.core.model.Shiny
import com.jalmarquest.core.model.ShinyRarity
import com.jalmarquest.core.state.hoard.HoardRankManager
import com.jalmarquest.core.state.hoard.HoardViewState
import com.jalmarquest.ui.app.utils.toLocalizedString
import dev.icerock.moko.resources.compose.stringResource
import com.jalmarquest.ui.app.MR

@Composable
fun HoardSection(manager: HoardRankManager) {
    val viewState by manager.viewState.collectAsState()
    var selectedTab by remember { mutableStateOf(HoardTab.COLLECTION) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Pack Rat's Hoard",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Rank Overview Card
        HoardRankOverview(viewState)

        Spacer(modifier = Modifier.height(16.dp))

        // Tabs
        TabRow(selectedTabIndex = selectedTab.ordinal) {
            HoardTab.values().forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = { Text(tab.title) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tab Content
        when (selectedTab) {
            HoardTab.COLLECTION -> CollectionTab(viewState, manager)
            HoardTab.CATALOG -> CatalogTab(viewState, manager)
            HoardTab.RANKINGS -> RankingsTab(viewState)
        }
    }
}

@Composable
private fun HoardRankOverview(viewState: HoardViewState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hoard Rank",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = viewState.rank.tier.toLocalizedString(),
                        style = MaterialTheme.typography.headlineSmall,
                        color = getTierColor(viewState.rank.tier)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Total Value",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${viewState.rank.totalValue} Seeds",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress to next tier
            viewState.nextTierThreshold?.let { threshold ->
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Progress to next tier",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "${viewState.rank.totalValue} / $threshold",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = viewState.tierProgress,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } ?: run {
                Text(
                    text = "Maximum tier achieved!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${viewState.rank.shiniesCollected} unique Shinies collected",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun CollectionTab(viewState: HoardViewState, manager: HoardRankManager) {
    if (viewState.collection.ownedShinies.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Your hoard is empty. Visit the catalog to start collecting!",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(viewState.collection.ownedShinies.sortedByDescending { it.baseValue }) { shiny ->
                ShinyCard(shiny, owned = true, onAcquire = null)
            }
        }
    }
}

@Composable
private fun CatalogTab(viewState: HoardViewState, manager: HoardRankManager) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(viewState.catalog.sortedBy { it.rarity.ordinal }) { shiny ->
            val owned = viewState.collection.hasShiny(shiny.id)
            ShinyCard(
                shiny = shiny,
                owned = owned,
                onAcquire = if (!owned) {
                    { manager.acquireShiny(shiny.id) }
                } else null
            )
        }
    }
}

@Composable
private fun RankingsTab(viewState: HoardViewState) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Leaderboards",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Global rankings coming soon!",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 16.dp)
        )
        if (viewState.rank.rank > 0) {
            Text(
                text = "Your current rank: #${viewState.rank.rank}",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            Text(
                text = "Collect more Shinies to earn a rank!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun ShinyCard(
    shiny: Shiny,
    owned: Boolean,
    onAcquire: (() -> Unit)?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (owned) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = shiny.nameKey, // TODO: Use localization
                    style = MaterialTheme.typography.titleMedium,
                    color = if (owned) {
                        getRarityColor(shiny.rarity)
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    }
                )
                Text(
                    text = shiny.descriptionKey, // TODO: Use localization
                    style = MaterialTheme.typography.bodySmall,
                    color = if (owned) {
                        MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    },
                    modifier = Modifier.padding(top = 4.dp)
                )
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Badge {
                        Text(
                            text = shiny.rarity.toLocalizedString(),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    Text(
                        text = "${shiny.baseValue} Seeds",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (onAcquire != null) {
                Button(
                    onClick = onAcquire,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(stringResource(MR.strings.hoard_acquire))
                }
            } else if (owned) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(MR.strings.content_desc_owned),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun getTierColor(tier: HoardRankTier): androidx.compose.ui.graphics.Color {
    return when (tier) {
        HoardRankTier.SCAVENGER -> MaterialTheme.colorScheme.onPrimaryContainer
        HoardRankTier.COLLECTOR -> MaterialTheme.colorScheme.tertiary
        HoardRankTier.CURATOR -> MaterialTheme.colorScheme.primary
        HoardRankTier.MAGNATE -> MaterialTheme.colorScheme.secondary
        HoardRankTier.LEGEND -> androidx.compose.ui.graphics.Color(0xFFFFD700) // Gold
        HoardRankTier.MYTH -> androidx.compose.ui.graphics.Color(0xFFFF00FF) // Magenta
    }
}

@Composable
private fun getRarityColor(rarity: ShinyRarity): androidx.compose.ui.graphics.Color {
    return when (rarity) {
        ShinyRarity.COMMON -> MaterialTheme.colorScheme.onSurface
        ShinyRarity.UNCOMMON -> androidx.compose.ui.graphics.Color(0xFF00AA00) // Green
        ShinyRarity.RARE -> androidx.compose.ui.graphics.Color(0xFF0066CC) // Blue
        ShinyRarity.EPIC -> androidx.compose.ui.graphics.Color(0xFF9933CC) // Purple
        ShinyRarity.LEGENDARY -> androidx.compose.ui.graphics.Color(0xFFFF8800) // Orange
        ShinyRarity.MYTHIC -> androidx.compose.ui.graphics.Color(0xFFFF0066) // Pink
    }
}

private enum class HoardTab(val title: String) {
    COLLECTION("My Hoard"),
    CATALOG("Catalog"),
    RANKINGS("Rankings")
}
