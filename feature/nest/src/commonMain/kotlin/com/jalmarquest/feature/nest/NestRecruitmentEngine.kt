package com.jalmarquest.feature.nest

import com.jalmarquest.core.model.CritterRecruitmentOffer
import com.jalmarquest.core.model.CritterRole
import com.jalmarquest.core.model.CritterTemperament
import com.jalmarquest.core.model.NestCritter
import com.jalmarquest.core.model.NestState
import kotlin.math.max
import kotlin.random.Random

class NestRecruitmentEngine(
    private val random: Random = Random(0xC0FFEE),
    private val nameGenerator: CritterNameGenerator = CritterNameGenerator()
) {
    fun ensureOffers(state: NestState, now: Long, config: NestConfig): NestState {
        val active = state.recruitmentPool.filter { it.expiresAtMillis > now }
        val needed = config.maxRecruitmentOffers - active.size
        if (needed <= 0) {
            return state.copy(recruitmentPool = active)
        }
        val offers = buildList {
            addAll(active)
            val existingIds = active.mapTo(mutableSetOf()) { it.id }
            repeat(needed) {
                val offer = generateOffer(now, existingIds, config)
                existingIds += offer.id
                add(offer)
            }
        }
        return state.copy(recruitmentPool = offers)
    }

    private fun generateOffer(
        now: Long,
        existingIds: Set<String>,
        config: NestConfig
    ): CritterRecruitmentOffer {
        val offerId = nextId(existingIds)
        val role = CritterRole.values().random(random)
        val temperament = CritterTemperament.values().random(random)
        val name = nameGenerator.nextName(random)
        val cost = config.recruitment.baseSeedCost + randomCostIncrement(config.recruitment.variableSeedCost)
        val critter = NestCritter(
            id = "critter-$offerId",
            displayName = name,
            roleAffinity = role,
            temperament = temperament,
            traits = nameGenerator.sampleTraits(random)
        )
        return CritterRecruitmentOffer(
            id = "offer-$offerId",
            critter = critter,
            seedCost = cost,
            expiresAtMillis = now + config.recruitment.offerLifetimeMillis
        )
    }

    private fun nextId(existingIds: Set<String>): String {
        var candidate: String
        do {
            candidate = buildString {
                append(random.nextInt(10, 99))
                append('-')
                append(random.nextInt(100, 999))
            }
        } while ("offer-$candidate" in existingIds)
        return candidate
    }

    private fun randomCostIncrement(range: Int): Int {
        if (range <= 0) return 0
        return random.nextInt(max(range + 1, 1))
    }
}

class CritterNameGenerator {
    private val adjectives = listOf("Spry", "Pebbled", "Downy", "Sunspun", "Mossy", "Fleet")
    private val nouns = listOf("Wing", "Whistle", "Stride", "Glimmer", "Crest", "Twirl")
    private val traits = listOf("seed_savant", "stormwatcher", "burrow_buddy", "lullaby_looper", "glisten_guard")

    fun nextName(random: Random): String {
        val adjective = adjectives.random(random)
        val noun = nouns.random(random)
        return "$adjective $noun"
    }

    fun sampleTraits(random: Random): List<String> {
        val count = random.nextInt(1, minOf(traits.size, 3) + 1)
        return traits.shuffled(random).take(count)
    }
}
