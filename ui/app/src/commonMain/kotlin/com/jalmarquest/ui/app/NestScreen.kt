package com.jalmarquest.ui.app

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jalmarquest.core.model.*
import com.jalmarquest.core.state.GameStateManager
import com.jalmarquest.core.state.managers.NestCustomizationManager
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.launch

/**
 * Nest customization screen with edit mode, grid visualization, and cosmetic placement.
 * 
 * Milestone 5 Phase 6 Task 3: Interactive UI for player home personalization.
 */
@Composable
fun NestScreen(
    customizationManager: NestCustomizationManager,
    gameStateManager: GameStateManager,
    modifier: Modifier = Modifier
) {
    val player by gameStateManager.playerState.collectAsState()
    val nestState = player.nestCustomization
    val scope = rememberCoroutineScope()
    
    var editMode by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<CosmeticCategory?>(null) }
    var selectedCosmetic by remember { mutableStateOf<CosmeticItem?>(null) }
    var previewPosition by remember { mutableStateOf<Pair<Float, Float>?>(null) }
    var showPurchaseDialog by remember { mutableStateOf<CosmeticItem?>(null) }
    
    val ownedCosmetics = remember(nestState.ownedCosmetics) {
        customizationManager.getCosmeticsByCategory(CosmeticCategory.FURNITURE)
            .filter { nestState.ownsCosmetic(it.id) }
    }
    
    val availableForPurchase = remember(nestState.ownedCosmetics) {
        customizationManager.getAvailableCosmetics()
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        NestHeader(
            editMode = editMode,
            onToggleEditMode = { 
                scope.launch {
                    customizationManager.setEditMode(!editMode)
                    editMode = !editMode
                    selectedCosmetic = null
                    previewPosition = null
                }
            },
            nestState = nestState
        )
        
        Divider()
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Left Panel: Grid View
            Card(
                modifier = Modifier.weight(1f),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = if (editMode) "Edit Mode - Drag to Place" else "View Mode",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    NestGridView(
                        nestState = nestState,
                        editMode = editMode,
                        selectedCosmetic = selectedCosmetic,
                        previewPosition = previewPosition,
                        onCellClick = { x, y ->
                            if (editMode && selectedCosmetic != null) {
                                scope.launch {
                                    val result = customizationManager.placeCosmetic(
                                        cosmeticId = selectedCosmetic!!.id,
                                        x = x,
                                        y = y
                                    )
                                    if (result is PlacementResult.Success) {
                                        selectedCosmetic = null
                                        previewPosition = null
                                    }
                                }
                            }
                        },
                        onCellHover = { x, y ->
                            if (editMode && selectedCosmetic != null) {
                                previewPosition = Pair(x, y)
                            }
                        },
                        onRemoveCosmetic = { instanceId ->
                            scope.launch {
                                customizationManager.removeCosmetic(instanceId)
                            }
                        }
                    )
                }
            }
            
            // Right Panel: Cosmetics List & Shop
            Card(
                modifier = Modifier.width(300.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    if (editMode) {
                        // Edit Mode: Show owned cosmetics
                        Text(
                            text = "Your Cosmetics",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        CategoryTabs(
                            selectedCategory = selectedCategory,
                            onSelectCategory = { selectedCategory = it }
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        CosmeticList(
                            cosmetics = ownedCosmetics.filter { 
                                selectedCategory == null || it.category == selectedCategory 
                            },
                            selectedCosmeticId = selectedCosmetic?.id,
                            onSelectCosmetic = { 
                                selectedCosmetic = it
                                previewPosition = null
                            },
                            isOwned = true
                        )
                    } else {
                        // View Mode: Show stats and shop
                        NestStatsPanel(
                            nestState = nestState,
                            customizationManager = customizationManager
                        )
                        
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        
                        Text(
                            text = "Shop - Available Items",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        CosmeticList(
                            cosmetics = availableForPurchase.take(5),
                            selectedCosmeticId = null,
                            onSelectCosmetic = { showPurchaseDialog = it },
                            isOwned = false
                        )
                    }
                }
            }
        }
        
        // Trophy Room Section
        TrophyRoomSection(
            nestState = nestState,
            customizationManager = customizationManager
        )
    }
    
    // Purchase Dialog
    showPurchaseDialog?.let { cosmetic ->
        PurchaseDialog(
            cosmetic = cosmetic,
            currentGlimmer = player.glimmerWallet.balance,
            onConfirm = {
                scope.launch {
                    val result = customizationManager.purchaseCosmetic(cosmetic.id)
                    if (result is CosmeticPurchaseResult.Success) {
                        showPurchaseDialog = null
                    }
                }
            },
            onDismiss = { showPurchaseDialog = null }
        )
    }
}

@Composable
private fun NestHeader(
    editMode: Boolean,
    onToggleEditMode: () -> Unit,
    nestState: NestCustomizationState
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "🏠 Your Nest",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "${nestState.ownedCosmetics.size} cosmetics owned • ${nestState.placedCosmetics.size} placed",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        
        Button(
            onClick = onToggleEditMode,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (editMode) MaterialTheme.colorScheme.primary 
                                else MaterialTheme.colorScheme.secondary
            )
        ) {
            Icon(
                imageVector = if (editMode) Icons.Filled.Check else Icons.Filled.Edit,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = if (editMode) "Done" else "Edit")
        }
    }
}

@Composable
private fun NestGridView(
    nestState: NestCustomizationState,
    editMode: Boolean,
    selectedCosmetic: CosmeticItem?,
    previewPosition: Pair<Float, Float>?,
    onCellClick: (Float, Float) -> Unit,
    onCellHover: (Float, Float) -> Unit,
    onRemoveCosmetic: (String) -> Unit
) {
    val gridSize = 10
    val cellSize = 40.dp
    
    Box(
        modifier = Modifier
            .size(cellSize * gridSize)
            .border(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(editMode) {
                    if (editMode) {
                        detectDragGestures { change, _ ->
                            val cellX = (change.position.x / size.width * gridSize).toInt()
                            val cellY = (change.position.y / size.height * gridSize).toInt()
                            if (cellX in 0 until gridSize && cellY in 0 until gridSize) {
                                onCellHover(cellX.toFloat(), cellY.toFloat())
                            }
                        }
                    }
                }
                .clickable(enabled = editMode) { /* handled below */ }
        ) {
            val cellWidth = size.width / gridSize
            val cellHeight = size.height / gridSize
            
            // Draw grid lines
            for (i in 0..gridSize) {
                // Vertical lines
                drawLine(
                    color = Color.Gray.copy(alpha = 0.3f),
                    start = Offset(i * cellWidth, 0f),
                    end = Offset(i * cellWidth, size.height),
                    strokeWidth = 1f
                )
                // Horizontal lines
                drawLine(
                    color = Color.Gray.copy(alpha = 0.3f),
                    start = Offset(0f, i * cellHeight),
                    end = Offset(size.width, i * cellHeight),
                    strokeWidth = 1f
                )
            }
            
            // Draw placed cosmetics
            nestState.placedCosmetics.forEach { placed ->
                drawRect(
                    color = Color.Blue.copy(alpha = 0.6f),
                    topLeft = Offset(placed.x * cellWidth, placed.y * cellHeight),
                    size = Size(cellWidth, cellHeight)
                )
            }
            
            // Draw preview
            previewPosition?.let { (x, y) ->
                drawRect(
                    color = Color.Green.copy(alpha = 0.4f),
                    topLeft = Offset(x * cellWidth, y * cellHeight),
                    size = Size(cellWidth, cellHeight)
                )
            }
        }
        
        // Click handler overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(editMode) {
                    if (editMode) {
                        detectDragGestures(
                            onDragEnd = {
                                previewPosition?.let { (x, y) ->
                                    onCellClick(x, y)
                                }
                            }
                        ) { change, _ ->
                            val cellX = (change.position.x / size.width * gridSize).toInt()
                            val cellY = (change.position.y / size.height * gridSize).toInt()
                            if (cellX in 0 until gridSize && cellY in 0 until gridSize) {
                                onCellHover(cellX.toFloat(), cellY.toFloat())
                            }
                        }
                    }
                }
        )
    }
}

@Composable
private fun CategoryTabs(
    selectedCategory: CosmeticCategory?,
    onSelectCategory: (CosmeticCategory?) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        FilterChip(
            selected = selectedCategory == null,
            onClick = { onSelectCategory(null) },
            label = { Text("All", style = MaterialTheme.typography.bodySmall) }
        )
        CosmeticCategory.entries.take(3).forEach { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onSelectCategory(category) },
                label = { 
                    Text(
                        category.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodySmall
                    ) 
                }
            )
        }
    }
}

@Composable
private fun CosmeticList(
    cosmetics: List<CosmeticItem>,
    selectedCosmeticId: CosmeticItemId?,
    onSelectCosmetic: (CosmeticItem) -> Unit,
    isOwned: Boolean
) {
    LazyColumn(
        modifier = Modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(cosmetics, key = { it.id.value }) { cosmetic ->
            CosmeticCard(
                cosmetic = cosmetic,
                isSelected = cosmetic.id == selectedCosmeticId,
                isOwned = isOwned,
                onClick = { onSelectCosmetic(cosmetic) }
            )
        }
    }
}

@Composable
private fun CosmeticCard(
    cosmetic: CosmeticItem,
    isSelected: Boolean,
    isOwned: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 2.dp
        )
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = cosmetic.name,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = cosmetic.category.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                
                if (!isOwned) {
                    Text(
                        text = "${cosmetic.glimmerCost} ✨",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            RarityIndicator(rarity = cosmetic.rarity)
        }
    }
}

@Composable
private fun RarityIndicator(rarity: CosmeticRarity) {
    val color = when (rarity) {
        CosmeticRarity.COMMON -> Color.Gray
        CosmeticRarity.UNCOMMON -> Color.Green
        CosmeticRarity.RARE -> Color.Blue
        CosmeticRarity.EPIC -> Color.Magenta
        CosmeticRarity.LEGENDARY -> Color(0xFFFFD700) // Gold
    }
    
    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(rarity.ordinal + 1) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

@Composable
private fun NestStatsPanel(
    nestState: NestCustomizationState,
    customizationManager: NestCustomizationManager
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Active Bonuses",
            style = MaterialTheme.typography.titleMedium
        )
        
        val hoardBonus = customizationManager.getHoardXpBonus()
        val seedBonus = customizationManager.getSeedStorageBonus()
        val thoughtSlots = customizationManager.getExtraThoughtSlots()
        val companionBonus = customizationManager.getCompanionXpBonus()
        
        if (hoardBonus > 0) {
            BonusChip(icon = "💎", text = "+${(hoardBonus * 100).toInt()}% Hoard XP")
        }
        if (seedBonus > 0) {
            BonusChip(icon = "🌱", text = "+${(seedBonus * 100).toInt()}% Seed Storage")
        }
        if (thoughtSlots > 0) {
            BonusChip(icon = "🧠", text = "+$thoughtSlots Thought Slots")
        }
        if (companionBonus > 0) {
            BonusChip(icon = "🐾", text = "+${(companionBonus * 100).toInt()}% Companion XP")
        }
        
        if (nestState.canCraftInNest()) {
            BonusChip(icon = "🔨", text = "Craft in Nest")
        }
        
        if (hoardBonus == 0f && seedBonus == 0f && thoughtSlots == 0 && 
            companionBonus == 0f && !nestState.canCraftInNest()) {
            Text(
                text = "No bonuses active. Purchase functional upgrades to boost your abilities!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun BonusChip(icon: String, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.secondaryContainer,
                MaterialTheme.shapes.small
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
private fun PurchaseDialog(
    cosmetic: CosmeticItem,
    currentGlimmer: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Purchase ${cosmetic.name}?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(cosmetic.description)
                Divider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Cost:")
                    Text("${cosmetic.glimmerCost} ✨", color = MaterialTheme.colorScheme.primary)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Your Glimmer:")
                    Text(
                        "$currentGlimmer ✨",
                        color = if (currentGlimmer >= cosmetic.glimmerCost) 
                            MaterialTheme.colorScheme.primary 
                        else MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = currentGlimmer >= cosmetic.glimmerCost
            ) {
                Text("Purchase")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Trophy Room display - shows quest achievement trophies.
 */
@Composable
fun TrophyRoomSection(
    nestState: NestCustomizationState,
    customizationManager: NestCustomizationManager,
    modifier: Modifier = Modifier
) {
    val hasTrophyRoom = nestState.hasActiveUpgrade(FunctionalUpgradeType.TROPHY_ROOM)
    val trophies = nestState.trophyDisplay
    val scope = rememberCoroutineScope()
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🏆 Trophy Room",
                    style = MaterialTheme.typography.titleMedium
                )
                
                if (!hasTrophyRoom) {
                    Text(
                        text = "Locked - Purchase Trophy Room upgrade",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))
            
            if (!hasTrophyRoom) {
                Text(
                    text = "The Trophy Room functional upgrade displays your quest achievements. Purchase and activate it to unlock this feature!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else if (trophies.isEmpty()) {
                Text(
                    text = "No trophies yet. Complete quests to earn trophies!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(trophies) { trophy ->
                        TrophyCard(
                            trophy = trophy,
                            onTogglePlacement = {
                                scope.launch {
                                    customizationManager.toggleTrophyPlacement(trophy.questId)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual trophy display card.
 */
@Composable
fun TrophyCard(
    trophy: TrophyDisplay,
    onTogglePlacement: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (trophy.placedInRoom) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "🏆 ${trophy.displayName}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = trophy.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (trophy.placedInRoom) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "✓ Displayed in room",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Button(
                onClick = onTogglePlacement,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(if (trophy.placedInRoom) "Remove" else "Display")
            }
        }
    }
}
