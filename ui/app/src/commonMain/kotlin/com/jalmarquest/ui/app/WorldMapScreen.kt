package com.jalmarquest.ui.app

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jalmarquest.core.model.*
import com.jalmarquest.core.state.worldmap.WorldMapNavigationManager
import com.jalmarquest.core.state.worldmap.TravelResult
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import dev.icerock.moko.resources.compose.stringResource
import com.jalmarquest.ui.app.MR

/**
 * Professional full-screen world map with zoom/pan gestures, node-based navigation,
 * fog of war system, and fast travel via Nest Scrapes.
 * 
 * Features:
 * - Interactive node-based world graph
 * - Pinch-to-zoom and drag-to-pan gestures
 * - Fog of war (unrevealed nodes are hidden)
 * - Visual distinction for node types (hub, danger, resource, etc.)
 * - Fast travel menu for activated Nest Scrapes
 * - Quest markers and NPC indicators
 * - Animated player position
 */
@Composable
fun WorldMapScreen(
    worldMapNavigationManager: WorldMapNavigationManager,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val worldMapState by worldMapNavigationManager.worldMapState.collectAsState()
    val currentNode by worldMapNavigationManager.currentNode.collectAsState()
    val scope = rememberCoroutineScope()
    
    // Zoom and pan state
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    
    // Fast travel dialog state
    var showFastTravelDialog by remember { mutableStateOf(false) }
    
    // Selected node for details
    var selectedNode by remember { mutableStateOf<LocationNode?>(null) }
    
    // Error state for travel
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Pre-resolved localized messages used outside of @Composable lambdas
    val notDiscoveredMsg = stringResource(MR.strings.worldmap_not_discovered_location)
    val notConnectedMsg = stringResource(MR.strings.worldmap_not_connected)
    
    Scaffold(
        topBar = {
            WorldMapTopBar(
                currentNode = currentNode,
                onBack = onBack,
                onFastTravel = { showFastTravelDialog = true }
            )
        },
        bottomBar = {
            if (selectedNode != null) {
                NodeDetailsBottomSheet(
                    node = selectedNode!!,
                    isCurrentNode = selectedNode?.id == currentNode?.id,
                    onTravelTo = { node ->
                        scope.launch {
                            when (val result = worldMapNavigationManager.travelTo(node.id)) {
                                is TravelResult.Success -> {
                                    selectedNode = null
                                    errorMessage = null
                                }
                                is TravelResult.RequirementNotMet -> {
                                    errorMessage = result.reason
                                }
                                is TravelResult.NodeNotRevealed -> {
                                    errorMessage = notDiscoveredMsg
                                }
                                is TravelResult.NotConnected -> {
                                    errorMessage = notConnectedMsg
                                }
                            }
                        }
                    },
                    onDismiss = { selectedNode = null }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFF1A1A2E))
        ) {
            // World map canvas with zoom/pan
            WorldMapCanvas(
                worldGraph = worldMapNavigationManager.worldGraph,
                worldMapState = worldMapState,
                currentNode = currentNode,
                scale = scale,
                offsetX = offsetX,
                offsetY = offsetY,
                onNodeClick = { node ->
                    selectedNode = node
                },
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(0.5f, 3f)
                            offsetX += pan.x
                            offsetY += pan.y
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures { tapOffset ->
                            // Find tapped node
                            val tappedNode = findNodeAtPosition(
                                worldMapNavigationManager.worldGraph,
                                worldMapState,
                                tapOffset,
                                size,
                                scale,
                                offsetX,
                                offsetY
                            )
                            if (tappedNode != null) {
                                selectedNode = tappedNode
                            }
                        }
                    }
            )
            
            // Map controls
            MapControls(
                onZoomIn = { scale = (scale * 1.2f).coerceAtMost(3f) },
                onZoomOut = { scale = (scale / 1.2f).coerceAtLeast(0.5f) },
                onRecenter = {
                    scale = 1f
                    offsetX = 0f
                    offsetY = 0f
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            )
            
            // Error snackbar
            if (errorMessage != null) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { errorMessage = null }) {
                            Text(stringResource(MR.strings.common_dismiss))
                        }
                    }
                ) {
                    Text(errorMessage ?: "")
                }
            }
        }
    }
    
    // Fast travel dialog
    if (showFastTravelDialog) {
        FastTravelDialog(
            worldMapNavigationManager = worldMapNavigationManager,
            onTravelTo = { nodeId ->
                scope.launch {
                    worldMapNavigationManager.fastTravelTo(nodeId)
                    showFastTravelDialog = false
                }
            },
            onDismiss = { showFastTravelDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorldMapTopBar(
    currentNode: LocationNode?,
    onBack: () -> Unit,
    onFastTravel: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = stringResource(MR.strings.worldmap_title_world_map),
                    style = MaterialTheme.typography.titleMedium
                )
                if (currentNode != null) {
                    Text(
                        text = currentNode.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, stringResource(MR.strings.content_desc_back))
            }
        },
        actions = {
            IconButton(onClick = onFastTravel) {
                Icon(Icons.Default.LocationOn, stringResource(MR.strings.content_desc_fast_travel))
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF16213E)
        )
    )
}

@Composable
private fun WorldMapCanvas(
    worldGraph: com.jalmarquest.core.model.WorldGraph,
    worldMapState: WorldMapState?,
    currentNode: LocationNode?,
    scale: Float,
    offsetX: Float,
    offsetY: Float,
    onNodeClick: (LocationNode) -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val revealedNodes = worldMapState?.fogOfWar?.revealedNodes ?: emptySet()
    
    // Animated player position
    val animatedPlayerX by animateFloatAsState(
        targetValue = currentNode?.position?.x ?: 0.5f,
        animationSpec = tween(500)
    )
    val animatedPlayerY by animateFloatAsState(
        targetValue = currentNode?.position?.y ?: 0.5f,
        animationSpec = tween(500)
    )
    
    Canvas(modifier = modifier.graphicsLayer {
        scaleX = scale
        scaleY = scale
        translationX = offsetX
        translationY = offsetY
    }) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        
        // Draw connections first (behind nodes)
        worldGraph.nodes.values.forEach { node ->
            if (revealedNodes.contains(node.id)) {
                node.connections.forEach { connection ->
                    val targetNode = worldGraph.nodes[connection.targetNodeId]
                    if (targetNode != null && revealedNodes.contains(targetNode.id)) {
                        drawConnection(
                            from = node.position.toOffset(canvasWidth, canvasHeight),
                            to = targetNode.position.toOffset(canvasWidth, canvasHeight),
                            isActive = currentNode?.connections?.any { it.targetNodeId == targetNode.id } == true
                        )
                    }
                }
            }
        }
        
        // Draw revealed nodes
        worldGraph.nodes.values.forEach { node ->
            if (revealedNodes.contains(node.id)) {
                drawNode(
                    node = node,
                    position = node.position.toOffset(canvasWidth, canvasHeight),
                    isCurrentNode = node.id == currentNode?.id,
                    textMeasurer = textMeasurer
                )
            }
        }
        
        // Draw fog of war overlay for unrevealed areas
        drawFogOfWar(revealedNodes, worldGraph, canvasWidth, canvasHeight)
        
        // Draw player icon
        if (currentNode != null) {
            drawPlayer(
                position = Offset(
                    animatedPlayerX * canvasWidth,
                    animatedPlayerY * canvasHeight
                )
            )
        }
    }
}

private fun DrawScope.drawConnection(
    from: Offset,
    to: Offset,
    isActive: Boolean
) {
    drawLine(
        color = if (isActive) Color(0xFF4ECCA3) else Color(0xFF393E46),
        start = from,
        end = to,
        strokeWidth = if (isActive) 4f else 2f,
        alpha = if (isActive) 1f else 0.5f
    )
}

private fun DrawScope.drawNode(
    node: LocationNode,
    position: Offset,
    isCurrentNode: Boolean,
    textMeasurer: androidx.compose.ui.text.TextMeasurer
) {
    val nodeColor = when (node.type) {
        NodeType.HUB -> Color(0xFF4ECCA3)
        NodeType.KEY_LOCATION -> Color(0xFFFFD700)
        NodeType.DANGER -> Color(0xFFFF6B6B)
        NodeType.RESOURCE -> Color(0xFF95E1D3)
        NodeType.POINT_OF_INTEREST -> Color(0xFFAA96DA)
        NodeType.NEST -> Color(0xFFFCBF49)
        NodeType.NEST_SCRAPE -> Color(0xFFF77F00)
        NodeType.FILLER -> Color(0xFF6C757D)
    }
    
    val nodeRadius = when (node.type) {
        NodeType.HUB, NodeType.KEY_LOCATION -> 24f
        NodeType.DANGER, NodeType.POINT_OF_INTEREST -> 20f
        NodeType.NEST, NodeType.NEST_SCRAPE -> 18f
        NodeType.RESOURCE, NodeType.FILLER -> 16f
    }
    
    // Draw node circle
    drawCircle(
        color = nodeColor,
        radius = nodeRadius,
        center = position,
        alpha = if (isCurrentNode) 1f else 0.8f
    )
    
    // Draw outer ring for current node
    if (isCurrentNode) {
        drawCircle(
            color = Color.White,
            radius = nodeRadius + 6f,
            center = position,
            style = Stroke(width = 3f)
        )
    }
    
    // Draw nest scrape icon
    if (node.isNestScrape) {
        drawCircle(
            color = Color.White,
            radius = 6f,
            center = position
        )
    }
    
    // Draw node label (only for non-filler nodes)
    if (node.type != NodeType.FILLER) {
        val textLayoutResult = textMeasurer.measure(
            text = node.name,
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )
        drawText(
            textLayoutResult = textLayoutResult,
            topLeft = Offset(
                x = position.x - textLayoutResult.size.width / 2,
                y = position.y + nodeRadius + 8f
            )
        )
    }
}

private fun DrawScope.drawFogOfWar(
    revealedNodes: Set<LocationNodeId>,
    worldGraph: com.jalmarquest.core.model.WorldGraph,
    canvasWidth: Float,
    canvasHeight: Float
) {
    // Simple fog overlay - darken areas far from revealed nodes
    // This is a simplified version; could be enhanced with gradient masks
    val fogColor = Color(0xFF0F0F1E).copy(alpha = 0.7f)
    
    // For now, just draw a subtle vignette effect
    drawRect(
        color = fogColor,
        size = Size(canvasWidth, canvasHeight),
        alpha = 0.1f
    )
}

private fun DrawScope.drawPlayer(position: Offset) {
    // Draw player icon (pulsing circle)
    drawCircle(
        color = Color.White,
        radius = 12f,
        center = position
    )
    drawCircle(
        color = Color(0xFF4ECCA3),
        radius = 8f,
        center = position
    )
}

@Composable
private fun MapControls(
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onRecenter: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FloatingActionButton(
            onClick = onZoomIn,
            containerColor = Color(0xFF16213E),
            modifier = Modifier.size(48.dp)
        ) {
            Icon(Icons.Default.Add, stringResource(MR.strings.content_desc_zoom_in), tint = Color.White)
        }
        FloatingActionButton(
            onClick = onZoomOut,
            containerColor = Color(0xFF16213E),
            modifier = Modifier.size(48.dp)
        ) {
            Icon(Icons.Default.Remove, stringResource(MR.strings.content_desc_zoom_out), tint = Color.White)
        }
        FloatingActionButton(
            onClick = onRecenter,
            containerColor = Color(0xFF16213E),
            modifier = Modifier.size(48.dp)
        ) {
            Icon(Icons.Default.MyLocation, stringResource(MR.strings.content_desc_recenter), tint = Color.White)
        }
    }
}

@Composable
private fun NodeDetailsBottomSheet(
    node: LocationNode,
    isCurrentNode: Boolean,
    onTravelTo: (LocationNode) -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF16213E),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = node.name,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White
            )
            
            Text(
                text = node.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFB0B0B0)
            )
            
            if (node.npcIds.isNotEmpty()) {
                Text(
                    text = stringResource(MR.strings.worldmap_npcs_count, node.npcIds.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF4ECCA3)
                )
            }
            
            if (node.questIds.isNotEmpty()) {
                Text(
                    text = stringResource(MR.strings.worldmap_quests_count, node.questIds.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFFFD700)
                )
            }
            
            if (node.enemyIds.isNotEmpty()) {
                Text(
                    text = stringResource(MR.strings.worldmap_enemies_count, node.enemyIds.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFFF6B6B)
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(MR.strings.common_close))
                }
                
                if (!isCurrentNode) {
                    Button(
                        onClick = { onTravelTo(node) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4ECCA3)
                        )
                    ) {
                        Text(stringResource(MR.strings.worldmap_travel_here), color = Color.Black)
                    }
                }
            }
        }
    }
}

@Composable
private fun FastTravelDialog(
    worldMapNavigationManager: WorldMapNavigationManager,
    onTravelTo: (LocationNodeId) -> Unit,
    onDismiss: () -> Unit
) {
    val worldMapState by worldMapNavigationManager.worldMapState.collectAsState()
    val activatedScrapes = worldMapState?.fogOfWar?.activatedNestScrapes ?: emptySet()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(MR.strings.dialog_fast_travel_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (activatedScrapes.isEmpty()) {
                    Text(stringResource(MR.strings.dialog_fast_travel_empty))
                } else {
                    Text(stringResource(MR.strings.dialog_fast_travel_select_prompt))
                    activatedScrapes.forEach { nodeId ->
                        val node = worldMapNavigationManager.worldGraph.nodes[nodeId]
                        if (node != null) {
                            OutlinedButton(
                                onClick = { onTravelTo(nodeId) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(node.name)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(MR.strings.common_close))
            }
        }
    )
}

private fun NodePosition.toOffset(canvasWidth: Float, canvasHeight: Float): Offset {
    return Offset(
        x = this.x * canvasWidth,
        y = this.y * canvasHeight
    )
}

private fun findNodeAtPosition(
    worldGraph: com.jalmarquest.core.model.WorldGraph,
    worldMapState: WorldMapState?,
    tapOffset: Offset,
    canvasSize: androidx.compose.ui.unit.IntSize,
    scale: Float,
    offsetX: Float,
    offsetY: Float
): LocationNode? {
    val revealedNodes = worldMapState?.fogOfWar?.revealedNodes ?: return null
    
    // Adjust tap position for zoom and pan
    val adjustedX = (tapOffset.x - offsetX) / scale
    val adjustedY = (tapOffset.y - offsetY) / scale
    
    // Find nearest revealed node within tap radius
    val tapRadius = 50f / scale
    
    return worldGraph.nodes.values
        .filter { revealedNodes.contains(it.id) }
        .minByOrNull { node ->
            val nodeX = node.position.x * canvasSize.width
            val nodeY = node.position.y * canvasSize.height
            val dx = nodeX - adjustedX
            val dy = nodeY - adjustedY
            kotlin.math.sqrt(dx * dx + dy * dy)
        }
        ?.takeIf { node ->
            val nodeX = node.position.x * canvasSize.width
            val nodeY = node.position.y * canvasSize.height
            val dx = nodeX - adjustedX
            val dy = nodeY - adjustedY
            kotlin.math.sqrt(dx * dx + dy * dy) <= tapRadius
        }
}
