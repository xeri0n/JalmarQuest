package com.jalmarquest.ui.app.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jalmarquest.core.model.DifficultyLevel
import com.jalmarquest.core.model.Playstyle

/**
 * Alpha 2.2: Difficulty level badge showing current AI Director difficulty.
 * 
 * Displays a compact badge with difficulty name and color coding:
 * - EASY: Green
 * - NORMAL: Blue
 * - HARD: Orange
 * - EXPERT: Red
 */
@Composable
fun DifficultyBadge(
    difficulty: DifficultyLevel,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor, label) = when (difficulty) {
        DifficultyLevel.EASY -> Triple(
            Color(0xFF4CAF50),  // Green
            Color.White,
            "Easy"
        )
        DifficultyLevel.NORMAL -> Triple(
            Color(0xFF2196F3),  // Blue
            Color.White,
            "Normal"
        )
        DifficultyLevel.HARD -> Triple(
            Color(0xFFFF9800),  // Orange
            Color.White,
            "Hard"
        )
        DifficultyLevel.EXPERT -> Triple(
            Color(0xFFF44336),  // Red
            Color.White,
            "Expert"
        )
    }
    
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = textColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Alpha 2.2: Playstyle icon indicator showing detected player preferences.
 * 
 * Displays an icon representing the dominant playstyle:
 * - CAUTIOUS: Shield
 * - AGGRESSIVE: Sword (Star as substitute)
 * - EXPLORER: Explore/Map
 * - HOARDER: Inventory (AccountBox as substitute)
 * - SOCIAL: People/Group
 * - BALANCED: Balance scale (Dashboard as substitute)
 */
@Composable
fun PlaystyleIcon(
    playstyle: Playstyle,
    modifier: Modifier = Modifier,
    showLabel: Boolean = false
) {
    val (icon, label, color) = when (playstyle) {
        Playstyle.CAUTIOUS -> Triple(
            Icons.Default.Shield,
            "Cautious",
            Color(0xFF4CAF50)  // Green
        )
        Playstyle.AGGRESSIVE -> Triple(
            Icons.Default.Star,  // Sword substitute
            "Aggressive",
            Color(0xFFF44336)  // Red
        )
        Playstyle.EXPLORER -> Triple(
            Icons.Default.Explore,
            "Explorer",
            Color(0xFF2196F3)  // Blue
        )
        Playstyle.HOARDER -> Triple(
            Icons.Default.AccountBox,  // Inventory substitute
            "Hoarder",
            Color(0xFFFF9800)  // Orange
        )
        Playstyle.SOCIAL -> Triple(
            Icons.Default.People,
            "Social",
            Color(0xFF9C27B0)  // Purple
        )
        Playstyle.BALANCED -> Triple(
            Icons.Default.Dashboard,  // Balance substitute
            "Balanced",
            Color(0xFF607D8B)  // Blue Grey
        )
    }
    
    if (showLabel) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.2f),
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.padding(8.dp),
                    tint = color
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    } else {
        Surface(
            modifier = modifier.size(40.dp),
            shape = CircleShape,
            color = color.copy(alpha = 0.2f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.padding(8.dp),
                tint = color
            )
        }
    }
}

/**
 * Alpha 2.2: Fatigue meter showing events since last rest.
 * 
 * Displays a compact progress indicator:
 * - Green (0-2 events): Fresh
 * - Yellow (3-4 events): Tired
 * - Red (5+ events): Exhausted
 */
@Composable
fun FatigueMeter(
    eventsSinceRest: Int,
    maxEvents: Int = 5,
    modifier: Modifier = Modifier,
    showLabel: Boolean = false
) {
    val progress = (eventsSinceRest.toFloat() / maxEvents).coerceIn(0f, 1f)
    val color = when {
        eventsSinceRest <= 2 -> Color(0xFF4CAF50)  // Green
        eventsSinceRest <= 4 -> Color(0xFFFF9800)  // Orange
        else -> Color(0xFFF44336)  // Red
    }
    
    val label = when {
        eventsSinceRest <= 2 -> "Fresh"
        eventsSinceRest <= 4 -> "Tired"
        else -> "Exhausted"
    }
    
    Column(modifier = modifier) {
        if (showLabel) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = color,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "$eventsSinceRest/$maxEvents",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
        
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = color,
            trackColor = color.copy(alpha = 0.2f),
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}

/**
 * Alpha 2.2: Compact AI Director status HUD overlay.
 * 
 * Shows difficulty badge, playstyle icon, and optional fatigue meter in a compact row.
 * Designed for non-intrusive display in the main game UI.
 */
@Composable
fun AIDirectorHUD(
    difficulty: DifficultyLevel,
    playstyle: Playstyle,
    eventsSinceRest: Int,
    showFatigueMeter: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DifficultyBadge(difficulty = difficulty)
            
            PlaystyleIcon(playstyle = playstyle)
            
            if (showFatigueMeter && eventsSinceRest >= 3) {
                // Only show fatigue when approaching rest threshold
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .padding(horizontal = 8.dp)
                ) {
                    FatigueMeter(
                        eventsSinceRest = eventsSinceRest,
                        showLabel = false
                    )
                }
            }
        }
    }
}

/**
 * Alpha 2.2: Developer debug panel showing detailed AI Director stats.
 * 
 * Displays comprehensive performance metrics:
 * - Combat W/L ratio
 * - Quest success rate
 * - Death count
 * - Event fatigue
 * - Playstyle breakdown (all 6 types with scores)
 * 
 * Intended for development/testing only, toggle via Settings.
 */
@Composable
fun AIDirectorDebugPanel(
    difficulty: DifficultyLevel,
    playstyle: Playstyle,
    combatWins: Int,
    combatLosses: Int,
    questCompletions: Int,
    questFailures: Int,
    deaths: Int,
    eventsSinceRest: Int,
    playstyleScores: Map<Playstyle, Int>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "AI Director Debug Panel",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            HorizontalDivider()
            
            // Difficulty and Playstyle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Difficulty",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    DifficultyBadge(difficulty = difficulty)
                }
                
                Column {
                    Text(
                        text = "Playstyle",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    PlaystyleIcon(playstyle = playstyle, showLabel = true)
                }
            }
            
            HorizontalDivider()
            
            // Performance Metrics
            Text(
                text = "Performance",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium
            )
            
            StatRow(label = "Combat", value = "$combatWins W / $combatLosses L")
            StatRow(label = "Quests", value = "$questCompletions Done / $questFailures Failed")
            StatRow(label = "Deaths", value = deaths.toString())
            
            HorizontalDivider()
            
            // Fatigue
            Text(
                text = "Fatigue",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium
            )
            
            FatigueMeter(
                eventsSinceRest = eventsSinceRest,
                showLabel = true
            )
            
            HorizontalDivider()
            
            // Playstyle Breakdown
            Text(
                text = "Playstyle Breakdown",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium
            )
            
            playstyleScores.entries
                .sortedByDescending { it.value }
                .forEach { (type, score) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PlaystyleIcon(playstyle = type, showLabel = true)
                        Text(
                            text = score.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
        }
    }
}

@Composable
private fun StatRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
