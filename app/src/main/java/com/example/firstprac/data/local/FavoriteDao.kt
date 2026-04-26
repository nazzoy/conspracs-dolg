package com.example.firstprac.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    // Получаем все избранное в виде Flow.
    @Query("SELECT * FROM favorites")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    // Добавление в базу.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    // Удаление из базы
    @Delete
    suspend fun deleteFavorite(favorite: FavoriteEntity)

    // Проверка есть ли такой id в базе
    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE id = :id)")
    suspend fun isFavorite(id: Long): Boolean
}