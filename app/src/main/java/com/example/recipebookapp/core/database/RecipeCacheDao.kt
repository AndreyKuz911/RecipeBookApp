package com.example.recipebookapp.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RecipeCacheDao {
    @Query("SELECT * FROM cached_recipes ORDER BY cachedAtEpochMs DESC LIMIT :limit")
    suspend fun getRecentRecipes(limit: Int): List<RecipeCacheEntity>

    @Query("SELECT * FROM cached_recipes WHERE isFavorite = 1 ORDER BY cachedAtEpochMs DESC")
    suspend fun getFavoriteRecipes(): List<RecipeCacheEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRecipes(recipes: List<RecipeCacheEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRecipe(recipe: RecipeCacheEntity)

    @Query("UPDATE cached_recipes SET isFavorite = 0 WHERE isFavorite = 1")
    suspend fun clearFavoriteFlags()

    @Query("UPDATE cached_recipes SET isFavorite = 1 WHERE id IN (:ids)")
    suspend fun markFavorites(ids: List<String>)

    @Query("UPDATE cached_recipes SET isFavorite = :isFavorite WHERE id = :recipeId")
    suspend fun updateFavoriteState(recipeId: String, isFavorite: Boolean)

    @Query("DELETE FROM cached_recipes WHERE id = :recipeId")
    suspend fun deleteRecipe(recipeId: String)
}
