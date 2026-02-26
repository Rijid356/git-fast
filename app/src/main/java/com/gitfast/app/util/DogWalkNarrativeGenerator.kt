package com.gitfast.app.util

import com.gitfast.app.data.model.DogWalkEvent
import com.gitfast.app.data.model.DogWalkEventType
import com.gitfast.app.data.model.EventCategory

object DogWalkNarrativeGenerator {

    fun generateNarrative(events: List<DogWalkEvent>, durationMinutes: Int): String {
        if (events.isEmpty()) {
            return zeroEventNarrative(durationMinutes)
        }

        val counts = events.groupBy { it.eventType }.mapValues { it.value.size }

        // Check for special combos first
        val hasSquirrelChase = (counts[DogWalkEventType.SQUIRREL_CHASE] ?: 0) > 0
        val hasZoomies = (counts[DogWalkEventType.ZOOMIES] ?: 0) > 0

        if (counts.size == 1) {
            return singleTypeNarrative(counts.entries.first(), durationMinutes)
        }

        val parts = mutableListOf<String>()

        // Foraging
        val snacks = counts[DogWalkEventType.SNACK_FOUND] ?: 0
        val sniffs = counts[DogWalkEventType.DEEP_SNIFF] ?: 0
        val waterBreaks = counts[DogWalkEventType.WATER_BREAK] ?: 0
        if (snacks > 0) parts.add("found ${pluralize(snacks, "snack")}")
        if (sniffs > 0) parts.add("investigated ${pluralize(sniffs, "interesting spot")}")
        if (waterBreaks > 0) parts.add("took ${pluralize(waterBreaks, "water break")}")

        // Bathroom
        val poops = counts[DogWalkEventType.POOP] ?: 0
        val pees = counts[DogWalkEventType.PEE] ?: 0
        if (poops > 0 && pees > 0) {
            parts.add("took ${pluralize(poops + pees, "bathroom break")}")
        } else if (poops > 0) {
            parts.add("made ${pluralize(poops, "pit stop")}")
        } else if (pees > 0) {
            parts.add("marked ${pluralize(pees, "territory", "territories")}")
        }

        // Energy - special combo
        if (hasSquirrelChase && hasZoomies) {
            parts.add("spotted a squirrel and went full zoomie mode")
        } else {
            val chases = counts[DogWalkEventType.SQUIRREL_CHASE] ?: 0
            val zoomies = counts[DogWalkEventType.ZOOMIES] ?: 0
            if (chases > 0) parts.add("chased ${pluralize(chases, "squirrel")}")
            if (zoomies > 0) parts.add("had ${pluralize(zoomies, "zoomie burst")}")
        }
        val pulls = counts[DogWalkEventType.LEASH_PULL] ?: 0
        if (pulls > 0) parts.add("pulled on the leash ${pluralize(pulls, "time")}")

        // Social
        val dogs = counts[DogWalkEventType.FRIENDLY_DOG] ?: 0
        val barks = counts[DogWalkEventType.BARK_REACT] ?: 0
        if (dogs > 0) parts.add("made ${pluralize(dogs, "friend")}")
        if (barks > 0) parts.add("barked at ${pluralize(barks, "thing")}")

        val joined = joinNarrative(parts)
        return "Juniper $joined on today's $durationMinutes-minute walk!"
    }

    private fun zeroEventNarrative(durationMinutes: Int): String {
        val templates = listOf(
            "A peaceful $durationMinutes-minute walk - Juniper was on her best behavior today!",
            "Juniper enjoyed a calm $durationMinutes-minute stroll with no drama!",
            "A quiet $durationMinutes-minute walk - Juniper was all business today!",
        )
        return templates.random()
    }

    private fun singleTypeNarrative(
        entry: Map.Entry<DogWalkEventType, Int>,
        durationMinutes: Int,
    ): String {
        val (type, count) = entry
        return when (type) {
            DogWalkEventType.DEEP_SNIFF -> "Juniper was laser-focused on sniffing - ${pluralize(count, "deep sniff")} and nothing else on this $durationMinutes-minute walk!"
            DogWalkEventType.SNACK_FOUND -> "Juniper was in foraging mode - ${pluralize(count, "snack")} found in $durationMinutes minutes!"
            DogWalkEventType.SQUIRREL_CHASE -> "Squirrel alert! Juniper chased ${pluralize(count, "squirrel")} in $durationMinutes minutes!"
            DogWalkEventType.ZOOMIES -> "Pure energy! ${pluralize(count, "zoomie burst")} in $durationMinutes minutes!"
            DogWalkEventType.FRIENDLY_DOG -> "Social butterfly! Juniper met ${pluralize(count, "friendly dog")} on a $durationMinutes-minute walk!"
            DogWalkEventType.POOP -> "All business - ${pluralize(count, "pit stop")} on a $durationMinutes-minute walk!"
            DogWalkEventType.PEE -> "Juniper marked ${pluralize(count, "territory", "territories")} in $durationMinutes minutes!"
            DogWalkEventType.WATER_BREAK -> "Hydration hero! Juniper took ${pluralize(count, "water break")} in $durationMinutes minutes!"
            DogWalkEventType.BARK_REACT -> "Juniper had opinions - barked at ${pluralize(count, "thing")} in $durationMinutes minutes!"
            DogWalkEventType.LEASH_PULL -> "Juniper was strong-willed - ${pluralize(count, "leash pull")} in $durationMinutes minutes!"
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
