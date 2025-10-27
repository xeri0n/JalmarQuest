package com.jalmarquest.core.di

import com.jalmarquest.core.model.CharacterAccount

/**
 * Provides a default CharacterAccount for initializing the DI graph.
 * The actual account should be loaded from persistent storage in production.
 */
fun defaultCharacterAccount(accountId: String = "default-account"): CharacterAccount {
    return CharacterAccount(accountId = accountId)
}
