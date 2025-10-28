package com.jalmarquest.ui.app.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.jalmarquest.feature.worldinfo.WorldInfoState
import dev.icerock.moko.resources.compose.stringResource
import com.jalmarquest.ui.app.MR

/**
 * World Info Card - Displays current location, weather, season, and difficulty information
 * 
 * Integrates with Phase 4 systems:
 * - PlayerLocationTracker: Current location and region
 * - WeatherSystem: Current weather condition and severity
 * - SeasonalCycleManager: Current season and day progression
 * - RegionDifficultyManager: Difficulty tier and warnings
 */

@Composable
fun WorldInfoCard(
    state: WorldInfoState,
    modifier: Modifier = Modifier
) {
    val difficultyColor = Color(state.difficultyColorHex)
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Location Section
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = stringResource(MR.strings.components_worldinfo_location),
                    tint = MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(
                        text = state.locationName,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = state.regionName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Difficulty Indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (state.difficultyWarning != null) {
                    Icon(
                        imageVector = Icons.Outlined.Warning,
                        contentDescription = stringResource(MR.strings.components_worldinfo_difficulty_warning),
                        tint = difficultyColor
                    )
                }
                Text(
                    text = stringResource(MR.strings.worldinfo_difficulty_label, state.difficultyTier),
                    style = MaterialTheme.typography.bodyMedium,
                    color = difficultyColor
                )
            }
            state.difficultyWarning?.let { warning ->
                Surface(
                    color = difficultyColor.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = warning,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(8.dp),
                        color = difficultyColor
                    )
                }
            }

            Divider()

            // Weather Section
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Cloud,
                    contentDescription = stringResource(MR.strings.components_worldinfo_weather),
                    tint = MaterialTheme.colorScheme.secondary
                )
                Column {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = state.weatherDisplay,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = stringResource(MR.strings.worldinfo_weather_severity, state.weatherSeverity),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = state.weatherDesc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Season Section
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.WbSunny,
                    contentDescription = stringResource(MR.strings.components_worldinfo_season),
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Column {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = state.seasonDisplay,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = stringResource(MR.strings.worldinfo_season_day, state.seasonDay),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = state.seasonDesc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Divider()

            // Resource Availability
            Text(
                text = stringResource(MR.strings.worldinfo_resources_label, state.resourceAvailability),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Compact version for minimal display
 */
@Composable
fun WorldInfoCompact(
    state: WorldInfoState,
    modifier: Modifier = Modifier
) {
    val difficultyColor = Color(state.difficultyColorHex)
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Location + Difficulty
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = state.locationName,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "•",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = state.difficultyTier,
                    style = MaterialTheme.typography.bodySmall,
                    color = difficultyColor
                )
            }

            // Weather + Season
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Cloud,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = state.weatherDisplay,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "•",
                    style = MaterialTheme.typography.bodySmall
                )
                Icon(
                    imageVector = Icons.Outlined.WbSunny,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = state.seasonDisplay,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
