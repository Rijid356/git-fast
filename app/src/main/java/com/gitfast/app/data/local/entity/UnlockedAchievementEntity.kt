package com.gitfast.app.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "unlocked_achievements",
    primaryKeys = ["achievementId", "profileId"],
)
data class UnlockedAchievementEntity(
    val achievementId: String,
    val unlockedAt: Long,
    val xpAwarded: Int,
    val profileId: Int = 1,
)
