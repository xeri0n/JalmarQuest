package com.jalmarquest.ui.app.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jalmarquest.core.model.Equipment
import com.jalmarquest.core.model.EquipmentStats
import com.jalmarquest.ui.app.theme.AppSpacing

@Composable
fun EquipmentComparisonPanel(
    currentEquipment: Equipment?,
    newEquipment: Equipment,
    onEquip: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(AppSpacing.medium),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.large)
        ) {
            Text(
                text = "Equipment Comparison",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(AppSpacing.medium))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Current equipment
                EquipmentCard(
                    equipment = currentEquipment,
                    title = "Currently Equipped",
                    modifier = Modifier.weight(1f)
                )
                
                // Arrow indicator
                Column(
                    modifier = Modifier
                        .width(40.dp)
                        .align(Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("→", style = MaterialTheme.typography.headlineMedium)
                }
                
                // New equipment
                EquipmentCard(
                    equipment = newEquipment,
                    title = "New Equipment",
                    isHighlighted = true,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(AppSpacing.large))
            
            // Stat comparison
            StatComparisonSection(
                currentStats = currentEquipment?.stats ?: EquipmentStats(),
                newStats = newEquipment.stats
            )
            
            Spacer(modifier = Modifier.height(AppSpacing.large))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f).padding(end = AppSpacing.small)
                ) {
                    Text("Cancel")
                }
                
                Button(
                    onClick = onEquip,
                    modifier = Modifier.weight(1f).padding(start = AppSpacing.small)
                ) {
                    Text("Equip New Item")
                }
            }
        }
    }
}

@Composable
private fun EquipmentCard(
    equipment: Equipment?,
    title: String,
    isHighlighted: Boolean = false,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isHighlighted) MaterialTheme.colorScheme.primary else Color.Gray
    
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(2.dp, borderColor, RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(AppSpacing.medium)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(AppSpacing.small))
        
        if (equipment != null) {
            Text(
                text = equipment.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = equipment.rarity.name,
                style = MaterialTheme.typography.bodySmall,
                color = Color(android.graphics.Color.parseColor(equipment.rarity.colorHex))
            )
            
            Spacer(modifier = Modifier.height(AppSpacing.small))
            
            Text(
                text = "Durability: ${equipment.currentDurability}/${equipment.maxDurability}",
                style = MaterialTheme.typography.bodySmall
            )
        } else {
            Text(
                text = "No Equipment",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatComparisonSection(
    currentStats: EquipmentStats,
    newStats: EquipmentStats
) {
    Column {
        Text(
            text = "Stat Changes",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(AppSpacing.small))
        
        StatComparisonRow("Attack", currentStats.attack, newStats.attack)
        StatComparisonRow("Defense", currentStats.defense, newStats.defense)
        StatComparisonRow("Speed", currentStats.speed, newStats.speed)
        StatComparisonRow("Luck", currentStats.luck, newStats.luck)
        StatComparisonRow("Health", currentStats.health, newStats.health)
        StatComparisonRow("Stamina", currentStats.stamina, newStats.stamina)
    }
}

@Composable
private fun StatComparisonRow(
    statName: String,
    currentValue: Int,
    newValue: Int
) {
    val difference = newValue - currentValue
    val color = when {
        difference > 0 -> Color.Green
        difference < 0 -> Color.Red
        else -> MaterialTheme.colorScheme.onSurface
    }
    val prefix = if (difference > 0) "+" else ""
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = statName,
            style = MaterialTheme.typography.bodyMedium
        )
        
        Row {
            Text(
                text = "$currentValue → $newValue",
                style = MaterialTheme.typography.bodyMedium
            )
            
            if (difference != 0) {
                Text(
                    text = " ($prefix$difference)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
