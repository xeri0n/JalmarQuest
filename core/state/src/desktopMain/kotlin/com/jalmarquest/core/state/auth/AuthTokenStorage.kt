package com.jalmarquest.core.state.auth

import com.jalmarquest.core.model.SessionToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.coroutines.cancellation.CancellationException

private const val ENV_PATH = "JALMARQUEST_AUTH_TOKEN_PATH"
private const val SYSTEM_PROPERTY_PATH = "jalmarquest.authToken.path"

actual class AuthTokenStorage {
    private val mutex = Mutex()
    private var cached: SessionToken? = null
    private val platformDriver by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        JvmFileTokenPersistenceDriver(resolveStoragePath())
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

private fun resolveStoragePath(): Path {
    val override = System.getProperty(SYSTEM_PROPERTY_PATH)?.takeIf { it.isNotBlank() }
        ?: System.getenv(ENV_PATH)?.takeIf { it.isNotBlank() }
    return override?.let { Paths.get(it) } ?: defaultStoragePath()
}

private fun defaultStoragePath(): Path {
    val home = System.getProperty("user.home").orEmpty()
    return Paths.get(home, ".jalmarquest", "auth_token.json")
}

private class JvmFileTokenPersistenceDriver(
    private val storagePath: Path
) : TokenPersistenceDriver {
    override suspend fun readToken(): SessionToken? = withContext(Dispatchers.IO) {
        try {
            if (!Files.exists(storagePath)) return@withContext null
            val raw = Files.readString(storagePath)
            if (raw.isBlank()) return@withContext null
            json.decodeFromString<SessionToken>(raw)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Throwable) {
            null
        }
    }

    override suspend fun writeToken(token: SessionToken?) {
        withContext(Dispatchers.IO) {
            if (token == null) {
                Files.deleteIfExists(storagePath)
            } else {
                storagePath.parent?.let { parent ->
                    if (!Files.exists(parent)) {
                        Files.createDirectories(parent)
                    }
                }
                val payload = json.encodeToString(token)
                Files.writeString(storagePath, payload)
            }
        }
    }
}

private val json: Json = Json {
    encodeDefaults = true
    ignoreUnknownKeys = true
}
