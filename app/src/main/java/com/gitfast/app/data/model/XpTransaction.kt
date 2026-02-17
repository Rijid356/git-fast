package com.gitfast.app.data.model

import java.time.Instant

data class XpTransaction(
    val id: String,
    val workoutId: String,
    val xpAmount: Int,
    val reason: String,
    val timestamp: Instant,
)
