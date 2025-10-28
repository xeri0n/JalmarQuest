package com.jalmarquest.core.state.performance

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Batches multiple state updates within a time window to reduce UI recompositions
 * and improve performance, especially during rapid state mutations.
 */
class StateUpdateBatcher<T>(
    private val batchWindow: Duration = 16.milliseconds, // ~60fps
    private val scope: CoroutineScope
) {
    private val pendingUpdates = MutableSharedFlow<T>()
    private val batchedUpdates = MutableStateFlow<List<T>>(emptyList())
    
    init {
        scope.launch {
            pendingUpdates
                .buffer()
                .chunked(batchWindow) { updates ->
                    if (updates.isNotEmpty()) {
                        batchedUpdates.value = updates
                    }
                }
        }
    }
    
    suspend fun enqueue(update: T) {
        pendingUpdates.emit(update)
    }
    
    fun getBatchedUpdates(): StateFlow<List<T>> = batchedUpdates.asStateFlow()
    
    /**
     * Extension function for time-based chunking
     */
    private fun <T> Flow<T>.chunked(
        duration: Duration,
        transform: suspend (List<T>) -> Unit
    ): Job = scope.launch {
        val chunk = mutableListOf<T>()
        var lastEmit = System.currentTimeMillis()
        
        collect { value ->
            chunk.add(value)
            val now = System.currentTimeMillis()
            
            if (now - lastEmit >= duration.inWholeMilliseconds) {
                if (chunk.isNotEmpty()) {
                    transform(chunk.toList())
                    chunk.clear()
                }
                lastEmit = now
            }
        }
    }
}

/**
 * Memory pool for frequently allocated objects to reduce GC pressure
 */
class ObjectPool<T>(
    private val factory: () -> T,
    private val reset: (T) -> Unit,
    private val maxPoolSize: Int = 50
) {
    private val pool = mutableListOf<T>()
    
    fun acquire(): T {
        return if (pool.isNotEmpty()) {
            pool.removeAt(pool.size - 1)
        } else {
            factory()
        }
    }
    
    fun release(obj: T) {
        if (pool.size < maxPoolSize) {
            reset(obj)
            pool.add(obj)
        }
    }
    
    inline fun <R> use(block: (T) -> R): R {
        val obj = acquire()
        return try {
            block(obj)
        } finally {
            release(obj)
        }
    }
}
