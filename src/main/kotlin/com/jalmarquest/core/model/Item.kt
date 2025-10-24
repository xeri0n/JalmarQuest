package com.jalmarquest.core.model

data class Item(
    val id: String,
    val name: String,
    val description: String,
    val type: ItemType
)

enum class ItemType { WEAPON, ARMOR, CONSUMABLE, MATERIAL, LORE }

enum class Slot { HEAD, BODY, WEAPON }