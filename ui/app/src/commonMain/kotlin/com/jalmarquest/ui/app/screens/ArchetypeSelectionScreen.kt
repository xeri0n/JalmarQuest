package com.jalmarquest.ui.app.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jalmarquest.core.model.ArchetypeType
import com.jalmarquest.core.state.GameStateManager
import com.jalmarquest.ui.app.theme.AppSpacing
import kotlinx.coroutines.launch

@Composable
fun ArchetypeSelectionScreen(
    gameStateManager: GameStateManager,
    onArchetypeSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedArchetype by remember { mutableStateOf<ArchetypeType?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(AppSpacing.large),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title
        Text(
            text = "Choose Your Path",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "As a tiny button quail, how will you survive in this vast world?",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = AppSpacing.medium)
        )
        
        Spacer(modifier = Modifier.height(AppSpacing.large))
        
        // Archetype Grid
        val archetypes = listOf(
            ArchetypeData(
                type = ArchetypeType.SCHOLAR,
                icon = Icons.Default.School,
                color = Color(0xFF4A90E2),
                title = "The Scholar",
                description = "Master of knowledge and thought. Faster learning, better internalization.",
                benefits = listOf(
                    "+20% Experience gain",
                    "+15% Thought internalization speed",
                    "Unique dialogue options",
                    "Access to rare lore"
                )
            ),
            ArchetypeData(
                type = ArchetypeType.COLLECTOR,
                icon = Icons.Default.Inventory,
                color = Color(0xFFFFB74D),
                title = "The Collector",
                description = "Hoarder of shinies and treasures. Better loot, improved trading.",
                benefits = listOf(
                    "+25% Shiny value",
                    "+15% Shop discounts",
                    "Extra inventory space",
                    "Rare item discovery"
                )
            ),
            ArchetypeData(
                type = ArchetypeType.ALCHEMIST,
                icon = Icons.Default.Science,
                color = Color(0xFF9C27B0),
                title = "The Alchemist",
                description = "Brewer of powerful concoctions. Enhanced crafting and potion effects.",
                benefits = listOf(
                    "+20% Potion duration",
                    "+15% Recipe discovery",
                    "Unique brewing recipes",
                    "Ingredient expertise"
                )
            ),
            ArchetypeData(
                type = ArchetypeType.SCAVENGER,
                icon = Icons.Default.Search,
                color = Color(0xFF66BB6A),
                title = "The Scavenger",
                description = "Expert forager and survivor. Better resource gathering.",
                benefits = listOf(
                    "+30% Foraging yield",
                    "+20% Resource respawn",
                    "Hidden cache detection",
                    "Survival expertise"
                )
            ),
            ArchetypeData(
                type = ArchetypeType.SOCIALITE,
                icon = Icons.Default.Groups,
                color = Color(0xFFE91E63),
                title = "The Socialite",
                description = "Master of relationships. Better companion and NPC interactions.",
                benefits = listOf(
                    "+25% Companion affinity",
                    "+20% Gift effectiveness",
                    "Unique social quests",
                    "Faction reputation bonus"
                )
            ),
            ArchetypeData(
                type = ArchetypeType.WARRIOR,
                icon = Icons.Default.Shield,
                color = Color(0xFFD32F2F),
                title = "The Warrior",
                description = "Brave fighter despite tiny size. Combat and defense bonuses.",
                benefits = listOf(
                    "+15% Damage",
                    "+20% Defense",
                    "Combat skill mastery",
                    "Predator resistance"
                )
            )
        )
        
        archetypes.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { archetype ->
                    ArchetypeCard(
                        data = archetype,
                        isSelected = selectedArchetype == archetype.type,
                        onClick = { selectedArchetype = archetype.type },
                        modifier = Modifier
                            .weight(1f)
                            .padding(AppSpacing.small)
                    )
                }
                
                // Fill empty space in odd rows
                if (row.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(AppSpacing.extraLarge))
        
        // Confirm Button
        AnimatedVisibility(
            visible = selectedArchetype != null,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            Button(
                onClick = { showConfirmDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Begin Your Journey",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
    
    // Confirmation Dialog
    if (showConfirmDialog && selectedArchetype != null) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = {
                Text("Confirm Your Path")
            },
            text = {
                Text(
                    "Once chosen, your archetype cannot be changed for this character. " +
                    "Are you sure you want to become ${getArchetypeTitle(selectedArchetype!!)}?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            gameStateManager.selectArchetype(selectedArchetype!!)
                            onArchetypeSelected()
                        }
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ArchetypeCard(
    data: ArchetypeData,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(dampingRatio = 0.8f)
    )
    
    Card(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                data.color.copy(alpha = 0.15f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isSelected) {
            BorderStroke(2.dp, data.color)
        } else {
            null
        },
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon
            Icon(
                imageVector = data.icon,
                contentDescription = data.title,
                modifier = Modifier.size(48.dp),
                tint = data.color
            )
            
            Spacer(modifier = Modifier.height(AppSpacing.small))
            
            // Title
            Text(
                text = data.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) data.color else MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(AppSpacing.tiny))
            
            // Description
            Text(
                text = data.description,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (isSelected) {
                Spacer(modifier = Modifier.height(AppSpacing.small))
                
                // Benefits list
                Column {
                    data.benefits.forEach { benefit ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFF4CAF50)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = benefit,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class ArchetypeData(
    val type: ArchetypeType,
    val icon: ImageVector,
    val color: Color,
    val title: String,
    val description: String,
    val benefits: List<String>
)

private fun getArchetypeTitle(type: ArchetypeType): String = when (type) {
    ArchetypeType.SCHOLAR -> "The Scholar"
    ArchetypeType.COLLECTOR -> "The Collector"
    ArchetypeType.ALCHEMIST -> "The Alchemist"
    ArchetypeType.SCAVENGER -> "The Scavenger"
    ArchetypeType.SOCIALITE -> "The Socialite"
    ArchetypeType.WARRIOR -> "The Warrior"
}
