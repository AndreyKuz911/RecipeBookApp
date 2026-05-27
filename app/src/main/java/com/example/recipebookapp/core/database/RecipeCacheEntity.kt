package com.example.recipebookapp.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.recipebookapp.core.model.Recipe
import com.example.recipebookapp.core.model.RecipeDetails
import com.example.recipebookapp.core.model.UserSummary

@Entity(tableName = "cached_recipes")
data class RecipeCacheEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val category: String,
    val cookingTimeMinutes: Int,
    val imageUrl: String? = null,
    val authorId: String,
    val authorUsername: String,
    val authorAvatarUrl: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val likesCount: Int,
    val dislikesCount: Int,
    val rating: Int,
    val isFavorite: Boolean,
    val myRating: Int? = null,
    val cachedAtEpochMs: Long,
)

fun RecipeCacheEntity.toDomain(): Recipe = Recipe(
    id = id,
    title = title,
    description = description,
    category = category,
    cookingTimeMinutes = cookingTimeMinutes,
    imageUrl = imageUrl,
    author = UserSummary(
        id = authorId,
        username = authorUsername,
        avatarUrl = authorAvatarUrl,
    ),
    createdAt = createdAt,
    updatedAt = updatedAt,
    likesCount = likesCount,
    dislikesCount = dislikesCount,
    rating = rating,
    isFavorite = isFavorite,
    myRating = myRating,
)

fun Recipe.toCacheEntity(cachedAtEpochMs: Long = System.currentTimeMillis()): RecipeCacheEntity = RecipeCacheEntity(
    id = id,
    title = title,
    description = description,
    category = category,
    cookingTimeMinutes = cookingTimeMinutes,
    imageUrl = imageUrl,
    authorId = author.id,
    authorUsername = author.username,
    authorAvatarUrl = author.avatarUrl,
    createdAt = createdAt,
    updatedAt = updatedAt,
    likesCount = likesCount,
    dislikesCount = dislikesCount,
    rating = rating,
    isFavorite = isFavorite,
    myRating = myRating,
    cachedAtEpochMs = cachedAtEpochMs,
)

fun RecipeDetails.toCacheEntity(cachedAtEpochMs: Long = System.currentTimeMillis()): RecipeCacheEntity = RecipeCacheEntity(
    id = id,
    title = title,
    description = description,
    category = category,
    cookingTimeMinutes = cookingTimeMinutes,
    imageUrl = imageUrls.firstOrNull(),
    authorId = author.id,
    authorUsername = author.username,
    authorAvatarUrl = author.avatarUrl,
    createdAt = createdAt,
    updatedAt = updatedAt,
    likesCount = likesCount,
    dislikesCount = dislikesCount,
    rating = rating,
    isFavorite = isFavorite,
    myRating = myRating,
    cachedAtEpochMs = cachedAtEpochMs,
)
