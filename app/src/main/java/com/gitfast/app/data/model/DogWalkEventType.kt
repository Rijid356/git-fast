package com.gitfast.app.data.model

enum class EventCategory {
    FORAGING, BATHROOM, ENERGY, SOCIAL
}

enum class DogWalkEventType(
    val displayName: String,
    val icon: String,
    val category: EventCategory,
    val shortLabel: String,
) {
    SNACK_FOUND("Snack Found", "🍖", EventCategory.FORAGING, "Snack"),
    POOP("Poop", "💩", EventCategory.BATHROOM, "Poop"),
    PEE("Pee", "💧", EventCategory.BATHROOM, "Pee"),
    DEEP_SNIFF("Deep Sniff", "🐾", EventCategory.FORAGING, "Sniff"),
    SQUIRREL_CHASE("Squirrel Chase", "🐿️", EventCategory.ENERGY, "Squirrel"),
    FRIENDLY_DOG("Friendly Dog", "🐶", EventCategory.SOCIAL, "Friend"),
    WATER_BREAK("Water Break", "💦", EventCategory.FORAGING, "Water"),
    BARK_REACT("Bark/React", "🗣️", EventCategory.SOCIAL, "Bark"),
    LEASH_PULL("Leash Pull", "🔗", EventCategory.ENERGY, "Pull"),
    HUMAN_FRIEND("Human Friend", "🧑", EventCategory.SOCIAL, "Human"),
}
