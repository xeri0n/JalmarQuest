package com.jalmarquest.ui.app.utils

import androidx.compose.runtime.Composable
import com.jalmarquest.core.model.CosmeticRarity
import com.jalmarquest.core.model.HoardRankTier
import com.jalmarquest.core.model.ShinyRarity
import com.jalmarquest.ui.app.MR
import dev.icerock.moko.resources.compose.stringResource

/**
 * Localization mapping for enum values displayed in UI.
 * Provides composable and non-composable (MR key) variants.
 */

// ===== HOARD RANK TIER =====

@Composable
fun HoardRankTier.toLocalizedString(): String = when (this) {
    HoardRankTier.SCAVENGER -> stringResource(MR.strings.hoard_tier_scavenger)
    HoardRankTier.COLLECTOR -> stringResource(MR.strings.hoard_tier_collector)
    HoardRankTier.CURATOR -> stringResource(MR.strings.hoard_tier_curator)
    HoardRankTier.MAGNATE -> stringResource(MR.strings.hoard_tier_magnate)
    HoardRankTier.LEGEND -> stringResource(MR.strings.hoard_tier_legend)
    HoardRankTier.MYTH -> stringResource(MR.strings.hoard_tier_myth)
}

// ===== SHINY RARITY =====

@Composable
fun ShinyRarity.toLocalizedString(): String = when (this) {
    ShinyRarity.COMMON -> stringResource(MR.strings.rarity_common)
    ShinyRarity.UNCOMMON -> stringResource(MR.strings.rarity_uncommon)
    ShinyRarity.RARE -> stringResource(MR.strings.rarity_rare)
    ShinyRarity.EPIC -> stringResource(MR.strings.rarity_epic)
    ShinyRarity.LEGENDARY -> stringResource(MR.strings.rarity_legendary)
    ShinyRarity.MYTHIC -> stringResource(MR.strings.rarity_mythic)
}

// ===== COSMETIC RARITY =====

@Composable
fun CosmeticRarity.toLocalizedString(): String = when (this) {
    CosmeticRarity.COMMON -> stringResource(MR.strings.rarity_common)
    CosmeticRarity.UNCOMMON -> stringResource(MR.strings.rarity_uncommon)
    CosmeticRarity.RARE -> stringResource(MR.strings.rarity_rare)
    CosmeticRarity.EPIC -> stringResource(MR.strings.rarity_epic)
    CosmeticRarity.LEGENDARY -> stringResource(MR.strings.rarity_legendary)
}

/**
 * Note: Item names (cosmetic.name, item.name, location.name, etc.) should
 * eventually use nameKey → MR pattern. This is documented for future content
 * localization pass.
 * 
 * Effect types, ingredient rarities, and other game mechanics should be
 * reviewed on a case-by-case basis during future localization phases.
 */
