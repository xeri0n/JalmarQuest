package com.jalmarquest.ui.app.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jalmarquest.core.model.*
import com.jalmarquest.core.state.managers.LocationWithStatus
import com.jalmarquest.core.state.managers.RegionWithStatus
import com.jalmarquest.feature.worldmap.ViewMode
import com.jalmarquest.feature.worldmap.WorldMapController
import com.jalmarquest.ui.app.layout.AppSpacing

/**
 * World Map screen - allows exploration beyond Buttonburgh.
 * Shows regions, locations, and handles travel/fast travel.
 */
@Composable
fun WorldMapScreen(
    controller: WorldMapController,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val regions by controller.regions.collectAsState()
    val selectedRegion by controller.selectedRegion.collectAsState()
    val locationsInRegion by controller.locationsInRegion.collectAsState()
    val fastTravelLocations by controller.fastTravelLocations.collectAsState()
    val currentLocation by controller.currentLocation.collectAsState()
    val travelResult by controller.travelResult.collectAsState()
    val viewMode by controller.viewMode.collectAsState()
    
    Scaffold(
        topBar = {
            WorldMapTopBar(
                viewMode = viewMode,
                currentLocationName = currentLocation?.name ?: "Unknown",
                onNavigateBack = {
                    when (viewMode) {
                        ViewMode.REGIONS -> onNavigateBack()
                        ViewMode.LOCATIONS -> controller.deselectRegion()
                        ViewMode.FAST_TRAVEL -> controller.deselectRegion()
                    }
                },
                onShowFastTravel = { controller.showFastTravelView() }
            )
        }
    ) { padding ->
        Box(modifier = modifier.fillMaxSize().padding(padding)) {
            when (viewMode) {
                ViewMode.REGIONS -> {
                    RegionListView(
                        regions = regions,
                        onSelectRegion = { controller.selectRegion(it) }
                    )
                }
                ViewMode.LOCATIONS -> {
                    LocationListView(
                        locations = locationsInRegion,
                        currentLocationId = currentLocation?.id,
                        onTravelToLocation = { controller.travelToLocation(it) }
                    )
                }
                ViewMode.FAST_TRAVEL -> {
                    FastTravelView(
                        locations = fastTravelLocations,
                        currentLocationId = currentLocation?.id,
                        onFastTravel = { controller.fastTravel(it) }
                    )
                }
            }
            
            // Travel result notification
            travelResult?.let { result ->
                TravelResultNotification(
                    result = result,
                    onDismiss = { controller.clearTravelResult() },
                    modifier = Modifier.align(Alignment.BottomCenter).padding(AppSpacing.medium)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorldMapTopBar(
    viewMode: ViewMode,
    currentLocationName: String,
    onNavigateBack: () -> Unit,
    onShowFastTravel: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = when (viewMode) {
                        ViewMode.REGIONS -> "World Map"
                        ViewMode.LOCATIONS -> "Locations"
                        ViewMode.FAST_TRAVEL -> "Fast Travel"
                    },
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Current: $currentLocationName",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            if (viewMode == ViewMode.REGIONS) {
                IconButton(onClick = onShowFastTravel) {
                    Icon(Icons.Default.Navigation, contentDescription = "Fast Travel")
                }
            }
        }
    )
}

@Composable
private fun RegionListView(
    regions: List<RegionWithStatus>,
    onSelectRegion: (WorldRegionId) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(AppSpacing.medium),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)
    ) {
        items(regions) { regionStatus ->
            RegionCard(
                regionStatus = regionStatus,
                onClick = { if (regionStatus.isUnlocked) onSelectRegion(regionStatus.region.id) }
            )
        }
    }
}

@Composable
private fun RegionCard(
    regionStatus: RegionWithStatus,
    onClick: () -> Unit
) {
    val region = regionStatus.region
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = regionStatus.isUnlocked) { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (regionStatus.isUnlocked) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.medium)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = getBiomeIcon(region.biomeType),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = getBiomeColor(region.biomeType)
                        )
                        Spacer(Modifier.width(AppSpacing.small))
                        Text(
                            text = region.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(Modifier.height(AppSpacing.tiny))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        DifficultyBadge(level = region.difficultyLevel)
                        Spacer(Modifier.width(AppSpacing.small))
                        if (regionStatus.isDiscovered) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = "Discovered",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                
                // Lock icon if not unlocked
                if (!regionStatus.isUnlocked) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(Modifier.height(AppSpacing.small))
            
            // Description
            Text(
                text = region.description,
                style = MaterialTheme.typography.bodyMedium,
                color = if (regionStatus.isUnlocked) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                }
            )
            
            // Exploration progress
            if (regionStatus.isDiscovered && regionStatus.totalLocations > 0) {
                Spacer(Modifier.height(AppSpacing.small))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Explored: ${regionStatus.visitedLocationCount}/${regionStatus.totalLocations}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    LinearProgressIndicator(
                        progress = regionStatus.explorationProgress,
                        modifier = Modifier.width(100.dp).height(4.dp)
                    )
                }
            }
            
            // Unlock requirement
            if (!regionStatus.isUnlocked) {
                val requirement = region.unlockRequirement
                if (requirement != null) {
                    Spacer(Modifier.height(AppSpacing.small))
                    UnlockRequirementChip(requirement = requirement)
                }
            }
        }
    }
}

@Composable
private fun DifficultyBadge(level: Int) {
    val (text, color) = when {
        level <= 2 -> "Safe" to MaterialTheme.colorScheme.tertiary
        level <= 4 -> "Moderate" to MaterialTheme.colorScheme.primary
        level <= 6 -> "Dangerous" to MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
        else -> "Deadly" to MaterialTheme.colorScheme.error
    }
    
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.2f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun UnlockRequirementChip(requirement: RegionUnlockRequirement) {
    val text = when (requirement) {
        is RegionUnlockRequirement.QuestCompletion -> "Complete quest to unlock"
        is RegionUnlockRequirement.MinimumLevel -> "Reach level ${requirement.level}"
        is RegionUnlockRequirement.DiscoverRegion -> "Discover another region first"
        is RegionUnlockRequirement.AllOf -> "Multiple requirements"
    }
    
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.errorContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = AppSpacing.small, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
private fun LocationListView(
    locations: List<LocationWithStatus>,
    currentLocationId: String?,
    onTravelToLocation: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(AppSpacing.medium),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
    ) {
        items(locations) { locationStatus ->
            LocationCard(
                locationStatus = locationStatus,
                isCurrent = locationStatus.location.id == currentLocationId,
                onTravel = { onTravelToLocation(locationStatus.location.id) }
            )
        }
    }
}

@Composable
private fun LocationCard(
    locationStatus: LocationWithStatus,
    isCurrent: Boolean,
    onTravel: () -> Unit
) {
    val location = locationStatus.location
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent) {
                MaterialTheme.colorScheme.primaryContainer
            } else if (locationStatus.isAccessible) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.medium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isCurrent) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = "Current location",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(4.dp))
                    } else if (locationStatus.isVisited) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Visited",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(Modifier.width(4.dp))
                    }
                    
                    Text(
                        text = location.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                    )
                }
                
                Spacer(Modifier.height(AppSpacing.tiny))
                
                Text(
                    text = location.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (!isCurrent && locationStatus.isAccessible) {
                Button(
                    onClick = onTravel,
                    modifier = Modifier.padding(start = AppSpacing.small)
                ) {
                    Text("Travel")
                }
            } else if (!locationStatus.isAccessible) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun FastTravelView(
    locations: List<LocationWithStatus>,
    currentLocationId: String?,
    onFastTravel: (String) -> Unit
) {
    if (locations.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No fast travel locations unlocked yet.\nVisit safe locations to enable fast travel.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(AppSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
        ) {
            items(locations) { locationStatus ->
                LocationCard(
                    locationStatus = locationStatus,
                    isCurrent = locationStatus.location.id == currentLocationId,
                    onTravel = { onFastTravel(locationStatus.location.id) }
                )
            }
        }
    }
}

@Composable
private fun TravelResultNotification(
    result: TravelResult,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (message, isError) = when (result) {
        is TravelResult.Success -> "Arrived at new location" to false
        is TravelResult.SuccessWithRewards -> {
            val rewardCount = result.discoveries.size
            "Arrived! Discovered $rewardCount new things" to false
        }
        is TravelResult.RegionLocked -> "Region locked: requirements not met" to true
        is TravelResult.LocationLocked -> "Location locked: complete required quest" to true
        TravelResult.InvalidLocation -> "Invalid location" to true
    }
    
    Snackbar(
        modifier = modifier,
        action = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        },
        containerColor = if (isError) {
            MaterialTheme.colorScheme.errorContainer
        } else {
            MaterialTheme.colorScheme.tertiaryContainer
        }
    ) {
        Text(
            text = message,
            color = if (isError) {
                MaterialTheme.colorScheme.onErrorContainer
            } else {
                MaterialTheme.colorScheme.onTertiaryContainer
            }
        )
    }
}

@Composable
private fun getBiomeIcon(biomeType: BiomeType): androidx.compose.ui.graphics.vector.ImageVector {
    return when (biomeType) {
        BiomeType.TOWN -> Icons.Default.Home
        BiomeType.FOREST -> Icons.Default.Park
        BiomeType.GRASSLAND -> Icons.Default.Landscape
        BiomeType.WETLAND -> Icons.Default.Water
        BiomeType.BEACH -> Icons.Default.BeachAccess
        BiomeType.MOUNTAIN -> Icons.Default.Terrain
        BiomeType.DESERT -> Icons.Default.WbSunny
        BiomeType.CAVE -> Icons.Default.DarkMode
        BiomeType.RUINS -> Icons.Default.AccountBalance
        BiomeType.GARDEN -> Icons.Default.LocalFlorist
    }
}

@Composable
private fun getBiomeColor(biomeType: BiomeType): androidx.compose.ui.graphics.Color {
    return when (biomeType) {
        BiomeType.TOWN -> MaterialTheme.colorScheme.tertiary
        BiomeType.FOREST -> androidx.compose.ui.graphics.Color(0xFF2E7D32)
        BiomeType.GRASSLAND -> androidx.compose.ui.graphics.Color(0xFF7CB342)
        BiomeType.WETLAND -> androidx.compose.ui.graphics.Color(0xFF0277BD)
        BiomeType.BEACH -> androidx.compose.ui.graphics.Color(0xFF0288D1)
        BiomeType.MOUNTAIN -> androidx.compose.ui.graphics.Color(0xFF5D4037)
        BiomeType.DESERT -> androidx.compose.ui.graphics.Color(0xFFF57C00)
        BiomeType.CAVE -> MaterialTheme.colorScheme.onSurfaceVariant
        BiomeType.RUINS -> androidx.compose.ui.graphics.Color(0xFF616161)
        BiomeType.GARDEN -> androidx.compose.ui.graphics.Color(0xFFC62828)
    }
}
