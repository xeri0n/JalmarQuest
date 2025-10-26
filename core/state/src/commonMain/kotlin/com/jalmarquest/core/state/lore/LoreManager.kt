package com.jalmarquest.core.state.lore

import com.jalmarquest.core.state.perf.currentTimeMillis
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Represents a discovered lore entry with metadata.
 */
data class LoreEntry(
    val id: String,
    val title: String,
    val content: String,
    val category: String,
    val discoveredAt: Long
)

/**
 * Manages lore discovery and retrieval.
 */
class LoreManager(
    private val timestampProvider: () -> Long = { currentTimeMillis() }
) {
    private val _discoveredLore = MutableStateFlow<Map<String, LoreEntry>>(emptyMap())
    val discoveredLore: StateFlow<Map<String, LoreEntry>> = _discoveredLore.asStateFlow()
    
    fun unlockLore(loreId: String, title: String, content: String, category: String = "general") {
        if (_discoveredLore.value.containsKey(loreId)) return
        
        val entry = LoreEntry(
            id = loreId,
            title = title,
            content = content,
            category = category,
            discoveredAt = timestampProvider()
        )
        
        _discoveredLore.value = _discoveredLore.value + (loreId to entry)
    }
    
    fun hasDiscovered(loreId: String): Boolean {
        return _discoveredLore.value.containsKey(loreId)
    }
    
    fun getLoreById(loreId: String): LoreEntry? {
        return _discoveredLore.value[loreId]
    }
    
    fun getAllLore(): List<LoreEntry> {
        return _discoveredLore.value.values.sortedByDescending { it.discoveredAt }
    }
    
    fun getLoreByCategory(category: String): List<LoreEntry> {
        return _discoveredLore.value.values
            .filter { it.category == category }
            .sortedByDescending { it.discoveredAt }
    }
}
