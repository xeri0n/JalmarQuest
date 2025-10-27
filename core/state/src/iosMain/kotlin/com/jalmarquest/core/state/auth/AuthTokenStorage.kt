package com.jalmarquest.core.state.auth

import com.jalmarquest.core.model.SessionToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import platform.Foundation.NSUserDefaults
import platform.Foundation.removeObjectForKey
import platform.Foundation.setObject
import platform.Foundation.stringForKey
import platform.Foundation.synchronize
import kotlin.coroutines.cancellation.CancellationException

private const val TOKEN_KEY = "com.jalmarquest.auth.session_token"

actual class AuthTokenStorage {
    private val mutex = Mutex()
    private var cached: SessionToken? = null
    private val platformDriver by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        IosUserDefaultsTokenPersistenceDriver(NSUserDefaults.standardUserDefaults())
    }

    actual suspend fun loadToken(): SessionToken? {
        val existing = mutex.withLock { cached }
        if (existing != null) return existing

        val driver = resolveDriver()
        val loaded = try {
            driver.readToken()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Throwable) {
            null
        }

        return mutex.withLock {
            cached = loaded
            loaded
        }
    }

    actual suspend fun saveToken(token: SessionToken?) {
        val driver = resolveDriver()
        driver.writeToken(token)
        mutex.withLock { cached = token }
    }

    private fun resolveDriver(): TokenPersistenceDriver =
        AuthTokenStorageHooks.currentDriver() ?: platformDriver
}

private class IosUserDefaultsTokenPersistenceDriver(
    private val defaults: NSUserDefaults
) : TokenPersistenceDriver {
    override suspend fun readToken(): SessionToken? = withContext(Dispatchers.Default) {
        val raw = defaults.stringForKey(TOKEN_KEY) ?: return@withContext null
        if (raw.isBlank()) return@withContext null
        try {
            json.decodeFromString<SessionToken>(raw)
        } catch (_: Throwable) {
            null
        }
    }

    override suspend fun writeToken(token: SessionToken?) {
        withContext(Dispatchers.Default) {
            if (token == null) {
                defaults.removeObjectForKey(TOKEN_KEY)
            } else {
                val payload = json.encodeToString(token)
                defaults.setObject(payload, forKey = TOKEN_KEY)
            }
            defaults.synchronize()
        }
    }
}

private val json: Json = Json {
    encodeDefaults = true
    ignoreUnknownKeys = true
}
