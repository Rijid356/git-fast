package com.gitfast.app.data.model

enum class EventCategory {
    FORAGING, BATHROOM, ENERGY, SOCIAL
}

enum class DogWalkEventType(
    val displayName: String,
    val icon: String,
    val category: EventCategory,
) {
    SNACK_FOUND("Snack Found", "[SF]", EventCategory.FORAGING),
    POOP("Poop", "[PO]", EventCategory.BATHROOM),
    PEE("Pee", "[PE]", EventCategory.BATHROOM),
    DEEP_SNIFF("Deep Sniff", "[DS]", EventCategory.FORAGING),
    SQUIRREL_CHASE("Squirrel Chase", "[SC]", EventCategory.ENERGY),
    FRIENDLY_DOG("Friendly Dog", "[FD]", EventCategory.SOCIAL),
    ZOOMIES("Zoomies", "[ZM]", EventCategory.ENERGY),
}
