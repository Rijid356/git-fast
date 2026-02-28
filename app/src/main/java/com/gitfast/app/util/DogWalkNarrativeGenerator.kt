package com.gitfast.app.util

import com.gitfast.app.data.model.DogWalkEvent
import com.gitfast.app.data.model.DogWalkEventType

object DogWalkNarrativeGenerator {

    // --- Word pools ---

    private val QUEST_TYPES = listOf(
        "quest", "expedition", "patrol", "adventure", "campaign", "raid", "mission"
    )

    private val OPENERS = listOf(
        "Quest complete!", "Adventure logged!", "Mission report:", "Field notes:"
    )

    private val QUIET_OPENERS = listOf(
        "All clear!", "Recon complete!", "Peaceful patrol!"
    )

    private val QUIET_ACTIONS = listOf(
        "kept watch over the realm",
        "surveyed the land undisturbed",
        "held the line with no encounters"
    )

    private val JUNIPER_REFS = listOf(
        "Juniper", "the party leader", "our brave explorer", "the adventurer"
    )

    // Verb pools per event type — each pool MUST contain the required test keywords
    private val SNACK_VERBS = listOf("discovered", "looted", "scavenged", "found")
    private val SNIFF_VERBS = listOf("investigated", "scouted", "surveyed", "analyzed")
    private val SQUIRREL_VERBS = listOf("chased", "pursued", "engaged", "hunted")
    private val FRIENDLY_DOG_VERBS = listOf("recruited", "allied with", "befriended", "made friends with")
    private val POOP_VERBS = listOf("made a pit stop", "completed a pit stop", "took a pit stop")
    private val PEE_VERBS = listOf("claimed territory", "marked territory")
    private val WATER_VERBS = listOf("took a water break", "hit the hydration station for a water break", "refueled with a water break")
    private val BARK_VERBS = listOf("barked at", "sounded the alarm and barked at", "called out and barked at")
    private val LEASH_VERBS = listOf("pulled on the leash", "surged forward on the leash", "tested the leash")
    private val HUMAN_VERBS = listOf("greeted a human friend", "greeted and charmed a human friend", "greeted and befriended a human friend")

    // Hype openers for single-event narratives
    private val HYPE_OPENERS = mapOf(
        DogWalkEventType.SNACK_FOUND to listOf("Loot acquired!", "Foraging frenzy!"),
        DogWalkEventType.DEEP_SNIFF to listOf("Intel gathered!", "Recon mode!"),
        DogWalkEventType.SQUIRREL_CHASE to listOf("BOSS ENCOUNTER!", "Target spotted!"),
        DogWalkEventType.FRIENDLY_DOG to listOf("Alliance formed!", "New ally!"),
        DogWalkEventType.POOP to listOf("Pit stop logged!", "Mission pause!"),
        DogWalkEventType.PEE to listOf("Territory claimed!", "Land marked!"),
        DogWalkEventType.WATER_BREAK to listOf("Hydration check!", "Refuel complete!"),
        DogWalkEventType.BARK_REACT to listOf("Alert sounded!", "Alarm triggered!"),
        DogWalkEventType.LEASH_PULL to listOf("Full throttle!", "Power surge!"),
        DogWalkEventType.HUMAN_FRIEND to listOf("Diplomacy!", "Social encounter!"),
    )

    fun generateNarrative(events: List<DogWalkEvent>, durationMinutes: Int): String {
        if (events.isEmpty()) {
            return zeroEventNarrative(durationMinutes)
        }

        val counts = events.groupBy { it.eventType }.mapValues { it.value.size }

        if (counts.size == 1) {
            return singleTypeNarrative(counts.entries.first(), durationMinutes)
        }

        return multiEventNarrative(counts, durationMinutes)
    }

    private fun zeroEventNarrative(durationMinutes: Int): String {
        val opener = QUIET_OPENERS.random()
        val ref = JUNIPER_REFS.random()
        val action = QUIET_ACTIONS.random()
        val quest = QUEST_TYPES.random()
        return "$opener $ref $action on a $durationMinutes-minute $quest!"
    }

    private fun singleTypeNarrative(
        entry: Map.Entry<DogWalkEventType, Int>,
        durationMinutes: Int,
    ): String {
        val (type, count) = entry
        val hype = HYPE_OPENERS[type]?.random() ?: OPENERS.random()
        val ref = JUNIPER_REFS.random()
        val quest = QUEST_TYPES.random()
        val action = verbForEvent(type, count)
        return "$hype $ref $action on a $durationMinutes-minute $quest!"
    }

    private fun multiEventNarrative(
        counts: Map<DogWalkEventType, Int>,
        durationMinutes: Int,
    ): String {
        val opener = OPENERS.random()
        val ref = JUNIPER_REFS.random()
        val quest = QUEST_TYPES.random()

        val parts = buildActionParts(counts)
        val joined = joinNarrative(parts)

        val combo = comboPrefix(counts)
        return "$combo$opener $ref $joined on a $durationMinutes-minute $quest!"
    }

    private fun buildActionParts(counts: Map<DogWalkEventType, Int>): List<String> {
        val parts = mutableListOf<String>()

        // Foraging
        val snacks = counts[DogWalkEventType.SNACK_FOUND] ?: 0
        val sniffs = counts[DogWalkEventType.DEEP_SNIFF] ?: 0
        val waterBreaks = counts[DogWalkEventType.WATER_BREAK] ?: 0
        if (snacks > 0) parts.add(verbForEvent(DogWalkEventType.SNACK_FOUND, snacks))
        if (sniffs > 0) parts.add(verbForEvent(DogWalkEventType.DEEP_SNIFF, sniffs))
        if (waterBreaks > 0) parts.add(verbForEvent(DogWalkEventType.WATER_BREAK, waterBreaks))

        // Bathroom — poop+pee combo merges into "bathroom break"
        val poops = counts[DogWalkEventType.POOP] ?: 0
        val pees = counts[DogWalkEventType.PEE] ?: 0
        if (poops > 0 && pees > 0) {
            parts.add("took ${pluralize(poops + pees, "bathroom break")}")
        } else if (poops > 0) {
            parts.add(verbForEvent(DogWalkEventType.POOP, poops))
        } else if (pees > 0) {
            parts.add(verbForEvent(DogWalkEventType.PEE, pees))
        }

        // Energy
        val chases = counts[DogWalkEventType.SQUIRREL_CHASE] ?: 0
        val pulls = counts[DogWalkEventType.LEASH_PULL] ?: 0
        if (chases > 0) parts.add(verbForEvent(DogWalkEventType.SQUIRREL_CHASE, chases))
        if (pulls > 0) parts.add(verbForEvent(DogWalkEventType.LEASH_PULL, pulls))

        // Social
        val dogs = counts[DogWalkEventType.FRIENDLY_DOG] ?: 0
        val humans = counts[DogWalkEventType.HUMAN_FRIEND] ?: 0
        val barks = counts[DogWalkEventType.BARK_REACT] ?: 0
        if (dogs > 0) parts.add(verbForEvent(DogWalkEventType.FRIENDLY_DOG, dogs))
        if (humans > 0) parts.add(verbForEvent(DogWalkEventType.HUMAN_FRIEND, humans))
        if (barks > 0) parts.add(verbForEvent(DogWalkEventType.BARK_REACT, barks))

        return parts
    }

    private fun verbForEvent(type: DogWalkEventType, count: Int): String {
        return when (type) {
            DogWalkEventType.SNACK_FOUND -> "${SNACK_VERBS.random()} ${pluralize(count, "snack")}"
            DogWalkEventType.DEEP_SNIFF -> "${SNIFF_VERBS.random()} ${pluralize(count, "deep sniff")}"
            DogWalkEventType.SQUIRREL_CHASE -> "${SQUIRREL_VERBS.random()} ${pluralize(count, "squirrel")}"
            DogWalkEventType.FRIENDLY_DOG -> "${FRIENDLY_DOG_VERBS.random()} ${pluralize(count, "dog friend")}"
            DogWalkEventType.POOP -> POOP_VERBS.random()
            DogWalkEventType.PEE -> PEE_VERBS.random()
            DogWalkEventType.WATER_BREAK -> WATER_VERBS.random()
            DogWalkEventType.BARK_REACT -> "${BARK_VERBS.random()} ${pluralize(count, "thing")}"
            DogWalkEventType.LEASH_PULL -> "${LEASH_VERBS.random()} ${pluralize(count, "time")}"
            DogWalkEventType.HUMAN_FRIEND -> HUMAN_VERBS.random()
        }
    }

    private fun comboPrefix(counts: Map<DogWalkEventType, Int>): String {
        val hasSquirrel = (counts[DogWalkEventType.SQUIRREL_CHASE] ?: 0) > 0
        val hasLeashPull = (counts[DogWalkEventType.LEASH_PULL] ?: 0) > 0
        val socialCount = (counts[DogWalkEventType.FRIENDLY_DOG] ?: 0) +
            (counts[DogWalkEventType.HUMAN_FRIEND] ?: 0)
        val snackCount = counts[DogWalkEventType.SNACK_FOUND] ?: 0
        val hasPoop = (counts[DogWalkEventType.POOP] ?: 0) > 0
        val hasPee = (counts[DogWalkEventType.PEE] ?: 0) > 0

        return when {
            hasSquirrel && hasLeashPull -> "Berserker mode! "
            socialCount >= 3 -> "Diplomacy maxed! "
            snackCount >= 3 -> "Legendary foraging run! "
            hasPoop && hasPee -> ""  // bathroom combo already handled in text
            else -> ""
        }
    }

    private fun pluralize(count: Int, singular: String, plural: String? = null): String {
        val word = if (count == 1) singular else (plural ?: "${singular}s")
        return "$count $word"
    }

    private fun joinNarrative(parts: List<String>): String {
        return when (parts.size) {
            0 -> "had a quiet walk"
            1 -> parts[0]
            2 -> "${parts[0]} and ${parts[1]}"
            else -> parts.dropLast(1).joinToString(", ") + ", and ${parts.last()}"
        }
    }
}
