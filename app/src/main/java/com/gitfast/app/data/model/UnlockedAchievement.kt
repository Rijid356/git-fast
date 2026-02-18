package com.gitfast.app.data.model

import java.time.Instant

data class UnlockedAchievement(
    val achievementId: String,
    val unlockedAt: Instant,
    val xpAwarded: Int,
)
