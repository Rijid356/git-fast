package com.gitfast.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "xp_transactions",
    foreignKeys = [
        ForeignKey(
            entity = com.gitfast.app.data.local.entity.WorkoutEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("workoutId")],
)
data class XpTransactionEntity(
    @PrimaryKey val id: String,
    val workoutId: String,
    val xpAmount: Int,
    val reason: String,
    val timestamp: Long,
    val profileId: Int = 1,
)
