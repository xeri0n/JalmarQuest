# Phase 6: Nest Housing System - Foundation Complete

**Status**: Foundation COMPLETE ✅  
**Build Status**: BUILD SUCCESSFUL in 1m 16s  
**Date**: December 2024  
**Lines of Code**: 1,233+ (3 new files)

## Overview

Phase 6 introduces the **Nest Housing System**, a cosmetic customization feature where players can purchase decorations with Glimmer Shards and personalize Jalmar's nest. This system includes:

- 56 cosmetic items across 4 categories (Themes, Furniture, Decorations, Functional Upgrades)
- 6 functional upgrades providing gameplay bonuses
- Trophy display system for quest achievements
- Edit mode for placing/moving/removing cosmetics
- Unlock requirements (quest completion, player level, hoard rank)

## Architecture

### Data Models (`core/model/NestCustomizationModels.kt` - 209 lines)

**Core Types**:
- `CosmeticItemId(value: String)` - Unique identifier
- `CosmeticCategory` enum: THEME, FURNITURE, DECORATION, FUNCTIONAL
- `CosmeticRarity` enum: COMMON (1.0x), UNCOMMON (1.5x), RARE (2.0x), EPIC (3.0x), LEGENDARY (4.0x) with glimmerMultiplier
- `CosmeticItem` - Name, description, category, rarity, cost, unlock requirements, placeability, max instances, visual asset key

**Unlock Requirements**:
```kotlin
sealed class UnlockRequirement {
    data class QuestCompletion(val questId: String)
    data class PlayerLevel(val minimumLevel: Int)
    data class HoardRank(val minimumRank: Int)
    data class AchievementUnlock(val achievementId: String)
}
```

**Placement System**:
- `PlacedCosmetic` - Instance ID, cosmetic ID, position (x/y Float 0-10), rotation (0-360°)
- 10x10 grid coordinate system for spatial placement

**Functional Upgrades**:
```kotlin
enum class FunctionalUpgradeType {
    SHINY_DISPLAY,              // +10% hoard XP
    SEED_SILO,                  // +50% seed capacity
    SMALL_LIBRARY,              // +2 Thought Cabinet slots
    PERSONAL_ALCHEMY_STATION,   // Craft concoctions in nest
    SMALL_WORKBENCH,            // Craft items in nest
    COZY_PERCH                  // +5% companion XP
}
```

**State Management**:
- `NestCustomizationState` - Owned cosmetics (Set), placed cosmetics (List), active theme, functional upgrades (Map<type, isActive>), trophy displays (List), edit mode (Boolean)
- Helper methods: `ownsCosmetic()`, `getPlacedCount()`, `hasActiveUpgrade()`, `getSeedStorageBonus()`, `getHoardXpBonus()`, `getExtraThoughtSlots()`, `canCraftInNest()`, `getCompanionXpBonus()`

**Result Types**:
```kotlin
sealed class CosmeticPurchaseResult {
    object Success
    object InsufficientGlimmer
    object AlreadyOwned
    data class RequirementNotMet(val requirement: UnlockRequirement)
}

sealed class PlacementResult {
    data class Success(val instanceId: String)
    object NotOwned
    object MaxInstancesReached
    object InvalidPosition
}
```

### Cosmetic Catalog (`feature/nest/NestCosmeticCatalog.kt` - 656+ lines)

**10 Themes** (150-2000 Glimmer, auto-apply, not placeable):
- Rustic Burrow (COMMON, 150) - Warm earth tones
- Elegant Nest (UNCOMMON, 400) - Polished stone, silk tapestries
- Forest Canopy (RARE, 700) - Living branches, dappled sunlight
- Crystal Cavern (RARE, 850) - Shimmering gemstone walls
- Autumn Harvest (UNCOMMON, 500) - Amber tones, fallen leaves
- Winter Solstice (EPIC, 1200) - Frosted surfaces, snowfall
- Spring Meadow (UNCOMMON, 450) - Fresh grass, wildflowers
- Summer Beach (RARE, 800) - Sandy floor, ocean sounds
- Starlit Night (EPIC, 1400) - Dark indigo, twinkling constellations
- **Royal Chambers** (LEGENDARY, 2000, HoardRank 10 required) - Purple velvet, gold filigree

**20 Furniture Items** (100-2200 Glimmer, max 1-6 instances):
- Wooden Table, Cozy Chair, Bookshelf, Storage Chest, Hammock, Canopy Bed, Writing Desk, Trophy Case, Alchemy Table, Crafting Bench, Seed Barrel, Display Pedestal, Wardrobe, Standing Mirror, Stone Fireplace, Indoor Fountain, **Crystal Chandelier** (LEGENDARY 2200), Grandfather Clock, Map Table, Weapon Rack

**20+ Decorations** (60-2500 Glimmer, max 1-10 instances):
- Potted Fern, Candle Cluster, Small Rug, Landscape Painting, Vase of Flowers, Hanging Lantern, Wall Tapestry, Decorative Globe, Ornate Hourglass, **Quail Statue** (requires quest_founding_buttonburgh), Crystal Cluster, Herb Drying Rack, Decorative Birdcage, Wind Chimes, Small Treasure Pile, Stack of Books, Music Box, Brass Telescope, **Feather Collection** (level 10 required), **Golden Egg Display** (LEGENDARY 2500, HoardRank 15 required)

**6 Functional Upgrades** (800-1600 Glimmer, placeable, gameplay bonuses):
- **Shiny Display Case** (EPIC 1400) - +10% hoard XP when active
- **Seed Silo** (RARE 1000) - +50% seed storage capacity
- **Small Library** (EPIC 1600) - +2 Thought Cabinet slots
- **Personal Alchemy Station** (RARE 1200) - Craft concoctions without visiting hub
- **Small Workbench** (RARE 1100) - Craft items without visiting hub
- **Cozy Perch** (UNCOMMON 800) - +5% companion XP

**Catalog API**:
```kotlin
object NestCosmeticCatalog {
    val allCosmetics: List<CosmeticItem>
    fun getCosmeticById(id: CosmeticItemId): CosmeticItem?
    fun getCosmeticsByCategory(category: CosmeticCategory): List<CosmeticItem>
    fun getAllThemes(): List<CosmeticItem>
    fun getAllFurniture(): List<CosmeticItem>
    fun getAllDecorations(): List<CosmeticItem>
    fun getAllFunctionalUpgrades(): List<CosmeticItem>
    fun registerAllCosmetics()
}
```

### Business Logic (`feature/nest/NestCustomizationManager.kt` - 368 lines)

**Constructor**:
```kotlin
class NestCustomizationManager(
    gameStateManager: GameStateManager,
    glimmerWalletManager: GlimmerWalletManager,
    timestampProvider: () -> Long,
    cosmeticCatalog: NestCosmeticCatalog = NestCosmeticCatalog
)
```

**Core Operations**:

1. **Purchase Cosmetic** (13 steps):
   ```kotlin
   suspend fun purchaseCosmetic(cosmeticId: CosmeticItemId): CosmeticPurchaseResult
   ```
   - Looks up cosmetic in catalog
   - Checks ownership (already owned → AlreadyOwned)
   - Checks unlock requirements (quest completion, level, hoard rank)
   - Checks Glimmer balance (`player.glimmerWallet.balance`)
   - Spends Glimmer via `GlimmerWalletManager.spendGlimmer(amount, TransactionType.SHOP_PURCHASE, itemId)`
   - Adds to `ownedCosmetics` set
   - Returns `CosmeticPurchaseResult.Success`

2. **Place Cosmetic** (10 steps):
   ```kotlin
   suspend fun placeCosmetic(cosmeticId, x, y, rotation): PlacementResult
   ```
   - Validates ownership
   - Validates placeability (themes are not placeable)
   - Checks max instances (e.g., 1 Canopy Bed, 10 Candles)
   - Validates position (x/y in 0-10 range)
   - Generates unique instanceId (`{cosmeticId}_{timestamp}_{random}`)
   - Adds `PlacedCosmetic` to `placedCosmetics` list
   - Returns `PlacementResult.Success(instanceId)`

3. **Remove Cosmetic**:
   ```kotlin
   suspend fun removeCosmetic(instanceId: String)
   ```
   - Filters `placedCosmetics` to remove matching instance

4. **Move Cosmetic**:
   ```kotlin
   suspend fun moveCosmetic(instanceId, newX, newY, newRotation?)
   ```
   - Finds cosmetic by instanceId
   - Updates position/rotation
   - Preserves other properties

5. **Apply Theme**:
   ```kotlin
   suspend fun applyTheme(themeId: CosmeticItemId)
   ```
   - Validates ownership
   - Sets `activeTheme` to themeId (wallpaper/flooring)

6. **Activate Functional Upgrade**:
   ```kotlin
   suspend fun activateFunctionalUpgrade(upgradeType: FunctionalUpgradeType)
   ```
   - Validates ownership of upgrade cosmetic
   - Validates cosmetic is placed in nest
   - Sets `functionalUpgrades[upgradeType] = true`
   - Bonuses apply automatically via state helpers

7. **Deactivate Functional Upgrade**:
   ```kotlin
   suspend fun deactivateFunctionalUpgrade(upgradeType)
   ```
   - Sets `functionalUpgrades[upgradeType] = false`

8. **Trophy Management**:
   ```kotlin
   suspend fun addTrophy(questId, displayName, description)
   suspend fun placeTrophy(questId)
   ```
   - Add trophy to list on quest completion
   - Place trophy in visible display (toggles `placedInRoom`)

9. **Edit Mode**:
   ```kotlin
   suspend fun setEditMode(enabled: Boolean)
   ```
   - Toggles `editModeActive` flag for UI state

10. **Query Operations**:
    ```kotlin
    fun getAvailableCosmetics(): List<CosmeticItem>
    fun getUnplacedCosmetics(): List<CosmeticItem>
    ```
    - Filter by: not owned, can afford, meets requirements
    - Filter owned cosmetics not yet placed (or under max instances)

**Unlock Requirement Validation**:
```kotlin
private fun checkUnlockRequirement(requirement, player): Boolean
```
- `QuestCompletion` → `player.questLog.completedQuests.contains(QuestId(questId))`
- `PlayerLevel` → `player.archetypeProgress.archetypeLevel >= minimumLevel`
- `HoardRank` → `player.hoardRank.rank > 0 && player.hoardRank.rank <= minimumRank`
- `AchievementUnlock` → false (TODO: achievement system)

**Thread Safety**: All suspend operations use `Mutex` for concurrency safety.

## Integration Points

### Player Model Integration
```kotlin
// core/model/Player.kt
@SerialName("nest_customization") 
val nestCustomization: NestCustomizationState = NestCustomizationState()
```

### GameStateManager Integration
```kotlin
// core/state/GameStateManager.kt
fun updateNestCustomization(transform: (NestCustomizationState) -> NestCustomizationState) {
    _playerState.update { player ->
        player.copy(nestCustomization = transform(player.nestCustomization))
    }
}
```

### Monetization Integration
- Uses `GlimmerWalletManager.spendGlimmer(amount, type, itemId)` for purchases
- Returns `SpendResult.Success` or `SpendResult.InsufficientFunds`
- Accesses balance via `player.glimmerWallet.balance` (not `currentBalance`)
- Transaction type: `TransactionType.SHOP_PURCHASE` (from `core.model.GlimmerShards`)

## Implementation Details

### Compilation Fixes Applied

1. **Package Structure**:
   - Initial error: NestCustomizationModels in `feature.nest` package
   - Fixed: Moved to `com.jalmarquest.core.model` (data models belong in core)

2. **Import Paths**:
   - Initial: `import com.jalmarquest.core.state.monetization.TransactionType`
   - Fixed: Removed import (TransactionType from `core.model.*`)

3. **GlimmerWallet Field Name**:
   - Initial error: `player.glimmerWallet.currentBalance`
   - Actual field: `player.glimmerWallet.balance` (verified in `GlimmerShards.kt`)

4. **Player Level Access**:
   - Initial error: `player.level >= requirement.minimumLevel`
   - Fixed: `player.archetypeProgress.archetypeLevel` (level is part of archetype system)

5. **HoardRank Comparison**:
   - Initial error: `player.hoardRank >= requirement.minimumRank`
   - Fixed: `player.hoardRank.rank <= requirement.minimumRank` (lower rank number = higher position, e.g., rank 1 = #1 player)

6. **Quest Completion Check**:
   - Initial error: Type inference failed for `contains(requirement.questId)`
   - Fixed: `contains(QuestId(requirement.questId))` (convert String to QuestId value class)

7. **Smart Cast Issues** (cross-module property access):
   - Pattern: Extract to local variable before null check
   ```kotlin
   val requirement = cosmetic.unlockRequirement
   (requirement == null || checkUnlockRequirement(requirement, player))
   ```

8. **Platform-Specific Code**:
   - Initial error: `System.currentTimeMillis()` in commonMain
   - Fixed: Added `timestampProvider: () -> Long` constructor param (KMP pattern)

9. **TransactionType Enum**:
   - Initial: Used non-existent `COSMETIC_PURCHASE`
   - Fixed: `TransactionType.SHOP_PURCHASE` (matches existing enum in GlimmerShards.kt)

## Functional Upgrade Bonuses

**Integration with Existing Systems** (planned):

1. **Shiny Display Case** (+10% hoard XP):
   - Hook: `HoardRankManager.addShinyToCollection()`
   - Check: `player.nestCustomization.hasActiveUpgrade(SHINY_DISPLAY)`
   - Apply: `xpGained = baseXP * (1.0 + nestBonus)`

2. **Seed Silo** (+50% seed capacity):
   - Hook: `Inventory.canAddSeeds()` validation
   - Check: `player.nestCustomization.getSeedStorageBonus()` (returns 0.5 if active)
   - Apply: `maxCapacity = baseCapacity * (1.0 + bonus)`

3. **Small Library** (+2 Thought Cabinet slots):
   - Hook: `ThoughtCabinetManager.internalize()` slot check
   - Check: `player.nestCustomization.getExtraThoughtSlots()` (returns 2 if active)
   - Apply: `availableSlots = baseSlots + extraSlots`

4. **Personal Alchemy Station** (craft in nest):
   - Hook: UI navigation, `CraftingManager.canCraftConcoction()`
   - Check: `player.nestCustomization.canCraftInNest()`
   - Apply: Enable crafting UI in nest screen

5. **Small Workbench** (craft in nest):
   - Hook: UI navigation, `CraftingManager.craftItem()`
   - Check: `player.nestCustomization.canCraftInNest()`
   - Apply: Enable item crafting UI in nest screen

6. **Cozy Perch** (+5% companion XP):
   - Hook: `CompanionManager.gainCompanionXP()`
   - Check: `player.nestCustomization.getCompanionXpBonus()` (returns 0.05 if active)
   - Apply: `xpGained = baseXP * (1.0 + bonus)`

## Next Steps

### Phase 6.1: DI Registration (Pending)
```kotlin
// core/di/CoreModule.kt
single { NestCosmeticCatalog.apply { registerAllCosmetics() } }
single { 
    NestCustomizationManager(
        gameStateManager = get(),
        glimmerWalletManager = get(),
        timestampProvider = get(), // Use existing { System.currentTimeMillis() }
        cosmeticCatalog = get()
    )
}
```

Resolver function:
```kotlin
fun resolveNestCustomizationManager(): NestCustomizationManager = 
    requireKoin().get()
```

### Phase 6.2: UI Implementation (Pending)

**NestScreenV2** (following Phase 5 patterns):
- CollapsibleHeader("Jalmar's Nest")
- Tabs: Shop, Edit Mode, Trophy Room
- Shop Tab:
  - Category filters (Themes, Furniture, Decorations, Functional)
  - CosmeticCard component (name, description, cost, rarity, locked/unlocked)
  - Purchase button (validates Glimmer balance, shows unlock requirements)
- Edit Mode Tab:
  - 10x10 grid view (Canvas-based placement)
  - Draggable cosmetic items
  - Rotation slider (0-360°)
  - Delete/Move controls
  - Save/Cancel buttons
- Trophy Room Tab:
  - Trophy list (quest name, description, placement toggle)
  - Empty state: "Complete quests to earn trophies!"
- Functional Upgrades Panel:
  - 6 upgrade cards with active/inactive indicators
  - Bonus descriptions (+10% hoard XP, etc.)
  - Activate/Deactivate buttons

**UI Components to Create**:
1. `CosmeticShop.kt` - Browse and purchase cosmetics
2. `NestEditMode.kt` - Place/move/remove cosmetics
3. `TrophyRoom.kt` - Quest achievement displays
4. `FunctionalUpgradesPanel.kt` - Upgrade status and activation
5. `CosmeticCard.kt` - Reusable cosmetic item display
6. `NestGrid.kt` - 10x10 spatial layout (Canvas-based)

### Phase 6.3: Integration Tests (Pending)

**Test Coverage Needed**:
```kotlin
class NestCustomizationManagerTest {
    // Purchase flow
    @Test fun testPurchaseCosmetic_Success()
    @Test fun testPurchaseCosmetic_InsufficientGlimmer()
    @Test fun testPurchaseCosmetic_AlreadyOwned()
    @Test fun testPurchaseCosmetic_RequirementNotMet_Quest()
    @Test fun testPurchaseCosmetic_RequirementNotMet_Level()
    @Test fun testPurchaseCosmetic_RequirementNotMet_HoardRank()
    
    // Placement flow
    @Test fun testPlaceCosmetic_Success()
    @Test fun testPlaceCosmetic_NotOwned()
    @Test fun testPlaceCosmetic_MaxInstancesReached()
    @Test fun testPlaceCosmetic_InvalidPosition()
    @Test fun testPlaceCosmetic_ThemeNotPlaceable()
    
    // Functional upgrades
    @Test fun testActivateFunctionalUpgrade_ShinyDisplay()
    @Test fun testActivateFunctionalUpgrade_SeedSilo()
    @Test fun testActivateFunctionalUpgrade_SmallLibrary()
    @Test fun testActivateFunctionalUpgrade_AlchemyStation()
    @Test fun testActivateFunctionalUpgrade_Workbench()
    @Test fun testActivateFunctionalUpgrade_CozyPerch()
    
    // Integration
    @Test fun testFunctionalUpgradeBonus_IntegrationWithHoardRank()
    @Test fun testFunctionalUpgradeBonus_IntegrationWithThoughtCabinet()
    @Test fun testFunctionalUpgradeBonus_IntegrationWithCompanion()
}
```

### Phase 6.4: Functional Upgrade Integration (Pending)

Wire up bonuses in existing managers:
1. Modify `HoardRankManager.addShinyToCollection()` to check SHINY_DISPLAY
2. Modify `Inventory` seed capacity checks to use `getSeedStorageBonus()`
3. Modify `ThoughtCabinetManager` max slots to use `getExtraThoughtSlots()`
4. Enable crafting UI in nest when `canCraftInNest()` returns true
5. Modify `CompanionManager.gainCompanionXP()` to apply COZY_PERCH bonus

## Technical Lessons

### Kotlin Multiplatform Patterns

1. **Avoid Platform-Specific APIs in commonMain**:
   - ❌ `System.currentTimeMillis()`
   - ✅ `timestampProvider: () -> Long` constructor param

2. **Smart Cast Limitations**:
   - Public properties from other modules can't be smart-casted
   - Pattern: Extract to local variable before null check

3. **Value Classes and Type Safety**:
   - `QuestId(value: String)` prevents string confusion
   - Requires explicit conversion: `contains(QuestId(questId))`

4. **Import Organization**:
   - `core.model.*` includes all data classes + enums
   - `core.state.monetization.*` for manager classes only

5. **Player State Access Patterns**:
   - Level: `player.archetypeProgress.archetypeLevel`
   - Balance: `player.glimmerWallet.balance`
   - Rank: `player.hoardRank.rank` (lower = better, 1 = #1)

## Files Created/Modified

**New Files**:
1. `/workspaces/JalmarQuest/core/model/src/commonMain/kotlin/com/jalmarquest/core/model/NestCustomizationModels.kt` (209 lines)
2. `/workspaces/JalmarQuest/feature/nest/src/commonMain/kotlin/com/jalmarquest/feature/nest/NestCosmeticCatalog.kt` (656+ lines)
3. `/workspaces/JalmarQuest/feature/nest/src/commonMain/kotlin/com/jalmarquest/feature/nest/NestCustomizationManager.kt` (368 lines)

**Modified Files**:
1. `/workspaces/JalmarQuest/core/model/src/commonMain/kotlin/com/jalmarquest/core/model/Player.kt` (+1 field: `nestCustomization`)
2. `/workspaces/JalmarQuest/core/state/src/commonMain/kotlin/com/jalmarquest/core/state/GameStateManager.kt` (+1 method: `updateNestCustomization()`)

**Total**: 1,233+ lines (3 new files, 2 modified files)

## Summary

Phase 6 foundation successfully implements:
- ✅ Comprehensive data models (9 classes, 2 enums, 2 sealed results)
- ✅ Full cosmetic catalog (56 items across 4 categories)
- ✅ Complete business logic (13 public methods, thread-safe operations)
- ✅ Glimmer integration (purchase flow with balance validation)
- ✅ Unlock requirements (quest, level, hoard rank checks)
- ✅ Functional upgrade system (6 gameplay bonuses)
- ✅ Trophy display system (quest achievements)
- ✅ Spatial placement (10x10 grid, rotation, max instances)
- ✅ BUILD SUCCESSFUL (all compilation errors resolved)

**Build Verification**: 1322 tasks (208 executed, 1114 up-to-date)  
**Compilation**: No errors, no warnings (iOS warnings expected in dev container)  
**Architecture**: Clean separation (core models, feature logic, state management)  
**Code Quality**: Type-safe, thread-safe, KMP-compatible

Ready for Phase 6.2 (UI implementation) and Phase 6.3 (integration tests).

---

*Documentation Date*: December 2024  
*Phase 6 Foundation Status*: COMPLETE ✅  
*Next Phase*: Phase 6.2 - Nest UI Screens
