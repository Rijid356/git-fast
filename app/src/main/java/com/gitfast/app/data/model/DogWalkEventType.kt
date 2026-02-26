package com.gitfast.app.data.model

enum class EventCategory {
    FORAGING, BATHROOM, ENERGY, SOCIAL
}

enum class DogWalkEventType(
    val displayName: String,
    val icon: String,
    val category: EventCategory,
) {
    SNACK_FOUND("Snack Found", "🦴", EventCategory.FORAGING),
    POOP("Poop", "💩", EventCategory.BATHROOM),
    PEE("Pee", "💧", EventCategory.BATHROOM),
    DEEP_SNIFF("Deep Sniff", "🐾", EventCategory.FORAGING),
    SQUIRREL_CHASE("Squirrel Chase", "🐿️", EventCategory.ENERGY),
    FRIENDLY_DOG("Friendly Dog", "🐶", EventCategory.SOCIAL),
    ZOOMIES("Zoomies", "⚡", EventCategory.ENERGY),
    WATER_BREAK("Water Break", "💦", EventCategory.FORAGING),
    BARK_REACT("Bark/React", "🗣️", EventCategory.SOCIAL),
}
