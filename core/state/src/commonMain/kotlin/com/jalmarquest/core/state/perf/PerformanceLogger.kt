package com.jalmarquest.core.state.perf

/**
 * Simple performance logger for Phase 1 QA.
 * Logs timing information to help identify bottlenecks in key operations.
 * Production-grade APM (Application Performance Monitoring) deferred to Milestone 5.
 */
object PerformanceLogger {
    private const val TAG = "JalmarQuest:Perf"
    private val startupTime = currentTimeMillis()
    
    /**
     * Log application startup completion.
     */
    fun logStartupComplete() {
        val elapsed = currentTimeMillis() - startupTime
        log("App startup completed in ${elapsed}ms")
    }
    
    /**
     * Log a state mutation event with optional metadata.
     * 
     * @param stateName The name of the state being mutated
     * @param operation The operation being performed (e.g., "update", "load", "save")
     * @param metadata Optional key-value pairs providing context
     */
    fun logStateMutation(stateName: String, operation: String, metadata: Map<String, Any> = emptyMap()) {
        val metaStr = if (metadata.isNotEmpty()) {
            " | " + metadata.entries.joinToString(", ") { "${it.key}=${it.value}" }
        } else {
            ""
        }
        log("State[$stateName].$operation$metaStr")
    }
    
    private fun log(message: String) {
        println("[$TAG] $message")
    }
}

/**
 * Returns the current time in milliseconds since epoch.
 * Platform-specific implementation.
 */
expect fun currentTimeMillis(): Long
