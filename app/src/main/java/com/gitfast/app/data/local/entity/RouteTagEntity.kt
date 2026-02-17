package com.gitfast.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "route_tags")
data class RouteTagEntity(
    @PrimaryKey
    val name: String,
    val createdAt: Long,
    val lastUsed: Long
)
