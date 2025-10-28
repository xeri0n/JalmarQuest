package com.jalmarquest.ui.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import com.jalmarquest.core.state.perf.currentTimeMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jalmarquest.core.model.*
import com.jalmarquest.core.state.concoctions.ConcoctionCrafter
import com.jalmarquest.core.state.concoctions.ConcoctionViewState
import com.jalmarquest.core.state.concoctions.CraftResult
import com.jalmarquest.core.state.concoctions.ExperimentResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import dev.icerock.moko.resources.compose.stringResource
import com.jalmarquest.ui.app.MR

/**
 * Main Concoctions section with tabbed interface.
 */
@Composable
fun ConcoctionsSection(
    manager: ConcoctionCrafter,
    modifier: Modifier = Modifier
) {
    val viewState by manager.viewState.collectAsState()
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(ConcoctionTab.INVENTORY) }
    
    // Periodically update expired effects
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000) // Check every second
            manager.updateExpiredEffects()
        }
    }
    
    Column(modifier = modifier.fillMaxSize()) {
        // Header with active effects
        ActiveEffectsPanel(viewState = viewState)
        
        // Tab row
        TabRow(selectedTabIndex = selectedTab.ordinal) {
            ConcoctionTab.values().forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = { Text(tab.displayName) }
                )
            }
        }
        
        // Tab content
        when (selectedTab) {
            ConcoctionTab.INVENTORY -> IngredientInventoryTab(
                viewState = viewState,
                modifier = Modifier.fillMaxSize()
            )
            ConcoctionTab.RECIPES -> RecipesTab(
                viewState = viewState,
                onCraft = { recipe ->
                    scope.launch {
                        manager.craftConcoction(recipe.id)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            ConcoctionTab.BREWING -> BrewingTab(
                viewState = viewState,
                onHarvest = { location ->
                    scope.launch {
                        manager.harvestAtLocation(location)
                    }
                },
                onExperiment = { ingredients ->
                    scope.launch {
                        manager.experimentCraft(ingredients)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * Panel showing active concoctions and their effects.
 */
@Composable
private fun ActiveEffectsPanel(viewState: ConcoctionViewState) {
    if (viewState.activeConcoctions.active.isEmpty()) {
        return
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Active Concoctions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            viewState.activeConcoctions.active.forEach { active ->
                val remainingMs = active.expiresAt - currentTimeMillis()
                val remainingSeconds = (remainingMs / 1000L).coerceAtLeast(0L)
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = active.template.nameKey, // TODO: Localize
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = active.template.effects.joinToString(", ") { 
                                "${it.type.name} ${if (it.isPositive) "+" else "-"}${it.magnitude * active.stacks}%"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                    
                    Column(horizontalAlignment = Alignment.End) {
                        if (active.stacks > 1) {
                            Text(
                                text = "x${active.stacks}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = formatDuration(remainingSeconds),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (remainingSeconds < 60L) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Tab showing player's ingredient inventory.
 */
@Composable
private fun IngredientInventoryTab(
    viewState: ConcoctionViewState,
    modifier: Modifier = Modifier
) {
    if (viewState.ingredientInventory.ingredients.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No ingredients yet. Visit the Brewing tab to harvest!",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }
    
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(viewState.ingredientInventory.ingredients.entries.toList()) { (ingredientId, quantity) ->
            val ingredient = viewState.allIngredients.find { it.id == ingredientId }
            if (ingredient != null) {
                IngredientCard(
                    ingredient = ingredient,
                    quantity = quantity
                )
            }
        }
    }
}

/**
 * Card displaying an ingredient with quantity.
 */
@Composable
private fun IngredientCard(
    ingredient: Ingredient,
    quantity: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = getRarityColor(ingredient.rarity).copy(alpha = 0.1f)
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
                    text = ingredient.nameKey, // TODO: Localize
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = ingredient.descriptionKey, // TODO: Localize
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Rarity badge
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = getRarityColor(ingredient.rarity),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = ingredient.rarity.name,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color.White
                    )
                }
                
                // Properties
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    ingredient.properties.take(3).forEach { property ->
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = property.name.lowercase().capitalize(),
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
            
            // Quantity
            Text(
                text = "×$quantity",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = getRarityColor(ingredient.rarity)
            )
        }
    }
}

/**
 * Tab showing discovered recipes and crafting interface.
 */
@Composable
private fun RecipesTab(
    viewState: ConcoctionViewState,
    onCraft: (Recipe) -> Unit,
    modifier: Modifier = Modifier
) {
    if (viewState.discoveredRecipes.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No recipes discovered yet. Complete milestones or experiment in the Brewing tab!",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }
    
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(viewState.discoveredRecipes) { recipe ->
            val canCraft = viewState.craftableRecipes.contains(recipe)
            RecipeCard(
                recipe = recipe,
                canCraft = canCraft,
                onCraft = { onCraft(recipe) },
                allIngredients = viewState.allIngredients,
                playerInventory = viewState.ingredientInventory
            )
        }
    }
}

/**
 * Card displaying a recipe with crafting button.
 */
@Composable
private fun RecipeCard(
    recipe: Recipe,
    canCraft: Boolean,
    onCraft: () -> Unit,
    allIngredients: List<Ingredient>,
    playerInventory: IngredientInventory
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (canCraft) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Recipe name and description
            Text(
                text = recipe.nameKey, // TODO: Localize
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = recipe.descriptionKey, // TODO: Localize
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Required ingredients
            Text(
                text = "Required Ingredients:",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            recipe.requiredIngredients.forEach { (ingredientId, required) ->
                val ingredient = allIngredients.find { it.id == ingredientId }
                val owned = playerInventory.getQuantity(ingredientId)
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = ingredient?.nameKey ?: ingredientId.value, // TODO: Localize
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "$owned / $required",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (owned >= required) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )
                }
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Effects
            Text(
                text = "Effects:",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            
            recipe.resultingConcoction.effects.forEach { effect ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (effect.isPositive) "+" else "-",
                        color = if (effect.isPositive) {
                            Color(0xFF4CAF50)
                        } else {
                            Color(0xFFF44336)
                        },
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${effect.type.name} ${effect.magnitude}%",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Text(
                text = "Duration: ${formatDuration(recipe.resultingConcoction.durationSeconds.toLong())}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
            
            // Craft button
            Button(
                onClick = onCraft,
                enabled = canCraft,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                Text(if (canCraft) "Craft Concoction" else "Missing Ingredients")
            }
        }
    }
}

/**
 * Tab for harvesting and experimentation.
 */
@Composable
private fun BrewingTab(
    viewState: ConcoctionViewState,
    onHarvest: (String) -> Unit,
    onExperiment: (List<IngredientId>) -> Unit,
    modifier: Modifier = Modifier
) {
    val harvestLocations = listOf(
        "forest", "meadow", "cave", "swamp", "river", 
        "volcano", "mountain", "ocean", "dragon_lair", "abyss"
    )
    
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Harvesting section
        item {
            Text(
                text = "Harvest Ingredients",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Visit different locations to gather ingredients for your concoctions",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        items(harvestLocations) { location ->
            HarvestLocationCard(
                location = location,
                onHarvest = { onHarvest(location) },
                allIngredients = viewState.allIngredients
            )
        }
        
        // Experimentation section
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Experimentation",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Combine ingredients to discover new recipes (30 minute cooldown)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // TODO: Add experimentation UI with ingredient selection
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Experimentation UI Coming Soon",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
    }
}

/**
 * Card for a harvest location.
 */
@Composable
private fun HarvestLocationCard(
    location: String,
    onHarvest: () -> Unit,
    allIngredients: List<Ingredient>
) {
    val availableIngredients = allIngredients.filter { 
        it.harvestLocations.contains(location) 
    }
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = location.capitalize(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${availableIngredients.size} ingredient types available",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Button(onClick = onHarvest) {
                    Text(stringResource(MR.strings.harvest_button))
                }
            }
            
            // Show ingredient icons/badges
            if (availableIngredients.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    availableIngredients.take(5).forEach { ingredient ->
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = getRarityColor(ingredient.rarity).copy(alpha = 0.3f)
                        ) {
                            Text(
                                text = ingredient.rarity.name.first().toString(),
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    if (availableIngredients.size > 5) {
                        Text(
                            text = "+${availableIngredients.size - 5}",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Tabs for the Concoctions section.
 */
private enum class ConcoctionTab(val displayName: String) {
    INVENTORY("Inventory"),
    RECIPES("Recipes"),
    BREWING("Brewing")
}

/**
 * Get color for ingredient rarity.
 */
private fun getRarityColor(rarity: IngredientRarity): Color {
    return when (rarity) {
        IngredientRarity.COMMON -> Color(0xFF9E9E9E)
        IngredientRarity.UNCOMMON -> Color(0xFF4CAF50)
        IngredientRarity.RARE -> Color(0xFF2196F3)
        IngredientRarity.EXOTIC -> Color(0xFF9C27B0)
        IngredientRarity.LEGENDARY -> Color(0xFFFF9800)
    }
}

/**
 * Format duration in seconds to readable string.
 */
private fun formatDuration(seconds: Long): String {
    return when {
        seconds < 60 -> "${seconds}s"
        seconds < 3600 -> "${seconds / 60}m ${seconds % 60}s"
        else -> "${seconds / 3600}h ${(seconds % 3600) / 60}m"
    }
}
