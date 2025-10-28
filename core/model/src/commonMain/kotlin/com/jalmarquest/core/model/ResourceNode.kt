package com.jalmarquest.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * Represents a harvestable resource node in the world.
 * Resource nodes provide crafting reagents through foraging/gathering.
 * 
 * Alpha 2.3: Reagent System
 * - RARE_HERB_NODE: Medicinal herbs, alchemical plants
 * - RARE_MINERAL_NODE: Ore, stone, metal fragments  
 * - RARE_ESSENCE_NODE: Magical essences, elemental cores
 */
@Serializable
data class ResourceNode(
    val id: ResourceNodeId,
    val nodeType: ResourceNodeType,
    val name: String,
    val description: String,
    val baseDifficultyLevel: Int, // 1-10, affects loot quality
    val harvestTime: Int, // Seconds required to harvest
    val respawnTime: Int, // Seconds until node respawns after depletion
    val lootTable: List<NodeLootDrop> = emptyList()
)

/**
 * A loot drop from a resource node.
 */
@Serializable
data class NodeLootDrop(
    val itemId: String,
    val dropChance: Float, // 0.0-1.0
    val quantityMin: Int = 1,
    val quantityMax: Int = 1
)

@Serializable
@JvmInline
value class ResourceNodeId(val value: String)

/**
 * Types of resource nodes in the world.
 */
@Serializable
enum class ResourceNodeType {
    @SerialName("herb")
    RARE_HERB_NODE,      // Alchemical herbs and plants
    
    @SerialName("mineral")
    RARE_MINERAL_NODE,   // Ores, stones, metal fragments
    
    @SerialName("essence")
    RARE_ESSENCE_NODE    // Magical essences and elemental cores
}

/**
 * Tracks player's resource node harvesting state.
 */
@Serializable
data class HarvestingState(
    @SerialName("depleted_nodes")
    val depletedNodes: Map<ResourceNodeId, Long> = emptyMap(), // NodeId -> respawn timestamp
    
    @SerialName("harvest_history")
    val harvestHistory: List<HarvestEntry> = emptyList(),
    
    @SerialName("total_herbs_harvested")
    val totalHerbsHarvested: Int = 0,
    
    @SerialName("total_minerals_harvested")
    val totalMineralsHarvested: Int = 0,
    
    @SerialName("total_essences_harvested")
    val totalEssencesHarvested: Int = 0
) {
    /**
     * Check if a node is available for harvesting.
     */
    fun isNodeAvailable(nodeId: ResourceNodeId, currentTime: Long): Boolean {
        val respawnTime = depletedNodes[nodeId] ?: return true
        return currentTime >= respawnTime
    }
}

/**
 * Entry in harvest history log.
 */
@Serializable
data class HarvestEntry(
    @SerialName("node_id")
    val nodeId: ResourceNodeId,
    
    @SerialName("timestamp")
    val timestamp: Long,
    
    @SerialName("items_obtained")
    val itemsObtained: List<String> // Item IDs obtained from harvest
)

/**
 * Result of a harvesting attempt.
 */
@Serializable
sealed class HarvestResult {
    @Serializable
    @SerialName("success")
    data class Success(
        val nodeId: ResourceNodeId,
        val itemsObtained: List<ItemStack>
    ) : HarvestResult()
    
    @Serializable
    @SerialName("node_depleted")
    data class NodeDepleted(
        val nodeId: ResourceNodeId,
        val respawnTimestamp: Long
    ) : HarvestResult()
    
    @Serializable
    @SerialName("skill_too_low")
    data class SkillTooLow(
        val requiredSkill: SkillType,
        val requiredLevel: Int
    ) : HarvestResult()
    
    @Serializable
    @SerialName("node_not_found")
    object NodeNotFound : HarvestResult()
}
