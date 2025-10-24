package com.jalmarquest.core.model

data class Journal(
    val activeQuests: MutableSet<String> = mutableSetOf(), // Quest IDs
    val completedQuests: MutableSet<String> = mutableSetOf(),
    val unlockedLoreIds: MutableSet<String> = mutableSetOf() // LoreReveal IDs
)