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
import dev.icerock.moko.resources.compose.stringResource
import com.jalmarquest.ui.app.MR

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
    var equipResult by remember { mutableStateOf<Pair<EquipResult, String>?>(null) }
    
    Column(modifier = modifier.fillMaxSize()) {
        // Header
        Text(
            text = stringResource(MR.strings.cosmetics_title),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(16.dp)
        )
        
        // Equip result snackbar
        equipResult?.let { (result, slotLabel) ->
            Snackbar(
                modifier = Modifier.padding(8.dp),
                action = {
                    TextButton(onClick = { equipResult = null }) {
                        Text(stringResource(MR.strings.common_dismiss))
                    }
                }
            ) {
                val msg = when (result) {
                    is EquipResult.Success -> stringResource(MR.strings.cosmetics_result_success, slotLabel)
                    is EquipResult.NotOwned -> stringResource(MR.strings.cosmetics_result_not_owned)
                    is EquipResult.ItemNotFound -> stringResource(MR.strings.cosmetics_result_item_not_found)
                    is EquipResult.NotCosmetic -> stringResource(MR.strings.cosmetics_result_not_cosmetic)
                }
                Text(msg)
            }
        }
        
        // Equipment slots
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Pre-resolve localized labels for non-composable callbacks
                val labelCrown = stringResource(MR.strings.cosmetics_slot_crown)
                val labelCloak = stringResource(MR.strings.cosmetics_slot_cloak)
                val labelMantle = stringResource(MR.strings.cosmetics_slot_mantle)
                val labelNecklace = stringResource(MR.strings.cosmetics_slot_necklace)
                val labelAura = stringResource(MR.strings.cosmetics_slot_aura)
                val labelRegalia = stringResource(MR.strings.cosmetics_slot_regalia_full)
                Text(
                    text = stringResource(MR.strings.cosmetics_equipped_section_title),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                EquipmentSlot(
                    type = CosmeticType.CROWN,
                    label = labelCrown,
                    icon = Icons.Outlined.WbSunny,
                    equippedItemId = equippedCosmetics.crown,
                    onClick = { selectedSlot = CosmeticType.CROWN },
                    onUnequip = {
                        controller.unequipCosmetic(CosmeticType.CROWN) { result ->
                            equipResult = result to labelCrown
                        }
                    }
                )
                
                EquipmentSlot(
                    type = CosmeticType.CLOAK,
                    label = labelCloak,
                    icon = Icons.Outlined.Bookmarks,
                    equippedItemId = equippedCosmetics.cloak,
                    onClick = { selectedSlot = CosmeticType.CLOAK },
                    onUnequip = {
                        controller.unequipCosmetic(CosmeticType.CLOAK) { result ->
                            equipResult = result to labelCloak
                        }
                    }
                )
                
                EquipmentSlot(
                    type = CosmeticType.MANTLE,
                    label = labelMantle,
                    icon = Icons.Outlined.Shield,
                    equippedItemId = equippedCosmetics.mantle,
                    onClick = { selectedSlot = CosmeticType.MANTLE },
                    onUnequip = {
                        controller.unequipCosmetic(CosmeticType.MANTLE) { result ->
                            equipResult = result to labelMantle
                        }
                    }
                )
                
                EquipmentSlot(
                    type = CosmeticType.NECKLACE,
                    label = labelNecklace,
                    icon = Icons.Outlined.FavoriteBorder,
                    equippedItemId = equippedCosmetics.necklace,
                    onClick = { selectedSlot = CosmeticType.NECKLACE },
                    onUnequip = {
                        controller.unequipCosmetic(CosmeticType.NECKLACE) { result ->
                            equipResult = result to labelNecklace
                        }
                    }
                )
                
                EquipmentSlot(
                    type = CosmeticType.AURA,
                    label = labelAura,
                    icon = Icons.Outlined.AutoAwesome,
                    equippedItemId = equippedCosmetics.aura,
                    onClick = { selectedSlot = CosmeticType.AURA },
                    onUnequip = {
                        controller.unequipCosmetic(CosmeticType.AURA) { result ->
                            equipResult = result to labelAura
                        }
                    }
                )
                
                EquipmentSlot(
                    type = CosmeticType.REGALIA,
                    label = labelRegalia,
                    icon = Icons.Outlined.Diamond,
                    equippedItemId = equippedCosmetics.regalia,
                    onClick = { selectedSlot = CosmeticType.REGALIA },
                    onUnequip = {
                        controller.unequipCosmetic(CosmeticType.REGALIA) { result ->
                            equipResult = result to labelRegalia
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
                onEquip = { itemId, slotLabel ->
                    controller.equipCosmetic(itemId) { result ->
                        equipResult = result to slotLabel
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
                            text = stringResource(MR.strings.cosmetics_equipped_colon, equippedItemId.value),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = stringResource(MR.strings.cosmetics_no_item_equipped),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            if (equippedItemId != null) {
                TextButton(onClick = onUnequip) {
                    Text(stringResource(MR.strings.common_unequip))
                }
            } else {
                TextButton(onClick = onClick) {
                    Text(stringResource(MR.strings.common_equip))
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
    onEquip: (ShopItemId, String) -> Unit,
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
            // Resolve label once for reuse below
            val slotLabel = when (slot) {
                CosmeticType.CROWN -> stringResource(MR.strings.cosmetics_slot_crown)
                CosmeticType.CLOAK -> stringResource(MR.strings.cosmetics_slot_cloak)
                CosmeticType.MANTLE -> stringResource(MR.strings.cosmetics_slot_mantle)
                CosmeticType.NECKLACE -> stringResource(MR.strings.cosmetics_slot_necklace)
                CosmeticType.AURA -> stringResource(MR.strings.cosmetics_slot_aura)
                CosmeticType.REGALIA -> stringResource(MR.strings.cosmetics_slot_regalia_full)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(MR.strings.cosmetics_select_slot_title, slotLabel),
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = onClose) {
                    Icon(Icons.Outlined.Close, contentDescription = stringResource(MR.strings.content_desc_close))
                }
            }
            
            if (availableCosmetics.isEmpty()) {
                Text(
                    text = stringResource(MR.strings.cosmetics_empty_owned_for_slot, slotLabel.lowercase()),
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
                            onEquip = { onEquip(item.id, slotLabel) }
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
                    contentDescription = stringResource(MR.strings.content_desc_equipped),
                    tint = MaterialTheme.colorScheme.primary
                )
            } else {
                Button(onClick = onEquip) {
                    Text(stringResource(MR.strings.common_equip))
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

// Note: Result messages are localized in-composable where shown; no non-composable formatter needed.
