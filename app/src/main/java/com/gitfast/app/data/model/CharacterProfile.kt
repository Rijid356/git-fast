package com.gitfast.app.data.model

data class CharacterProfile(
    val level: Int = 1,
    val totalXp: Int = 0,
    val xpForCurrentLevel: Int = 0,
    val xpForNextLevel: Int = 100,
    val xpProgressInLevel: Int = 0,
    val xpProgress: Float = 0f,
)
