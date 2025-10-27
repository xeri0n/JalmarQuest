package com.jalmarquest.ui.app.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.jalmarquest.core.model.*
import com.jalmarquest.core.state.shop.ShopManager
import com.jalmarquest.core.state.shop.PurchaseResult
import com.jalmarquest.core.state.shop.EquipResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Shop UI for cosmetic purchases
 * Part of Milestone 5 Phase 3: Shop & Cosmetic Storefront
 */

/**
 * Controller wrapper for ShopManager with UI-friendly async methods
 */
class ShopController(
    private val manager: ShopManager,
    private val scope: CoroutineScope
) {
    val state: StateFlow<com.jalmarquest.core.state.shop.ShopManagerState> = manager.state
    
    fun purchaseItem(itemId: ShopItemId, onResult: (PurchaseResult) -> Unit) {
        scope.launch {
            val result = manager.purchaseItem(itemId)
            onResult(result)
        }
    }
    
    fun equipCosmetic(itemId: ShopItemId, onResult: (EquipResult) -> Unit) {
        scope.launch {
            val result = manager.equipCosmetic(itemId)
            onResult(result)
        }
    }
    
    fun unequipCosmetic(type: CosmeticType, onResult: (EquipResult) -> Unit) {
        scope.launch {
            val result = manager.unequipCosmetic(type)
            onResult(result)
        }
    }
    
    fun checkRotations() {
        scope.launch {
            manager.checkDailyRotation()
            manager.checkWeeklyRotation()
        }
    }
    
    fun getAvailableItems(): List<ShopItem> = manager.getAvailableItems()
    fun getPermanentItems(): List<ShopItem> = manager.getPermanentItems()
    fun getCurrentDailyDeals(): List<ShopItem> = manager.getCurrentDailyDeals()
    fun getCurrentWeeklyFeatured(): List<ShopItem> = manager.getCurrentWeeklyFeatured()
    fun ownsItem(itemId: ShopItemId): Boolean = manager.ownsItem(itemId)
}

/**
 * Main shop section with tabs for Permanent, Daily Deals, Weekly Featured
 */
@Composable
fun ShopSection(
    controller: ShopController,
    glimmerBalance: Int,
    modifier: Modifier = Modifier
) {
    // Check for rotation updates on load
    LaunchedEffect(Unit) {
        controller.checkRotations()
    }
    
    val shopState by controller.state.collectAsState()
    var selectedTabIndex by remember { mutableStateOf(0) }
    var purchaseResultMessage by remember { mutableStateOf<String?>(null) }
    
    Column(modifier = modifier.fillMaxSize()) {
        // Header with Glimmer balance
        GlimmerBalanceHeader(balance = glimmerBalance)
        
        // Purchase result snackbar
        purchaseResultMessage?.let { message ->
            Snackbar(
                modifier = Modifier.padding(8.dp),
                action = {
                    TextButton(onClick = { purchaseResultMessage = null }) {
                        Text("Dismiss")
                    }
                }
            ) {
                Text(message)
            }
        }
        
        // Tab row
        TabRow(selectedTabIndex = selectedTabIndex) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = { Text("Permanent") }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = { Text("Daily Deals") }
            )
            Tab(
                selected = selectedTabIndex == 2,
                onClick = { selectedTabIndex = 2 },
                text = { Text("Weekly Featured") }
            )
        }
        
        // Tab content
        when (selectedTabIndex) {
            0 -> PermanentTab(
                controller = controller,
                shopState = shopState,
                glimmerBalance = glimmerBalance,
                onPurchaseResult = { purchaseResultMessage = it }
            )
            1 -> DailyDealsTab(
                controller = controller,
                shopState = shopState,
                glimmerBalance = glimmerBalance,
                onPurchaseResult = { purchaseResultMessage = it }
            )
            2 -> WeeklyFeaturedTab(
                controller = controller,
                shopState = shopState,
                glimmerBalance = glimmerBalance,
                onPurchaseResult = { purchaseResultMessage = it }
            )
        }
    }
}

@Composable
private fun GlimmerBalanceHeader(balance: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Your Glimmer Balance",
                style = MaterialTheme.typography.titleMedium
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "Glimmer",
                    tint = Color(0xFFFFD700)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = balance.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color(0xFFFFD700)
                )
            }
        }
    }
}

@Composable
private fun PermanentTab(
    controller: ShopController,
    shopState: com.jalmarquest.core.state.shop.ShopManagerState,
    glimmerBalance: Int,
    onPurchaseResult: (String) -> Unit
) {
    val permanentItems = controller.getPermanentItems()
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(permanentItems) { item ->
            ShopItemCard(
                item = item,
                owned = shopState.shopState.ownsItem(item.id),
                glimmerBalance = glimmerBalance,
                onPurchase = { itemId ->
                    controller.purchaseItem(itemId) { result ->
                        onPurchaseResult(formatPurchaseResult(result))
                    }
                }
            )
        }
    }
}

@Composable
private fun DailyDealsTab(
    controller: ShopController,
    shopState: com.jalmarquest.core.state.shop.ShopManagerState,
    glimmerBalance: Int,
    onPurchaseResult: (String) -> Unit
) {
    val dailyDeals = controller.getCurrentDailyDeals()
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Today's Special Offers",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Resets in 24 hours",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        items(dailyDeals) { item ->
            ShopItemCard(
                item = item,
                owned = shopState.shopState.ownsItem(item.id),
                glimmerBalance = glimmerBalance,
                onPurchase = { itemId ->
                    controller.purchaseItem(itemId) { result ->
                        onPurchaseResult(formatPurchaseResult(result))
                    }
                },
                badge = "DAILY DEAL"
            )
        }
    }
}

@Composable
private fun WeeklyFeaturedTab(
    controller: ShopController,
    shopState: com.jalmarquest.core.state.shop.ShopManagerState,
    glimmerBalance: Int,
    onPurchaseResult: (String) -> Unit
) {
    val weeklyFeatured = controller.getCurrentWeeklyFeatured()
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "This Week's Featured Items",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Resets in 7 days",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        items(weeklyFeatured) { item ->
            ShopItemCard(
                item = item,
                owned = shopState.shopState.ownsItem(item.id),
                glimmerBalance = glimmerBalance,
                onPurchase = { itemId ->
                    controller.purchaseItem(itemId) { result ->
                        onPurchaseResult(formatPurchaseResult(result))
                    }
                },
                badge = "FEATURED"
            )
        }
    }
}

@Composable
private fun ShopItemCard(
    item: ShopItem,
    owned: Boolean,
    glimmerBalance: Int,
    onPurchase: (ShopItemId) -> Unit,
    badge: String? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (owned) 
                MaterialTheme.colorScheme.surfaceVariant 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header row with name and rarity
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    val cosmeticType = item.cosmeticType // Local variable for smart cast
                    if (cosmeticType != null) {
                        Text(
                            text = cosmeticType.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Rarity stars
                Row {
                    repeat(item.rarityTier) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Rarity",
                            modifier = Modifier.size(16.dp),
                            tint = getRarityColor(item.rarityTier)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Badge if present
            badge?.let {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            // Description
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            // Preview icon
            Icon(
                imageVector = getCosmeticIcon(item.cosmeticType),
                contentDescription = item.name,
                modifier = Modifier
                    .size(64.dp)
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 8.dp),
                tint = getRarityColor(item.rarityTier)
            )
            
            // Footer with price and purchase button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Price
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Glimmer",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.glimmerCost.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFFFD700)
                    )
                }
                
                // Purchase button or owned badge
                if (owned) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Owned",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Owned",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    Button(
                        onClick = { onPurchase(item.id) },
                        enabled = glimmerBalance >= item.glimmerCost
                    ) {
                        Text("Purchase")
                    }
                }
            }
        }
    }
}

@Composable
private fun getCosmeticIcon(type: CosmeticType?): ImageVector {
    return when (type) {
        CosmeticType.CROWN -> Icons.Outlined.WbSunny
        CosmeticType.CLOAK -> Icons.Outlined.Bookmarks
        CosmeticType.MANTLE -> Icons.Outlined.Shield
        CosmeticType.NECKLACE -> Icons.Outlined.FavoriteBorder
        CosmeticType.AURA -> Icons.Outlined.AutoAwesome
        CosmeticType.REGALIA -> Icons.Outlined.Diamond
        null -> Icons.Outlined.ShoppingBag
    }
}

@Composable
private fun getRarityColor(tier: Int): Color {
    return when (tier) {
        1 -> Color.Gray
        2 -> Color(0xFF4CAF50) // Green
        3 -> Color(0xFF2196F3) // Blue
        4 -> Color(0xFF9C27B0) // Purple
        5 -> Color(0xFFFFD700) // Gold
        else -> Color.Gray
    }
}

private fun formatPurchaseResult(result: PurchaseResult): String {
    return when (result) {
        is PurchaseResult.Success -> "Successfully purchased ${result.item.name}!"
        is PurchaseResult.AlreadyOwned -> "You already own this item"
        is PurchaseResult.InsufficientGlimmer -> "Not enough Glimmer Shards"
        is PurchaseResult.ItemNotFound -> "Item not found"
        is PurchaseResult.NotAvailable -> "This item is not currently available"
        is PurchaseResult.WalletUnavailable -> "Wallet service unavailable"
    }
}
