package com.gitfast.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "unlocked_achievements")
data class UnlockedAchievementEntity(
    @PrimaryKey val achievementId: String,
    val unlockedAt: Long,
    val xpAwarded: Int,
)
