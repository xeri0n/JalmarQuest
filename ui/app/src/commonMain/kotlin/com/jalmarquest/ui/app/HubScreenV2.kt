package com.jalmarquest.ui.app

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jalmarquest.feature.hub.HubController
import com.jalmarquest.core.state.hoard.HoardRankManager
import com.jalmarquest.core.state.concoctions.ConcoctionCrafter
import com.jalmarquest.core.state.GameStateManager
import com.jalmarquest.core.state.catalogs.LocationCatalog
import com.jalmarquest.ui.app.navigation.Screen
import com.jalmarquest.ui.app.layout.AppSpacing

/**
 * Refactored Hub screen demonstrating Phase 5 UI/UX pattern.
 * 
 * Key improvements:
 * - Card-based layout for touch-friendly navigation
 * - Responsive spacing using AppSpacing system
 * - Screen navigation instead of bottom sheets
 * - Minimum 48dp touch targets
 * - Clear visual hierarchy
 */
@Composable
fun HubScreenV2(
    controller: HubController,
    hoardManager: HoardRankManager,
    concoctionCrafter: ConcoctionCrafter,
    questController: QuestController,
    gameStateManager: GameStateManager,
    locationCatalog: LocationCatalog,
    onNavigateToScreen: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    // Collect player state to access current location
    val player by gameStateManager.playerState.collectAsState()
    
    // Get current location details
    val currentLocationId = player.worldExploration.currentLocationId
    val currentLocation = remember(currentLocationId) {
        locationCatalog.getLocationById(currentLocationId)
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(AppSpacing.medium),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)
    ) {
        // Welcome section
        Text(
            text = "Hub - Centre of Buttonburgh",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "Your home base in the wilderness. Choose your next adventure.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(AppSpacing.small))
        
        // Quick actions grid
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)
            ) {
                HubActionCard(
                    title = "Explore",
                    description = if (currentLocation != null) {
                        "Venture around ${currentLocation.name}"
                    } else {
                        "Venture into the wilderness"
                    },
                    icon = Icons.Default.Explore,
                    onClick = { onNavigateToScreen(Screen.Explore) },
                    modifier = Modifier.weight(1f)
                )
                
                HubActionCard(
                    title = "World Map",
                    description = "Travel to distant lands",
                    icon = Icons.Default.Map,
                    onClick = { onNavigateToScreen(Screen.WorldMap) },
                    modifier = Modifier.weight(1f)
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)
            ) {
                HubActionCard(
                    title = "The Nest",
                    description = "Visit your home",
                    icon = Icons.Default.House,
                    onClick = { onNavigateToScreen(Screen.Nest) },
                    modifier = Modifier.weight(1f)
                )
                
                HubActionCard(
                    title = "Quest Log",
                    description = "Check active quests",
                    icon = Icons.Default.Assignment,
                    onClick = { onNavigateToScreen(Screen.QuestLog) },
                    modifier = Modifier.weight(1f)
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)
            ) {
                HubActionCard(
                    title = "Inventory",
                    description = "Manage items",
                    icon = Icons.Default.Inventory,
                    onClick = { onNavigateToScreen(Screen.Inventory) },
                    modifier = Modifier.weight(1f)
                )
                
                HubActionCard(
                    title = "Skills",
                    description = "Crafting & abilities",
                    icon = Icons.Default.Build,
                    onClick = { onNavigateToScreen(Screen.Skills) },
                    modifier = Modifier.weight(1f)
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)
            ) {
                HubActionCard(
                    title = "Shop",
                    description = "Purchase items",
                    icon = Icons.Default.ShoppingCart,
                    onClick = { onNavigateToScreen(Screen.Shop) },
                    modifier = Modifier.weight(1f)
                )
                
                HubActionCard(
                    title = "Activities",
                    description = "Secondary activities",
                    icon = Icons.Default.LocalActivity,
                    onClick = { onNavigateToScreen(Screen.Activities) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(AppSpacing.medium))
        
        // In-hub activities (expandable sections)
        HubActivitySection(
            title = "🏆 Hoard Status",
            icon = Icons.Default.Diamond,
            onClick = { /* Show hoard details */ }
        ) {
            // Inline hoard summary
            HoardSection(manager = hoardManager)
        }
        
        HubActivitySection(
            title = "🧪 Concoctions",
            icon = Icons.Default.Science,
            onClick = { /* Show concoctions */ }
        ) {
            // Inline concoctions summary
            ConcoctionsSection(manager = concoctionCrafter)
        }
        
        HubActivitySection(
            title = "📜 Active Quests",
            icon = Icons.Default.Assignment,
            onClick = { onNavigateToScreen(Screen.QuestLog) }
        ) {
            // Inline quest summary
            QuestSection(controller = questController)
        }
        
        // Padding for bottom nav
        Spacer(modifier = Modifier.height(AppSpacing.large))
    }
}

/**
 * Touch-friendly card for hub actions.
 * Meets WCAG 2.5.5 touch target requirements.
 */
@Composable
private fun HubActionCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.heightIn(min = 100.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppSpacing.medium),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * Expandable section for in-hub activities.
 */
@Composable
private fun HubActivitySection(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            // Header (clickable to expand/collapse)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(AppSpacing.medium),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Expandable content
            if (expanded) {
                HorizontalDivider()
                Box(modifier = Modifier.padding(AppSpacing.medium)) {
                    content()
                }
            }
        }
    }
}
