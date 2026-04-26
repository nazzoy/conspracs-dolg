package com.example.firstprac.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val id: Long, // Уникальный id репозитория
    val name: String,
    val description: String?,
    val stars: Int,
    val language: String?
)