package com.jalmarquest.backend.database

import com.jalmarquest.core.model.AuthPreferences
import com.jalmarquest.core.model.GuestProfile
import com.jalmarquest.core.model.SessionToken
import com.jalmarquest.core.state.auth.GuestAuthGateway
import com.jalmarquest.core.state.auth.GuestAuthResult
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID

class JsonGuestAuthGateway(
    private val storagePath: Path,
    private val json: Json = Json { prettyPrint = true; encodeDefaults = true }
) : GuestAuthGateway {
    private val mutex = Mutex()
    private var sessions = loadFromDisk()

    override suspend fun provisionGuest(displayName: String?): GuestAuthResult = mutex.withLock {
        val now = System.currentTimeMillis()
        val guestId = UUID.randomUUID().toString()
        val profile = GuestProfile(
            id = guestId,
            displayName = displayName?.takeIf { it.isNotBlank() } ?: defaultDisplayName(guestId),
            createdAtMillis = now
        )
        val token = SessionToken(
            value = UUID.randomUUID().toString(),
            issuedAtMillis = now,
            expiresAtMillis = null
        )
        val preferences = AuthPreferences()
        sessions[token.value] = PersistedSession(profile, token, preferences)
        persist()
        GuestAuthResult(profile = profile, token = token, preferences = preferences)
    }

    override suspend fun restoreGuest(token: SessionToken): GuestAuthResult? = mutex.withLock {
        sessions[token.value]?.let { persisted ->
            GuestAuthResult(persisted.profile, persisted.token, persisted.preferences)
        }
    }

    override suspend fun revokeGuestSession(token: SessionToken) {
        mutex.withLock {
            if (sessions.remove(token.value) != null) {
                persist()
            }
        }
    }

    private fun loadFromDisk(): MutableMap<String, PersistedSession> {
        if (!Files.exists(storagePath)) {
            storagePath.parent?.let { parent ->
                if (!Files.exists(parent)) {
                    Files.createDirectories(parent)
                }
            }
            Files.createFile(storagePath)
            return mutableMapOf()
        }
        val raw = Files.readString(storagePath)
        if (raw.isBlank()) return mutableMapOf()
        return try {
            val decoded = json.decodeFromString<PersistedSessions>(raw)
            decoded.sessions.associateBy { it.token.value }.toMutableMap()
        } catch (_: Exception) {
            mutableMapOf()
        }
    }

    private fun persist() {
        val payload = json.encodeToString(PersistedSessions(sessions.values.toList()))
        Files.writeString(storagePath, payload)
    }

    private fun defaultDisplayName(guestId: String): String = "Guest-${guestId.take(4)}"

    @Serializable
    private data class PersistedSessions(
        val sessions: List<PersistedSession>
    )

    @Serializable
    private data class PersistedSession(
        val profile: GuestProfile,
        val token: SessionToken,
        val preferences: AuthPreferences
    )
}
