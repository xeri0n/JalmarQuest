package com.jalmarquest.ui.app.sections

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jalmarquest.feature.nest.NestCustomizationController
import com.jalmarquest.feature.nest.NestTab
import com.jalmarquest.core.model.CosmeticCategory
import com.jalmarquest.ui.app.layout.AppSpacing
import com.jalmarquest.ui.app.utils.toLocalizedString
import dev.icerock.moko.resources.compose.stringResource
import com.jalmarquest.ui.app.MR

/**
 * Main screen for Nest Customization (Housing System).
 * 
 * Features:
 * - Shop: Browse and purchase cosmetics with Glimmer
 * - Edit Mode: Place/move/remove cosmetics in 10x10 grid
 * - Trophy Room: Display quest achievements
 * 
 * Follows Phase 5 UI patterns:
 * - Tab-based navigation
 * - Card-based layout
 * - Responsive spacing
 * - Touch-friendly targets (48dp minimum)
 */
@Composable
fun NestCustomizationScreen(
    controller: NestCustomizationController,
    modifier: Modifier = Modifier
) {
    val selectedTab by controller.selectedTab.collectAsState()
    val playerState by controller.playerState.collectAsState()
    val nestState = playerState.nestCustomization
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Header with Glimmer balance
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppSpacing.medium)
            ) {
                Text(
                    text = stringResource(MR.strings.hub_nest_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(AppSpacing.small))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Glimmer balance
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.tiny)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = stringResource(MR.strings.content_desc_glimmer),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = stringResource(MR.strings.nest_glimmer_balance_label, playerState.glimmerWallet.balance),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    // Edit mode toggle
                    if (selectedTab == NestTab.EDIT_MODE) {
                        TextButton(
                            onClick = { controller.toggleEditMode() }
                        ) {
                            Icon(
                                imageVector = if (nestState.editModeActive) Icons.Default.CheckCircle else Icons.Default.Edit,
                                contentDescription = if (nestState.editModeActive) stringResource(MR.strings.nest_edit_mode_done) else stringResource(MR.strings.nest_edit_mode_edit),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(AppSpacing.tiny))
                            Text(if (nestState.editModeActive) stringResource(MR.strings.nest_edit_mode_done) else stringResource(MR.strings.nest_edit_mode_edit))
                        }
                    }
                }
            }
        }
        
        // Tab navigation
        TabRow(
            selectedTabIndex = selectedTab.ordinal,
            modifier = Modifier.fillMaxWidth()
        ) {
            NestTab.values().forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { controller.selectTab(tab) },
                    text = {
                        Text(
                            text = when (tab) {
                                NestTab.SHOP -> stringResource(MR.strings.nest_tab_shop)
                                NestTab.EDIT_MODE -> stringResource(MR.strings.nest_tab_edit_mode)
                                NestTab.TROPHY_ROOM -> stringResource(MR.strings.nest_tab_trophy_room)
                            },
                            style = MaterialTheme.typography.labelLarge
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = when (tab) {
                                NestTab.SHOP -> Icons.Default.ShoppingCart
                                NestTab.EDIT_MODE -> Icons.Default.Edit
                                NestTab.TROPHY_ROOM -> Icons.Default.EmojiEvents
                            },
                            contentDescription = null
                        )
                    }
                )
            }
        }
        
        // Tab content
        when (selectedTab) {
            NestTab.SHOP -> CosmeticShopTab(controller)
            NestTab.EDIT_MODE -> EditModeTab(controller)
            NestTab.TROPHY_ROOM -> TrophyRoomTab(controller)
        }
    }
}

@Composable
private fun CosmeticShopTab(controller: NestCustomizationController) {
    val selectedCategory by controller.selectedShopCategory.collectAsState()
    val availableCosmetics by controller.availableCosmetics.collectAsState(initial = emptyList())
    val playerState by controller.playerState.collectAsState()
    val purchaseResult by controller.purchaseResult.collectAsState()
    
    // Show purchase result snackbar
    purchaseResult?.let { result ->
        LaunchedEffect(result) {
            // TODO: Show snackbar based on result type
            kotlinx.coroutines.delay(2000)
            controller.clearPurchaseResult()
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Category filter tabs
        ScrollableTabRow(
            selectedTabIndex = selectedCategory.ordinal,
            modifier = Modifier.fillMaxWidth(),
            edgePadding = AppSpacing.small
        ) {
            CosmeticCategory.values().forEach { category ->
                Tab(
                    selected = selectedCategory == category,
                    onClick = { controller.selectShopCategory(category) },
                    text = {
                        Text(
                            text = when (category) {
                                CosmeticCategory.THEME -> stringResource(MR.strings.nest_shop_category_themes)
                                CosmeticCategory.FURNITURE -> stringResource(MR.strings.nest_shop_category_furniture)
                                CosmeticCategory.DECORATION -> stringResource(MR.strings.nest_shop_category_decorations)
                                CosmeticCategory.FUNCTIONAL -> stringResource(MR.strings.nest_shop_category_functional)
                            },
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                )
            }
        }
        
        // Cosmetics grid
        if (availableCosmetics.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(AppSpacing.large),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Column(
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                    Text(
                        text = stringResource(MR.strings.nest_shop_all_unlocked_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = stringResource(MR.strings.nest_shop_all_unlocked_body),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(AppSpacing.medium),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
            ) {
                items(availableCosmetics.size) { index ->
                    val cosmetic = availableCosmetics[index]
                    CosmeticCard(
                        cosmetic = cosmetic,
                        currentBalance = playerState.glimmerWallet.balance,
                        onPurchase = { controller.purchaseCosmetic(cosmetic.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CosmeticCard(
    cosmetic: com.jalmarquest.core.model.CosmeticItem,
    currentBalance: Int,
    onPurchase: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.medium)
        ) {
            // Header: Name + Rarity badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    text = cosmetic.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                
                // Rarity badge
                Surface(
                    color = when (cosmetic.rarity) {
                        com.jalmarquest.core.model.CosmeticRarity.COMMON -> MaterialTheme.colorScheme.surfaceVariant
                        com.jalmarquest.core.model.CosmeticRarity.UNCOMMON -> MaterialTheme.colorScheme.tertiaryContainer
                        com.jalmarquest.core.model.CosmeticRarity.RARE -> MaterialTheme.colorScheme.primaryContainer
                        com.jalmarquest.core.model.CosmeticRarity.EPIC -> MaterialTheme.colorScheme.secondaryContainer
                        com.jalmarquest.core.model.CosmeticRarity.LEGENDARY -> MaterialTheme.colorScheme.errorContainer
                    },
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = cosmetic.rarity.toLocalizedString(),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(AppSpacing.small))
            
            // Description
            Text(
                text = cosmetic.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(AppSpacing.medium))
            
            // Footer: Cost + Purchase button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                // Cost display
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.tiny),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = stringResource(MR.strings.content_desc_glimmer),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${cosmetic.glimmerCost}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Purchase button
                val canAfford = currentBalance >= cosmetic.glimmerCost
                Button(
                    onClick = onPurchase,
                    enabled = canAfford,
                    modifier = Modifier.height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.tiny))
                    Text(if (canAfford) stringResource(MR.strings.common_purchase) else stringResource(MR.strings.common_insufficient_glimmer))
                }
            }
        }
    }
}

@Composable
private fun EditModeTab(controller: NestCustomizationController) {
    val playerState by controller.playerState.collectAsState()
    val nestState = playerState.nestCustomization
    val unplacedCosmetics by controller.unplacedCosmetics.collectAsState(initial = emptyList())
    
    if (!nestState.editModeActive) {
        // Not in edit mode - show preview
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppSpacing.large),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Column(
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
                Text(
                    text = stringResource(MR.strings.nest_edit_mode_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(MR.strings.nest_edit_mode_prompt),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.height(AppSpacing.small))
                
                // Show placed cosmetics count
                Text(
                    text = stringResource(MR.strings.nest_items_placed_count, nestState.placedCosmetics.size),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    } else {
        // Edit mode active - show grid + controls
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // TODO: Implement 10x10 grid with Canvas
            // For now, show placeholder
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(AppSpacing.medium),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text(
                    text = stringResource(MR.strings.nest_grid_editor_placeholder),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            
            // Unplaced cosmetics toolbar
            if (unplacedCosmetics.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier.padding(AppSpacing.medium)
                    ) {
                        Text(
                            text = stringResource(MR.strings.nest_unplaced_items_title),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(AppSpacing.small))
                        
                        androidx.compose.foundation.lazy.LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)
                        ) {
                            items(unplacedCosmetics.size) { index ->
                                val cosmetic = unplacedCosmetics[index]
                                Card(
                                    modifier = Modifier
                                        .width(120.dp)
                                        .height(80.dp),
                                    onClick = {
                                        // TODO: Allow placing cosmetic
                                    }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(AppSpacing.small),
                                        contentAlignment = androidx.compose.ui.Alignment.Center
                                    ) {
                                        Text(
                                            text = cosmetic.name,
                                            style = MaterialTheme.typography.labelSmall,
                                            maxLines = 2
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TrophyRoomTab(controller: NestCustomizationController) {
    val playerState by controller.playerState.collectAsState()
    val trophyDisplay = playerState.nestCustomization.trophyDisplay
    
    if (trophyDisplay.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppSpacing.large),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Column(
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
                Text(
                    text = stringResource(MR.strings.nest_trophy_empty_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(MR.strings.nest_trophy_empty_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    } else {
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(AppSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
        ) {
            items(trophyDisplay.size) { index ->
                val trophy = trophyDisplay[index]
                TrophyCard(trophy)
            }
        }
    }
}

@Composable
private fun TrophyCard(trophy: com.jalmarquest.core.model.TrophyDisplay) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.medium),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.medium)
        ) {
            // Trophy icon
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = if (trophy.placedInRoom) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
            
            // Trophy details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.tiny)
            ) {
                Text(
                    text = trophy.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = trophy.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                if (trophy.placedInRoom) {
                    Text(
                        text = stringResource(MR.strings.nest_trophy_displayed_in_room),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
