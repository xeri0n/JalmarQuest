package com.jalmarquest.core.state.optimization

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Frame Budget Monitor for Phase 6 Performance Optimization
 * 
 * Purpose: Track execution time of world updates and ensure 60fps target (16.67ms budget)
 * 
 * Features:
 * - Per-system timing tracking
 * - Frame budget enforcement (warn when exceeding 16ms)
 * - Adaptive throttling (skip low-priority systems when running slow)
 * - Performance statistics for debugging
 */

/**
 * Performance budget thresholds (in milliseconds)
 */
object PerformanceBudget {
    const val TARGET_FRAME_TIME_MS = 16.67  // 60 FPS
    const val WARNING_THRESHOLD_MS = 14.0   // Warn at 85% of budget
    const val CRITICAL_THRESHOLD_MS = 20.0  // Critical at 120% of budget
    
    // Per-system budgets (recommended allocation)
    const val WORLD_UPDATE_BUDGET_MS = 5.0
    const val NPC_AI_BUDGET_MS = 3.0
    const val RENDER_BUDGET_MS = 8.0
}

/**
 * Timing measurement for a system
 */
data class SystemTiming(
    val systemName: String,
    val executionTimeMs: Double,
    val timestamp: Long
)

/**
 * Performance statistics
 */
data class PerformanceStats(
    val averageFrameTimeMs: Double,
    val maxFrameTimeMs: Double,
    val minFrameTimeMs: Double,
    val framesAboveBudget: Int,
    val totalFrames: Int,
    val systemTimings: Map<String, Double>  // System name -> average time
) {
    val budgetViolationRate: Double get() = 
        if (totalFrames > 0) (framesAboveBudget.toDouble() / totalFrames) * 100.0 else 0.0
}

/**
 * Monitors frame budget and provides adaptive throttling
 */
class FrameBudgetMonitor(
    private val timestampProvider: () -> Long
) {
    private val frameTimes = mutableListOf<Double>()
    private val systemTimings = mutableMapOf<String, MutableList<Double>>()
    private var framesAboveBudget = 0
    private var lastFrameStartTime = 0L
    
    private val _currentFrameTime = MutableStateFlow(0.0)
    val currentFrameTime: StateFlow<Double> = _currentFrameTime.asStateFlow()
    
    private val _isAboveBudget = MutableStateFlow(false)
    val isAboveBudget: StateFlow<Boolean> = _isAboveBudget.asStateFlow()
    
    /**
     * Start frame timing
     */
    fun startFrame() {
        lastFrameStartTime = timestampProvider()
    }
    
    /**
     * End frame timing and record results
     */
    fun endFrame() {
        val frameTime = (timestampProvider() - lastFrameStartTime).toDouble()
        recordFrameTime(frameTime)
    }
    
    /**
     * Measure system execution time
     */
    fun <T> measureSystem(systemName: String, block: () -> T): T {
        val startTime = timestampProvider()
        val result = block()
        val executionTime = (timestampProvider() - startTime).toDouble()
        
        recordSystemTime(systemName, executionTime)
        return result
    }
    
    /**
     * Measure system execution time (suspend version)
     */
    suspend fun <T> measureSystemSuspend(systemName: String, block: suspend () -> T): T {
        val startTime = timestampProvider()
        val result = block()
        val executionTime = (timestampProvider() - startTime).toDouble()
        
        recordSystemTime(systemName, executionTime)
        return result
    }
    
    /**
     * Check if we have budget for additional work
     * Returns true if current frame is under warning threshold
     */
    fun hasBudget(): Boolean {
        return _currentFrameTime.value < PerformanceBudget.WARNING_THRESHOLD_MS
    }
    
    /**
     * Check if we should skip low-priority work
     * Returns true if we're approaching or over budget
     */
    fun shouldThrottle(): Boolean {
        return _currentFrameTime.value >= PerformanceBudget.WARNING_THRESHOLD_MS
    }
    
    /**
     * Get performance statistics
     */
    fun getStatistics(): PerformanceStats {
        if (frameTimes.isEmpty()) {
            return PerformanceStats(
                averageFrameTimeMs = 0.0,
                maxFrameTimeMs = 0.0,
                minFrameTimeMs = 0.0,
                framesAboveBudget = 0,
                totalFrames = 0,
                systemTimings = emptyMap()
            )
        }
        
        val averageSystemTimings = systemTimings.mapValues { (_, times) ->
            if (times.isEmpty()) 0.0 else times.average()
        }
        
        return PerformanceStats(
            averageFrameTimeMs = frameTimes.average(),
            maxFrameTimeMs = frameTimes.maxOrNull() ?: 0.0,
            minFrameTimeMs = frameTimes.minOrNull() ?: 0.0,
            framesAboveBudget = framesAboveBudget,
            totalFrames = frameTimes.size,
            systemTimings = averageSystemTimings
        )
    }
    
    /**
     * Reset statistics
     */
    fun reset() {
        frameTimes.clear()
        systemTimings.clear()
        framesAboveBudget = 0
        _currentFrameTime.value = 0.0
        _isAboveBudget.value = false
    }
    
    /**
     * Record frame time
     */
    private fun recordFrameTime(frameTime: Double) {
        frameTimes.add(frameTime)
        _currentFrameTime.value = frameTime
        
        if (frameTime > PerformanceBudget.TARGET_FRAME_TIME_MS) {
            framesAboveBudget++
            _isAboveBudget.value = true
        } else {
            _isAboveBudget.value = false
        }
        
        // Keep only last 100 frames to prevent memory growth
        if (frameTimes.size > 100) {
            frameTimes.removeAt(0)
        }
    }
    
    /**
     * Record system execution time
     */
    private fun recordSystemTime(systemName: String, executionTime: Double) {
        val times = systemTimings.getOrPut(systemName) { mutableListOf() }
        times.add(executionTime)
        
        // Keep only last 100 measurements per system
        if (times.size > 100) {
            times.removeAt(0)
        }
    }
}

/**
 * Adaptive world update coordinator with performance awareness
 * 
 * Integrates with WorldUpdateCoordinator to provide adaptive throttling
 */
class AdaptiveUpdateCoordinator(
    private val frameBudgetMonitor: FrameBudgetMonitor,
    private val spatialPartitioning: SpatialPartitioningSystem
) {
    /**
     * Decide which update operations to perform this frame
     * 
     * Priority order (when budget is tight):
     * 1. IMMEDIATE priority entities (player's location)
     * 2. Critical systems (weather, season)
     * 3. NEAR priority entities
     * 4. FAR priority entities (skip if over budget)
     * 5. INACTIVE entities (skip if over budget)
     */
    fun planUpdates(): UpdatePlan {
        val hasBudget = frameBudgetMonitor.hasBudget()
        val shouldThrottle = frameBudgetMonitor.shouldThrottle()
        
        val stats = spatialPartitioning.getStatistics()
        
        return UpdatePlan(
            updateImmediateEntities = true,  // Always update player's location
            updateNearEntities = hasBudget,   // Skip if tight on budget
            updateFarEntities = !shouldThrottle,  // Skip if approaching budget
            updateInactiveEntities = hasBudget && !shouldThrottle,  // Only if lots of budget
            updateWeather = true,  // Critical system
            updateSeasons = !shouldThrottle,
            spatialStats = stats
        )
    }
}

/**
 * Update plan for current frame
 */
data class UpdatePlan(
    val updateImmediateEntities: Boolean,
    val updateNearEntities: Boolean,
    val updateFarEntities: Boolean,
    val updateInactiveEntities: Boolean,
    val updateWeather: Boolean,
    val updateSeasons: Boolean,
    val spatialStats: SpatialStatistics
)
