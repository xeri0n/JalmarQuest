package com.jalmarquest.ui.app.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.jalmarquest.core.state.coordinator.OptimizedWorldUpdateCoordinator
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * Debug overlay showing performance statistics
 * Only shown in debug builds for development monitoring
 * 
 * Displays:
 * - Frame budget statistics (avg/max frame time, budget violations)
 * - Spatial partitioning statistics (entity counts by priority)
 * - Entity update reduction percentage
 */
@Composable
fun PerformanceStatsOverlay(
    coordinator: OptimizedWorldUpdateCoordinator,
    modifier: Modifier = Modifier,
    visible: Boolean = true
) {
    if (!visible) return
    
    var perfStats by remember { mutableStateOf(coordinator.getPerformanceStats()) }
    var spatialStats by remember { mutableStateOf(coordinator.getSpatialStats()) }
    
    // Update stats every second
    LaunchedEffect(coordinator) {
        while (isActive) {
            delay(1000)
            perfStats = coordinator.getPerformanceStats()
            spatialStats = coordinator.getSpatialStats()
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        contentAlignment = Alignment.TopEnd
    ) {
        Column(
            modifier = Modifier
                .width(300.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.7f),
                    shape = MaterialTheme.shapes.small
                )
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Header
            Text(
                text = "Performance Stats (Debug)",
                style = MaterialTheme.typography.titleSmall,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Frame Budget Stats
            Text(
                text = "Frame Budget",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFFFFEB3B)
            )
            
            StatRow("Avg Frame", "${(perfStats.averageFrameTimeMs * 100).toInt() / 100.0}ms")
            StatRow("Max Frame", "${(perfStats.maxFrameTimeMs * 100).toInt() / 100.0}ms")
            StatRow(
                "Budget Violations", 
                "${(perfStats.budgetViolationRate * 10).toInt() / 10.0}%",
                color = if (perfStats.budgetViolationRate > 10.0) Color.Red else Color.Green
            )
            StatRow("Total Frames", "${perfStats.totalFrames}")
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Spatial Stats
            Text(
                text = "Spatial Partitioning",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFFFFEB3B)
            )
            
            StatRow("Total Entities", "${spatialStats.totalEntities}")
            StatRow("Immediate", "${spatialStats.immediateCount}", Color(0xFF4CAF50))
            StatRow("Near", "${spatialStats.nearCount}", Color(0xFF8BC34A))
            StatRow("Far", "${spatialStats.farCount}", Color(0xFFFFC107))
            StatRow("Inactive", "${spatialStats.inactiveCount}", Color(0xFF9E9E9E))
            StatRow(
                "Reduction", 
                "${(spatialStats.reductionPercentage * 10).toInt() / 10.0}%",
                color = Color(0xFF00BCD4)
            )
            StatRow("Update Cycle", "${spatialStats.currentCycle}")
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // System Timings (if any)
            if (perfStats.systemTimings.isNotEmpty()) {
                Text(
                    text = "System Timings",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFFFFEB3B)
                )
                
                perfStats.systemTimings.forEach { (system, avgTime) ->
                    StatRow(
                        system, 
                        "${(avgTime * 100).toInt() / 100.0}ms",
                        color = if (avgTime > 5.0) Color.Red else Color.White
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
    color: Color = Color.White
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFFBBBBBB)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = color
        )
    }
}
