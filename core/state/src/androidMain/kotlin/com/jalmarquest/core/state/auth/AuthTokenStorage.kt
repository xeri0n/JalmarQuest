package com.jalmarquest.core.state.auth

import android.content.Context
import android.content.SharedPreferences
import com.jalmarquest.core.model.SessionToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.coroutines.cancellation.CancellationException

private const val PREFS_NAME = "com.jalmarquest.auth"
private const val TOKEN_KEY = "session_token"

actual class AuthTokenStorage {
    private val mutex = Mutex()
    private var cached: SessionToken? = null
    private val fallbackDriver = InMemoryTokenPersistenceDriver()

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
        AuthTokenStorageHooks.currentDriver() ?: fallbackDriver
}

fun installAndroidAuthTokenStorage(context: Context) {
    val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    AuthTokenStorageHooks.installDefault(SharedPreferencesTokenPersistenceDriver(prefs))
}

private class SharedPreferencesTokenPersistenceDriver(
    private val preferences: SharedPreferences
) : TokenPersistenceDriver {
    override suspend fun readToken(): SessionToken? = withContext(Dispatchers.IO) {
        val raw = preferences.getString(TOKEN_KEY, null) ?: return@withContext null
        if (raw.isBlank()) return@withContext null
        try {
            json.decodeFromString<SessionToken>(raw)
        } catch (_: Throwable) {
            null
        }
    }

    override suspend fun writeToken(token: SessionToken?) {
        withContext(Dispatchers.IO) {
            val editor = preferences.edit()
            if (token == null) {
                editor.remove(TOKEN_KEY)
            } else {
                editor.putString(TOKEN_KEY, json.encodeToString(token))
            }
            editor.apply()
        }
    }
}

private class InMemoryTokenPersistenceDriver : TokenPersistenceDriver {
    private var snapshot: SessionToken? = null

    override suspend fun readToken(): SessionToken? = snapshot

    override suspend fun writeToken(token: SessionToken?) {
        snapshot = token
    }
}

private val json: Json = Json {
    encodeDefaults = true
    ignoreUnknownKeys = true
}
