package com.jalmarquest.ui.app.sections

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.jalmarquest.core.model.*
import com.jalmarquest.core.state.shop.EquipResult

/**
 * Cosmetic Equipment Panel
 * Allows players to equip/unequip purchased cosmetics
 * Part of Milestone 5 Phase 3: Shop & Cosmetic Storefront
 */

@Composable
fun CosmeticEquipmentPanel(
    controller: ShopController,
    modifier: Modifier = Modifier
) {
    val shopState by controller.state.collectAsState()
    val equippedCosmetics = shopState.shopState.equippedCosmetics
    var selectedSlot by remember { mutableStateOf<CosmeticType?>(null) }
    var equipResultMessage by remember { mutableStateOf<String?>(null) }
    
    Column(modifier = modifier.fillMaxSize()) {
        // Header
        Text(
            text = "Cosmetic Equipment",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(16.dp)
        )
        
        // Equip result snackbar
        equipResultMessage?.let { message ->
            Snackbar(
                modifier = Modifier.padding(8.dp),
                action = {
                    TextButton(onClick = { equipResultMessage = null }) {
                        Text("Dismiss")
                    }
                }
            ) {
                Text(message)
            }
        }
        
        // Equipment slots
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Equipped Cosmetics",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                EquipmentSlot(
                    type = CosmeticType.CROWN,
                    label = "Crown",
                    icon = Icons.Outlined.WbSunny,
                    equippedItemId = equippedCosmetics.crown,
                    onClick = { selectedSlot = CosmeticType.CROWN },
                    onUnequip = {
                        controller.unequipCosmetic(CosmeticType.CROWN) { result ->
                            equipResultMessage = formatEquipResult(result, "Crown")
                        }
                    }
                )
                
                EquipmentSlot(
                    type = CosmeticType.CLOAK,
                    label = "Cloak",
                    icon = Icons.Outlined.Bookmarks,
                    equippedItemId = equippedCosmetics.cloak,
                    onClick = { selectedSlot = CosmeticType.CLOAK },
                    onUnequip = {
                        controller.unequipCosmetic(CosmeticType.CLOAK) { result ->
                            equipResultMessage = formatEquipResult(result, "Cloak")
                        }
                    }
                )
                
                EquipmentSlot(
                    type = CosmeticType.MANTLE,
                    label = "Mantle",
                    icon = Icons.Outlined.Shield,
                    equippedItemId = equippedCosmetics.mantle,
                    onClick = { selectedSlot = CosmeticType.MANTLE },
                    onUnequip = {
                        controller.unequipCosmetic(CosmeticType.MANTLE) { result ->
                            equipResultMessage = formatEquipResult(result, "Mantle")
                        }
                    }
                )
                
                EquipmentSlot(
                    type = CosmeticType.NECKLACE,
                    label = "Necklace",
                    icon = Icons.Outlined.FavoriteBorder,
                    equippedItemId = equippedCosmetics.necklace,
                    onClick = { selectedSlot = CosmeticType.NECKLACE },
                    onUnequip = {
                        controller.unequipCosmetic(CosmeticType.NECKLACE) { result ->
                            equipResultMessage = formatEquipResult(result, "Necklace")
                        }
                    }
                )
                
                EquipmentSlot(
                    type = CosmeticType.AURA,
                    label = "Aura",
                    icon = Icons.Outlined.AutoAwesome,
                    equippedItemId = equippedCosmetics.aura,
                    onClick = { selectedSlot = CosmeticType.AURA },
                    onUnequip = {
                        controller.unequipCosmetic(CosmeticType.AURA) { result ->
                            equipResultMessage = formatEquipResult(result, "Aura")
                        }
                    }
                )
                
                EquipmentSlot(
                    type = CosmeticType.REGALIA,
                    label = "Regalia (Full Outfit)",
                    icon = Icons.Outlined.Diamond,
                    equippedItemId = equippedCosmetics.regalia,
                    onClick = { selectedSlot = CosmeticType.REGALIA },
                    onUnequip = {
                        controller.unequipCosmetic(CosmeticType.REGALIA) { result ->
                            equipResultMessage = formatEquipResult(result, "Regalia")
                        }
                    }
                )
            }
        }
        
        // Available cosmetics for selected slot
        selectedSlot?.let { slot ->
            AvailableCosmeticsForSlot(
                controller = controller,
                slot = slot,
                currentlyEquipped = when (slot) {
                    CosmeticType.CROWN -> equippedCosmetics.crown
                    CosmeticType.CLOAK -> equippedCosmetics.cloak
                    CosmeticType.MANTLE -> equippedCosmetics.mantle
                    CosmeticType.NECKLACE -> equippedCosmetics.necklace
                    CosmeticType.AURA -> equippedCosmetics.aura
                    CosmeticType.REGALIA -> equippedCosmetics.regalia
                },
                onEquip = { itemId ->
                    controller.equipCosmetic(itemId) { result ->
                        equipResultMessage = formatEquipResult(result, slot.name)
                        if (result is EquipResult.Success) {
                            selectedSlot = null // Close after successful equip
                        }
                    }
                },
                onClose = { selectedSlot = null }
            )
        }
    }
}

@Composable
private fun EquipmentSlot(
    type: CosmeticType,
    label: String,
    icon: ImageVector,
    equippedItemId: ShopItemId?,
    onClick: () -> Unit,
    onUnequip: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (equippedItemId != null)
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleSmall
                    )
                    if (equippedItemId != null) {
                        Text(
                            text = "Equipped: ${equippedItemId.value}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "No item equipped",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            if (equippedItemId != null) {
                TextButton(onClick = onUnequip) {
                    Text("Unequip")
                }
            } else {
                TextButton(onClick = onClick) {
                    Text("Equip")
                }
            }
        }
    }
}

@Composable
private fun AvailableCosmeticsForSlot(
    controller: ShopController,
    slot: CosmeticType,
    currentlyEquipped: ShopItemId?,
    onEquip: (ShopItemId) -> Unit,
    onClose: () -> Unit
) {
    val shopState by controller.state.collectAsState()
    val allItems = controller.getAvailableItems()
    val ownedItems = shopState.shopState.purchasedItems
    
    // Filter to owned cosmetics of the correct type
    val availableCosmetics = allItems.filter { item ->
        item.cosmeticType == slot && ownedItems.contains(item.id)
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Select ${slot.name.lowercase().replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = onClose) {
                    Icon(Icons.Outlined.Close, contentDescription = "Close")
                }
            }
            
            if (availableCosmetics.isEmpty()) {
                Text(
                    text = "You don't own any ${slot.name.lowercase()} cosmetics yet. Visit the shop to purchase some!",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableCosmetics) { item ->
                        CosmeticItem(
                            item = item,
                            isEquipped = item.id == currentlyEquipped,
                            onEquip = { onEquip(item.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CosmeticItem(
    item: ShopItem,
    isEquipped: Boolean,
    onEquip: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isEquipped)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = getCosmeticIconForItem(item.cosmeticType),
                    contentDescription = item.name,
                    modifier = Modifier.size(40.dp),
                    tint = getRarityColorForItem(item.rarityTier)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }
            
            if (isEquipped) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Equipped",
                    tint = MaterialTheme.colorScheme.primary
                )
            } else {
                Button(onClick = onEquip) {
                    Text("Equip")
                }
            }
        }
    }
}

@Composable
private fun getCosmeticIconForItem(type: CosmeticType?): ImageVector {
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
private fun getRarityColorForItem(tier: Int): androidx.compose.ui.graphics.Color {
    return when (tier) {
        1 -> androidx.compose.ui.graphics.Color.Gray
        2 -> androidx.compose.ui.graphics.Color(0xFF4CAF50) // Green
        3 -> androidx.compose.ui.graphics.Color(0xFF2196F3) // Blue
        4 -> androidx.compose.ui.graphics.Color(0xFF9C27B0) // Purple
        5 -> androidx.compose.ui.graphics.Color(0xFFFFD700) // Gold
        else -> androidx.compose.ui.graphics.Color.Gray
    }
}

private fun formatEquipResult(result: EquipResult, slotName: String): String {
    return when (result) {
        is EquipResult.Success -> "Successfully equipped $slotName!"
        is EquipResult.NotOwned -> "You don't own this item"
        is EquipResult.ItemNotFound -> "Item not found"
        is EquipResult.NotCosmetic -> "This item is not a cosmetic"
    }
}
