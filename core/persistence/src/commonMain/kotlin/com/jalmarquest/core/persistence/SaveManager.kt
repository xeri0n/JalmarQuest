class SaveManager(
    private val storage: SaveStorage,
    private val serializer: SaveSerializer
) {
    // FIX: Add save versioning and migration support
    suspend fun saveGame(player: Player): SaveResult {
        return try {
            val saveData = SaveData(
                version = CURRENT_SAVE_VERSION,
                player = player,
                timestamp = currentTimeMillis(),
                checksum = calculateChecksum(player)
            )
            
            val json = serializer.serialize(saveData)
            
            // FIX: Create backup before overwriting
            storage.backupExistingSave()
            
            storage.writeSave(json)
            
            SaveResult.Success
        } catch (e: Exception) {
            // FIX: Log detailed error for debugging
            Logger.error("Save failed", e)
            SaveResult.Failure(e.message ?: "Unknown error")
        }
    }
    
    suspend fun loadGame(): LoadResult {
        return try {
            val json = storage.readSave()
                ?: return LoadResult.NoSaveFound
            
            val saveData = serializer.deserialize(json)
            
            // FIX: Validate checksum
            if (!validateChecksum(saveData)) {
                Logger.warn("Save checksum mismatch - possible corruption")
                // Attempt recovery from backup
                return loadFromBackup()
            }
            
            // FIX: Handle version migration
            val migratedPlayer = if (saveData.version < CURRENT_SAVE_VERSION) {
                migrateSave(saveData.player, saveData.version)
            } else {
                saveData.player
            }
            
            LoadResult.Success(migratedPlayer)
        } catch (e: Exception) {
            Logger.error("Load failed", e)
            // FIX: Attempt recovery before failing
            loadFromBackup()
        }
    }
    
    private suspend fun loadFromBackup(): LoadResult {
        return try {
            val backupJson = storage.readBackup()
                ?: return LoadResult.CorruptedSave("No backup available")
            
            val saveData = serializer.deserialize(backupJson)
            LoadResult.Success(saveData.player)
        } catch (e: Exception) {
            LoadResult.CorruptedSave("Backup also corrupted: ${e.message}")
        }
    }
    
    private fun calculateChecksum(player: Player): String {
        // Simple checksum based on key player data
        val data = "${player.id}${player.experience}${player.choiceLog.entries.size}"
        return data.hashCode().toString()
    }
    
    private fun validateChecksum(saveData: SaveData): Boolean {
        return calculateChecksum(saveData.player) == saveData.checksum
    }
    
    private fun migrateSave(player: Player, fromVersion: Int): Player {
        var migrated = player
        
        // Apply migrations sequentially
        if (fromVersion < 2) {
            // Migration from v1 to v2
            migrated = migrated.copy(
                // Add new fields with defaults
                companions = emptyList()
            )
        }
        
        if (fromVersion < 3) {
            // Migration from v2 to v3
            migrated = migrated.copy(
                thoughtCabinet = ThoughtCabinet()
            )
        }
        
        return migrated
    }
    
    companion object {
        private const val CURRENT_SAVE_VERSION = 3
    }
}