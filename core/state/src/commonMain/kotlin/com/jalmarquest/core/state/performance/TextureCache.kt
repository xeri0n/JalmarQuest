package com.jalmarquest.core.state.performance

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * LRU cache for game textures/images to reduce memory allocation and loading times
 */
class TextureCache(
    private val maxSizeBytes: Long = 50_000_000 // 50MB default
) {
    private val cache = LinkedHashMap<String, CachedTexture>(16, 0.75f, true)
    private val mutex = Mutex()
    private var currentSizeBytes = 0L
    
    data class CachedTexture(
        val key: String,
        val data: ByteArray,
        val width: Int,
        val height: Int,
        val sizeBytes: Long,
        val lastAccessTime: Long = System.currentTimeMillis()
    )
    
    suspend fun get(key: String): CachedTexture? = mutex.withLock {
        cache[key]?.also {
            // Update access time on LRU
            cache[key] = it.copy(lastAccessTime = System.currentTimeMillis())
        }
    }
    
    suspend fun put(key: String, texture: CachedTexture) = mutex.withLock {
        // Remove existing entry if present
        cache[key]?.let {
            currentSizeBytes -= it.sizeBytes
            cache.remove(key)
        }
        
        // Evict oldest entries if needed
        while (currentSizeBytes + texture.sizeBytes > maxSizeBytes && cache.isNotEmpty()) {
            val oldest = cache.entries.first()
            currentSizeBytes -= oldest.value.sizeBytes
            cache.remove(oldest.key)
        }
        
        // Add new entry
        cache[key] = texture
        currentSizeBytes += texture.sizeBytes
    }
    
    suspend fun preload(keys: List<String>, loader: suspend (String) -> CachedTexture?) {
        keys.forEach { key ->
            if (get(key) == null) {
                loader(key)?.let { texture ->
                    put(key, texture)
                }
            }
        }
    }
    
    suspend fun clear() = mutex.withLock {
        cache.clear()
        currentSizeBytes = 0
    }
    
    fun getCacheStats() = CacheStats(
        entries = cache.size,
        sizeBytes = currentSizeBytes,
        maxSizeBytes = maxSizeBytes,
        hitRate = 0f // Would need to track hits/misses for this
    )
    
    data class CacheStats(
        val entries: Int,
        val sizeBytes: Long,
        val maxSizeBytes: Long,
        val hitRate: Float
    )
}
