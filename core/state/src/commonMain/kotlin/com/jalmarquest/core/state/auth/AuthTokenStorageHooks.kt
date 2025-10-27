package com.jalmarquest.core.state.auth

import com.jalmarquest.core.model.SessionToken
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.runBlocking

/**
 * Allows platform actuals and tests to supply custom persistence backends for [AuthTokenStorage].
 * A default driver can be installed during app start-up, while overrides let tests inject
 * deterministic fakes without leaking global state across executions.
 */
interface TokenPersistenceDriver {
    suspend fun readToken(): SessionToken?
    suspend fun writeToken(token: SessionToken?)
}

object AuthTokenStorageHooks {
    @PublishedApi
    internal val mutex = Mutex()
    @PublishedApi
    internal var overrideDriver: TokenPersistenceDriver? = null
    @PublishedApi
    internal var defaultDriver: TokenPersistenceDriver? = null

    fun installDefault(driver: TokenPersistenceDriver) = runBlocking {
        mutex.withLock {
            defaultDriver = driver
        }
    }

    fun clearDefault() = runBlocking {
        mutex.withLock {
            defaultDriver = null
        }
    }

    fun override(driver: TokenPersistenceDriver?) = runBlocking {
        mutex.withLock {
            overrideDriver = driver
        }
    }

    fun currentDriver(): TokenPersistenceDriver? = runBlocking {
        mutex.withLock {
            overrideDriver ?: defaultDriver
        }
    }

    inline fun <T> withDriver(driver: TokenPersistenceDriver, crossinline block: () -> T): T = runBlocking {
        val previous = mutex.withLock {
            val existing = overrideDriver
            overrideDriver = driver
            existing
        }
        return@runBlocking try {
            block()
        } finally {
            mutex.withLock {
                overrideDriver = previous
            }
        }
    }
}
